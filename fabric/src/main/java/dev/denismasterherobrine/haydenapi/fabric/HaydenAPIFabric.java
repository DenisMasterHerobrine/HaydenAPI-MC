package dev.denismasterherobrine.haydenapi.fabric;

import net.fabricmc.api.ModInitializer;

import dev.denismasterherobrine.haydenapi.HaydenAPI;

public final class HaydenAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        HaydenAPI.init();
    }
}
