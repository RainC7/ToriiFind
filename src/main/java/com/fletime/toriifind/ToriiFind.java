package com.fletime.toriifind;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ToriiFind implements ClientModInitializer {
	public static final String MOD_ID = "toriifind";
	public static final int CONFIG_VERSION = 4; // 当前配置文件版本
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		createOrUpdateConfigFile();
		ToriiFindCommand.register();
	}
	
	private void createOrUpdateConfigFile() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path configFile = configDir.resolve("toriifind.json");
		
		boolean shouldUpdateConfig = false;
		
		if (!Files.exists(configFile)) {
			shouldUpdateConfig = true;
		} else {
			try {
				int fileVersion = getConfigFileVersion(configFile);
				if (fileVersion < CONFIG_VERSION) {
					shouldUpdateConfig = true;
				}
			} catch (Exception e) {
				shouldUpdateConfig = true;
			}
		}
		
		if (shouldUpdateConfig) {
			try {
				Files.createDirectories(configDir);
				
				InputStream defaultConfigStream = ToriiFind.class.getClassLoader()
						.getResourceAsStream("assets/" + MOD_ID + "/toriifind_default.json");
				
				if (defaultConfigStream != null) {
					Files.copy(defaultConfigStream, configFile, StandardCopyOption.REPLACE_EXISTING);
					defaultConfigStream.close();
					}
				} catch (IOException e) {
			}
		}
	}
	
	private int getConfigFileVersion(Path configFile) throws IOException {
		try (Reader reader = Files.newBufferedReader(configFile)) {
			JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
			if (jsonObject.has("version")) {
				return jsonObject.get("version").getAsInt();
			}
			return 0; // 如果没有版本号，返回0，表示版本最低
		}
	}
	
	// 多语言的方法，如果崩了先干掉这个，但是没崩，好好好
	public static Text translate(String key) {
		return Text.translatable(key);
	}
	
	public static Text translate(String key, Object... args) {
		return Text.translatable(key, args);
	}
}
