package dev.darkness.commands.argument.defaults;

import dev.darkness.commands.argument.ArgumentParseException;
import dev.darkness.commands.argument.ArgumentResolver;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class FloatResolver implements ArgumentResolver<Float> {

    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public Float resolve(CommandSender sender, String input) throws ArgumentParseException {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("float", input, Float.class);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender) {
        return Collections.emptyList();
    }
}

