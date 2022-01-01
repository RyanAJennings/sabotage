package us.rjennings.sabotage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PlayerHandler implements Listener {

    private static int MIN_PLAYERS_TO_START = 2;

    private Sabotage gameInstance;

    private ArrayList<SabotagePlayer> saboteurs;
    private HashMap<UUID, SabotagePlayer> players;
    private HashSet<String> admins;

    private int numLivingSabs = 0;
    private int numLivingInnocents = 0;

    private String playerRecordsPath;

    public PlayerHandler(Sabotage _gameInstance, HashSet<String> _admins) {
        gameInstance = _gameInstance;
        admins = _admins;
        saboteurs = new ArrayList<>();
        players = new HashMap<>();

        numLivingInnocents = 0;
        numLivingSabs = 0;

        playerRecordsPath = gameInstance.getDataFolder().getAbsolutePath() + "/PlayerRecords";
        try {
            Files.createDirectories(Paths.get(playerRecordsPath));
        }
        catch (FileAlreadyExistsException e) {
            Bukkit.getLogger().info("PlayerRecords directory found: " + playerRecordsPath);
        }
        catch (Exception e) {
            Bukkit.getLogger().severe("Failed to create PlayerRecords directory. Player stats will not persist.");
            e.printStackTrace();
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            boolean playerIsAdmin = admins.contains(player.getDisplayName());
            SabotagePlayer sabPlayer = new SabotagePlayer(playerRecordsPath, player, playerIsAdmin);
            players.put(player.getUniqueId(), sabPlayer);

            player.teleport(gameInstance.getMapHandler().getLobby().getSpawnLocation());
        });

        if (players.size() >= MIN_PLAYERS_TO_START) {
            gameInstance.startGameTimer();
        }
    }

    public void reset() {
        numLivingInnocents = 0;
        numLivingSabs = 0;
        teleportAllPlayers(gameInstance.getMapHandler().getLobby());
    }

    public void teleportAllPlayers(World world) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.teleport(world.getSpawnLocation());
        });
    }

    private String getSaboteurListExcept(String toExclude) {
        StringBuilder sabList = new StringBuilder();
        for (int i = 0; i < saboteurs.size(); i++) {
            SabotagePlayer saboteur = saboteurs.get(i);
            if (saboteur.isAlive()  && !saboteur.getName().equals(toExclude)) {
                sabList.append(ChatColor.RED).append(saboteur.getName());
            }
            else if (!saboteur.getName().equals(toExclude)) {
                sabList.append(ChatColor.GRAY).append(saboteur.getName());
            }

            if (i < saboteurs.size() - 1 && !saboteur.getName().equals(toExclude)) {
                sabList.append(", ");
            }
        }
        return sabList.toString();
    }

    public void assignRoles() {
        ArrayList<SabotagePlayer> playersList = new ArrayList<>(players.values());
        Collections.shuffle(playersList);

        playersList.get(0).setRole(Role.DETECTIVE);
        playersList.get(0).sendRole();
        numLivingInnocents = 1;

        int numSabs = getNumSabsToAssign(playersList.size());
        for (int i = 1; i < numSabs; i++) {
            SabotagePlayer player = playersList.get(i);
            player.setRole(Role.SABOTEUR);
            player.sendRole();
            saboteurs.add(player);
            numLivingSabs++;
        }
        for (SabotagePlayer saboteur : saboteurs)  {
            saboteur.sendMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    "Your fellow SABOTEURS are: " + getSaboteurListExcept(saboteur.getName())));
        }

        for (int i = numSabs + 1; i < playersList.size(); i++) {
            playersList.get(i).setRole(Role.INNOCENT);
            playersList.get(i).sendRole();
            numLivingInnocents++;
        }
    }

    // TODO: allow sab proportion to be configurable?
    private int getNumSabsToAssign(int totalPlayers) {
        if (totalPlayers <= 3) return 1;
        return totalPlayers / 4;
    }

    private boolean isGameOver() {
        return (numLivingSabs == 0 || numLivingInnocents == 0);
    }

    public void distributeEndGameKarma() {
        for (SabotagePlayer player : players.values()) {
            Karma.adjustKarmaGameOver(player);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /// PLAYER EVENT HANDLERS

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "[+] " +
                player.getDisplayName() + ChatColor.GRAY.toString() + " joined the game.");

        String playerRecordsPath = gameInstance.getDataFolder().getAbsolutePath() + "/PlayerRecords";
        SabotagePlayer sabPlayer = new SabotagePlayer(playerRecordsPath, player, admins.contains(player.getDisplayName()));
        players.put(player.getUniqueId(), sabPlayer);

        if (gameInstance.getMode() == Sabotage.Mode.LOBBY) {
            player.sendMessage("Welcome to Sabotage!"); // TODO: format
            player.sendMessage(gameInstance.getMapHandler().getBallot());
            if (players.size() >= MIN_PLAYERS_TO_START) {
                gameInstance.startGameTimer();
            }
        } else if (gameInstance.getMode() == Sabotage.Mode.GAME || gameInstance.getMode() == Sabotage.Mode.ENDGAME) {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("The round has already begun. You are spectating."); // TODO: format
        }
        // TODO: instructions for loading new maps
        else if (gameInstance.getMode() == Sabotage.Mode.CONFIG) {
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage("Sabotage is in CONFIG mode."); // TODO: format
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        // TODO: Handle combat-logging
        // TODO: Retract vote
        Player player = event.getPlayer();
        SabotagePlayer sabPlayer = players.get(player.getUniqueId());

        if (sabPlayer.getRole() == Role.SABOTEUR) {
            numLivingSabs--;
        }
        else if (sabPlayer.getRole() == Role.INNOCENT || sabPlayer.getRole() == Role.DETECTIVE) {
            numLivingInnocents--;
        }

        sabPlayer.leave();
        event.setQuitMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "[-] " +
                player.getDisplayName() + ChatColor.GRAY.toString() + " left the game.");
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(false);
        event.setKeepLevel(true);
        event.setDeathMessage(null);

        SabotagePlayer victim = players.get(event.getEntity().getPlayer().getUniqueId());
        SabotagePlayer killer = null;
        if (event.getEntity().getKiller() != null) {
            killer = players.get(event.getEntity().getKiller().getUniqueId());
            if (!killer.isAlive()) {
                Bukkit.getLogger().severe(killer.getName() + " killed " + victim.getName() + " but was marked dead.");
            }
        }

        DeathEvent deathEvent = new DeathEvent(killer, victim);
        if (gameInstance.getMode() != Sabotage.Mode.GAME) {
            Bukkit.getLogger().warning(victim.getName() + " died while Sabotage was in " +
                    gameInstance.getMode() + " mode.");
            return;
        }
        if (!victim.isAlive()) {
            Bukkit.getLogger().severe(victim.getName() + " died but was already marked dead.");
            return;
        }

        if (victim.getRole() == Role.SABOTEUR) {
            numLivingSabs--;
        } else if (victim.getRole() == Role.DETECTIVE) {
            numLivingInnocents--;
            Bukkit.broadcastMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    ChatColor.GRAY + "The " + ChatColor.BLUE + "DETECTIVE " + ChatColor.GRAY + "has died!"));
        } else if (victim.getRole() == Role.INNOCENT) {
            numLivingInnocents--;
        } else {
            Bukkit.getLogger().warning(victim.getName() + " died with an unexpected Role: " + victim.getRole());
            return;
        }
        Karma.adjustKarma(deathEvent);

        victim.die();
        // TODO: Implement blood & magnifying glass
        // killer.applyBlood();

        if (isGameOver()) {
            gameInstance.gameOver();
        }
        else {
            int numRemaining = numLivingInnocents + numLivingSabs;
            event.setDeathMessage(MessageFormatter.formatMessage(MessageFormatter.Format.INFO,
                    "A player died..." + numRemaining + " players remain."));
        }
    }

    @EventHandler
    private void onPlayerChat(AsyncPlayerChatEvent event) {
        SabotagePlayer sender = players.get(event.getPlayer().getUniqueId());
        Role senderRole = sender.getRole();
        String message = event.getMessage();

        if (gameInstance.getMode() == Sabotage.Mode.LOBBY || gameInstance.getMode() == Sabotage.Mode.CONFIG) {
            if (sender.isAdmin()) {
                event.setFormat(MessageFormatter.getFormatPlayerChat(ChatColor.DARK_PURPLE.toString()));
            }
            event.setFormat(MessageFormatter.getFormatPlayerChat(ChatColor.GRAY.toString()));
            return;
        }

        // Display roles for everyone in chat
        if (gameInstance.getMode() == Sabotage.Mode.ENDGAME) {
            if (senderRole == Role.SABOTEUR) {
                event.setFormat(MessageFormatter.getFormatPlayerChat(ChatColor.RED.toString()));
            }
            else if (senderRole == Role.DETECTIVE) {
                event.setFormat(MessageFormatter.getFormatPlayerChat(ChatColor.BLUE.toString()));
            }
            else if (senderRole == Role.INNOCENT){
                event.setFormat(MessageFormatter.getFormatPlayerChat(ChatColor.GREEN.toString()));
            }
            else {
                event.setFormat(MessageFormatter.getFormatPlayerChat(ChatColor.GRAY.toString()));
            }
            return;
        }

        if (gameInstance.getMode() == Sabotage.Mode.GAME) {
            event.setCancelled(true);

            for (Player player : Bukkit.getOnlinePlayers()) {
                //  TODO: sender.sendMessage(recipient, message);
                SabotagePlayer recipient = players.get(player.getUniqueId());
                String format;

                if (!sender.isAlive() && !recipient.isAlive()) {
                    // Dead chat
                    if (!recipient.isAlive()) {
                        format = MessageFormatter.getFormatPlayerChat(ChatColor.GRAY.toString());
                        player.sendMessage(String.format(format, ChatColor.ITALIC + "[DEAD] " +
                                ChatColor.GRAY + sender.getName(), message));
                    }
                    // Hide dead player chat from living
                    continue;
                }

                Role recipientRole = recipient.getRole();
                if (recipientRole == Role.SABOTEUR && senderRole == Role.SABOTEUR) {
                    format = MessageFormatter.getFormatPlayerChat(ChatColor.RED.toString());
                } else if (recipientRole == Role.SABOTEUR && senderRole == Role.INNOCENT) {
                    format = MessageFormatter.getFormatPlayerChat(ChatColor.GREEN.toString());
                } else if (senderRole == Role.DETECTIVE) {
                    format = MessageFormatter.getFormatPlayerChat(ChatColor.BLUE.toString());
                } else {
                    format = MessageFormatter.getFormatPlayerChat(ChatColor.GRAY.toString());
                }
                player.sendMessage(String.format(format, sender.getName(), message));
            }
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (admins.contains(player.getUniqueId().toString())) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (admins.contains(player.getUniqueId().toString())) return;
        event.setCancelled(true);
    }
}
