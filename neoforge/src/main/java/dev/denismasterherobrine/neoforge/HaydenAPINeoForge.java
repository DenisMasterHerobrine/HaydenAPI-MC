package dev.denismasterherobrine.neoforge;

import net.neoforged.fml.common.Mod;

import dev.denismasterherobrine.haydenapi.HaydenAPI;

@Mod(HaydenAPI.MOD_ID)
public final class HaydenAPINeoForge {
    public HaydenAPINeoForge() {
        // Run our common setup.
        HaydenAPI.init();
    }
}
