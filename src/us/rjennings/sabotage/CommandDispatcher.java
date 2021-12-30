package us.rjennings.sabotage;

public class CommandDispatcher {

    private Sabotage gameInstance;

    public CommandDispatcher(Sabotage _gameInstance) {
        gameInstance = _gameInstance;
        setExecutors();
    }

    private void setExecutors() {
        // TODO: /karma, /role, /shop, ..
        gameInstance.getCommand("vote").setExecutor(new CommandVote(gameInstance));
    }
}
