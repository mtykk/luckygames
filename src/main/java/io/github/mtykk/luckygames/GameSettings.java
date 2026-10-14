package io.github.mtykk.luckygames;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

public class GameSettings {
    private static final int MAX_ITEMS_PER_SECTION = 10_000;
    private static final int MAX_SECTION_COUNT = 10_000;
    private static final int MAX_TRACK_LOCATION_COUNT = 10_000;

    private boolean keepInventory;
    private boolean saturation;
    private boolean nightVision;
    private int itemsPerSection;
    private int sectionCount;
    private final int[] luckyBlockDistribution = new int[4];

    GameSettings(boolean keepInventory, boolean saturation, boolean nightVision, int itemsPerSection, int sectionCount) {
        this.keepInventory = keepInventory;
        this.saturation = saturation;
        this.nightVision = nightVision;
        validateTrackShape(itemsPerSection, sectionCount);
        this.itemsPerSection = itemsPerSection;
        this.sectionCount = sectionCount;
        luckyBlockDistribution[0] = 12;
        luckyBlockDistribution[1] = 70;
        luckyBlockDistribution[2] = 13;
        luckyBlockDistribution[3] = 5;
    }

    GameSettings(GameSettings gameSettings) {
        this.keepInventory = gameSettings.keepInventory;
        this.saturation = gameSettings.saturation;
        this.nightVision = gameSettings.nightVision;
        this.sectionCount = gameSettings.sectionCount;
        this.itemsPerSection = gameSettings.itemsPerSection;
        System.arraycopy(gameSettings.luckyBlockDistribution, 0, luckyBlockDistribution, 0, luckyBlockDistribution.length);
    }

