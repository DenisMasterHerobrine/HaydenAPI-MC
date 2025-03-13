package dev.denismasterherobrine.haydenapi.configuration;

import dev.denismasterherobrine.haydenapi.configuration.annotation.Config;
import dev.denismasterherobrine.haydenapi.configuration.annotation.Entry;
import dev.denismasterherobrine.haydenapi.configuration.annotation.Reloadable;
import dev.denismasterherobrine.haydenapi.configuration.exception.ConfigurationException;
import dev.denismasterherobrine.haydenapi.configuration.format.FormatHandler;
import dev.denismasterherobrine.haydenapi.configuration.format.Json5ConfigurationHandler;
import dev.denismasterherobrine.haydenapi.configuration.format.TomlConfigurationHandler;
import dev.denismasterherobrine.haydenapi.configuration.format.YamlConfigurationHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {
    private final Map<String, FormatHandler> formatHandlerMap = new HashMap<>();

    public ConfigurationManager() {
        formatHandlerMap.put("json5", new Json5ConfigurationHandler());
        formatHandlerMap.put("toml", new TomlConfigurationHandler());
        formatHandlerMap.put("yaml", new YamlConfigurationHandler());
    }

    private String getFormatFromFile(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');

        if (dotIndex == -1) {
            return "json5";
        }

        return name.substring(dotIndex + 1).toLowerCase();
    }

    public <T> T initializeConfig(String filename, Class<T> configClass) throws ConfigurationException {
        if (!configClass.isAnnotationPresent(Config.class)) {
            throw new ConfigurationException("Class " + configClass.getName() + " was not annotated with @Config!");
        }

        File file = new File(filename);
        T config;

        if (!file.exists()) {
            config = createNewConfig(configClass);
            saveConfig(file, config);
        } else {
            config = loadConfig(file, configClass);
        }

        if (hasReloadableFields(configClass)) {
            startFileWatcher(file, config, configClass);
        }
        return config;
    }

    public <T> T createNewConfig(Class<T> configClass) throws ConfigurationException {
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Error creating instance of configuration file for class: "
                    + configClass.getName(), e);
        }
    }

    public <T> T loadConfig(File file, Class<T> configClass) throws ConfigurationException {
        String format = getFormatFromFile(file);
        FormatHandler handler = formatHandlerMap.get(format);

        if (handler == null) {
            throw new ConfigurationException("Unsupported file format: " + format);
        }

        return handler.load(file, configClass);
    }

    public <T> void saveConfig(File file, T config) throws ConfigurationException {
        String format = getFormatFromFile(file);
        FormatHandler handler = formatHandlerMap.get(format);

        if (handler == null) {
            throw new ConfigurationException("Unsupported file format: " + format);
        }

        handler.save(file, config);
    }

    private <T> boolean hasReloadableFields(Class<T> configClass) {
        for (Field field : configClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Entry.class) && field.isAnnotationPresent(Reloadable.class)) {
                return true;
            }
        }

        return false;
    }

    private <T> void startFileWatcher(File file, T config, Class<T> configClass) {
        Path dir = file.toPath().getParent();

        if (dir == null) {
            return;
        }

        Thread watcherThread = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Path changed = (Path) event.context();
                            if (changed.toString().equals(file.getName())) {
                                T newConfig = loadConfig(file, configClass);
                                updateReloadableFields(config, newConfig, configClass);
                            }
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                System.err.println("Error watching configuration file: " + e.getMessage());
            }
        });

        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private <T> void updateReloadableFields(T original, T updated, Class<T> configClass) {
        Field[] fields = configClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Entry.class) && field.isAnnotationPresent(Reloadable.class)) {
                try {
                    field.setAccessible(true);
                    Object newValue = field.get(updated);
                    field.set(original, newValue);
                    System.out.println("Field " + field.getName() + " updated to value: " + newValue);
                } catch (IllegalAccessException e) {
                    System.err.println("Error updating field " + field.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
