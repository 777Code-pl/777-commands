package dev.darkness.commands;

import dev.darkness.commands.argument.ArgumentResolver;
import dev.darkness.commands.argument.defaults.*;
import dev.darkness.commands.internal.BukkitCommand;
import dev.darkness.commands.internal.ResolverRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandRegistry {

    private final JavaPlugin plugin;
    private final MessageProvider messageProvider;
    private final ResolverRegistry resolverRegistry;
    private final CommandMap commandMap;

    public CommandRegistry(JavaPlugin plugin, MessageProvider messageProvider) {
        this.plugin = plugin;
        this.messageProvider = messageProvider;
        this.resolverRegistry = new ResolverRegistry();
        this.commandMap = Bukkit.getServer().getCommandMap();
        registerDefaults();
    }

    private void registerDefaults() {
        resolverRegistry.register(new StringResolver());
        resolverRegistry.register(new IntegerResolver());
        resolverRegistry.register(new DoubleResolver());
        resolverRegistry.register(new FloatResolver());
        resolverRegistry.register(new LongResolver());
        resolverRegistry.register(new BooleanResolver());
        resolverRegistry.register(new PlayerResolver());
    }

    public <T> CommandRegistry resolver(ArgumentResolver<T> resolver) {
        resolverRegistry.register(resolver);
        return this;
    }

    public CommandRegistry register(Object commandInstance) {
        BukkitCommand command = new BukkitCommand(commandInstance, messageProvider, resolverRegistry);
        commandMap.register(plugin.getName(), command);
        return this;
    }

    public void sync() {
        try {
            java.lang.reflect.Method syncMethod = plugin.getServer().getClass().getMethod("syncCommands");
            syncMethod.invoke(plugin.getServer());
        } catch (Exception ignored) {
        }
    }
}