    GameSettings() {
        itemsPerSection = 10;
        sectionCount = 5;
        luckyBlockDistribution[0] = 12;
        luckyBlockDistribution[1] = 70;
        luckyBlockDistribution[2] = 13;
        luckyBlockDistribution[3] = 5;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    public boolean isSaturation() {
        return saturation;
    }

    public void setSaturation(boolean saturation) {
        this.saturation = saturation;
    }

    public boolean isNightVision() {
        return nightVision;
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
    }

    public int getItemsPerSection() {
        return itemsPerSection;
    }

    public void setItemsPerSection(int itemsPerSection) throws IllegalArgumentException {
        validateTrackShape(itemsPerSection, sectionCount);
        this.itemsPerSection = itemsPerSection;
    }

    public int getSectionCount() {
        return sectionCount;
    }

    public void setSectionCount(int sectionCount) throws IllegalArgumentException {
        validateTrackShape(itemsPerSection, sectionCount);
        this.sectionCount = sectionCount;
    }

    public int[] getLuckyBlockDistribution() {
        return luckyBlockDistribution.clone();
    }

    public boolean isCheckpoint(int index) {
        return index >= 0 && index % (itemsPerSection + 1) == 0;
    }

    public int getLastIndex() {
        return Math.toIntExact((long) sectionCount * (itemsPerSection + 1));
    }

    private static void validateTrackShape(int itemsPerSection, int sectionCount) {
        if (itemsPerSection < 1 || itemsPerSection > MAX_ITEMS_PER_SECTION) {
            throw new IllegalArgumentException("Items per section must be between 1 and " + MAX_ITEMS_PER_SECTION);
        }
        if (sectionCount < 1 || sectionCount > MAX_SECTION_COUNT) {
            throw new IllegalArgumentException("Section count must be between 1 and " + MAX_SECTION_COUNT);
        }
        long locationCount = (long) sectionCount * (itemsPerSection + 1L) + 1L;
        if (locationCount > MAX_TRACK_LOCATION_COUNT) {
            throw new IllegalArgumentException("The configured track exceeds the maximum of " + MAX_TRACK_LOCATION_COUNT + " locations");
        }
    }

    public boolean isLastCheckpoint(int index){
        return index == getLastIndex();
    }

    public void readFromConfig(JavaPlugin plugin) {
        setKeepInventory(plugin.getConfig().getBoolean("luckygames.keep-inventory", true));
        setSaturation(plugin.getConfig().getBoolean("luckygames.saturation", false));
        setNightVision(plugin.getConfig().getBoolean("luckygames.night-vision", false));

        int configuredItemsPerSection = plugin.getConfig().getInt("luckygames.items-per-section", 10);
        int configuredSectionCount = plugin.getConfig().getInt("luckygames.section-count", 5);
        validateTrackShape(configuredItemsPerSection, configuredSectionCount);
        itemsPerSection = configuredItemsPerSection;
        sectionCount = configuredSectionCount;

        int[] configuredDistribution = {
                plugin.getConfig().getInt("luckygames.luckyblock-distribution.unlucky", 12),
                plugin.getConfig().getInt("luckygames.luckyblock-distribution.normal", 70),
                plugin.getConfig().getInt("luckygames.luckyblock-distribution.lucky", 13),
                plugin.getConfig().getInt("luckygames.luckyblock-distribution.challenge", 5)
        };
        long totalWeight = 0;
        for (int weight : configuredDistribution) {
            if (weight < 0) {
                throw new IllegalArgumentException("Lucky-block distribution weights cannot be negative");
            }
            totalWeight += weight;
        }
        if (totalWeight == 0) {
            throw new IllegalArgumentException("At least one lucky-block distribution weight must be positive");
        }
        System.arraycopy(configuredDistribution, 0, luckyBlockDistribution, 0, luckyBlockDistribution.length);
    }
    public void saveToConfig(JavaPlugin plugin){
        plugin.getConfig().set("luckygames.keep-inventory",isKeepInventory());
        plugin.getConfig().set("luckygames.saturation",isSaturation());
        plugin.getConfig().set("luckygames.night-vision",isNightVision());
        plugin.getConfig().set("luckygames.items-per-section",getItemsPerSection());
        plugin.getConfig().set("luckygames.section-count",getSectionCount());
        plugin.getConfig().set("luckygames.luckyblock-distribution.unlucky",luckyBlockDistribution[0]);
        plugin.getConfig().set("luckygames.luckyblock-distribution.normal",luckyBlockDistribution[1]);
        plugin.getConfig().set("luckygames.luckyblock-distribution.lucky",luckyBlockDistribution[2]);
        plugin.getConfig().set("luckygames.luckyblock-distribution.challenge",luckyBlockDistribution[3]);
    }

    public LiteralArgumentBuilder<CommandSourceStack> settingsCommand(){
        LiteralArgumentBuilder<CommandSourceStack> keepInventoryCommand = Commands.literal("keep_inventory").then(Commands.argument("value", BoolArgumentType.bool())
                .requires(sender -> sender.getSender().hasPermission("luckygames.change_game_settings"))
                .executes(ctx->{
                    boolean value = BoolArgumentType.getBool(ctx,"value");
                    setKeepInventory(value);
                    WorldLobby.updateRuleDisplay(this);
                    ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.message.settings_changed"));
                    return Command.SINGLE_SUCCESS;
                })
        );

        LiteralArgumentBuilder<CommandSourceStack> saturationCommand = Commands.literal("saturation").then(Commands.argument("value", BoolArgumentType.bool())
                .requires(sender -> sender.getSender().hasPermission("luckygames.change_game_settings"))
                .executes(ctx->{
                    boolean value = BoolArgumentType.getBool(ctx,"value");
                    setSaturation(value);
                    WorldLobby.updateRuleDisplay(this);
                    ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.message.settings_changed"));
                    return Command.SINGLE_SUCCESS;
                })
        );

        LiteralArgumentBuilder<CommandSourceStack> nightVisionCommand = Commands.literal("night_vision").then(Commands.argument("value", BoolArgumentType.bool())
                .requires(sender -> sender.getSender().hasPermission("luckygames.change_game_settings"))
                .executes(ctx->{
                    boolean value = BoolArgumentType.getBool(ctx,"value");
                    setNightVision(value);
                    WorldLobby.updateRuleDisplay(this);
                    ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.message.settings_changed"));
                    return Command.SINGLE_SUCCESS;
                })
        );

        LiteralArgumentBuilder<CommandSourceStack> itemsPerSectionCommand = Commands.literal("items_per_section").then(Commands.argument("value", IntegerArgumentType.integer(1, MAX_ITEMS_PER_SECTION))
                .requires(sender -> sender.getSender().hasPermission("luckygames.change_game_settings"))
                .executes(ctx->{
                    int value = IntegerArgumentType.getInteger(ctx,"value");
                    try {
                        setItemsPerSection(value);
                    } catch (IllegalArgumentException exception) {
                        ctx.getSource().getSender().sendMessage(Component.text(exception.getMessage()));
                        return 0;
                    }
                    ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.message.settings_changed"));
                    return Command.SINGLE_SUCCESS;
                })
        );

        LiteralArgumentBuilder<CommandSourceStack> sectionCountCommand = Commands.literal("section_count").then(Commands.argument("value", IntegerArgumentType.integer(1, MAX_SECTION_COUNT))
                .requires(sender -> sender.getSender().hasPermission("luckygames.change_game_settings"))
                .executes(ctx->{
                    int value = IntegerArgumentType.getInteger(ctx,"value");
                    try {
                        setSectionCount(value);
                    } catch (IllegalArgumentException exception) {
                        ctx.getSource().getSender().sendMessage(Component.text(exception.getMessage()));
                        return 0;
                    }
                    ctx.getSource().getSender().sendMessage(Component.translatable("luckygames.message.settings_changed"));
                    return Command.SINGLE_SUCCESS;
                })
        );

        return Commands.literal("set")
                .then(keepInventoryCommand)
                .then(saturationCommand)
                .then(nightVisionCommand)
                .then(itemsPerSectionCommand)
                .then(sectionCountCommand);
    }
}
