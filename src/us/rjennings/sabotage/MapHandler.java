package us.rjennings.sabotage;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.UUID;

public class MapHandler {

    private MapPoll mapPoll;
    private World lobby;
    private ArrayList<String> mapNames;
    private ArrayList<World> maps;

    private Sabotage plugin;
    private MVWorldManager worldManager;

    public MapHandler(Sabotage _plugin, String _lobby, ArrayList<String> _maps) {
        plugin = _plugin;
        worldManager = getMultiverseCore().getMVWorldManager();

        loadMaps(_lobby, _maps);
        if (maps.size() == 0) {
            // TODO: Build a default map when no map can be loaded
            throw new RuntimeException("Unable to load any maps!");
        }
    }

    private MultiverseCore getMultiverseCore() {
        Plugin mvcPlugin = plugin.getServer().getPluginManager().getPlugin("MultiverseCore");
        if (mvcPlugin.getName().equalsIgnoreCase("Multiverse-Core") ) {
            return (MultiverseCore) mvcPlugin;
        }
        throw new RuntimeException("MultiVerse not found!");
    }

    public void reset() {
        mapPoll = new MapPoll(mapNames);
    }

    private void loadMaps(String lobbyWorldName, ArrayList<String> mapWorldNames) {
        mapNames = new ArrayList<>();
        maps = new ArrayList<>();

        mapWorldNames.forEach(mapName -> {
            if (worldManager.loadWorld(mapName)) {
                Bukkit.getLogger().info("Successfully loaded map: " + mapName);
                maps.add(Bukkit.getWorld(mapName));
                mapNames.add(mapName);
            }
            else {
                Bukkit.getLogger().severe("Failed to load map: " + mapName);
            }
        });

        if (!worldManager.loadWorld(lobbyWorldName)) {
            Bukkit.getLogger().severe("Unable to load the lobby world! Using a random world.");
            lobby = Bukkit.createWorld(new WorldCreator("LOBBY"));
        }
        else {
            Bukkit.getLogger().info("Successfully loaded the lobby world.");
        }
    }

    public World getGameMap() {
        return Bukkit.getWorld(mapPoll.getWinner());
    }

    public World getLobby() {
        return lobby;
    }

    //// MAP POLL ////

    // TODO: Generalize
    public String handleVote(UUID playerUUID, int vote) {
        return mapPoll.handleVote(playerUUID, vote);
    }

    // TODO: Generalize
    public String getBallot() {
        return mapPoll.getBallot();
    }
}
