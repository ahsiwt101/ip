import java.util.ArrayList;

/**
 * Entry point of the Milo chatbot.
 * Milo greets the user, records todos, deadlines and events, marks them done,
 * deletes them, lists them on request, and exits when the user types
 * {@value #EXIT_COMMAND}.
 * Bad input is reported as a {@link MiloException} and explained to the user
 * rather than being allowed to crash the program.
 */
public class Milo {
    /** The command that makes Milo exit. */
    private static final String EXIT_COMMAND = "bye";

    /** The command that makes Milo display the stored tasks. */
    private static final String LIST_COMMAND = "list";

    /** The command that marks a task as done. */
    private static final String MARK_COMMAND = "mark";

    /** The command that marks a task as not done yet. */
    private static final String UNMARK_COMMAND = "unmark";

    /** The command that removes a task from the list. */
    private static final String DELETE_COMMAND = "delete";

    /** The command that adds a task with no date attached. */
    private static final String TODO_COMMAND = "todo";

    /** The command that adds a task due by a given date. */
    private static final String DEADLINE_COMMAND = "deadline";

    /** The command that adds a task spanning a start and an end date. */
    private static final String EVENT_COMMAND = "event";

    /** Separates a deadline's description from its due date. */
    private static final String BY_MARKER = "/by";

    /** Separates an event's description from its start date. */
    private static final String FROM_MARKER = "/from";

    /** Separates an event's start date from its end date. */
    private static final String TO_MARKER = "/to";

    /** Folder holding the save file, relative to where Milo is run. */
    private static final String DATA_DIRECTORY = "data";

    /** Name of the save file inside {@value #DATA_DIRECTORY}. */
    private static final String DATA_FILE = "milo.txt";

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        Storage storage = new Storage(DATA_DIRECTORY, DATA_FILE);
        TaskList tasks = new TaskList(storage.load());
        ui.showLoadStatus(storage.getLoadWarnings(), tasks.size());

        // hasNextCommand() guards against the input ending without a "bye",
        // which would otherwise make readCommand() throw.
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            if (command.isEmpty()) {
                continue;
            }

            // Split once, so the first word is the command and the remainder
            // (which may itself contain spaces) is that command's arguments.
            String[] parts = command.split("\\s+", 2);
            String keyword = parts[0];
            String arguments = (parts.length > 1) ? parts[1].trim() : "";

            if (keyword.equals(EXIT_COMMAND)) {
                break;
            }

