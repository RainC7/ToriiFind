package com.fletime.toriifind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ToriiFindCommand {
    // 零洲数据类
    private static class Torii {
        private final String 编号;
        private final String 名称;
        private final String 等级;

        public Torii(String 编号, String 名称, String 等级) {
            this.编号 = 编号;
            this.名称 = 名称;
            this.等级 = 等级;
        }

        @Override
        public String toString() {
            return 编号 + " " + 等级 + " " + 名称;
        }
    }

    // 后土数据类
    private static class Houtu {
        private final String 编号;
        private final String 名称;
        private final String 等级;

        public Houtu(String 编号, String 名称, String 等级) {
            this.编号 = 编号;
            this.名称 = 名称;
            this.等级 = 等级;
        }

        @Override
        public String toString() {
            return 编号 + " " + 等级 + " " + 名称;
        }
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            literal("toriifind")
                .executes(context -> showHelp(context))
                .then(literal("help")
                    .executes(context -> showHelp(context)))
                .then(literal("zeroth")
                    .then(literal("num")
                        .then(argument("number", IntegerArgumentType.integer(1))
                            .executes(context -> searchZerothByNumber(context, IntegerArgumentType.getInteger(context, "number")))))
                    .then(literal("name")
                        .then(argument("keyword", StringArgumentType.greedyString())
                            .executes(context -> searchZerothByName(context, StringArgumentType.getString(context, "keyword"))))))
                .then(literal("houtu")
                    .then(literal("num")
                        .then(argument("number", StringArgumentType.string())
                            .executes(context -> searchHoutuByNumber(context, StringArgumentType.getString(context, "number")))))
                    .then(literal("name")
                        .then(argument("keyword", StringArgumentType.greedyString())
                            .executes(context -> searchHoutuByName(context, StringArgumentType.getString(context, "keyword"))))))
        );
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
        context.getSource().sendFeedback(Text.literal("§6§lToriiFind 指令帮助"));
        context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
        context.getSource().sendFeedback(Text.literal("§7/toriifind help §8| §f显示此帮助信息"));
        context.getSource().sendFeedback(Text.literal("§7/toriifind zeroth num <编号> §8| §f按编号查找零洲鸟居"));
        context.getSource().sendFeedback(Text.literal("§7/toriifind zeroth name <关键字> §8| §f按名称关键字查找零洲鸟居"));
        context.getSource().sendFeedback(Text.literal("§7/toriifind houtu num <编号> §8| §f按编号查找后土境地"));
        context.getSource().sendFeedback(Text.literal("§7/toriifind houtu name <关键字> §8| §f按名称关键字查找后土境地"));
        context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
        return 1;
    }

    private static int searchZerothByNumber(CommandContext<FabricClientCommandSource> context, int number) {
        List<Torii> results = new ArrayList<>();
        
        try {
            List<Torii> toriiList = loadZerothData();
            
            for (Torii torii : toriiList) {
                if (torii.编号.equals(String.valueOf(number))) {
                    results.add(torii);
                }
            }
            
            displayZerothResults(context, results);
            
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c读取配置文件时出错: " + e.getMessage()));
        }
        
        return 1;
    }

    private static int searchZerothByName(CommandContext<FabricClientCommandSource> context, String keyword) {
        List<Torii> results = new ArrayList<>();
        
        try {
            List<Torii> toriiList = loadZerothData();
            
            for (Torii torii : toriiList) {
                if (torii.名称.contains(keyword)) {
                    results.add(torii);
                }
            }
            
            displayZerothResults(context, results);
            
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c读取配置文件时出错: " + e.getMessage()));
        }
        
        return 1;
    }

    private static int searchHoutuByNumber(CommandContext<FabricClientCommandSource> context, String number) {
        List<Houtu> results = new ArrayList<>();
        
        try {
            List<Houtu> houtuList = loadHoutuData();
            
            for (Houtu houtu : houtuList) {
                if (houtu.编号.contains(number)) {
                    results.add(houtu);
                }
            }
            
            displayHoutuResults(context, results);
            
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c读取配置文件时出错: " + e.getMessage()));
        }
        
        return 1;
    }

    private static int searchHoutuByName(CommandContext<FabricClientCommandSource> context, String keyword) {
        List<Houtu> results = new ArrayList<>();
        
        try {
            List<Houtu> houtuList = loadHoutuData();
            
            for (Houtu houtu : houtuList) {
                if (houtu.名称.contains(keyword)) {
                    results.add(houtu);
                }
            }
            
            displayHoutuResults(context, results);
            
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c读取配置文件时出错: " + e.getMessage()));
        }
        
        return 1;
    }
    
    private static void displayZerothResults(CommandContext<FabricClientCommandSource> context, List<Torii> results) {
        if (results.isEmpty()) {
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            context.getSource().sendFeedback(Text.literal("§c未找到任何匹配的鸟居信息"));
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
        } else {
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            context.getSource().sendFeedback(Text.literal("§6§l查询结果 §7(共 " + results.size() + " 个)"));
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            context.getSource().sendFeedback(Text.literal("§7编号 §8| §7等级 §8| §7鸟居名称"));
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            
            for (Torii torii : results) {
                MutableText baseText = Text.literal("§f" + torii.编号 + " §8| §f" + 
                                                   torii.等级 + " §8| §f" + 
                                                   torii.名称 + " ");
                
                String wikiUrl = "https://wiki.ria.red/wiki/" + torii.名称;
                
                MutableText linkText = Text.literal("§9[WIKI]")
                    .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, wikiUrl))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§7点击查看Wiki页面\n§f" + wikiUrl)))
                        .withFormatting(Formatting.UNDERLINE));
                
                context.getSource().sendFeedback(baseText.append(linkText));
            }
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
        }
    }

    private static void displayHoutuResults(CommandContext<FabricClientCommandSource> context, List<Houtu> results) {
        if (results.isEmpty()) {
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            context.getSource().sendFeedback(Text.literal("§c未找到任何匹配的境地信息"));
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
        } else {
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            context.getSource().sendFeedback(Text.literal("§6§l查询结果 §7(共 " + results.size() + " 个)"));
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            context.getSource().sendFeedback(Text.literal("§7编号 §8| §7等级 §8| §7境地名称"));
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
            
            for (Houtu houtu : results) {
                MutableText baseText = Text.literal("§f" + houtu.编号 + " §8| §f" + 
                                                   houtu.等级 + " §8| §f" + 
                                                   houtu.名称 + " ");
                
                String wikiUrl = "https://wiki.ria.red/wiki/" + houtu.名称;
                
                MutableText linkText = Text.literal("§9[WIKI]")
                    .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, wikiUrl))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§7点击查看Wiki页面\n§f" + wikiUrl)))
                        .withFormatting(Formatting.UNDERLINE));
                
                context.getSource().sendFeedback(baseText.append(linkText));
            }
            context.getSource().sendFeedback(Text.literal("§8§m----------------------------------------"));
        }
    }
    
    private static List<Torii> loadZerothData() throws IOException {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("toriifind.json");
        List<Torii> toriiList = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(configFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray zerothArray = jsonObject.getAsJsonArray("zeroth");
            
            Gson gson = new Gson();
            for (int i = 0; i < zerothArray.size(); i++) {
                Torii torii = gson.fromJson(zerothArray.get(i), Torii.class);
                toriiList.add(torii);
            }
        }
        
        return toriiList;
    }

    private static List<Houtu> loadHoutuData() throws IOException {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("toriifind.json");
        List<Houtu> houtuList = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(configFile)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray houtuArray = jsonObject.getAsJsonArray("houtu");
            
            Gson gson = new Gson();
            for (int i = 0; i < houtuArray.size(); i++) {
                Houtu houtu = gson.fromJson(houtuArray.get(i), Houtu.class);
                houtuList.add(houtu);
            }
        }
        
        return houtuList;
    }
} 