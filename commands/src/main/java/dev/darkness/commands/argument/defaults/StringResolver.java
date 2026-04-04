package dev.darkness.commands.argument.defaults;

import dev.darkness.commands.argument.ArgumentParseException;
import dev.darkness.commands.argument.ArgumentResolver;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class StringResolver implements ArgumentResolver<String> {

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String resolve(CommandSender sender, String input) throws ArgumentParseException {
        return input;
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        return Collections.emptyList();
    }
}

