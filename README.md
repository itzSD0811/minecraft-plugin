# Aurex Countdown Minecraft Plugin

This is a simple countdown plugin for Spigot Minecraft servers. It allows players with the correct permissions to start, pause, resume, and reset their own personal countdown timer.

## Features

*   **Personal Countdowns:** Each player has their own countdown timer.
*   **Simple Commands:** Easy-to-use commands to manage the countdown.
*   **Time Formatting:** Supports `h` (hours), `m` (minutes), and `s` (seconds) for setting the time.
*   **Permissions:** Control who can use the countdown commands.

## Commands

The main command is `/countdown`.

*   `/countdown start <time>`: Starts a new countdown.
    *   Example: `/countdown start 1h30m`
*   `/countdown pause`: Pauses the current countdown.
*   `/countdown resume`: Resumes a paused countdown.
*   `/countdown reset`: Resets and stops the current countdown.

## Permissions

*   `countdown.start`: Allows a player to start a countdown.
*   `countdown.pause`: Allows a player to pause their countdown.
*   `countdown.resume`: Allows a player to resume their countdown.
*   `countdown.reset`: Allows a player to reset their countdown.

## Installation

1.  Make sure your server is running Spigot or a compatible fork (like Paper) for Minecraft 1.21 or later.
2.  Download the latest `minecraft-plugin-1.0.0.jar` from the project's releases page.
3.  Place the downloaded `.jar` file into the `plugins/` directory of your Minecraft server.
4.  Restart or reload your server.
