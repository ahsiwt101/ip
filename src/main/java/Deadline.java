/**
 * Represents a task that must be done before a specific date or time,
 * for example "submit report by 11/10/2019 5pm".
 */
public class Deadline extends Task {
    /** When the task is due. Stored as free text, not parsed as a date. */
    protected String by;

    /**
     * Creates a deadline.
     *
     * @param description what the task is about
     * @param by          when the task is due
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline tagged with its type and due date, for example
     * {@code [D][ ] return book (by: Sunday)}.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }

    /**
     * Returns this task as one line of the save file.
     */
    @Override
    public String toFileFormat() {
        return "D" + FILE_SEPARATOR + super.toFileFormat() + FILE_SEPARATOR + by;
    }
}
