package dev.denismasterherobrine.haydenapi.configuration.format;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.denismasterherobrine.haydenapi.configuration.annotation.Entry;
import dev.denismasterherobrine.haydenapi.configuration.exception.ConfigurationException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class YamlConfigurationHandler implements FormatHandler {
    private final YAMLMapper yamlMapper;

    public YamlConfigurationHandler() {
        yamlMapper = new YAMLMapper();
    }

    @Override
    public <T> T load(File file, Class<T> configClass) throws ConfigurationException {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            Map<String, Object> map = yamlMapper.readValue(bytes, HashMap.class);
            for (Field field : configClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Entry.class)) {
                    field.setAccessible(true);
                    if (map.containsKey(field.getName())) {
                        Object value = map.get(field.getName());
                        Object converted = yamlMapper.convertValue(value, field.getType());
                        field.set(null, converted);
                    }
                }
            }
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Error loading YAML file: " + file.getName(), e);
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
            byte[] bytes = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(map);
            Files.write(file.toPath(), bytes);
        } catch (Exception e) {
            throw new ConfigurationException("Error saving YAML file: " + file.getName(), e);
        }
    }
}

