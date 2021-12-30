package us.rjennings.sabotage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class Sabotage extends JavaPlugin implements Listener {

    private Configuration config;
    private PlayerHandler playerHandler;
    private MapHandler mapHandler;
    private CommandDispatcher commandDispatcher;

    private BukkitTask ballotBroadcastTask;

    enum Mode {
        CONFIG,
        LOBBY,
        GRACE_PERIOD,
        GAME,
        ENDGAME
    }
    private Mode mode;

    public Mode getMode() {
        return mode;
    }

    public void gameOver() {
        mode = Mode.ENDGAME;
        playerHandler.distributeEndGameKarma();
        endGameTimer();
        mapHandler.reset();
        playerHandler.reset();
        mode = Mode.LOBBY;
        startGameTimer();
    }

    private void endGameTimer() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            int seconds = 15;
            @Override
            public void run(){
                if (seconds == 15 || seconds <= 5) {
                    Bukkit.broadcastMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "[!] " + ChatColor.GRAY +
                            "The game has ended. You will be teleported back to the lobby in " + seconds + " seconds.");
                }
                if (seconds == 0) {
                    initializeNewGame();
                }
                seconds--;
            }
        }, 300 /* 300 ticks = 15 seconds */);
    }

    public void startGameTimer() {
        // TODO: If enough players leave in the middle of countdown, cancel game start and wait for more to join
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            int seconds = 15;
            @Override
            public void run() {
                if (seconds == 15 || (seconds <= 5 && seconds > 0)) {
                    Bukkit.broadcastMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "[!] " + ChatColor.GRAY +
                            "Sabotage will begin in " + seconds + " seconds.");
                }
                if (seconds == 0) {
                    startGame();
                }
                seconds--;
            }
        }, 300 /* 300 ticks = 15 seconds */);
    }

    public void startGracePeriodTimer() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            int seconds = 30;
            @Override
            public void run() {
                if (seconds == 15 || (seconds <= 5 && seconds > 0)) {
                    Bukkit.broadcastMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                            "The grace period will expire in " + seconds + " seconds."));
                }
                else if (seconds == 0) {
                    startGracePeriod();
                }
                seconds--;
            }
        }, 20 * 30);
    }

    private void startGracePeriod() {
        mode = Mode.GRACE_PERIOD;
        Bukkit.getOnlinePlayers().forEach(player -> {
            // TODO: Spawn players to random locations on the map?
            player.teleport(mapHandler.getGameMap().getSpawnLocation());
        });
        // TODO: Immobilize players and make a grace period start countdown?
        startGracePeriodTimer();
    }

    private void startGame() {
        playerHandler.assignRoles();
        mode = Mode.GAME;
    }

    private void startBallotBroadcastTask() {
        ballotBroadcastTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (mode != Mode.LOBBY) {
                    cancel();
                }
                else {
                    Bukkit.broadcastMessage(mapHandler.getBallot());
                }
            }
        }.runTaskTimer(this, 0, 20 * 15);
    }

    @Override
    public void onEnable() {
        // Read the config file once on enabling the plugin
        config  = new Configuration(this.getConfig());
        if (config.startGameInConfigMode()) {
            mode = Mode.CONFIG;
        }
        playerHandler = new PlayerHandler(this, config.getGameAdmins());
        getServer().getPluginManager().registerEvents(playerHandler, this);
        mapHandler = new MapHandler(this, config.getLobby(), config.getMaps());
        commandDispatcher = new CommandDispatcher(this);
        startBallotBroadcastTask();
    }

    @Override
    public void onLoad() {
        Bukkit.getLogger().info("Loaded " + this.getName());
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Disabled " + this.getName());
    }

    private void initializeNewGame() {
        mapHandler.reset();
        playerHandler.reset();
        // TODO: do we need to re-register events to this playerhandler?
    }

    public MapHandler getMapHandler() {
        return mapHandler;
    }
}
