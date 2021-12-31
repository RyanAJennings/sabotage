package us.rjennings.sabotage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

// TODO: Generalize to Poll
public class MapPoll {

    private static final int NUM_OPTIONS = 4;

    private String ballot;
    private ArrayList<String> mapOptions;
    private String winner;
    private HashMap<Integer, Integer> votes;
    private HashMap<UUID, Integer> voteRecord;

    public MapPoll(ArrayList<String> maps) {
        Random random = new Random();

        mapOptions = new ArrayList<>();
        if  (maps.size() < NUM_OPTIONS - 1) {
            mapOptions.add("dummy");
            mapOptions = maps;
        }
        else {
            for (int i = 0; i < NUM_OPTIONS - 1; i++) {
                mapOptions.add(maps.get(random.nextInt(maps.size())));
            }
        }
        mapOptions.add("Random");

        votes = new HashMap<>();
        for (int i = 1; i < mapOptions.size() + 1; i++) {
            votes.put(i, 0);
        }

        voteRecord = new HashMap<>();

        updateBallot();
    }

    public String getBallot() {
        return ballot;
    }

    public void endVoting() {
        int winnerIdx = mapOptions.size() - 1;  // Default to Random
        int max = 0;
        for (int i =  0; i  < mapOptions.size(); i++) {
            // In the event of a tie, choose the first option in the list with the max votes
            if (votes.get(i) > max) {
                winnerIdx = i;
            }
        }
        winner = mapOptions.get(winnerIdx);
    }

    public String getWinner() { return winner; }

    public String handleVote(UUID playerUUID, int vote) {
        if (vote > mapOptions.size() || vote < 0) {
            return MessageFormatter.formatMessage(MessageFormatter.Format.ERROR, "Invalid vote.");
        }

        boolean playerHasExistingVote = voteRecord.containsKey(playerUUID);

        if (playerHasExistingVote) {
            int oldVote = voteRecord.get(playerUUID);
            if (vote == 0) {
                voteRecord.remove(playerUUID);
                votes.put(oldVote, votes.get(oldVote) - 1);
                updateBallot();
                return MessageFormatter.formatMessage(MessageFormatter.Format.INFO, "Vote retracted");
            }
            return changeVote(playerUUID, oldVote, vote);
        }

        int newOptionVotes = votes.get(vote);
        voteRecord.put(playerUUID, vote);
        votes.put(vote, newOptionVotes + 1);
        updateBallot();
        return MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                "Voted for option " + vote);
    }

    private String changeVote(UUID playerUUID, int from, int to) {
        if (from == to) {
            return MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    "Vote already counted for option " + to);
        }

        int oldOptionVotes = votes.get(from);
        votes.put(from, oldOptionVotes - 1);
        votes.put(to, votes.get(to) + 1);
        voteRecord.put(playerUUID, to);
        updateBallot();
        return MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                "Vote changed from " + from + " to " + to);
    }

    // TODO: Format
    private void updateBallot() {
        ballot = ChatColor.GRAY.toString() + ChatColor.BOLD + "----------------------\n";
        ballot += ChatColor.GREEN + "--+-- " + ChatColor.GRAY + " Vote for a map!" + ChatColor.GREEN + " --+-- \n";
        int optionNum = 1;
        votes.forEach((mapOption, voteCount) -> {
            ballot += ChatColor.GRAY.toString() + optionNum + ". " +  mapOptions.get(mapOption - 1) + ": " + voteCount + "\n";
        });
        ballot += ChatColor.GRAY.toString() + ChatColor.BOLD + "----------------------\n";
    }
}
