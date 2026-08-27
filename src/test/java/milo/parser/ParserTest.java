package milo.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import milo.command.AddCommand;
import milo.command.Command;
import milo.command.DeleteCommand;
import milo.command.ExitCommand;
import milo.command.ListCommand;
import milo.command.MarkCommand;
import milo.exception.MiloException;
import milo.storage.Storage;
import milo.task.Deadline;
import milo.task.Event;
import milo.task.Task;
import milo.task.TaskList;
import milo.task.Todo;
import milo.ui.Ui;

/**
 * Parser.parse() is the busiest method in the codebase: every user command
 * passes through it, and it is responsible both for picking the right
 * Command subtype and for correctly wiring that command's data (e.g. that
 * "unmark" really does produce a mark-as-not-done command, not just "some
 * MarkCommand"). Where the built command's data is not directly inspectable,
 * these tests execute it against a small TaskList to verify the wiring.
 */
class ParserTest {
    @TempDir
    Path tempDir;

    private Storage newStorage() {
        return new Storage(tempDir.resolve("milo.txt").toString());
    }

    @Test
    void parse_bye_returnsExitCommand() throws MiloException {
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
    }

    @Test
    void parse_list_returnsListCommand() throws MiloException {
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
    }

    @Test
    void parse_delete_returnsDeleteCommand() throws MiloException {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 1"));
    }

    @Test
    void parse_mark_actuallyMarksTaskDoneWhenExecuted() throws MiloException {
        Command command = Parser.parse("mark 1");
        assertInstanceOf(MarkCommand.class, command);

        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        command.execute(tasks, new Ui(), newStorage());
        assertEquals("[T][X] read book", tasks.get(0).toString());
    }

    @Test
    void parse_unmark_actuallyMarksTaskNotDoneWhenExecuted() throws MiloException {
        Command command = Parser.parse("unmark 1");
        assertInstanceOf(MarkCommand.class, command);

        TaskList tasks = new TaskList();
        Todo todo = new Todo("read book");
        todo.markAsDone();
        tasks.add(todo);
        command.execute(tasks, new Ui(), newStorage());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
    }

    @Test
    void parse_todoWithDescription_returnsAddCommandCarryingTodo() throws MiloException {
        Command command = Parser.parse("todo borrow book");
        assertInstanceOf(AddCommand.class, command);
        Task task = ((AddCommand) command).getTask();
        assertInstanceOf(Todo.class, task);
        assertEquals("[T][ ] borrow book", task.toString());
    }

    @Test
    void parse_todoWithoutDescription_throwsMiloException() {
        assertThrows(MiloException.class, () -> Parser.parse("todo"));
    }

    @Test
    void parse_deadlineWithByMarker_returnsAddCommandCarryingDeadline() throws MiloException {
        Command command = Parser.parse("deadline return book /by 2019-10-15");
        Task task = ((AddCommand) command).getTask();
        assertInstanceOf(Deadline.class, task);
        assertEquals("[D][ ] return book (by: Oct 15 2019)", task.toString());
    }

    @Test
    void parse_deadlineWithoutByMarker_throwsMiloException() {
        assertThrows(MiloException.class, () -> Parser.parse("deadline return book"));
    }

    @Test
    void parse_deadlineMarkerBuriedInsideAWord_isNotTreatedAsAMarker() throws MiloException {
        // Regression test for the word-boundary fix: "/by" inside
        // "essay/byline" must not be mistaken for the real marker later in
        // the string.
        Command command = Parser.parse("deadline finish essay/byline /by 2019-10-15");
        Task task = ((AddCommand) command).getTask();
        assertEquals("[D][ ] finish essay/byline (by: Oct 15 2019)", task.toString());
    }

    @Test
    void parse_eventWithFromAndTo_returnsAddCommandCarryingEvent() throws MiloException {
        Command command = Parser.parse("event project meeting /from Mon 2pm /to 4pm");
        Task task = ((AddCommand) command).getTask();
        assertInstanceOf(Event.class, task);
        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)", task.toString());
    }

    @Test
    void parse_eventMarkersOutOfOrder_throwsMiloException() {
        assertThrows(MiloException.class, () -> Parser.parse("event m /to 4pm /from Mon"));
    }

    @Test
    void parse_eventMissingToMarker_throwsMiloException() {
        assertThrows(MiloException.class, () -> Parser.parse("event m /from Mon"));
    }

    @Test
    void parse_unknownKeyword_throwsMiloException() {
        assertThrows(MiloException.class, () -> Parser.parse("blah"));
    }

    @Test
    void parse_extraWhitespaceBetweenKeywordAndArguments_isHandled() throws MiloException {
        Command command = Parser.parse("todo   borrow book");
        Task task = ((AddCommand) command).getTask();
        assertEquals("[T][ ] borrow book", task.toString());
    }

    @Test
    void parse_trailingWhitespace_isTrimmedFromDescription() throws MiloException {
        Command command = Parser.parse("todo borrow book   ");
        Task task = ((AddCommand) command).getTask();
        assertEquals("[T][ ] borrow book", task.toString());
    }
}
