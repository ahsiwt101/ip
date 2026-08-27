/** Represents the "list" command, which displays the stored tasks. */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showResponse(tasks.getDisplayLines());
    }
}