            // Every command below reports bad input by throwing a
            // MiloException, so all the error reporting happens in one place
            // instead of being threaded back through return values.
            try {
                // Only the commands that change the list need saving, so
                // "list" does not rewrite the file for nothing.
                boolean hasListChanged = false;

                if (keyword.equals(LIST_COMMAND)) {
                    ui.showResponse(tasks.getDisplayLines());
                } else if (keyword.equals(MARK_COMMAND) || keyword.equals(UNMARK_COMMAND)) {
                    boolean shouldMarkDone = keyword.equals(MARK_COMMAND);
                    ui.showResponse(setDone(tasks, keyword, arguments, shouldMarkDone));
                    hasListChanged = true;
                } else if (keyword.equals(DELETE_COMMAND)) {
                    ui.showResponse(deleteTask(tasks, keyword, arguments));
                    hasListChanged = true;
                } else {
                    Task task = createTask(keyword, arguments);
                    tasks.add(task);
                    ui.showResponse(formatAdded(task, tasks.size()));
                    hasListChanged = true;
                }

                if (hasListChanged) {
                    storage.save(tasks.asArrayList());
                }
            } catch (MiloException e) {
                ui.showError(e.getMessage());
            }
        }

        ui.showGoodbye();
    }

    /**
     * Finds a marker such as {@value #BY_MARKER}, but only where it stands as
     * a word of its own rather than as part of a longer one.
     * A plain {@code indexOf} would split "finish essay/byline /by Sunday" at
     * the "/by" buried inside "essay/byline"; requiring whitespace (or the
     * end of the text) on both sides makes only the real marker count.
     *
     * @param arguments the text to search
     * @param marker    the marker to look for
     * @param fromIndex where in the text to start searching
     * @return the position of the marker, or -1 if it does not appear as a
     *         word of its own at or after fromIndex
     */
    private static int indexOfMarker(String arguments, String marker, int fromIndex) {
        int index = arguments.indexOf(marker, fromIndex);
        while (index >= 0) {
            int afterMarker = index + marker.length();
            boolean startsWord = (index == 0)
                    || Character.isWhitespace(arguments.charAt(index - 1));
            boolean endsWord = (afterMarker == arguments.length())
                    || Character.isWhitespace(arguments.charAt(afterMarker));
            if (startsWord && endsWord) {
                return index;
            }
            // This occurrence was part of a longer word; keep looking.
            index = arguments.indexOf(marker, index + 1);
        }
        return -1;
    }

    /**
     * Builds the task described by a command, choosing the subclass that
     * matches the command word.
     *
     * @param keyword   the first word of the user's input
     * @param arguments everything after that first word
     * @return the new task
     * @throws MiloException if the command is unknown, or its arguments are
     *                       missing or incomplete
     */
    private static Task createTask(String keyword, String arguments) throws MiloException {
        switch (keyword) {
        case TODO_COMMAND:
            if (arguments.isEmpty()) {
                throw new MiloException("A todo needs a description. "
                        + "Try: todo borrow book");
            }
            return new Todo(arguments);

        case DEADLINE_COMMAND: {
            int byIndex = indexOfMarker(arguments, BY_MARKER, 0);
            if (byIndex < 0) {
                throw new MiloException("I need to know when that is due. "
                        + "Add " + BY_MARKER + ", like: deadline return book "
                        + BY_MARKER + " Sunday");
            }
            String description = arguments.substring(0, byIndex).trim();
            String by = arguments.substring(byIndex + BY_MARKER.length()).trim();
            if (description.isEmpty()) {
                throw new MiloException("A deadline needs a description before "
                        + BY_MARKER + ". Try: deadline return book "
                        + BY_MARKER + " Sunday");
            }
            if (by.isEmpty()) {
                throw new MiloException("Tell me what comes after " + BY_MARKER
                        + ", like: deadline return book " + BY_MARKER + " Sunday");
            }
            return new Deadline(description, by);
        }

        case EVENT_COMMAND: {
            int fromIndex = indexOfMarker(arguments, FROM_MARKER, 0);
            if (fromIndex < 0) {
                throw new MiloException("I need to know when that event starts. "
                        + "Add " + FROM_MARKER + ", like: event project meeting "
                        + FROM_MARKER + " Mon 2pm " + TO_MARKER + " 4pm");
            }
            // Look for /to only after /from, so the two markers cannot be
            // picked up out of order.
            int toIndex = indexOfMarker(arguments, TO_MARKER, fromIndex + FROM_MARKER.length());
            if (toIndex < 0) {
                throw new MiloException("I need to know when that event ends. "
                        + "Add " + TO_MARKER + " after " + FROM_MARKER
                        + ", like: event project meeting " + FROM_MARKER
                        + " Mon 2pm " + TO_MARKER + " 4pm");
            }
            String description = arguments.substring(0, fromIndex).trim();
            String from = arguments.substring(fromIndex + FROM_MARKER.length(), toIndex).trim();
            String to = arguments.substring(toIndex + TO_MARKER.length()).trim();
            if (description.isEmpty()) {
                throw new MiloException("An event needs a description before "
                        + FROM_MARKER + ". Try: event project meeting "
                        + FROM_MARKER + " Mon 2pm " + TO_MARKER + " 4pm");
            }
            if (from.isEmpty() || to.isEmpty()) {
                throw new MiloException("An event needs both a start and an end. "
                        + "Try: event project meeting " + FROM_MARKER + " Mon 2pm "
                        + TO_MARKER + " 4pm");
            }
            return new Event(description, from, to);
        }

        default:
            throw new MiloException("I don't know what \"" + keyword + "\" means. "
                    + "I understand: todo, deadline, event, list, mark, unmark, "
                    + "delete and bye.");
        }
    }

    /**
     * Works out which task the user meant by the number they typed.
     * Shared by every command that acts on an existing task, so they all
     * validate the number the same way and report problems identically.
     *
     * @param tasks     the stored tasks
     * @param keyword   the command word used, quoted back in any error
     * @param arguments the task number the user typed, as text
     * @return the position of that task in the list, counting from 0
     * @throws MiloException if the list is empty, or no valid number was given
     */
    private static int parseTaskIndex(TaskList tasks, String keyword, String arguments)
            throws MiloException {
        if (tasks.isEmpty()) {
            throw new MiloException("There is nothing in your list to " + keyword + " yet. "
                    + "Add a task first, like: todo borrow book");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(arguments.trim());
        } catch (NumberFormatException e) {
            // Covers a missing number as well as a non-numeric one. The
            // original exception is not useful to the user, so it is replaced
            // with an explanation phrased in terms of the command they typed.
            throw new MiloException("Tell me which task number to " + keyword
                    + ", like: " + keyword + " 2");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new MiloException("There is no task " + taskNumber + " in your list. "
                    + "Pick a number between 1 and " + tasks.size() + ".");
        }

        // The user counts from 1, so 1 maps to index 0.
        return taskNumber - 1;
    }

    /**
     * Marks the task named by the user as done or not done.
     *
     * @param tasks          the stored tasks
     * @param keyword        the command word used, quoted back in any error
     * @param arguments      the task number the user typed, as text
     * @param shouldMarkDone true to mark the task done, false to undo that
     * @return the lines of the confirmation block
     * @throws MiloException if no valid task number was given
     */
    private static String[] setDone(TaskList tasks, String keyword, String arguments,
            boolean shouldMarkDone) throws MiloException {
        Task task = tasks.get(parseTaskIndex(tasks, keyword, arguments));

        if (shouldMarkDone) {
            task.markAsDone();
            return new String[] {"Nice! I've marked this task as done:", "  " + task};
        } else {
            task.markAsNotDone();
            return new String[] {"OK, I've marked this task as not done yet:", "  " + task};
        }
    }

    /**
     * Removes the task named by the user from the list.
     * Tasks after it shift up, so the numbers shown by "list" stay contiguous.
     *
     * @param tasks     the stored tasks
     * @param keyword   the command word used, quoted back in any error
     * @param arguments the task number the user typed, as text
     * @return the lines of the confirmation block
     * @throws MiloException if no valid task number was given
     */
    private static String[] deleteTask(TaskList tasks, String keyword, String arguments)
            throws MiloException {
        // delete() hands back the task it took out, so it can be shown to the
        // user without having to fetch it separately beforehand.
        Task removed = tasks.delete(parseTaskIndex(tasks, keyword, arguments));

        return new String[] {
            "Noted. I've removed this task:",
            "  " + removed,
            "Now you have " + tasks.size() + " tasks in the list."
        };
    }

    /**
     * Formats the confirmation shown after a task is added.
     *
     * @param task      the task that was just added
     * @param taskCount how many tasks are now stored
     * @return the lines of the confirmation block
     */
    private static String[] formatAdded(Task task, int taskCount) {
        return new String[] {
            "Got it. I've added this task:",
            "  " + task,
            "Now you have " + taskCount + " tasks in the list."
        };
    }
}
