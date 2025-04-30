package com.wortex.telegramnotifier; // Замени на свой пакет

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    private final TelegramNotifier plugin;

    public EventListener(TelegramNotifier plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR) // MONITOR - обрабатывать после других плагинов
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.isConfigured()) return;
        Player player = event.getPlayer();
        String message = plugin.formatMessage("player-join", player.getName(), null);
        plugin.sendMessageAsync(message);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isConfigured()) return;
        Player player = event.getPlayer();
        String message = plugin.formatMessage("player-quit", player.getName(), null);
        plugin.sendMessageAsync(message);
    }

    // Используем AsyncPlayerChatEvent для лучшей производительности
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) // ignoreCancelled = true - не отправлять отмененные сообщения
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.isConfigured()) return;
        Player player = event.getPlayer();
        String chatMessage = event.getMessage();
        String message = plugin.formatMessage("player-chat", player.getName(), chatMessage);
        plugin.sendMessageAsync(message);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.isConfigured()) return;
        // Не отправлять команды установки токена/чата в телеграм
        if (event.getMessage().toLowerCase().startsWith("/settoken") || event.getMessage().toLowerCase().startsWith("/setchatid")) {
            return;
        }
        Player player = event.getPlayer();
        String command = event.getMessage();
        String message = plugin.formatMessage("player-command", player.getName(), command);
        plugin.sendMessageAsync(message);
    }
}
