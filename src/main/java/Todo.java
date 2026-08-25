/**
 * Represents a task with no date or time attached to it,
 * for example "visit new theme park".
 */
public class Todo extends Task {
    /**
     * Creates a todo.
     *
     * @param description what the task is about
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns the todo tagged with its type, for example {@code [T][ ] borrow book}.
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
