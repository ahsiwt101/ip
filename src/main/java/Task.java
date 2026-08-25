/**
 * Represents a single task that Milo keeps track of.
 * A task consists of a description and a flag recording whether it is done.
 */
public class Task {
    /** What the user wants to do. */
    protected String description;

    /** Whether this task has been completed. */
    protected boolean isDone;

    /**
     * Creates a task, which always starts out not done.
     *
     * @param description what the task is about
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the icon representing this task's completion status.
     *
     * @return "X" if the task is done, a single space otherwise
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /** Marks this task as done. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not done yet. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns this task's description.
     *
     * @return the description supplied when the task was created
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the task rendered with its status box in front of the
     * description, for example {@code [X] read book}.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
