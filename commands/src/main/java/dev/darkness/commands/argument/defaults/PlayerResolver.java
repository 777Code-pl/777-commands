package dev.darkness.commands.argument.defaults;

import dev.darkness.commands.argument.ArgumentParseException;
import dev.darkness.commands.argument.ArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerResolver implements ArgumentResolver<Player> {

    @Override
    public Class<Player> getType() {
        return Player.class;
    }

    @Override
    public Player resolve(CommandSender sender, String input) throws ArgumentParseException {
        Player player = Bukkit.getPlayer(input);
        if (player == null) {
            throw new ArgumentParseException("player", input, Player.class);
        }
        return player;
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        List<String> names = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
        return names;
    }
}

