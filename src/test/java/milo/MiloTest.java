package milo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Milo.getResponse() is what the GUI calls for every message, so it needs
 * to cover what {@code run()} otherwise handles inline: a normal command,
 * an invalid one, and "bye" ending the session.
 */
class MiloTest {
    @TempDir
    Path tempDir;

    private Milo newMilo() {
        return new Milo(tempDir.resolve("milo.txt").toString());
    }

    @Test
    void getResponse_validCommand_returnsConfirmationAndStaysOpen() {
        Milo milo = newMilo();
        String response = milo.getResponse("todo read book");
        assertTrue(response.contains("[T][ ] read book"));
        assertFalse(milo.isExit());
    }

    @Test
    void getResponse_invalidCommand_returnsErrorMessage() {
        Milo milo = newMilo();
        String response = milo.getResponse("blah");
        assertTrue(response.contains("don't know what \"blah\" means"));
        assertFalse(milo.isExit());
    }

    @Test
    void getResponse_bye_returnsGoodbyeAndSetsExit() {
        Milo milo = newMilo();
        String response = milo.getResponse("bye");
        assertEquals("Bye. Hope to see you again soon!", response);
        assertTrue(milo.isExit());
    }

    @Test
    void isExit_beforeAnyCommand_isFalse() {
        assertFalse(newMilo().isExit());
    }

    @Test
    void getWelcomeMessage_emptyList_isJustTheGreeting() {
        String welcome = newMilo().getWelcomeMessage();
        assertEquals("Hello! I'm Milo.\nWhat can I do for you?", welcome);
    }

    @Test
    void getWelcomeMessage_afterTasksWereSaved_mentionsHowManyWereLoaded() {
        Milo first = newMilo();
        first.getResponse("todo read book");

        Milo second = newMilo();
        String welcome = second.getWelcomeMessage();
        assertTrue(welcome.contains("Hello! I'm Milo."));
        assertTrue(welcome.contains("I loaded 1 task(s) from last time."));
    }
}
