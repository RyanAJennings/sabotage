package us.rjennings.sabotage;

public class DeathEvent {
    private SabotagePlayer killer;
    private SabotagePlayer victim;

    public DeathEvent(SabotagePlayer _killer, SabotagePlayer _victim) {
        killer = _killer;
        victim = _victim;
    }

    public SabotagePlayer getKiller() {
        return killer;
    }

    public SabotagePlayer getVictim() {
        return victim;
    }
}
