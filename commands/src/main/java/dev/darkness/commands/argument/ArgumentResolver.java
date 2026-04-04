package dev.darkness.commands.argument;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ArgumentResolver<T> {
    Class<T> getType();
    T resolve(CommandSender sender, String input) throws ArgumentParseException;
    List<String> suggest(CommandSender sender);
}