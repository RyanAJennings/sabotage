package us.rjennings.sabotage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandVote implements CommandExecutor {

    private Sabotage gameInstance;

    public CommandVote(Sabotage _gameInstance) {
        gameInstance = _gameInstance;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player) || strings.length != 1) return false;
        Player player = (Player)commandSender;
        int vote;
        try {
            vote = Integer.parseInt(strings[0]);
        } catch(Exception e) {
            return false;
        }
        String message = gameInstance.getMapHandler().handleVote(player.getUniqueId(), vote);
        player.sendMessage(message);
        return true;
    }
}
