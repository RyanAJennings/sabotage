package us.rjennings.sabotage;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandVote implements CommandExecutor {

    private final Sabotage gameInstance;

    public CommandVote(Sabotage _gameInstance) {
        gameInstance = _gameInstance;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        if (strings.length == 0) {
            if (gameInstance.getMode() == Sabotage.Mode.LOBBY) {
                player.sendMessage(gameInstance.getMapHandler().getBallot());
            }
            else {
                player.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                        "The voting period has ended."));
            }
            return true;
        }

        int vote;
        try {
            vote = Integer.parseInt(strings[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        String message = gameInstance.getMapHandler().handleVote(player.getUniqueId(), vote);
        player.sendMessage(message);
        return true;
    }
}
