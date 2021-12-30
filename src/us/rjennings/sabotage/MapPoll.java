package us.rjennings.sabotage;

import org.bukkit.Bukkit;

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
        if  (maps.size() < NUM_OPTIONS - 1) {
            mapOptions = maps;
        }
        else {
            for (int i = 0; i < NUM_OPTIONS - 1; i++) {
                mapOptions.add(maps.get(random.nextInt()));
            }
        }
        mapOptions.add("Random");

        winner = new String();
        votes = new HashMap<>();
        voteRecord = new HashMap<>();

        ballot = new String();
        updateBallot();
    }

    public String getBallot() {
        return ballot;
    }

    public void endVoting() {
        int winnerIdx = NUM_OPTIONS - 1;  // Default to Random
        int max = 0;
        for (int i =  0; i  < NUM_OPTIONS - 1; i++) {
            // In the event of a tie, choose the first option in the list with the max votes
            if (votes.get(i) > max) {
                winnerIdx = i;
            }
        }
        winner = mapOptions.get(winnerIdx);
    }

    public String getWinner() { return winner; }

    public String handleVote(UUID playerUUID, int vote) {
        if (vote > NUM_OPTIONS || vote < 0) {
            return MessageFormatter.formatMessage(MessageFormatter.Format.ERROR,
                    "Invalid vote.");
        }

        int newOptionVotes = votes.get(vote);
        boolean playerHasExistingVote = voteRecord.containsKey(playerUUID);

        if (playerHasExistingVote) {
            int oldVote = voteRecord.get(playerUUID);
            if (vote == oldVote) {
                return MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                        "Vote already counted for option " + vote + ".");
            }

            int oldOptionVotes = votes.get(oldVote);
            votes.put(oldVote, oldOptionVotes - 1);

            if (vote == 0) {
                voteRecord.remove(playerUUID);
                updateBallot();
                return MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                        "Vote retracted.");
            }
        }

        voteRecord.put(playerUUID, vote);
        votes.put(vote, newOptionVotes + 1);
        updateBallot();
        return MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                "Voted for option " + vote + "!");
    }

    private void updateBallot() {
        ballot = "Vote for a map! \n";
        int optionNum = 1;
        votes.forEach((mapOption, votes) -> {
            ballot += optionNum + ". " +  mapOption + ": \t" + votes;
        });
    }
}
