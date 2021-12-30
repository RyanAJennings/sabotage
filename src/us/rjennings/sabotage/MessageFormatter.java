package us.rjennings.sabotage;

import org.bukkit.ChatColor;

public class MessageFormatter {

    public enum Format {
        BROADCAST,
        INFO,
        ERROR,
        DEATH,
        PLAYER_CHAT
    }

    public static String formatMessage(Format format, String message) {
        switch (format) {
            case BROADCAST:
                return "";
            case INFO:
                return formatInfoMessage(message);
            case DEATH:
                return formatDeathMessage(message);
        }
        return "";
    }

    private static String formatInfoMessage(String message) {
        return ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "[!]" +
                ChatColor.GRAY.toString() + ChatColor.BOLD.toString() + message;
    }

    private static String formatErrorMessage(String message) {
        return ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "[!]" +
                ChatColor.GRAY.toString() + ChatColor.BOLD.toString() + message;
    }

    private static String formatDeathMessage(String message) {
        return ChatColor.RED.toString() + ChatColor.BOLD.toString() + "[!]" +
                ChatColor.GRAY.toString() + ChatColor.BOLD.toString() + message;
    }

    public static String getFormatPlayerChat(String nameColor) {
        return nameColor + ChatColor.BOLD + "%s" + ChatColor.GRAY + " >> %s";
    }
}
