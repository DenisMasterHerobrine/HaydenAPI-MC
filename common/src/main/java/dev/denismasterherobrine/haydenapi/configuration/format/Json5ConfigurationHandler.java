package dev.denismasterherobrine.haydenapi.configuration.format;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.denismasterherobrine.haydenapi.configuration.annotation.Comment;
import dev.denismasterherobrine.haydenapi.configuration.annotation.Entry;
import dev.denismasterherobrine.haydenapi.configuration.exception.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json5ConfigurationHandler implements FormatHandler {
    private final ObjectMapper objectMapper;

    public Json5ConfigurationHandler() {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        factory.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        objectMapper = new ObjectMapper(factory);
    }

    @Override
    public <T> T load(File file, Class<T> configClass) throws ConfigurationException {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            Map<String, Object> map = objectMapper.readValue(bytes, HashMap.class);
            for (Field field : configClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Entry.class)) {
                    field.setAccessible(true);
                    if (map.containsKey(field.getName())) {
                        Object value = map.get(field.getName());
                        Object converted = objectMapper.convertValue(value, field.getType());
                        field.set(null, converted);
                    }
                }
            }

            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Error loading JSON5 file: " + file.getName(), e);
        }
    }

    @Override
    public <T> void save(File file, T config) throws ConfigurationException {
        try {
            Class<?> configClass = config.getClass();
            Field[] fields = configClass.getDeclaredFields();
            List<Field> staticEntryFields = new ArrayList<>();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Entry.class)) {
                    staticEntryFields.add(field);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            for (int i = 0; i < staticEntryFields.size(); i++) {
                Field field = staticEntryFields.get(i);
                field.setAccessible(true);
                Comment commentAnnotation = field.getAnnotation(Comment.class);

                if (commentAnnotation != null) {
                    sb.append("  // ").append(commentAnnotation.value()).append("\n");
                }

                sb.append("  \"").append(field.getName()).append("\": ");
                Object value = field.get(null);
                String jsonValue = objectMapper.writeValueAsString(value);
                sb.append(jsonValue);

                if (i < staticEntryFields.size() - 1) {
                    sb.append(",");
                }

                sb.append("\n");
            }

            sb.append("}\n");
            Files.write(file.toPath(), sb.toString().getBytes());
        } catch (IOException | IllegalAccessException e) {
            throw new ConfigurationException("Error saving JSON5 file: " + file.getName(), e);
        }
    }
}


