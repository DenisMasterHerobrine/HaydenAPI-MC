package dev.denismasterherobrine.haydenapi.configuration.format;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import dev.denismasterherobrine.haydenapi.configuration.annotation.Entry;
import dev.denismasterherobrine.haydenapi.configuration.exception.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TomlConfigurationHandler implements FormatHandler {
    private final TomlMapper tomlMapper;

    public TomlConfigurationHandler() {
        tomlMapper = new TomlMapper();
    }

    @Override
    public <T> T load(File file, Class<T> configClass) throws ConfigurationException {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            Map<String, Object> map = tomlMapper.readValue(bytes, HashMap.class);
            for (Field field : configClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Entry.class)) {
                    field.setAccessible(true);
                    if (map.containsKey(field.getName())) {
                        Object value = map.get(field.getName());
                        Object converted = tomlMapper.convertValue(value, field.getType());
                        field.set(null, converted);
                    }
                }
            }
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Error loading TOML file: " + file.getName(), e);
        }
    }

    @Override
    public <T> void save(File file, T config) throws ConfigurationException {
        try {
            Class<?> configClass = config.getClass();
            Map<String, Object> map = new HashMap<>();
            for (Field field : configClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Entry.class)) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    map.put(field.getName(), value);
                }
            }
            byte[] bytes = tomlMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(map);
            Files.write(file.toPath(), bytes);
        } catch (Exception e) {
            throw new ConfigurationException("Error saving TOML file: " + file.getName(), e);
        }
    }
}

