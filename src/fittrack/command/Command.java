package fittrack.command;
/** Command Pattern – executable action contract. */
public interface Command {
    void execute();
    void undo();
    String getDescription();
}
