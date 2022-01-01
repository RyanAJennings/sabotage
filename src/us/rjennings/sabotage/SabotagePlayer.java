package us.rjennings.sabotage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Scanner;
import java.util.UUID;

public class SabotagePlayer {
    private final int INITIAL_KARMA_VALUE = 100;

    private Player player;
    // TODO: private PlayerRecord playerRecord;
    private Role role;
    private boolean alive;
    private boolean isAdmin;
    private int karma;
    private String playerRecordPath;

    // TODO: Implement blood & magnifying glass

    public SabotagePlayer (String _playerRecordsPath, Player _player, boolean _isAdmin) {
        player = _player;
        role = Role.UNASSIGNED;
        playerRecordPath = _playerRecordsPath + "/" + player.getUniqueId() + ".txt";

        isAdmin = _isAdmin;
        if (isAdmin) {
            alive = false;
            player.setGameMode(GameMode.SPECTATOR);
        }
        else {
            alive = true;
            player.setGameMode(GameMode.SURVIVAL);
        }

        karma = 0;
        readPlayerRecord();
        player.setLevel(karma);
        Bukkit.getLogger().severe("SETTING PLAYER KARMA TO: " + karma);
        Bukkit.getLogger().severe("PLAYER LEVEL: " + player.getLevel());
    }

    public String getName() {
        return player.getName();
    }

    public void setRole(Role _role) {
        role = _role;
    }
    public Role getRole() {
        return role;
    }
    public void sendRole() {
        if (role == Role.UNASSIGNED) {
            player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    "You haven't been assigned a role yet."));
        }
        else if (role == Role.INNOCENT) {
            player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    "You are " + ChatColor.GREEN.toString() + ChatColor.BOLD + "INNOCENT"));
            player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO, "Find and kill the SABOTEURS"));
        }
        else if (role == Role.DETECTIVE) {
            player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    "You are the " + ChatColor.BLUE.toString() + ChatColor.BOLD + "DETECTIVE"));
            player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO, "Find and kill the SABOTEURS"));
        }
        else if (role == Role.SABOTEUR) {
            player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    "You are a " + ChatColor.RED.toString() + ChatColor.BOLD + "SABOTEUR"));
            player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO, "Kill INNOCENTS and the DETECTIVE"));
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void die() {
        alive = false;
        // TODO: Do we want the player to be able to noclip through the world? This could ruin hidden chests/rooms
        player.setGameMode(GameMode.SPECTATOR);
    }

    public void leave() {
        alive = false;
        writePlayerRecord();
    }

    public void addKarma(int karmaValue) {
        karma += karmaValue;
        player.setLevel(karma);
    }

    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    // TODO: This should probably be handled separately, by permissions
    public boolean isAdmin() {
        return isAdmin;
    }

    public UUID getUuid() {
        return player.getUniqueId();
    }

    /// PLAYER RECORDS ///
    // TODO: Should just use parser lib if we add more info to player records
    // TODO: Move to a PlayerRecord class

    public void readPlayerRecord() {
        try {
            File playerRecord = new File(playerRecordPath);
            Scanner reader = new Scanner(playerRecord);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] line = data.strip().split(":");
                if (line.length < 2) continue;
                String key = line[0].strip();
                String value = line[1].strip();
                handlePlayerRecordLine(key, value);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().info("Player record not found. First login? " + getUuid());
            karma = INITIAL_KARMA_VALUE;
        }
    }

    private void handlePlayerRecordLine(String key, String value) {
        if (key.equalsIgnoreCase("karma")) {
            try {
                karma = Integer.parseInt(value);
                player.setLevel(karma);
            } catch (NumberFormatException e) {
                Bukkit.getLogger().severe("Invalid karma value read from player record.");
            }
        }
    }

    // Should we do this at the end of every game? If the server is stopped, will this plugin stay alive long
    // enough to register that the player left? Or would that event be registered as a playerkickevent?
    public void writePlayerRecord() {
        try {
            File playerRecord = new File(playerRecordPath);
            if (playerRecord.createNewFile()) {
                Bukkit.getLogger().info("Player record created: " + playerRecord.getName());
            }
            else {
                Bukkit.getLogger().info("Found player record: " + playerRecord.getName());
                boolean deleted = playerRecord.delete();
                if (!deleted) {
                    Bukkit.getLogger().severe("Failed to delete player record: " + playerRecord.getName());
                }
                boolean created = playerRecord.createNewFile();
                if (!created) {
                    Bukkit.getLogger().severe("Failed to re-create player record: " + playerRecord.getName());
                }
            }

            FileWriter writer = new FileWriter(playerRecordPath);
            writer.write("karma: "+ karma);
            writer.close();
            Bukkit.getLogger().info("Successfully wrote player record: " + playerRecord.getName());
        } catch (IOException e) {
            Bukkit.getLogger().severe("An error occurred while attempting to write a player record: " +
                    playerRecordPath);
            e.printStackTrace();
        }
    }
}
