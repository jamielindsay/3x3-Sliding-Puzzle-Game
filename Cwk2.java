package puzzle;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Cwk2 is a 3x3 sliding tile puzzle game utilizing the State library to model the game
 * state. General turn based logic is handled within this class.
 *
 * @author Jamie Lindsay
 */
public class Cwk2 {
    /**
     * Analysis:
     *
     * This solution relies on three key parts. Cwk2 handles the game logic; State models the data
     * and performs operations on it; and Picture helps render the graphics. Functional programming
     * is mainly used for iteration, with the "isSolvable" method in State benefiting notably from
     * this approach. The use of stream and filter allows for a single, elegant line of code to find
     * each inversion in the shuffled list, instead of relying on a nested loop-if structure.
     */
    private static final State SOLUTION = new State();
    private static State currentState = new State().shuffle();
    private static int turns = 0;

    public static void main(String args[]) {
        IntStream.iterate(0, n -> n+1).limit(999).forEach(x -> {
            currentState.render();
            check(currentState);
            currentState = input(currentState);
            turns++;
        });
    }

    /**
     * Checks game state against the solution and terminates program if solved.
     * @param state The state of the game to check against solution.
     */
    private static void check(State state) {
        if (state.equals(SOLUTION)) {
            System.out.println("Solved");
            System.out.println("Turns taken: " + turns);
            System.exit(0);
        }
        else
            System.out.println("");
    }

    /**
     * Takes input from console and, if valid, return a new game state resulting from the player's
     * move.
     * @param state The current game state.
     * @return The updated game state.
     */
    private static State input(State state) {
        ArrayList<String> tilesBuffer = new ArrayList<>(state.getTiles());
        int i = (tilesBuffer.indexOf(" "));
        Scanner reader = new Scanner(System.in);
        String input = reader.next();

        if ((input.equals("d") || input.equals("D")) && i > 2)
            Collections.swap(tilesBuffer, i, i - 3);
        else if ((input.equals("u") || input.equals("U")) && i < 6)
            Collections.swap(tilesBuffer, i, i + 3);
        else if ((input.equals("r") || input.equals("R")) && !((i%3) == 0))
            Collections.swap(tilesBuffer, i, i - 1);
        else if ((input.equals("l") || input.equals("L")) && !(((i+1)%3) == 0))
            Collections.swap(tilesBuffer, i, i+ 1);
        else {
            System.out.println("Invalid input");
            return input(state);
        }

        System.out.println("");
        return new State(tilesBuffer, state.getInversions());
    }
}
