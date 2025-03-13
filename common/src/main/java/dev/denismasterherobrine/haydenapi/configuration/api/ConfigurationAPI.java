package dev.denismasterherobrine.haydenapi.configuration.api;

import dev.denismasterherobrine.haydenapi.configuration.ConfigurationManager;
import dev.denismasterherobrine.haydenapi.configuration.exception.ConfigurationException;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main API class for working with configuration files.
 * This class provides static methods to create, save, and reload configurations.
 *
 * <p>Example usage:
 * <pre>
 *   ExampleConfig config = ConfigurationAPI.createConfig("modname.json5", ExampleConfig.class);
 *   // Modify configuration values...
 *   ConfigurationAPI.saveConfig("modname.json5", config);
 *   // Later, if needed to reload updated configuration from file:
 *   config = ConfigurationAPI.reloadConfig("modname.json5", ExampleConfig.class);
 * </pre>
 * </p>
 */
public final class ConfigurationAPI {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationAPI.class.getName());
    private static final ConfigurationManager manager = new ConfigurationManager();

    private ConfigurationAPI() {}

    /**
     * Initializes the configuration. If the file does not exist, a new one is created with default values.
     * If the file exists, the configuration is loaded, and if it contains fields annotated with {@code @Reloadable},
     * file monitoring is started.
     *
     * @param path    the configuration file name or path
     * @param configClass the configuration class annotated with {@code @Config} that defines the configuration structure
     * @param <T>         the configuration type
     * @return the configuration instance
     * @throws ConfigurationException if an initialization error occurs
     */
    public static <T> T createConfig(String path, Class<T> configClass) throws ConfigurationException {
        try {
            return manager.initializeConfig(path, configClass);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Error initializing configuration: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Saves the configuration to the specified file.
     *
     * @param filename the configuration file name
     * @param config   the configuration instance to be saved
     * @param <T>      the configuration type
     * @throws ConfigurationException if an error occurs during saving
     */
    public static <T> void saveConfig(String filename, T config) throws ConfigurationException {
        File file = new File(filename);
        try {
            manager.saveConfig(file, config);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Error saving configuration: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Reloads the configuration from the specified file.
     * This method updates the configuration instance with the latest data from the file.
     *
     * @param filename    the configuration file name
     * @param configClass the configuration class annotated with {@code @Config}
     * @param <T>         the configuration type
     * @return the reloaded configuration instance
     * @throws ConfigurationException if an error occurs during reloading
     */
    public static <T> T reloadConfig(String filename, Class<T> configClass) throws ConfigurationException {
        File file = new File(filename);
        try {
            T config = manager.loadConfig(file, configClass);
            return config;
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Error reloading configuration: " + e.getMessage(), e);
            throw e;
        }
    }
}