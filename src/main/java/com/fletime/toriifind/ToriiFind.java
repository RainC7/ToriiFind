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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ToriiFind implements ClientModInitializer {
	public static final String MOD_ID = "toriifind";
	public static final int CONFIG_VERSION = 5; // 当前配置文件版本
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// 云端配置文件
	private static final String SERVER_CONFIG_URL = "https://wiki.ria.red/wiki/%E7%94%A8%E6%88%B7:FleTime/toriifind.json?action=raw";

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
						.getResourceAsStream("assets/" + MOD_ID + "/toriifind.json");
				
				if (defaultConfigStream != null) {
					Files.copy(defaultConfigStream, configFile, StandardCopyOption.REPLACE_EXISTING);
					defaultConfigStream.close();
					}
				} catch (IOException e) {
			}
		}

		// 检查云端配置文件版本
		try {
			int localVersion = getConfigFileVersion(configFile);
			int serverVersion = fetchServerConfigVersion();
			if (serverVersion > localVersion) {
				LOGGER.info("检测到云端配置文件有新版本，正在下载...");
				downloadServerConfig(configFile);
				LOGGER.info("云端配置文件已更新到最新版本。");
			}
		} catch (Exception e) {
			LOGGER.warn("检查或下载云端配置文件失败：" + e.getMessage());
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

	// 获取云端配置文件 version 字段
	private int fetchServerConfigVersion() throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(SERVER_CONFIG_URL).openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);
		try (InputStream in = conn.getInputStream()) {
			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
			if (jsonObject.has("version")) {
				return jsonObject.get("version").getAsInt();
			}
			return 0;
		}
	}

	// 下载云端配置文件并覆盖本地
	private void downloadServerConfig(Path configFile) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(SERVER_CONFIG_URL).openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);
		try (InputStream in = conn.getInputStream()) {
			Files.copy(in, configFile, StandardCopyOption.REPLACE_EXISTING);
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
