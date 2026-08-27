package milo.ui;

import java.util.Scanner;

/**
 * Deals with all interaction with the user: reading what they type and
 * printing what Milo says back. Nothing outside this class touches
 * {@code System.out} or {@code System.in} directly, so the console format
 * (dividers, indentation) only needs to be gotten right in one place.
 */
public class Ui {
    /** Indentation applied to every line Milo prints. */
    private static final String INDENT = "    ";

    /** Horizontal line used to separate blocks of Milo's output. */
    private static final String DIVIDER =
            INDENT + "____________________________________________________________";

    /**
     * ASCII-art banner spelling out the chatbot's name.
     * Note there is no trailing newline: showResponse supplies it.
     */
    private static final String BANNER = " __  __  _  _         \n"
            + "|  \\/  |(_)| |  ___  \n"
            + "| |\\/| || || | / _ \\ \n"
            + "| |  | || || || (_) |\n"
            + "|_|  |_||_||_| \\___/ ";

    /** Where user input is read from. */
    private final Scanner scanner = new Scanner(System.in);

    /** Prints the banner and the opening greeting. */
    public void showWelcome() {
        showResponse(BANNER, "Hello! I'm Milo.", "What can I do for you?");
    }

    /** Prints the closing message shown when the user exits. */
    public void showGoodbye() {
        showResponse("Bye. Hope to see you again soon!");
    }

    /**
     * Reports what happened while loading the saved tasks: any warnings from
     * an unreadable save file, or, if the load was clean, how many tasks were
     * carried over. Prints nothing on a clean load of an empty list, since a
     * first-time user has nothing to be told about yet.
     *
     * @param warnings  problems noticed while loading, empty if none
     * @param taskCount how many tasks were loaded
     */
    public void showLoadStatus(java.util.List<String> warnings, int taskCount) {
        if (!warnings.isEmpty()) {
            showResponse(warnings.toArray(new String[0]));
        } else if (taskCount > 0) {
            showResponse("I loaded " + taskCount + " task(s) from last time. "
                    + "Type list to see them.");
        }
    }

    /**
     * Reports an error to the user.
     *
     * @param message what went wrong, phrased for the user to read
     */
    public void showError(String message) {
        showResponse(message);
    }

    /**
     * Returns whether there is another line of input to read.
     * Checking this before {@link #readCommand()} lets the caller exit
     * gracefully when input ends without the user typing "bye", instead of
     * {@code readCommand()} throwing.
     *
     * @return true if another command can be read
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads one line of input from the user.
     *
     * @return the line, with leading and trailing whitespace removed
     */
    public String readCommand() {
        return scanner.nextLine().trim();
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
    public void showResponse(String... lines) {
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
