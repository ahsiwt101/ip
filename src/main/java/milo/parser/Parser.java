package milo.parser;

import milo.command.AddCommand;
import milo.command.Command;
import milo.command.DeleteCommand;
import milo.command.ExitCommand;
import milo.command.FindCommand;
import milo.command.ListCommand;
import milo.command.MarkCommand;
import milo.exception.MiloException;
import milo.task.Deadline;
import milo.task.Event;
import milo.task.Task;
import milo.task.Todo;

/**
 * Deals with making sense of the user's raw command text: splitting it into
 * a command word and its arguments, and turning that into the {@link Command}
 * that carries out the corresponding action.
 */
public class Parser {
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

    /** The command that searches for tasks by a keyword in their description. */
    private static final String FIND_COMMAND = "find";

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

    /**
     * Turns one line of user input into the {@link Command} it describes.
     *
     * @param fullCommand the line the user typed, already known not to be blank
     * @return the command to run
     * @throws MiloException if the command word is unknown, or its arguments
     *                       are missing or incomplete
     */
    public static Command parse(String fullCommand) throws MiloException {
        assert fullCommand != null : "parse() should never be handed null";

        // Split once, so the first word is the command and the remainder
        // (which may itself contain spaces) is that command's arguments.
        String[] parts = fullCommand.split("\\s+", 2);
        // split() always returns at least one element, which is what makes
        // reading parts[0] below safe without a length check of its own.
        assert parts.length >= 1 : "split() should always produce at least one element";

        String keyword = parts[0];
        String arguments = (parts.length > 1) ? parts[1].trim() : "";

        switch (keyword) {
            case EXIT_COMMAND:
                return new ExitCommand();

            case LIST_COMMAND:
                return new ListCommand();

            case MARK_COMMAND:
                return new MarkCommand(arguments, true);

            case UNMARK_COMMAND:
                return new MarkCommand(arguments, false);

            case DELETE_COMMAND:
                return new DeleteCommand(arguments);

            case FIND_COMMAND:
                if (arguments.isEmpty()) {
                    throw new MiloException("Tell me what to search for. Try: find book");
                }
                return new FindCommand(arguments);

            case TODO_COMMAND:
            case DEADLINE_COMMAND:
            case EVENT_COMMAND:
                return new AddCommand(buildTask(keyword, arguments));

            default:
                throw new MiloException("I don't know what \"" + keyword + "\" means. "
                        + "I understand: todo, deadline, event, list, find, mark, unmark, "
                        + "delete and bye.");
        }
    }

    /**
     * Builds the task described by an add-type command, choosing the
     * subclass that matches the command word.
     *
     * @param keyword   {@value #TODO_COMMAND}, {@value #DEADLINE_COMMAND} or
     *                  {@value #EVENT_COMMAND}
     * @param arguments everything after that first word
     * @return the new task
     * @throws MiloException if the arguments are missing or incomplete
     */
    private static Task buildTask(String keyword, String arguments) throws MiloException {
        // parse() routes only the three add-type command words here, so the
        // default branch below is unreachable in normal use and exists only
        // because the compiler cannot see that.
        assert keyword.equals(TODO_COMMAND) || keyword.equals(DEADLINE_COMMAND)
                || keyword.equals(EVENT_COMMAND)
                : "buildTask() was given a command word it cannot build: " + keyword;

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
                // Unreachable: parse() only calls buildTask for the three cases above.
                throw new MiloException("I don't know what \"" + keyword + "\" means.");
        }
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
        // Callers pass either 0 or a position just past a marker they already
        // found, so the search never starts outside the text.
        assert fromIndex >= 0 && fromIndex <= arguments.length()
                : "fromIndex should be a position within the arguments";

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
}
