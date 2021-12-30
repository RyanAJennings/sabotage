package us.rjennings.sabotage;

public class Karma {
    // TODO: Make these configurable?
    private static final int SAB_KILL_INNO = 20;
    private static final int SAB_KILL_DET = 40;
    private static final int SAB_KILL_SAB = -75;

    private static final int INNO_KILL_SAB = 40;
    private static final int INNO_KILL_INNO = -30;
    private static final int INNO_KILL_DET = -75;

    private static final int DET_KILL_SAB = 50;
    private static final int DET_KILL_INNO = -50;

    private static final int GAME_OVER_SAB = 30;
    private static final int GAME_OVER_INNO = 40;
    private static final int GAME_OVER_DET = 50;

    public static void adjustKarmaGameOver(SabotagePlayer player) {
        if (!player.isAlive()) return; // No karma given if player is dead at endgame
        if (player.getRole() == Role.SABOTEUR) {
            player.addKarma(GAME_OVER_SAB);
        }
        else if (player.getRole() == Role.INNOCENT) {
            player.addKarma(GAME_OVER_INNO);
        }
        else if (player.getRole() == Role.DETECTIVE) {
            player.addKarma(GAME_OVER_DET);
        }
    }

    public static void adjustKarma(DeathEvent deathEvent) {
        if (deathEvent.getKiller().getUuid().equals(deathEvent.getVictim().getUuid())) return;
        int karmaToAdd = getKarmaToAdd(deathEvent);
        deathEvent.getKiller().addKarma(karmaToAdd);
        // TODO: Sabs lose karma for dying?
    }

    private static int getKarmaToAdd(DeathEvent deathEvent) {
        Role killerRole = deathEvent.getKiller().getRole();
        Role victimRole = deathEvent.getVictim().getRole();

        if (killerRole == Role.SABOTEUR) {
            if (victimRole == Role.SABOTEUR) {
                return SAB_KILL_SAB;
            }
            else if (victimRole == Role.INNOCENT) {
                return SAB_KILL_INNO;
            }
            else if (victimRole ==  Role.DETECTIVE) {
                return SAB_KILL_DET;
            }
        }
        else if (killerRole == Role.INNOCENT) {
            if (victimRole == Role.SABOTEUR) {
                return INNO_KILL_SAB;
            }
            else if (victimRole == Role.INNOCENT) {
                return INNO_KILL_INNO;
            }
            else if (victimRole ==  Role.DETECTIVE) {
                return INNO_KILL_DET;
            }
        }
        else if (killerRole == Role.DETECTIVE) {
            if (victimRole == Role.SABOTEUR) {
                return DET_KILL_SAB;
            }
            else if (victimRole == Role.INNOCENT) {
                return DET_KILL_INNO;
            }
        }
        return 0;
    }
}
