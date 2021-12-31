package us.rjennings.sabotage;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;

public class Configuration {

    private FileConfiguration config;

    private boolean startGameInConfigMode;
    private HashSet<String> gameAdmins;
    private ArrayList<String> maps;
    private String lobby;

    public Configuration(FileConfiguration _config) {
        config = _config;
        this.load();
    }

    private void load() {
        startGameInConfigMode = config.getBoolean("configMode");
        gameAdmins =  new HashSet<String>(config.getStringList("administrators"));
        maps = new ArrayList<String>(config.getStringList("maps"));
        lobby = new String(config.getString("lobby"));
    }

    public boolean startGameInConfigMode() {
        return startGameInConfigMode;
    }

    public HashSet<String> getGameAdmins() {
        return gameAdmins;
    }

    public ArrayList<String> getMaps() {
        return maps;
    }

    public String getLobby() { return lobby; }
}
