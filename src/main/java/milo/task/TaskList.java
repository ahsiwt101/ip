package milo.task;

import java.util.ArrayList;

import milo.exception.MiloException;

/**
 * Contains the task list and the operations that act on it: adding,
 * removing, retrieving, and rendering it for display. An {@link ArrayList}
 * backs the list so it grows as needed, with no separate counter to keep in
 * step with the number of tasks actually stored.
 */
public class TaskList {
    /** The tasks, in the order they were added. */
    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list starting from an existing collection of tasks,
     * such as one just read back from disk.
     *
     * @param tasks the tasks to start with
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns the task at the given position.
     *
     * @param index the position to remove, counting from 0
     * @return the task that was removed
     * @throws MiloException if the index does not refer to a task in the list
     */
    public Task delete(int index) throws MiloException {
        requireInRange(index);
        return tasks.remove(index);
    }

    /**
     * Returns the task at the given position, without removing it.
     *
     * @param index the position to fetch, counting from 0
     * @return the task at that position
     * @throws MiloException if the index does not refer to a task in the list
     */
    public Task get(int index) throws MiloException {
        requireInRange(index);
        return tasks.get(index);
    }

    /**
     * Returns how many tasks are in the list.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list has no tasks in it.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns the underlying tasks, for code (such as {@link Storage}) that
     * needs to work with the whole collection rather than one task at a time.
     *
     * @return the tasks, in list order
     */
    public ArrayList<Task> asArrayList() {
        return tasks;
    }

    /**
     * Renders the tasks as numbered lines, ready to be displayed.
     *
     * @return the lines of the list block, or a single explanatory line if
     *         nothing has been stored yet
     */
    public String[] getDisplayLines() {
        if (tasks.isEmpty()) {
            return new String[] {"There is nothing in your list yet."};
        }

        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            // Tasks are numbered from 1 for the user, but indexed from 0.
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        return lines;
    }

    /**
     * Renders the tasks whose description contains the given keyword, as
     * numbered lines ready to be displayed. Matches are renumbered from 1
     * within the result, rather than keeping their position in the full
     * list, since the user searching has no way to know what those original
     * numbers were.
     *
     * @param keyword the text to search for in each task's description
     * @return the lines of the matching-tasks block, or a single explanatory
     *         line if nothing matched
     */
    public String[] getMatchingDisplayLines(String keyword) {
        ArrayList<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDescription().contains(keyword)) {
                matches.add(task);
            }
        }

        if (matches.isEmpty()) {
            return new String[] {"There are no matching tasks in your list."};
        }

        String[] lines = new String[matches.size() + 1];
        lines[0] = "Here are the matching tasks in your list:";
        for (int i = 0; i < matches.size(); i++) {
            lines[i + 1] = (i + 1) + "." + matches.get(i);
        }
        return lines;
    }

    /**
     * Checks that a position refers to an actual task.
     * A defensive check: normal use always validates the number first (see
     * {@code Milo.parseTaskIndex}), so this only guards against misuse.
     *
     * @param index the position to check, counting from 0
     * @throws MiloException if the position is out of range
     */
    private void requireInRange(int index) throws MiloException {
        if (index < 0 || index >= tasks.size()) {
            throw new MiloException("There is no task " + (index + 1) + " in your list.");
        }
    }
}
