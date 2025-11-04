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
    private Database database;
    private boolean offlineCountdown;
    private BarColor bossBarColor;
    private ChatColor textColor;
    private String countdownStartedMsg;
    private String countdownFinishedMsg;
    private String countdownReloadedMsg;
    private String countdownPausedMsg;
    private String countdownResumedMsg;
    private String timeAddedMsg;
    private String timeRemovedMsg;
    private String countdownCanceledMsg;
    private String timeAddedByAdminMsg;
    private String timeRemovedByAdminMsg;
    private String countdownCanceledByAdminMsg;
    private String adminTimeAddedMsg;
    private String adminTimeRemovedMsg;
    private String adminCountdownCanceledMsg;
    private String countdownStartedBossbarMsg;
    private String countdownFinishedBossbarMsg;
    private String bossbarPrefix;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        database = new Database(this);
        database.connect();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getCommand("countdown").setExecutor(new CountdownCommand(this));
        getCommand("countdownadmin").setExecutor(new CountdownAdminCommand(this));
        loadOnlinePlayersCountdowns();
        getLogger().info("AurexCountdown has been enabled.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
            "\n" +
            "&d█████╗ ██╗   ██╗██████╗ ███████╗██╗  ██╗\n" +
            "&d██╔══██╗██║   ██║██╔══██╗██╔════╝╚██╗██╔╝\n" +
            "&d███████║██║   ██║██████╔╝█████╗   ╚███╔╝\n" +
            "&d██╔══██║██║   ██║██╔══██╗██╔══╝   ██╔██╗\n" +
            "&d██║  ██║╚██████╔╝██║  ██║███████╗██╔╝ ██╗\n" +
            "&d╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝\n" +
            "&7               &lAurexCountdown\n" +
            "&7             Developed By itzSD, Aurex Studios\n"
        ));
    }

    @Override
    public void onDisable() {
        saveAllCountdowns();
        for (Countdown countdown : countdowns.values()) {
            countdown.getBossBar().removeAll();
        }
        countdowns.clear();
        database.disconnect();
        getLogger().info("AurexCountdown has been disabled.");
    }

    public void loadConfig() {
        reloadConfig();
        offlineCountdown = getConfig().getBoolean("offline-countdown", true);
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
        countdownPausedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.countdown-paused", "&eCountdown paused."));
        countdownResumedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.countdown-resumed", "&aCountdown resumed."));
        timeAddedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.time-added", "&a{time} has been added to your countdown."));
        timeRemovedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.time-removed", "&c{time} has been removed from your countdown."));
        countdownCanceledMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.countdown-canceled", "&cYour countdown has been canceled."));
        timeAddedByAdminMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.time-added-by-admin", "&aAn admin added {time} to your countdown."));
        timeRemovedByAdminMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.time-removed-by-admin", "&cAn admin removed {time} from your countdown."));
        countdownCanceledByAdminMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.countdown-canceled-by-admin", "&cAn admin canceled your countdown."));
        adminTimeAddedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.admin-time-added", "&aYou added {time} to {player}\'s countdown."));
        adminTimeRemovedMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.admin-time-removed", "&cYou removed {time} from {player}\'s countdown."));
        adminCountdownCanceledMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.admin-countdown-canceled", "&cYou canceled {player}\'s countdown."));
        countdownStartedBossbarMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("bossbar-messages.countdown-started", "&aCountdown has started!"));
        countdownFinishedBossbarMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("bossbar-messages.countdown-finished", "&cCountdown has finished!"));
        bossbarPrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("bossbar-messages.bossbar-prefix", "Countdown: "));
    }

    private void saveAllCountdowns() {
        for (Map.Entry<UUID, Countdown> entry : countdowns.entrySet()) {
            database.saveCountdown(entry.getKey(), entry.getValue());
        }
    }

    private void loadOnlinePlayersCountdowns() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            Database.CountdownData countdownData = database.loadCountdown(playerUUID);
            if (countdownData != null) {
                Countdown countdown = new Countdown(player, countdownData.getTotalTime(), this);
                countdown.setRemainingTime(countdownData.getRemainingTime());
                if (countdownData.isPaused()) {
                    countdown.pause();
                }
                countdowns.put(playerUUID, countdown);
                database.deleteCountdown(playerUUID);
            }
        }
    }

    public Map<UUID, Countdown> getCountdowns() {
        return countdowns;
    }

    public Database getDatabase() {
        return database;
    }

    public boolean isOfflineCountdown() {
        return offlineCountdown;
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

    public String getCountdownPausedMsg() {
        return countdownPausedMsg;
    }

    public String getCountdownResumedMsg() {
        return countdownResumedMsg;
    }

    public String getTimeAddedMsg() {
        return timeAddedMsg;
    }

    public String getTimeRemovedMsg() {
        return timeRemovedMsg;
    }

    public String getCountdownCanceledMsg() {
        return countdownCanceledMsg;
    }

    public String getTimeAddedByAdminMsg() {
        return timeAddedByAdminMsg;
    }

    public String getTimeRemovedByAdminMsg() {
        return timeRemovedByAdminMsg;
    }

    public String getCountdownCanceledByAdminMsg() {
        return countdownCanceledByAdminMsg;
    }

    public String getAdminTimeAddedMsg() {
        return adminTimeAddedMsg;
    }

    public String getAdminTimeRemovedMsg() {
        return adminTimeRemovedMsg;
    }

    public String getAdminCountdownCanceledMsg() {
        return adminCountdownCanceledMsg;
    }

    public String getCountdownStartedBossbarMsg() {
        return countdownStartedBossbarMsg;
    }

    public String getCountdownFinishedBossbarMsg() {
        return countdownFinishedBossbarMsg;
    }

    public String getBossbarPrefix() {
        return bossbarPrefix;
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
    private long totalTime;
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
                    bossBar.setTitle(plugin.getTextColor() + plugin.getBossbarPrefix() + AurexCountdown.formatTime(remainingTime));
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
                            plugin.getDatabase().deleteCountdown(player.getUniqueId());
                        }
                    }.runTaskLater(plugin, 60L); // 3 seconds
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
        if (bossBar != null) {
            bossBar.removeAll();
        }
        plugin.getCountdowns().remove(player.getUniqueId());
        plugin.getDatabase().deleteCountdown(player.getUniqueId());
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

    public void addTime(long time) {
        this.totalTime += time;
        this.remainingTime += time;
    }

    public void removeTime(long time) {
        this.totalTime -= time;
        if (totalTime < 0) {
             totalTime = 0;
        }
        this.remainingTime -= time;
        if (remainingTime < 0) {
            remainingTime = 0;
        }
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

    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
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
                player.sendMessage(plugin.getCountdownPausedMsg());
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
                player.sendMessage(plugin.getCountdownResumedMsg());
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

            case "cancel":
                if (!player.hasPermission("countdown.cancel")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                countdown = plugin.getCountdowns().get(player.getUniqueId());
                if (countdown == null) {
                    player.sendMessage(ChatColor.RED + "You don't have a countdown running.");
                    return true;
                }
                countdown.cancel();
                player.sendMessage(plugin.getCountdownCanceledMsg());
                break;
            
            case "add":
                if (!player.hasPermission("countdown.add")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /countdown add <time>");
                    return true;
                }
                countdown = plugin.getCountdowns().get(player.getUniqueId());
                if (countdown == null) {
                    player.sendMessage(ChatColor.RED + "You don't have a countdown running.");
                    return true;
                }
                long timeToAdd = AurexCountdown.parseTime(args[1]);
                if (timeToAdd <= 0) {
                    player.sendMessage(ChatColor.RED + "Invalid time format. Use h, m, or s.");
                    return true;
                }
                countdown.addTime(timeToAdd);
                player.sendMessage(plugin.getTimeAddedMsg().replace("{time}", AurexCountdown.formatTime(timeToAdd)));
                break;

            case "remove":
                if (!player.hasPermission("countdown.remove")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /countdown remove <time>");
                    return true;
                }
                countdown = plugin.getCountdowns().get(player.getUniqueId());
                if (countdown == null) {
                    player.sendMessage(ChatColor.RED + "You don't have a countdown running.");
                    return true;
                }
                long timeToRemove = AurexCountdown.parseTime(args[1]);
                if (timeToRemove <= 0) {
                    player.sendMessage(ChatColor.RED + "Invalid time format. Use h, m, or s.");
                    return true;
                }
                countdown.removeTime(timeToRemove);
                player.sendMessage(plugin.getTimeRemovedMsg().replace("{time}", AurexCountdown.formatTime(timeToRemove)));
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
        player.sendMessage(ChatColor.GOLD + "/countdown cancel - Cancel your countdown.");
        player.sendMessage(ChatColor.GOLD + "/countdown add <time> - Add time to your countdown.");
        player.sendMessage(ChatColor.GOLD + "/countdown remove <time> - Remove time from your countdown.");
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

            case "cancel":
                countdown = plugin.getCountdowns().get(target.getUniqueId());
                if (countdown == null) {
                    sender.sendMessage(ChatColor.RED + "That player doesn't have a countdown running.");
                    return true;
                }
                countdown.cancel();
                sender.sendMessage(plugin.getAdminCountdownCanceledMsg().replace("{player}", target.getName()));
                target.sendMessage(plugin.getCountdownCanceledByAdminMsg());
                break;

            case "add":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /countdownadmin add <player> <time>");
                    return true;
                }
                countdown = plugin.getCountdowns().get(target.getUniqueId());
                if (countdown == null) {
                    sender.sendMessage(ChatColor.RED + "That player doesn't have a countdown running.");
                    return true;
                }
                long timeToAdd = AurexCountdown.parseTime(args[2]);
                if (timeToAdd <= 0) {
                    sender.sendMessage(ChatColor.RED + "Invalid time format. Use h, m, or s.");
                    return true;
                }
                countdown.addTime(timeToAdd);
                String formattedTime = AurexCountdown.formatTime(timeToAdd);
                sender.sendMessage(plugin.getAdminTimeAddedMsg().replace("{time}", formattedTime).replace("{player}", target.getName()));
                target.sendMessage(plugin.getTimeAddedByAdminMsg().replace("{time}", formattedTime));
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /countdownadmin remove <player> <time>");
                    return true;
                }
                countdown = plugin.getCountdowns().get(target.getUniqueId());
                if (countdown == null) {
                    sender.sendMessage(ChatColor.RED + "That player doesn't have a countdown running.");
                    return true;
                }
                long timeToRemove = AurexCountdown.parseTime(args[2]);
                if (timeToRemove <= 0) {
                    sender.sendMessage(ChatColor.RED + "Invalid time format. Use h, m, or s.");
                    return true;
                }
                countdown.removeTime(timeToRemove);
                String formattedRemoveTime = AurexCountdown.formatTime(timeToRemove);
                sender.sendMessage(plugin.getAdminTimeRemovedMsg().replace("{time}", formattedRemoveTime).replace("{player}", target.getName()));
                target.sendMessage(plugin.getTimeRemovedByAdminMsg().replace("{time}", formattedRemoveTime));
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
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin cancel <player>");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin add <player> <time>");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin remove <player> <time>");
        sender.sendMessage(ChatColor.GOLD + "/countdownadmin reload");
    }
}
