package com.example.basescout;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * BaseScout: passive, client-side scanner with a small window (press B) and a
 * /basescan command. It only reads chunks already loaded on your client.
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
                mc.setScreen(new ScoutScreen());
            }
        });

        // Command still works if you prefer it.
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                ClientCommandManager.literal("basescan")
                    .executes(ctx -> {
                        // Screens can't open mid-command-processing, so defer to the next tick.
                        Minecraft.getInstance().schedule(() ->
                            Minecraft.getInstance().setScreen(new ScoutScreen()));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("minScore", IntegerArgumentType.integer(1, 10))
                        .executes(ctx -> {
                            ScanLogic.minScore = IntegerArgumentType.getInteger(ctx, "minScore");
                            ScanLogic.run();
                            ctx.getSource().sendFeedback(Component.literal(
                                "[BaseScout] " + ScanLogic.lastStatus));
                            for (ScanLogic.Hotspot h : ScanLogic.lastResults) {
                                ctx.getSource().sendFeedback(Component.literal(
                                    "  score " + h.score() + "  X " + h.x() + ", Z " + h.z()
                                        + "  (" + h.distance() + " blocks away)"));
                            }
                            return 1;
                        }))
            )
        );
    }
}
