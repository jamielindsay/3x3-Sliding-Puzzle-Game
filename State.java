package puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static puzzle.Picture.TOP;


/**
 * State is a library designed to model the state of a Cwk2 game - the current positioning of its
 * tiles and its number of inversions (used to determine the solvability of a game state).
 *
 * The Picture library is used to render the game state to the console graphically.
 *
 * @author Jamie Lindsay
 */
public class State {
    // Fields
    private final ArrayList<String> tiles;
    private final int inversions;

    // Constructors
    State() {
        tiles = new ArrayList<>(Stream.of
                ("1", "2", "3", "4", "5", "6", "7", "8", " ").collect(
                        toCollection(ArrayList::new)));
        inversions = 0;
    }

    State(ArrayList<String> tiles) {
        this.tiles = new ArrayList<>(tiles);
        inversions = 0;
    }

    State(ArrayList<String> tiles, int inversions) {
        this.tiles = new ArrayList<>(tiles);
        this.inversions = inversions;
    }

    State(State state) {
        this.tiles = new ArrayList<>(state.getTiles());
        this.inversions = state.getInversions();
    }

    // Methods
    public ArrayList<String> getTiles() {
        return tiles;
    }

    public int getInversions() {
        return inversions;
    }

    public Boolean equals(State state) {
        return tiles.equals(state.getTiles());
    }

    /**
     * Shuffles the positioning of tiles in the game state into a random order, ensuring the
     * shuffled state is solvable by players.
     * @return The shuffled game state.
     */
    public State shuffle() {
        // Create new state with shuffled tiles
        State state = new State(this.shuffleState());
        // Return state if it's solvable, else shuffle again
        return (state.isSolvable()) ? state : state.shuffle();
    }

    private State shuffleState() {
        ArrayList<String> tilesBuffer = new ArrayList<>(tiles);
        Collections.shuffle(tilesBuffer);
        return new State(tilesBuffer);
    }

    /**
     * Checks whether game state is solvable or not, using the number of inversions method.
     * @return The solvability of the game state.
     */
    private Boolean isSolvable() {
        // List to hold every inversion encountered
        ArrayList<String> inversionsList  = new ArrayList<>();

        // Create new list of tiles with empty tile removed, as it cannot be parsed into an integer
        ArrayList<String> tilesBuffer = new ArrayList<>(tiles);
        tilesBuffer.remove(" ");

        // Iterate through each tile in buffer list and add any inversions to inversions list
        tilesBuffer.forEach(x ->
            inversionsList.addAll(tilesBuffer.stream().filter(y ->
                    (Integer.parseInt(y) < Integer.parseInt(x))
                            || tilesBuffer.indexOf(y) <= tilesBuffer.indexOf(x)).collect(
                                    toCollection(ArrayList::new)))
        );

        // Since the width of tiles is 3, an even number of inversions means it is solvable
        return inversionsList.size() % 2 == 0;
    }

    /**
     * Prints game state to the console in a graphical format.
     */
    public void render() {
        // For each tile, convert to picture, add borders if in certain positions, and add to list
        ArrayList<Picture> p = tiles.stream().map(x -> {
            // Convert tile into picture
            Picture y = picturize(x);
            // Add sides borders ':' to middle column and bottom border '+' to first two rows
            if ((tiles.indexOf(x) + 2) % 3 == 0) y = y.leftBorder(':').rightBorder(':');
            if (tiles.indexOf(x) < 6) y = y.bottomBorder('+');
            return y;
        }).collect(toCollection(ArrayList::new));

        // Print list of pictures in a 3x3 format
        System.out.println(((p.get(0)).above((p.get(3)).above((p.get(6)), TOP), TOP)).beside(
                (p.get(1)).above((p.get(4)).above((p.get(7)), TOP), TOP).beside(
                        (p.get(2)).above((p.get(5)).above((p.get(8)), TOP), TOP), TOP), TOP)
        );
    }

    /**
     * Converts string from tile into picture.
     * @param s The string to be converted.
     * @return The resultant picture.
     */
    private Picture picturize(String s) {
        switch (s) {
            case "1": return one();
            case "2": return two();
            case "3": return three();
            case "4": return four();
            case "5": return five();
            case "6": return six();
            case "7": return seven();
            case "8": return eight();
        }
        return space();
    }

    /* Following methods return a 3x3 picture of the tiles 1-8 plus the empty tile. This is
     * accomplished by returning a picture made of 3 rows, with each row being a stream of pictures
     * converted to a list.
     */
    private Picture one() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture("X"), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                    , TOP),
                TOP);
    }

    private Picture two() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                    , TOP),
                TOP);
    }

    private Picture three() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture("X"), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture("X"))), TOP)
                    , TOP),
                TOP);
    }

    private Picture four() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                    , TOP),
                TOP);
    }

    private Picture five() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture("X"), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                    , TOP),
                TOP);
    }

    private Picture six() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                    , TOP),
                TOP);
    }

    private Picture seven() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture("X"), new Picture("X"))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture("X"), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture("X"), new Picture("X"))), TOP)
                    , TOP),
                TOP);
    }

    private Picture eight() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture("X"), new Picture("X"))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture(" "), new Picture("X"))), TOP)
                .above(
                    Picture.tableRow(List.streamToList(Stream.of(
                    new Picture("X"), new Picture("X"), new Picture("X"))), TOP)
                    , TOP),
                TOP);
    }

    private Picture space() {
        return Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                .above(
                Picture.tableRow(List.streamToList(Stream.of(
                    new Picture(" "), new Picture(" "), new Picture(" "))), TOP)
                    , TOP),
                TOP);
    }
}
