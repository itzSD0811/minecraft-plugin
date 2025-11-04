package com.example.demo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final AurexCountdown plugin;

    public PlayerListener(AurexCountdown plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Database.CountdownData countdownData = plugin.getDatabase().loadCountdown(playerUUID);

        if (countdownData != null) {
            Countdown countdown = new Countdown(player, countdownData.getTotalTime(), plugin);
            countdown.setRemainingTime(countdownData.getRemainingTime());
            if (countdownData.isPaused()) {
                countdown.pause();
            }
            plugin.getCountdowns().put(playerUUID, countdown);

            if (!plugin.isOfflineCountdown()) {
                countdown.resume();
            }

            player.sendMessage(plugin.getCountdownResumedMsg());
            plugin.getDatabase().deleteCountdown(playerUUID);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (plugin.getCountdowns().containsKey(playerUUID)) {
            Countdown countdown = plugin.getCountdowns().get(playerUUID);
            if (!plugin.isOfflineCountdown()) {
                countdown.pause();
            }
            plugin.getDatabase().saveCountdown(playerUUID, countdown);
            countdown.getTask().cancel();
            plugin.getCountdowns().remove(playerUUID);
        }
    }
}
