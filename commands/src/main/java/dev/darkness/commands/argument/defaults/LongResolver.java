package dev.darkness.commands.argument.defaults;

import dev.darkness.commands.argument.ArgumentParseException;
import dev.darkness.commands.argument.ArgumentResolver;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class LongResolver implements ArgumentResolver<Long> {

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    @Override
    public Long resolve(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("long", input, Long.class);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        return Collections.emptyList();
    }
}

