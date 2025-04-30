package com.wortex.telegramnotifier; // Замени на свой пакет

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCredentialsCommand implements CommandExecutor {

    private final TelegramNotifier plugin;

    public SetCredentialsCommand(TelegramNotifier plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем права
        if (!sender.hasPermission("telegramnotifier.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /" + label + " [значение]");
            return false; // Показывает usage из plugin.yml
        }

        String value = args[0];

        if (command.getName().equalsIgnoreCase("settoken")) {
            plugin.getConfig().set("bot-token", value);
            plugin.saveConfig(); // Сохраняем конфиг
            plugin.loadConfiguration(); // Перезагружаем конфиг в плагин
            sender.sendMessage(ChatColor.GREEN + "Токен Telegram бота успешно установлен!");
            plugin.getLogger().info("Токен бота обновлен " + (sender instanceof Player ? "игроком " + sender.getName() : "через консоль") + ".");
            if (!plugin.isConfigured()) {
                 sender.sendMessage(ChatColor.YELLOW + "Не забудьте установить Chat ID командой /setchatid [id]");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("setchatid")) {
            // Простая проверка, что это похоже на число (может быть отрицательным для групп)
            try {
                Long.parseLong(value);
            } catch (NumberFormatException e) {
                 sender.sendMessage(ChatColor.RED + "ID чата должен быть числом.");
                 return true;
            }

            plugin.getConfig().set("chat-id", value);
            plugin.saveConfig();
            plugin.loadConfiguration();
            sender.sendMessage(ChatColor.GREEN + "ID чата Telegram успешно установлен!");
             plugin.getLogger().info("Chat ID обновлен " + (sender instanceof Player ? "игроком " + sender.getName() : "через консоль") + ".");
             if (!plugin.isConfigured()) {
                 sender.sendMessage(ChatColor.YELLOW + "Не забудьте установить токен бота командой /settoken [token]");
             } else {
                 // Отправить тестовое сообщение после установки ID, если токен уже есть
                 plugin.sendMessageAsync("Тестовое сообщение! Плагин успешно настроен для этого чата.");
                 sender.sendMessage(ChatColor.AQUA + "Отправлено тестовое сообщение в Telegram.");
             }
            return true;
        }

        return false; // Если команда не распознана (хотя plugin.yml этого не допустит)
    }
}
