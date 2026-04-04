package dev.darkness.commands;

import org.bukkit.command.CommandSender;

public interface MessageProvider {
    void sendOnlyPlayer(CommandSender sender);
    void sendNoPermission(CommandSender sender, String permission);
    void sendUsage(CommandSender sender, String usage);
}