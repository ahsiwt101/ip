import java.util.Scanner;

/**
 * Entry point of the Milo chatbot.
 * Milo greets the user, echoes back each command entered, and exits
 * when the user types {@value #EXIT_COMMAND}.
 */
public class Milo {
    /** Indentation applied to every line Milo prints. */
    private static final String INDENT = "    ";

    /** Horizontal line used to separate blocks of Milo's output. */
    private static final String DIVIDER =
            INDENT + "____________________________________________________________";

    /** The command that makes Milo exit. */
    private static final String EXIT_COMMAND = "bye";

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

        Scanner scanner = new Scanner(System.in);
        // hasNextLine() guards against the input ending without a "bye",
        // which would otherwise make nextLine() throw.
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (command.equals(EXIT_COMMAND)) {
                break;
            }
            printBlock(command);
        }

        printBlock("Bye. Hope to see you again soon!");
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
