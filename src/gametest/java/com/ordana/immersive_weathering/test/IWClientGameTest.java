package com.ordana.immersive_weathering.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Dev only automated client test (gradlew runClientGameTest). Not part of the mod jar.
 * 1) single player: places every block of the mod in a wall and takes screenshots, spawns particles and a
 * falling layer to exercise the client renderers;
 * 2) multiplayer: starts a real dedicated server with the mod and joins it.
 */
public class IWClientGameTest implements FabricClientGameTest {

    private static final int PER_ROW = 20;

    @Override
    public void runTest(ClientGameTestContext context) {
        List<String> blocks = new ArrayList<>();
        for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            if (id.getNamespace().equals("immersive_weathering")) blocks.add(id.toString());
        }
        blocks.sort(String::compareTo);

        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            sp.getConnection().waitForChunksRender();
            TestServerContext server = sp.getServer();
            BlockPos p = context.computeOnClient(mc -> mc.player.blockPosition());
            server.runCommand("gamemode spectator @p");
            buildWall(server, blocks, p);
            context.waitTicks(40);
            sp.getConnection().waitForChunksRender();

            int rows = (blocks.size() + PER_ROW - 1) / PER_ROW;
            int shot = 0;
            for (int ry = 0; ry < rows; ry += 4) {
                for (int rx = 0; rx < PER_ROW; rx += 10) {
                    double x = p.getX() + rx + 5;
                    double y = p.getY() + ry * 2 + 2.4;
                    server.runCommand(String.format(java.util.Locale.ROOT,
                            "tp @p %.1f %.1f %.1f 180 0", x, y, (double) p.getZ() + 20 + 8.5));
                    context.waitTicks(30);
                    sp.getConnection().waitForChunksRender();
                    context.takeScreenshot(TestScreenshotOptions.of("iw_blocks_" + (shot++)).withSize(1280, 720));
                }
            }

            // particles, falling layer entity, items in hand
            server.runCommand("gamemode creative @p");
            server.runCommand(String.format("tp @p %d %d %d 180 20", p.getX() + 3, p.getY() + 2, p.getZ() + 29));
            server.runCommand("give @p immersive_weathering:oak_leaf_pile 16");
            server.runCommand(String.format("setblock %d %d %d immersive_weathering:sand_layer_block", p.getX() + 3, p.getY() + 9, p.getZ() + 25));
            server.runCommand(String.format("setblock %d %d %d immersive_weathering:icicle[tip_direction=down]", p.getX() + 2, p.getY() + 5, p.getZ() + 25));
            for (String particle : List.of("emberspark", "azalea_flower", "flower_bee", "moss", "scrape_rust", "gravity_azalea_flower")) {
                server.runCommand(String.format("particle immersive_weathering:%s %d %d %d 1 1 1 0 60 force", particle, p.getX() + 3, p.getY() + 3, p.getZ() + 26));
            }
            context.waitTicks(12);
            context.takeScreenshot(TestScreenshotOptions.of("iw_particles").withSize(1280, 720));
            context.waitTicks(40);
        }

        // real multiplayer: dedicated server + this client
        try (TestDedicatedServerContext server = context.worldBuilder().createServer();
             net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection connection = server.connect()) {
            connection.waitForChunksRender();
            BlockPos p = context.computeOnClient(mc -> mc.player.blockPosition());
            server.runCommand("op @a");
            for (int i = 0; i < 6; i++) {
                String b = List.of("immersive_weathering:mossy_bricks", "immersive_weathering:rusted_cut_iron",
                        "immersive_weathering:charred_log", "immersive_weathering:oak_leaf_pile[layers=3]",
                        "immersive_weathering:frosty_glass", "immersive_weathering:mulch_block").get(i);
                server.runCommand(String.format("setblock %d %d %d %s", p.getX() - 3 + i, p.getY() + 1, p.getZ() - 4, b));
            }
            server.runCommand(String.format("tp @a %d %d %d 180 10", p.getX(), p.getY(), p.getZ()));
            context.waitTicks(40);
            connection.waitForChunksRender();
            context.takeScreenshot(TestScreenshotOptions.of("iw_multiplayer").withSize(1280, 720));
        }
    }

    private static void buildWall(TestServerContext server, List<String> blocks, BlockPos p) {
        int z = p.getZ() + 20;
        int rows = (blocks.size() + PER_ROW - 1) / PER_ROW;
        server.runCommand(String.format("fill %d %d %d %d %d %d minecraft:air", p.getX() - 1, p.getY(), z - 1, p.getX() + PER_ROW, p.getY() + rows * 2 + 2, z + 12));
        for (int i = 0; i < blocks.size(); i++) {
            int x = p.getX() + (i % PER_ROW);
            int y = p.getY() + (i / PER_ROW) * 2;
            //each block sits on a dirt shelf so plants and layers survive
            server.runCommand(String.format("setblock %d %d %d minecraft:dirt", x, y, z));
            server.runCommand(String.format("setblock %d %d %d %s", x, y + 1, z, blocks.get(i)));
        }
    }
}
