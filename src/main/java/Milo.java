import java.util.Scanner;

/**
 * Entry point of the Milo chatbot.
 * Milo greets the user, records todos, deadlines and events, lists them on
 * request, and exits when the user types {@value #EXIT_COMMAND}.
 */
public class Milo {
    /** Indentation applied to every line Milo prints. */
    private static final String INDENT = "    ";

    /** Horizontal line used to separate blocks of Milo's output. */
    private static final String DIVIDER =
            INDENT + "____________________________________________________________";

    /** The command that makes Milo exit. */
    private static final String EXIT_COMMAND = "bye";

    /** The command that makes Milo display the stored tasks. */
    private static final String LIST_COMMAND = "list";

    /** The command that marks a task as done. */
    private static final String MARK_COMMAND = "mark";

    /** The command that marks a task as not done yet. */
    private static final String UNMARK_COMMAND = "unmark";

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

    /** Largest number of tasks Milo can hold. */
    private static final int MAX_TASKS = 100;

    /**
     * ASCII-art banner spelling out the chatbot's name.
     * Note there is no trailing newline: the printing helper supplies it.
     */
    private static final String BANNER = " __  __  _  _         \n"
            + "|  \\/  |(_)| |  ___  \n"
            + "| |\\/| || || | / _ \\ \n"
            + "| |  | || || || (_) |\n"
            + "|_|  |_||_||_| \\___/ ";

    public static void main(String[] args) {
        printBlock(BANNER, "Hello! I'm Milo.", "What can I do for you?");

        // Polymorphism at work: the array holds Task references, but each slot
        // may point at a Todo, a Deadline or an Event. Calling toString() on a
        // slot runs whichever subclass version belongs to the actual object.
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        // hasNextLine() guards against the input ending without a "bye",
        // which would otherwise make nextLine() throw.
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
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
            } else if (keyword.equals(LIST_COMMAND)) {
                printBlock(formatTasks(tasks, taskCount));
            } else if (keyword.equals(MARK_COMMAND) || keyword.equals(UNMARK_COMMAND)) {
                boolean shouldMarkDone = keyword.equals(MARK_COMMAND);
                printBlock(setDone(tasks, taskCount, arguments, shouldMarkDone));
            } else if (taskCount >= MAX_TASKS) {
                printBlock("Sorry, I can only remember " + MAX_TASKS + " tasks.");
            } else {
                Task task = createTask(keyword, arguments);
                if (task == null) {
                    printBlock("Sorry, I don't understand that. Try: todo, deadline, "
                            + "event, list or bye.");
                } else {
                    tasks[taskCount] = task;
                    taskCount++;
                    printBlock(formatAdded(task, taskCount));
                }
            }
        }

        printBlock("Bye. Hope to see you again soon!");
    }

    /**
     * Builds the task described by a command, choosing the subclass that
     * matches the command word.
     *
     * @param keyword   the first word of the user's input
     * @param arguments everything after that first word
     * @return the new task, or null if the command is unknown or its
     *         arguments are incomplete
     */
    private static Task createTask(String keyword, String arguments) {
        if (arguments.isEmpty()) {
            return null;
        }

        switch (keyword) {
        case TODO_COMMAND:
            return new Todo(arguments);

        case DEADLINE_COMMAND: {
            int byIndex = arguments.indexOf(BY_MARKER);
            if (byIndex < 0) {
                return null;
            }
            String description = arguments.substring(0, byIndex).trim();
            String by = arguments.substring(byIndex + BY_MARKER.length()).trim();
            if (description.isEmpty() || by.isEmpty()) {
                return null;
            }
            return new Deadline(description, by);
        }

        case EVENT_COMMAND: {
            int fromIndex = arguments.indexOf(FROM_MARKER);
            if (fromIndex < 0) {
                return null;
            }
            // Look for /to only after /from, so the two markers can't be
            // picked up out of order.
            int toIndex = arguments.indexOf(TO_MARKER, fromIndex + FROM_MARKER.length());
            if (toIndex < 0) {
                return null;
            }
            String description = arguments.substring(0, fromIndex).trim();
            String from = arguments.substring(fromIndex + FROM_MARKER.length(), toIndex).trim();
            String to = arguments.substring(toIndex + TO_MARKER.length()).trim();
            if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                return null;
            }
            return new Event(description, from, to);
        }

        default:
            return null;
        }
    }

    /**
     * Marks the task named by the user as done or not done.
     *
     * @param tasks          the array holding the stored tasks
     * @param taskCount      how many entries of the array are actually in use
     * @param arguments      the task number the user typed, as text
     * @param shouldMarkDone true to mark the task done, false to undo that
     * @return the lines of the block to display, whether a confirmation or an
     *         explanation of what went wrong
     */
    private static String[] setDone(Task[] tasks, int taskCount, String arguments,
            boolean shouldMarkDone) {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(arguments.trim());
        } catch (NumberFormatException e) {
            // Covers a missing number as well as a non-numeric one.
            return new String[] {"Please tell me which task number, for example: mark 2"};
        }

        // The user counts from 1, so 1 maps to index 0.
        if (taskNumber < 1 || taskNumber > taskCount) {
            return new String[] {"There is no task " + taskNumber + " in your list."};
        }

        Task task = tasks[taskNumber - 1];
        if (shouldMarkDone) {
            task.markAsDone();
            return new String[] {"Nice! I've marked this task as done:", "  " + task};
        } else {
            task.markAsNotDone();
            return new String[] {"OK, I've marked this task as not done yet:", "  " + task};
        }
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

    /**
     * Formats the stored tasks as numbered lines, ready to be displayed.
     *
     * @param tasks     the array holding the stored tasks
     * @param taskCount how many entries of the array are actually in use
     * @return the lines of the list block, or a single explanatory line if
     *         nothing has been stored yet
     */
    private static String[] formatTasks(Task[] tasks, int taskCount) {
        if (taskCount == 0) {
            return new String[] {"There is nothing in your list yet."};
        }

        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < taskCount; i++) {
            // Tasks are numbered from 1 for the user, but indexed from 0.
            lines[i + 1] = (i + 1) + "." + tasks[i];
        }
        return lines;
    }

    /**
     * Prints the given lines as one block: framed by divider lines above and
     * below, indented to match Milo's output format, and followed by a blank
     * line that separates it from whatever the user types next.
     * A line that itself contains newlines (such as the banner) is split so
     * that every physical line receives the same indentation.
     *
     * @param lines the lines of text to display inside the block
     */
    private static void printBlock(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            for (String physicalLine : line.split("\n")) {
                System.out.println(INDENT + " " + physicalLine);
            }
        }
        System.out.println(DIVIDER);
        System.out.println();
    }
}
