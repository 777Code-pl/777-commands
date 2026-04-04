package dev.darkness.commands.internal;

import dev.darkness.commands.MessageProvider;
import dev.darkness.commands.annotation.Command;
import dev.darkness.commands.annotation.Execute;
import dev.darkness.commands.argument.ArgumentParseException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitCommand extends org.bukkit.command.Command {

    private static final Logger LOGGER = Logger.getLogger("777-Commands");

    private final Object commandInstance;
    private final Command info;
    private final List<ExecuteMethodHandle> handles;
    private final MessageProvider messageProvider;

    public BukkitCommand(Object instance, MessageProvider messageProvider, ResolverRegistry resolverRegistry) {
        super("");
        this.commandInstance = instance;
        this.messageProvider = messageProvider;
        this.info = instance.getClass().getAnnotation(Command.class);

        if (this.info == null) {
            throw new IllegalStateException("missing @Command on " + instance.getClass().getName());
        }

        this.handles = new ArrayList<>();
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Execute.class)) {
                handles.add(new ExecuteMethodHandle(method, info.name(), resolverRegistry));
            }
        }

        if (handles.isEmpty()) {
            throw new IllegalStateException("no @Execute in " + instance.getClass().getName());
        }

        setName(info.name());
        setAliases(Arrays.asList(info.aliases()));
        if (!info.description().isEmpty()) setDescription(info.description());
        if (!info.permission().isEmpty()) setPermission(info.permission());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (info.playerOnly() && !(sender instanceof Player)) {
            messageProvider.sendOnlyPlayer(sender);
            return true;
        }

        if (getPermission() != null && !getPermission().isEmpty() && !sender.hasPermission(getPermission())) {
            messageProvider.sendNoPermission(sender, getPermission());
            return true;
        }

        ExecuteMethodHandle handle = resolveHandle(args);

        if (handle == null) {
            messageProvider.sendUsage(sender, buildAllUsages());
            return true;
        }

        String subPerm = handle.getPermission();
        if (!subPerm.isEmpty() && !sender.hasPermission(subPerm)) {
            messageProvider.sendNoPermission(sender, subPerm);
            return true;
        }

        String[] methodArgs = handle.isRootExecute() ? args : Arrays.copyOfRange(args, 1, args.length);

        if (methodArgs.length < handle.getMinArgs()) {
            messageProvider.sendUsage(sender, handle.getAutoUsage());
            return true;
        }

        try {
            Object[] invokeArgs = prepareArgs(sender, methodArgs, handle);
            handle.getMethod().invoke(commandInstance, invokeArgs);
        } catch (ArgumentParseException e) {
            messageProvider.sendUsage(sender, handle.getAutoUsage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "error executing command /" + info.name(), e);
        }

        return true;
    }

    private ExecuteMethodHandle resolveHandle(String[] args) {
        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            for (ExecuteMethodHandle handle : handles) {
                if (handle.getSubCommand().equalsIgnoreCase(sub)) {
                    return handle;
                }
            }
        }

        for (ExecuteMethodHandle handle : handles) {
            if (handle.isRootExecute()) {
                return handle;
            }
        }

        return null;
    }

    private Object[] prepareArgs(CommandSender sender, String[] args, ExecuteMethodHandle handle) throws ArgumentParseException {
        List<ParameterInfo> params = handle.getParameters();
        Object[] values = new Object[params.size()];
        int argIndex = 0;

        for (int i = 0; i < params.size(); i++) {
            ParameterInfo param = params.get(i);

            if (!param.isArg()) {
                values[i] = castSender(sender, param.getType());
                continue;
            }

            if (argIndex < args.length) {
                Object resolved = param.getResolver().resolve(sender, args[argIndex++]);
                values[i] = param.isOptional() ? Optional.of(resolved) : resolved;
            } else {
                values[i] = param.isOptional() ? Optional.empty() : null;
            }
        }

        return values;
    }

    private Object castSender(CommandSender sender, Class<?> type) {
        if (type.isAssignableFrom(sender.getClass())) {
            return type.cast(sender);
        }
        return sender;
    }

    private String buildAllUsages() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < handles.size(); i++) {
            sb.append(handles.get(i).getAutoUsage());
            if (i < handles.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            for (ExecuteMethodHandle handle : handles) {
                if (!handle.isRootExecute()) {
                    String sub = handle.getSubCommand();
                    if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                        suggestions.add(sub);
                    }
                }
            }

            if (!suggestions.isEmpty()) {
                return suggestions;
            }

            ExecuteMethodHandle rootHandle = getRootHandle();
            if (rootHandle != null) {
                return getArgSuggestions(sender, args, rootHandle, 0);
            }

            return Collections.emptyList();
        }

        ExecuteMethodHandle handle = resolveHandleForTab(args);
        if (handle == null) return Collections.emptyList();

        int argOffset = handle.isRootExecute() ? 0 : 1;
        int paramArgIndex = args.length - 1 - argOffset;

        return getArgSuggestions(sender, args, handle, paramArgIndex);
    }

    private List<String> getArgSuggestions(CommandSender sender, String[] args, ExecuteMethodHandle handle, int paramArgIndex) {
        List<ParameterInfo> params = handle.getParameters();
        int currentParamArg = 0;

        for (ParameterInfo param : params) {
            if (!param.isArg()) continue;
            if (currentParamArg == paramArgIndex) {
                String current = args[args.length - 1].toLowerCase();
                List<String> all = param.getResolver().suggest(sender);
                List<String> filtered = new ArrayList<>();
                for (String s : all) {
                    if (s.toLowerCase().startsWith(current)) {
                        filtered.add(s);
                    }
                }
                return filtered;
            }
            currentParamArg++;
        }

        return Collections.emptyList();
    }

    private ExecuteMethodHandle resolveHandleForTab(String[] args) {
        if (args.length > 1) {
            String sub = args[0].toLowerCase();
            for (ExecuteMethodHandle handle : handles) {
                if (handle.getSubCommand().equalsIgnoreCase(sub)) {
                    return handle;
                }
            }
        }
        return getRootHandle();
    }

    private ExecuteMethodHandle getRootHandle() {
        for (ExecuteMethodHandle handle : handles) {
            if (handle.isRootExecute()) return handle;
        }
        return null;
    }
}

