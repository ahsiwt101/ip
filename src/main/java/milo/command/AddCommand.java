package milo.command;

import milo.exception.MiloException;
import milo.storage.Storage;
import milo.task.Task;
import milo.task.TaskList;
import milo.ui.Ui;

/**
 * Represents a command that adds an already-built task to the list, such as
 * "todo borrow book" or "deadline return book /by 2019-10-15". The task
 * itself is built by {@link Parser}, since deciding what kind of task it is
 * and reading its fields is about interpreting the command text, not about
 * the list it ends up in.
 */
public class AddCommand extends Command {
    /** The task to add. */
    private final Task task;

    /**
     * Creates a command that will add the given task.
     *
     * @param task the task to add
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * Returns the task this command will add.
     * Lets a caller (notably a test verifying what {@link Parser} built)
     * inspect the task without triggering {@link #execute}'s side effects.
     *
     * @return the task this command carries
     */
    public Task getTask() {
        return task;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws MiloException {
        tasks.add(task);
        storage.save(tasks.asArrayList());
        ui.showResponse(
                "Got it. I've added this task:",
                "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }
}
