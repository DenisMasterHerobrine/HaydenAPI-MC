package dev.denismasterherobrine.haydenapi.configuration.format;

import dev.denismasterherobrine.haydenapi.configuration.exception.ConfigurationException;

import java.io.File;

public interface FormatHandler {
    <T> T load(File file, Class<T> configClass) throws ConfigurationException;

    <T> void save(File file, T config) throws ConfigurationException;
}

