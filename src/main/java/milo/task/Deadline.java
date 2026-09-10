package milo.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

import milo.exception.MiloException;

/**
 * Represents a task that must be done before a specific date or time,
 * for example "submit report by 2019-10-15 1800".
 * <p>
 * The due date is held as a {@link LocalDateTime} rather than as text, so it
 * is a real point in time that could be compared or sorted, and so the format
 * it is displayed in is independent of the format it was typed in.
 */
public class Deadline extends Task {
    // Input formats resolve STRICTly, so an impossible date such as
    // 31/2/2019 is rejected instead of being quietly moved to Feb 28, which
    // is what the default (lenient) resolver would do. Strict resolution
    // needs "uuuu" rather than "yyyy", because "yyyy" is year-of-era and
    // would additionally demand an era field.
    /** Accepted input formats that include a time of day. */
    private static final DateTimeFormatter[] DATE_TIME_FORMATS = {
        DateTimeFormatter.ofPattern("uuuu-MM-dd HHmm", Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("d/M/uuuu HHmm", Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT),
    };

    /** Accepted input formats that give only a date. */
    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT),
    };

    /** How a date with no time is shown to the user, e.g. "Oct 15 2019". */
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH);

    /** How a date with a time is shown, e.g. "Dec 2 2019, 6:00pm". */
    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("MMM d yyyy, h:mma", Locale.ENGLISH);

    /** How a date with no time is written to the save file. */
    private static final DateTimeFormatter FILE_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    /** How a date with a time is written to the save file. */
    private static final DateTimeFormatter FILE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm", Locale.ENGLISH);

    /** When the task is due. */
    protected LocalDateTime by;

    /**
     * Whether the user supplied a time of day. A date-only deadline is stored
     * at midnight, so this records the difference between "no time given" and
     * "due at 00:00", which would otherwise be indistinguishable.
     */
    protected boolean hasTime;

    /**
     * Creates a deadline, reading the due date from text.
     *
     * @param description what the task is about
     * @param by          when the task is due, in one of the accepted formats
     * @throws MiloException if the text is not a date Milo can understand
     */
    public Deadline(String description, String by) throws MiloException {
        super(description);

        String trimmed = by.trim();
        LocalDateTime parsed = null;
        boolean withTime = false;

        // Try the formats that carry a time first: "2019-10-15" would also
        // match the front of "2019-10-15 1800" and silently lose the time.
        for (DateTimeFormatter format : DATE_TIME_FORMATS) {
            try {
                parsed = LocalDateTime.parse(trimmed, format);
                withTime = true;
                break;
            } catch (DateTimeParseException e) {
                continue; // not this format; try the next
            }
        }

        if (parsed == null) {
            for (DateTimeFormatter format : DATE_FORMATS) {
                try {
                    parsed = LocalDate.parse(trimmed, format).atStartOfDay();
                    break;
                } catch (DateTimeParseException e) {
                    continue;
                }
            }
        }

        if (parsed == null) {
            throw new MiloException("I couldn't read \"" + trimmed + "\" as a date. "
                    + "Try 2019-10-15, or 2019-10-15 1800 to include a time "
                    + "(2/12/2019 and 2/12/2019 1800 work too).");
        }

        this.by = parsed;
        this.hasTime = withTime;
    }

    /**
     * Creates a deadline from an existing one, taking its already-parsed due
     * date rather than reading it from text again. Kept private because it
     * exists only to serve {@link #copy()}, and because the public
     * constructor's job of interpreting user input does not apply here.
     *
     * @param other the deadline to copy
     */
    private Deadline(Deadline other) {
        super(other.description);
        this.by = other.by;
        this.hasTime = other.hasTime;
        this.isDone = other.isDone;
    }

    /**
     * Returns an independent copy of this deadline.
     *
     * @return a deadline equal to this one but sharing no state with it
     */
    @Override
    public Deadline copy() {
        return new Deadline(this);
    }

    /**
     * Returns the due date written the way it is shown to the user.
     *
     * @return e.g. "Oct 15 2019", or "Dec 2 2019, 6:00pm" when a time was given
     */
    private String getFormattedBy() {
        if (!hasTime) {
            return by.format(DISPLAY_DATE);
        }
        // The pattern yields "6:00PM"; lower case reads better mid-sentence.
        return by.format(DISPLAY_DATE_TIME).replace("AM", "am").replace("PM", "pm");
    }

    /**
     * Returns the deadline tagged with its type and due date, for example
     * {@code [D][ ] return book (by: Oct 15 2019)}.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + getFormattedBy() + ")";
    }

    /**
     * Returns this task as one line of the save file.
     * The date is written back in the yyyy-MM-dd form so that a saved file
     * always reloads, whichever format the user originally typed.
     */
    @Override
    public String toFileFormat() {
        String savedDate = hasTime ? by.format(FILE_DATE_TIME) : by.format(FILE_DATE);
        return "D" + FILE_SEPARATOR + super.toFileFormat() + FILE_SEPARATOR + savedDate;
    }
}
