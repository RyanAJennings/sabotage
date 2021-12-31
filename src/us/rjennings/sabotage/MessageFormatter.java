package us.rjennings.sabotage;

import org.bukkit.ChatColor;

public class MessageFormatter {

    public enum Format {
        INFO,
        CONSOLE,
        ERROR
    }

    public static String formatMessage(Format format, String message) {
        switch (format) {
            case INFO:
                return formatInfoMessage(message);
            case CONSOLE:
                return formatConsoleLog(message);
            case ERROR:
                return formatErrorMessage(message);
        }
        return "";
    }

    private static String formatErrorMessage(String message) {
        return ChatColor.RED.toString() + ChatColor.BOLD + "[!] " + ChatColor.GRAY + message;
    }

    private static String formatInfoMessage(String message) {
        return ChatColor.GREEN.toString() + ChatColor.BOLD + "[!] " + ChatColor.GRAY + message;
    }

    public static String formatConsoleLog(String message) {
        return "[Sabotage] " + message;
    }

    public static String getFormatPlayerChat(String nameColor) {
        return nameColor + ChatColor.BOLD + "%s" + ChatColor.GRAY + " >> %s";
    }
}
