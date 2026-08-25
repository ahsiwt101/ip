import java.util.Scanner;

/**
 * Entry point of the Milo chatbot.
 * Milo greets the user, stores each piece of text entered, lists the stored
 * items on request, and exits when the user types {@value #EXIT_COMMAND}.
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

    /** Largest number of tasks Milo can hold, per the Level-2 assumption. */
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

        // A fixed-size array plus a running count is enough while the task
        // limit is known: taskCount is both the number stored and the index
        // that the next task goes into.
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        // hasNextLine() guards against the input ending without a "bye",
        // which would otherwise make nextLine() throw.
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.equals(EXIT_COMMAND)) {
                break;
            } else if (command.equals(LIST_COMMAND)) {
                printBlock(formatTasks(tasks, taskCount));
            } else if (taskCount < MAX_TASKS) {
                tasks[taskCount] = new Task(command);
                taskCount++;
                printBlock("added: " + command);
            } else {
                printBlock("Sorry, I can only remember " + MAX_TASKS + " tasks.");
            }
        }

        printBlock("Bye. Hope to see you again soon!");
    }

    /**
     * Formats the stored tasks as numbered lines, ready to be displayed.
     *
     * @param tasks     the array holding the stored tasks
     * @param taskCount how many entries of the array are actually in use
     * @return one numbered line per task, or a single explanatory line if
     *         nothing has been stored yet
     */
    private static String[] formatTasks(Task[] tasks, int taskCount) {
        if (taskCount == 0) {
            return new String[] {"There is nothing in your list yet."};
        }

        String[] lines = new String[taskCount];
        for (int i = 0; i < taskCount; i++) {
            // Tasks are numbered from 1 for the user, but indexed from 0.
            lines[i] = (i + 1) + ". " + tasks[i].getDescription();
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
