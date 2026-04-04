package dev.darkness.commands.internal;

import dev.darkness.commands.argument.ArgumentResolver;

import java.util.HashMap;
import java.util.Map;

public class ResolverRegistry {

    private final Map<Class<?>, ArgumentResolver<?>> resolvers = new HashMap<>();

    public <T> void register(ArgumentResolver<T> resolver) {
        resolvers.put(resolver.getType(), resolver);
        Class<?> primitive = getPrimitive(resolver.getType());
        if (primitive != null) {
            resolvers.put(primitive, resolver);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ArgumentResolver<T> get(Class<T> type) {
        return (ArgumentResolver<T>) resolvers.get(type);
    }

    public boolean has(Class<?> type) {
        return resolvers.containsKey(type);
    }

    private Class<?> getPrimitive(Class<?> boxed) {
        if (boxed == Integer.class) return int.class;
        if (boxed == Double.class) return double.class;
        if (boxed == Float.class) return float.class;
        if (boxed == Long.class) return long.class;
        if (boxed == Boolean.class) return boolean.class;
        if (boxed == Short.class) return short.class;
        if (boxed == Byte.class) return byte.class;
        if (boxed == Character.class) return char.class;
        return null;
    }
}

