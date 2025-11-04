# Aurex Countdown Minecraft Plugin

This is a versatile countdown plugin for Spigot Minecraft servers. It allows players and administrators to manage personal countdown timers with a variety of commands.

## Version

*   **Current Version:** 1.1.0

## Features

*   **Personal Countdowns:** Each player can have their own countdown timer.
*   **Boss Bar Display:** Shows the remaining time in a boss bar.
*   **Offline Timing:** (Optional) Countdown continues even when the player is offline.
*   **Database Support:** Saves countdowns to a database to persist through server restarts.
*   **Admin Commands:** Full administrative control over any player's countdown.
*   **Configurable Messages:** Customize all messages shown to players.

## Player Commands

The main command is `/countdown`.

*   `/countdown start <time>`: Starts a new countdown.
    *   Example: `/countdown start 1h30m`
*   `/countdown pause`: Pauses the current countdown.
*   `/countdown resume`: Resumes a paused countdown.
*   `/countdown reset`: Resets the countdown to its original time.
*   `/countdown cancel`: Stops and removes the current countdown.
*   `/countdown add <time>`: Adds time to the current countdown.
*   `/countdown remove <time>`: Removes time from the current countdown.
*   `/countdown reload`: Reloads the plugin's configuration.

## Admin Commands

The main admin command is `/countdownadmin`.

*   `/countdownadmin start <player> <time>`: Starts a countdown for a specific player.
*   `/countdownadmin pause <player>`: Pauses a player's countdown.
*   `/countdownadmin resume <player>`: Resumes a player's countdown.
*   `/countdownadmin reset <player>`: Resets a player's countdown.
*   `/countdownadmin cancel <player>`: Cancels a player's countdown.
*   `/countdownadmin add <player> <time>`: Adds time to a player's countdown.
*   `/countdownadmin remove <player> <time>`: Removes time from a player's countdown.
*   `/countdownadmin reload`: Reloads the plugin's configuration.

## Permissions

### Player Permissions
*   `countdown.start`: Allows a player to start a countdown.
*   `countdown.pause`: Allows a player to pause their countdown.
*   `countdown.resume`: Allows a player to resume their countdown.
*   `countdown.reset`: Allows a player to reset their countdown.
*   `countdown.cancel`: Allows a player to cancel their countdown.
*   `countdown.add`: Allows a player to add time to their countdown.
*   `countdown.remove`: Allows a player to remove time from their countdown.
*   `countdown.reload`: Allows a player to reload the configuration.

### Admin Permissions
*   `countdown.admin`: Grants access to all `/countdownadmin` commands.

## Installation

1.  Make sure your server is running a compatible version of Spigot or a fork (like Paper).
2.  Download the latest `AurexCountdown-1.1.0.jar` from the project's releases page.
3.  Place the downloaded `.jar` file into the `plugins/` directory of your Minecraft server.
4.  Restart or reload your server.
5.  (Optional) Configure messages and settings in `plugins/AurexCountdown/config.yml`.
