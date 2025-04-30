package com.wortex.telegramnotifier; // Замени на свой пакет

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public final class TelegramNotifier extends JavaPlugin {

    private String botToken;
    private String chatId;
    private boolean configured = false;

    @Override
    public void onEnable() {
        // Сохраняем конфиг по умолчанию, если его нет
        saveDefaultConfig();
        // Загружаем конфиг
        loadConfiguration();

        if (!configured) {
            getLogger().warning("Плагин не настроен! Используйте /settoken и /setchatid.");
        } else {
             getLogger().info("TelegramNotifier включен и настроен.");
             sendMessageAsync(getConfig().getString("messages.server-start", "✅ Сервер запущен!"));
        }

        // Регистрация команд
        this.getCommand("settoken").setExecutor(new SetCredentialsCommand(this));
        this.getCommand("setchatid").setExecutor(new SetCredentialsCommand(this));

        // Регистрация слушателя событий
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        getLogger().info("TelegramNotifier Plugin Enabled!");
    }

    @Override
    public void onDisable() {
         if (configured) {
             // Отправляем сообщение синхронно, т.к. сервер выключается
             // ВНИМАНИЕ: Это может не всегда успеть отправиться при резкой остановке!
             sendMessageSync(getConfig().getString("messages.server-stop", "❌ Сервер остановлен!"));
         }
        getLogger().info("TelegramNotifier Plugin Disabled!");
    }

    // Метод для загрузки конфигурации
    public void loadConfiguration() {
        reloadConfig(); // Перезагружаем из файла
        FileConfiguration config = getConfig();
        this.botToken = config.getString("bot-token", "");
        this.chatId = config.getString("chat-id", "");

        if (this.botToken == null || this.botToken.isEmpty() || this.chatId == null || this.chatId.isEmpty()) {
            this.configured = false;
        } else {
            this.configured = true;
        }
         getLogger().log(Level.INFO, "Конфигурация загружена. Бот настроен: " + configured);
    }

    // Геттер для проверки конфигурации
    public boolean isConfigured() {
        return configured;
    }

    // Метод для асинхронной отправки сообщения в Telegram
    public void sendMessageAsync(String message) {
        if (!configured) {
            getLogger().warning("Попытка отправить сообщение, но плагин не настроен.");
            return;
        }

        // Используем планировщик Bukkit для асинхронной отправки
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            sendMessageSync(message); // Вызываем синхронный метод из асинхронной задачи
        });
    }

    // Синхронный метод отправки (для вызова из onDisable и асинхронных задач)
    private void sendMessageSync(String message) {
        if (!configured) return; // Дополнительная проверка

        HttpURLConnection connection = null;
        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + encodedMessage + "&parse_mode=HTML"; // Можно использовать HTML или Markdown

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // GET-запрос для sendMessage обычно достаточен
            connection.setConnectTimeout(5000); // 5 секунд таймаут соединения
            connection.setReadTimeout(5000); // 5 секунд таймаут чтения

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                // Читаем ответ об ошибке от Telegram API
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String inputLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    errorResponse.append(inputLine);
                }
                in.close();
                 getLogger().warning("Ошибка отправки сообщения в Telegram! Код: " + responseCode + ", Ответ: " + errorResponse.toString());
            } else {
                // Опционально: Логировать успешную отправку (может засорять консоль)
                // getLogger().info("Сообщение успешно отправлено в Telegram.");
            }

        } catch (Exception e) {
            getLogger().severe("Исключение при отправке сообщения в Telegram: " + e.getMessage());
            // e.printStackTrace(); // Раскомментировать для детальной отладки
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Метод для форматирования сообщений (заменяет плейсхолдеры)
    public String formatMessage(String key, String player, String content) {
        String message = getConfig().getString("messages." + key, "");
        if (player != null) {
            message = message.replace("%player%", player);
        }
        if (content != null) {
            // Экранируем базовые HTML теги для безопасности, если не используем parse_mode=HTML
            // Если используем parse_mode=HTML, нужно быть осторожным с пользовательским вводом
             content = content.replace("&", "&").replace("<", "<").replace(">", ">");
             message = message.replace("%message%", content).replace("%command%", content);
        }
        return message;
    }
}