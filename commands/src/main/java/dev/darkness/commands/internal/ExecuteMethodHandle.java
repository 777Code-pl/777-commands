package dev.darkness.commands.internal;

import dev.darkness.commands.annotation.Arg;
import dev.darkness.commands.annotation.Context;
import dev.darkness.commands.annotation.Execute;
import dev.darkness.commands.argument.ArgumentResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExecuteMethodHandle {

    private final Method method;
    private final String subCommand;
    private final String permission;
    private final List<ParameterInfo> parameters;
    private final int minArgs;
    private final String autoUsage;

    public ExecuteMethodHandle(Method method, String commandName, ResolverRegistry resolverRegistry) {
        Execute execute = method.getAnnotation(Execute.class);
        this.method = method;
        this.method.setAccessible(true);
        this.subCommand = execute.value().trim();
        this.permission = execute.permission();

        this.parameters = new ArrayList<>();
        int required = 0;

        for (Parameter p : method.getParameters()) {
            ParameterInfo info = buildParameterInfo(p, resolverRegistry);
            parameters.add(info);
            if (info.isArg() && info.isRequired()) {
                required++;
            }
        }

        this.minArgs = required;
        this.autoUsage = buildAutoUsage(commandName);
    }

    private ParameterInfo buildParameterInfo(Parameter p, ResolverRegistry resolverRegistry) {
        if (p.isAnnotationPresent(Context.class)) {
            return ParameterInfo.context(p.getType());
        }

        if (p.isAnnotationPresent(Arg.class)) {
            Arg arg = p.getAnnotation(Arg.class);
            String argName = arg.value().isEmpty() ? p.getName() : arg.value();
            boolean required = arg.required();

            Class<?> rawType = p.getType();
            boolean isOptional = rawType == Optional.class;
            Class<?> resolvedType = isOptional ? resolveOptionalGeneric(p) : rawType;

            ArgumentResolver<?> resolver = resolverRegistry.get(resolvedType);
            if (resolver == null) {
                throw new IllegalStateException();
            }

            return ParameterInfo.arg(argName, resolvedType, resolver, required && !isOptional, isOptional);
        }

        if (isContextType(p.getType())) {
            return ParameterInfo.context(p.getType());
        }

        throw new IllegalStateException();
    }

    private Class<?> resolveOptionalGeneric(Parameter p) {
        java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) p.getParameterizedType();
        return (Class<?>) paramType.getActualTypeArguments()[0];
    }

    private boolean isContextType(Class<?> type) {
        return org.bukkit.command.CommandSender.class.isAssignableFrom(type);
    }

    private String buildAutoUsage(String commandName) {
        StringBuilder sb = new StringBuilder("/" + commandName);
        if (!subCommand.isEmpty()) {
            sb.append(" ").append(subCommand);
        }
        for (ParameterInfo param : parameters) {
            if (param.isArg()) {
                if (param.isRequired()) {
                    sb.append(" <").append(param.getName()).append(">");
                } else {
                    sb.append(" [").append(param.getName()).append("]");
                }
            }
        }
        return sb.toString();
    }

    public Method getMethod() {
        return method;
    }

    public String getSubCommand() {
        return subCommand;
    }

    public String getPermission() {
        return permission;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public int getMinArgs() {
        return minArgs;
    }

    public String getAutoUsage() {
        return autoUsage;
    }

    public boolean isRootExecute() {
        return subCommand.isEmpty();
    }
}


