package com.example.demo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AurexCountdown extends JavaPlugin {

    private final Map<UUID, Countdown> countdowns = new HashMap<>();
    private BarColor bossBarColor;
    private ChatColor textColor;
    private String countdownStartedMsg;
    private String countdownFinishedMsg;
    private String countdownReloadedMsg;
    private String countdownStartedBossbarMsg;
    private String countdownFinishedBossbarMsg;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getCommand("countdown").setExecutor(new CountdownCommand(this));
        getCommand("countdownadmin").setExecutor(new CountdownAdminCommand(this));
        getLogger().info("AurexCountdown has been enabled.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', """
                  &d█████╗ ██╗   ██╗██████╗ ███████╗██╗  ██╗
                  &d██╔══██╗██║   ██║██╔══██╗██╔════╝╚██╗██╔╝
                  &d███████║██║   ██║██████╔╝█████╗   ╚███╔╝
                  &d██╔══██║██║   ██║██╔══██╗██╔══╝   ██╔██╗
                  &d██║  ██║╚██████╔╝██║  ██║███████╗██╔╝ ██╗
                  &d╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝
                  &7               &lAurexCountdown
                  &7             Developed By itzSD, Aurex Studios
"""));
    }

    @Override
    public void onDisable() {
        for (Countdown countdown : countdowns.values()) {
            countdown.getBossBar().removeAll();
        }
        countdowns.clear();
        getLogger().info("AurexCountdown has been disabled.");
    }

    public void loadConfig() {
        reloadConfig();
        try {
            bossBarColor = BarColor.valueOf(getConfig().getString("bossbar-color", "BLUE").toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid bossbar-color in config.yml. Defaulting to BLUE.");
            bossBarColor = BarColor.BLUE;
        }
        try {
            textColor = ChatColor.valueOf(getConfig().getString("text-color", "WHITE").toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid text-color in config.yml. Defaulting to WHITE.");
            textColor = ChatColor.WHITE;
        }
        countdownStartedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.countdown-started", "&aCountdown started!"));
        countdownFinishedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.countdown-finished", "&cCountdown finished!"));
        countdownReloadedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.countdown-reloaded", "&eConfiguration reloaded!"));
        countdownStartedBossbarMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("bossbar-messages.countdown-started", "&aCountdown has started!"));
        countdownFinishedBossbarMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("bossbar-messages.countdown-finished", "&cCountdown has finished!"));
    }

    public Map<UUID, Countdown> getCountdowns() {
        return countdowns;
    }

    public BarColor getBossBarColor() {
        return bossBarColor;
    }

    public ChatColor getTextColor() {
        return textColor;
    }

    public String getCountdownStartedMsg() {
        return countdownStartedMsg;
    }

    public String getCountdownFinishedMsg() {
        return countdownFinishedMsg;
    }

    public String getCountdownReloadedMsg() {
        return countdownReloadedMsg;
    }

    public String getCountdownStartedBossbarMsg() {
        return countdownStartedBossbarMsg;
    }

    public String getCountdownFinishedBossbarMsg() {
        return countdownFinishedBossbarMsg;
    }

    public static String formatTime(long seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    public static long parseTime(String timeString) {
        long totalSeconds = 0;
        try {
            char unit = timeString.toLowerCase().charAt(timeString.length() - 1);
            int time = Integer.parseInt(timeString.substring(0, timeString.length() - 1));
            switch (unit) {
                case 'h':
                    totalSeconds = time * 3600;
                    break;
                case 'm':
                    totalSeconds = time * 60;
                    break;
                case 's':
                    totalSeconds = time;
                    break;
                default:
                    return -1;
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return -1;
        }
        return totalSeconds;
    }
}

class Countdown {
    private final Player player;
    private final long totalTime;
    private long remainingTime;
    private BukkitTask task;
    private final BossBar bossBar;
    private boolean paused;
    private final AurexCountdown plugin;

    public Countdown(Player player, long time, AurexCountdown plugin) {
        this.plugin = plugin;
        this.player = player;
        this.totalTime = time;
        this.remainingTime = time;
        this.bossBar = Bukkit.createBossBar(plugin.getCountdownStartedBossbarMsg(), plugin.getBossBarColor(), BarStyle.SOLID);
        this.bossBar.addPlayer(player);
        this.paused = false;

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (paused) {
                    return;
                }
                if (remainingTime > 0) {
                    bossBar.setTitle(plugin.getTextColor() + "Countdown: " + AurexCountdown.formatTime(remainingTime));
                    bossBar.setProgress((double) remainingTime / totalTime);
                    remainingTime--;
                } else {
                    bossBar.setTitle(plugin.getCountdownFinishedBossbarMsg());
                    bossBar.setProgress(0);
                    player.sendMessage(plugin.getCountdownFinishedMsg());
                    cancel();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            bossBar.removeAll();
                            plugin.getCountdowns().remove(player.getUniqueId());
                        }
                    }.runTaskLater(plugin, 60L); // 3 seconds
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void reset() {
        remainingTime = totalTime;
    }

    public Player getPlayer() {
        return player;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public BukkitTask getTask() {
        return task;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public boolean isPaused() {
        return paused;
    }
}

class CountdownCommand implements CommandExecutor {

    private final AurexCountdown plugin;

    public CountdownCommand(AurexCountdown plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "start":
                if (!player.hasPermission("countdown.start")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /countdown start <time>");
                    return true;
                }
                if (plugin.getCountdowns().containsKey(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You already have a countdown running.");
                    return true;
                }
                long time = AurexCountdown.parseTime(args[1]);
                if (time <= 0) {
                    player.sendMessage(ChatColor.RED + "Invalid time format. Use h, m, or s.");
                    return true;
                }
                plugin.getCountdowns().put(player.getUniqueId(), new Countdown(player, time, plugin));
                player.sendMessage(plugin.getCountdownStartedMsg());
                break;

            case "pause":
                if (!player.hasPermission("countdown.pause")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                Countdown countdown = plugin.getCountdowns().get(player.getUniqueId());
                if (countdown == null) {
                    player.sendMessage(ChatColor.RED + "You don't have a countdown running.");
                    return true;
                }
                countdown.pause();
                player.sendMessage(ChatColor.YELLOW + "Countdown paused.");
                break;

            case "resume":
                if (!player.hasPermission("countdown.resume")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                countdown = plugin.getCountdowns().get(player.getUniqueId());
                if (countdown == null) {
                    player.sendMessage(ChatColor.RED + "You don't have a countdown running.");
                    return true;
                }
                countdown.resume();
                player.sendMessage(ChatColor.GREEN + "Countdown resumed.");
                break;

            case "reset":
                if (!player.hasPermission("countdown.reset")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                countdown = plugin.getCountdowns().get(player.getUniqueId());
                if (countdown == null) {
                    player.sendMessage(ChatColor.RED + "You don't have a countdown running.");
                    return true;
                }
                countdown.reset();
                player.sendMessage(ChatColor.BLUE + "Countdown reset.");
                break;
            case "reload":
                 if (!player.hasPermission("countdown.reload")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                plugin.loadConfig();
                player.sendMessage(plugin.getCountdownReloadedMsg());
                break;

            default:
                sendUsage(player);
                break;
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.AQUA + "--- AurexCountdown Help ---");
        player.sendMessage(ChatColor.GOLD + "/countdown start <time> - Start a countdown.");
        player.sendMessage(ChatColor.GOLD + "/countdown pause - Pause your countdown.");
        player.sendMessage(ChatColor.GOLD + "/countdown resume - Resume your countdown.");
        player.sendMessage(ChatColor.GOLD + "/countdown reset - Reset your countdown.");
        player.sendMessage(ChatColor.GOLD + "/countdown reload - Reload the config.");
    }
}

class CountdownAdminCommand implements CommandExecutor {

    private final AurexCountdown plugin;

    public CountdownAdminCommand(AurexCountdown plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("countdown.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.loadConfig();
            sender.sendMessage(plugin.getCountdownReloadedMsg());
            return true;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        switch (subCommand) {
            case "start":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /countdownadmin start <player> <time>");
                    return true;
                }
                if (plugin.getCountdowns().containsKey(target.getUniqueId())) {
                    sender.sendMessage(ChatColor.RED + "That player already has a countdown running.");
                    return true;
                }
                long time = AurexCountdown.parseTime(args[2]);
                if (time <= 0) {
                    sender.sendMessage(ChatColor.RED + "Invalid time format. Use h, m, or s.");
                    return true;
                }
                plugin.getCountdowns().put(target.getUniqueId(), new Countdown(target, time, plugin));
                sender.sendMessage(ChatColor.GREEN + "Countdown started for " + target.getName() + ".");
                target.sendMessage(ChatColor.GREEN + "An admin has started a countdown for you.");
                break;

            case "pause":
                Countdown countdown = plugin.getCountdowns().get(target.getUniqueId());
                if (countdown == null) {
                    sender.sendMessage(ChatColor.RED + "That player doesn't have a countdown running.");
                    return true;
                }
                countdown.pause();
                sender.sendMessage(ChatColor.YELLOW + "Countdown paused for " + target.getName() + ".");
                target.sendMessage(ChatColor.YELLOW + "An admin has paused your countdown.");
                break;

            case "resume":
                countdown = plugin.getCountdowns().get(target.getUniqueId());
                if (countdown == null) {
                    sender.sendMessage(ChatColor.RED + "That player doesn't have a countdown running.");
                    return true;
                }
                countdown.resume();
                sender.sendMessage(ChatColor.GREEN + "Countdown resumed for " + target.getName() + ".");
                target.sendMessage(ChatColor.GREEN + "An admin has resumed your countdown.");
                break;

            case "reset":
                countdown = plugin.getCountdowns().get(target.getUniqueId());
                if (countdown == null) {
                    sender.sendMessage(ChatColor.RED + "That player doesn't have a countdown running.");
                    return true;
                }
                countdown.reset();
                sender.sendMessage(ChatColor.BLUE + "Countdown reset for " + target.getName() + ".");
                target.sendMessage(ChatColor.BLUE + "An admin has reset your countdown.");
                break;

            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "--- AurexCountdown Admin Help ---");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin start <player> <time>");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin pause <player>");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin resume <player>");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin reset <player>");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin reload");
    }
}
