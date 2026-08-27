/**
 * Entry point of the Milo chatbot.
 * Milo wires together the {@link Ui}, {@link Storage} and {@link TaskList}
 * and runs the read-parse-execute loop. It no longer knows how a command is
 * worded (that is {@link Parser}'s job) or how a task is stored on disk
 * (that is {@link Storage}'s job); it just connects the three together.
 */
public class Milo {
    /** Where the tasks are saved, relative to where Milo is run. */
    private static final String DATA_FILE_PATH = "data/milo.txt";

    /** How Milo talks to and reads from the user. */
    private final Ui ui;

    /** Where Milo's tasks are kept between runs. */
    private final Storage storage;

    /** The tasks currently in memory. */
    private final TaskList tasks;

    /**
     * Creates Milo, loading whatever tasks were saved from a previous run.
     *
     * @param filePath where the tasks are saved, e.g. "data/milo.txt"
     */
    public Milo(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);
        this.tasks = new TaskList(storage.load());
    }

    /** Greets the user, then reads and carries out commands until told to stop. */
    public void run() {
        ui.showWelcome();
        ui.showLoadStatus(storage.getLoadWarnings(), tasks.size());

        boolean isExit = false;
        // hasNextCommand() guards against the input ending without "bye",
        // which would otherwise make readCommand() throw.
        while (!isExit && ui.hasNextCommand()) {
            String fullCommand = ui.readCommand();
            if (fullCommand.isEmpty()) {
                continue;
            }

            try {
                Command command = Parser.parse(fullCommand);
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (MiloException e) {
                ui.showError(e.getMessage());
            }
        }

        // Printed once here regardless of why the loop ended, so input that
        // runs out without "bye" still gets a proper goodbye rather than the
        // session just stopping silently.
        ui.showGoodbye();
    }

    public static void main(String[] args) {
        new Milo(DATA_FILE_PATH).run();
    }
}
