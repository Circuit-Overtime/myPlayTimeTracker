package com.playtime;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.Style;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class playtime_rem implements ModInitializer {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PLAYTIME_FILE = Paths.get("playtime.json");  // Adjust path as needed
    private final Map<String, Long> joinTimeMap = new HashMap<>();

    @Override
    public void onInitialize() {
        // Registering the player join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getName().getString();
            
            // Load existing playtime
            int playtime = readPlayerData(playerName);
            
            // Store join time
            joinTimeMap.put(playerName, System.currentTimeMillis());
        });

        // Registering the player leave event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getName().getString();

            if (joinTimeMap.containsKey(playerName)) {
                long joinTime = joinTimeMap.remove(playerName);
                long leaveTime = System.currentTimeMillis();
                int sessionPlaytime = (int) ((leaveTime - joinTime) / 1000); // in seconds

                int totalPlaytime = readPlayerData(playerName) + sessionPlaytime;
                writePlayerData(playerName, totalPlaytime);
            }
        });

        // Register the /playtrack command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(CommandManager.literal("playtrack")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity player = source.getPlayer();
                    if (player != null) {
                        String playerName = player.getName().getString();
                        int playtime = readPlayerData(playerName);

                        if (playtime > 0) {
                            String formattedPlaytime = formatPlaytime(playtime);
                            Text response = Text.literal("Your playtime is ")
                                    .append(Text.literal(formattedPlaytime).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF00)))) // Yellow color for time
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))); // White color for text
                            
                            source.sendFeedback(() -> response, false); // Corrected
                        } else {
                            source.sendFeedback(() -> Text.literal("Data is not available.")
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000))), // Red color for error
                                    false); // Corrected
                        }
                    } else {
                        source.sendFeedback(() -> Text.literal("This command can only be run by a player.")
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))), // White color
                                false); // Corrected
                    }
                    return 1;
                })
            );
        });
    }

    private int readPlayerData(String playerName) {
        int playtime = 0;

        try (FileReader reader = new FileReader(PLAYTIME_FILE.toFile())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            if (jsonObject.has(playerName)) {
                playtime = jsonObject.getAsJsonPrimitive(playerName).getAsInt();
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }

        return playtime;
    }

    private void writePlayerData(String playerName, int playedDuration) {
        Map<String, Integer> playerData = new HashMap<>();
        try (FileReader reader = new FileReader(PLAYTIME_FILE.toFile())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : jsonObject.entrySet()) {
                playerData.put(entry.getKey(), entry.getValue().getAsInt());
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }

        playerData.put(playerName, playedDuration);

        try (FileWriter writer = new FileWriter(PLAYTIME_FILE.toFile())) {
            GSON.toJson(playerData, writer);
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

    private String formatPlaytime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}