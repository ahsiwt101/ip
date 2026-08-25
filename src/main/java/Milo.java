/**
 * Entry point of the Milo chatbot.
 * For now Milo simply greets the user, says goodbye, and exits.
 */
public class Milo {
    /** Horizontal line used to separate blocks of Milo's output. */
    private static final String DIVIDER =
            "____________________________________________________________";

    /**
     * ASCII-art banner spelling out the chatbot's name.
     * Note there is no trailing newline: {@code println} supplies it.
     */
    private static final String BANNER = " __  __  _  _         \n"
            + "|  \\/  |(_)| |  ___  \n"
            + "| |\\/| || || | / _ \\ \n"
            + "| |  | || || || (_) |\n"
            + "|_|  |_||_||_| \\___/ ";

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello! I'm Milo.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
    }
}
