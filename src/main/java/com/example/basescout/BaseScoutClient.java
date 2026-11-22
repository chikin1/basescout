package com.example.basescout;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * BaseScout: passive, client-side scanner with a small window (press B).
 * It only reads chunks already loaded on your client.
 */
public class BaseScoutClient implements ClientModInitializer {

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("basescout", "main"));
        openKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.basescout.open", InputConstants.KEY_B, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                mc.gui.setScreen(new ScoutScreen());
            }
        });
    }
}
