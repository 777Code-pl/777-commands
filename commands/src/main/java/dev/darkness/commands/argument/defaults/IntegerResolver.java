package dev.darkness.commands.argument.defaults;

import dev.darkness.commands.argument.ArgumentParseException;
import dev.darkness.commands.argument.ArgumentResolver;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class IntegerResolver implements ArgumentResolver<Integer> {

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Integer resolve(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("integer", input, Integer.class);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        return Collections.emptyList();
    }
}

