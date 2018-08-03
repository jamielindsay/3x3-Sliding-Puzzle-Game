package puzzle;

import static java.util.stream.Collectors.*;
import static puzzle.List.*;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Picture {

    private final List<List<Character>> text;
    private final int depth, width;
    private static final char space = ' ', horiz = '-', vert = '|';
    public static final int TOP = 0, MID = 50, BOT = 100, LFT = 0, CTR = 50, RGT = 100;

    /**
     * Left justify a lsit of chars within a given field width by adding space chars
     * @param line		the line to be justified
     * @param width		the overall field width (internal private method so width>line.length()
     * @return			the justified text
     */
    private static List<Character> leftJustify(List<Character> line, int width) {
        int n = width - line.length();
        List<Character> padding = repeat(n, space);
        return line.append(padding);
    }

    /**
     * Reduce a list of char into a string
     * @param line		the line to be converted
     * @return			the string of chars
     */
    private static String toString(List<Character> line) {
        return line.map(c -> c.toString()).foldr((a,b) -> a+b, "");
    }

    /**
     * Convert a string into a list of char
     * @param s		the string to be converted
     * @return		the list of chars
     */
    public static List<Character> stringToListOfCharacters(String s) {
        if (s.isEmpty())
            return new List<Character>();
        else
            return cons(s.charAt(0), stringToListOfCharacters(s.substring(1)));
    }


    /**
     * An empty picture constructor
     * @return		the empty picture
     */
    public static Picture emptyPicture() {
        return new Picture("");
    }

    /**
     * Construct a picture from a single char with given depth and width
     * @param d		depth of the picture
     * @param w		width of the picture
     * @param c		char to fill the picture
     * @return		the new rectangular picture
     */
    public static Picture box(int d, int w, Character c) {
        if (d<=0 || w<=0)
            return emptyPicture();
        String line = Stream.generate(() -> c.toString()).limit(w).collect(joining());
        return new Picture(Stream.generate(() -> line).limit(d).collect(joining("\n")));
    }

    /**
     * Return a new empty list of pictures. This is useful as the terminator when constructing
     * a list of pictures: e.g.  cons(p1, cons(p2,... cons(pk, emptyPictureList())...))
     * @return		an empty picture list
     */
    public static List<Picture> emptyPictureList() {
        return new List<Picture>();
    }

    /**
     * A constructor to return an instance from a list of list of char. The text will be left
     * justified to ensure every row is of the same width.  The depth and width are calculated.
     * @param lines		the lines to build the picture
     */
    public Picture(List<List<Character>> lines) {
        depth = lines.length();
        width = lines.map(List::length).foldr((a,b) -> Integer.max(a, b), 0);
        text  = lines.map(line -> leftJustify(line, width));
    }

    /**
     * A constructor to return an instance from a string. The newline chars split the rows.
     * The constructor splits the string and delegates to the constructor for lists of lists
     * @param string		the string to build the picture
     */
    public Picture(String string) {
        this(arrayToList(string.split("\n")).map(Picture::stringToListOfCharacters));
    }

    /**
     * Predicate to test if picture is empty
     * @return		true if picture is empty, else false
     */
    public boolean isEmpty() {
        return depth==0 || width==0;
    }

    /**
     * Convert a picture to a string with lines separated by newline chars
     */
    @Override
    public String toString() {
        return text.map(Picture::toString).toArrayList().stream().collect(joining("\n"));
    }

    /**
     * @return		the depth of a picture
     */
    public int depth() {
        return depth;
    }

    /**
     * @return		the width of a picture
     */
    public int width() {
        return width;
    }

    public Picture map(UnaryOperator<Character> f) {
        return new Picture(text.map(line -> line.map(f)));
    }

    /**
     * @return		the lines that make up the picture / a private get method
     */
    private List<List<Character>> lines() {
        return text;
    }

    /**
     * Put one picture above another.  These are guaranteed to be of same width
     * @param that		the picture that goes underneath
     * @return			the joined picture
     */
    private Picture aboveAligned(Picture that) {
        // empty pictures are aligned with any picture
        if (this.isEmpty())
            return that;
        else if (that.isEmpty())
            return this;
            // Precondition: this.width() == that.width()
        else
            return new Picture(this.lines().append(that.lines()));
    }

    /**
     * Put one picture beside another.  These are guaranteed to be of same depth
     * @param that		the picture that goes on the right
     * @return			the joined picture
     */
    private Picture besideAligned(Picture that) {
        // empty pictures are aligned with any picture
        if (this.isEmpty())
            return that;
        else if (that.isEmpty())
            return this;
            // Precondition: this.depth() == that.depth()
        else
            return new Picture(this.lines().zipWith(that.lines(), List::append));
    }

    /**
     * Make a picture a fixed width.  If the given width is too small then the picture is
     * clipped to fit.  If the given width is bigger than the picture width then padding
     * is added to the left/right.
     * @param width			the width that the picture must fit
     * @param position		proportion of whitespace to add (or columns to cut) from the left
     * @param fill			the char to use for padding
     * @return				the picture adjusted for width
     */
    public Picture fixWidth(int width, int position, Character fill) {
        int pos = Integer.min(Integer.max(position, 0), 100); // ensure in range 0..100
        int len = Math.abs(width - this.width());
        int leftWidth = len * pos / 100;
        int rightWidth = len - leftWidth;
        if (width < 1)
            return emptyPicture();
        else if (width > this.width())
            return box(this.depth(), leftWidth, fill)
                    .besideAligned(this)
                    .besideAligned(box(this.depth(), rightWidth, fill));
        else
            return new Picture(this.lines()
                    .map(line -> line.take(width+leftWidth))
                    .map(line -> line.drop(leftWidth)));
    }

    /**
     * Make a picture a fixed depth.  If the given depth is too small then the picture is
     * clipped to fit.  If the given width is bigger than the picture width then padding
     * is added to the top/bottom.
     * @param depth			the depth that the picture must fit
     * @param position		proportion of whitespace to add (or columns to cut) from the top
     * @param fill			the char to use for padding
     * @return				the picture adjusted for depth
     */
    public Picture fixDepth(int depth, int position, Character fill) {
        int pos = Integer.min(Integer.max(position, 0), 100); // ensure in range 0..100
        int len = Math.abs(depth - this.depth());
        int topDepth = len * pos / 100;
        int botDepth = len - topDepth;
        if (depth < 1)
            return emptyPicture();
        else if (depth > this.depth())
            return box(topDepth, this.width(), fill)
                    .aboveAligned(this)
                    .aboveAligned(box(botDepth, this.width(), fill));
        else
            return new Picture(this.lines().take(depth+topDepth).drop(topDepth));
    }

    /**
     * Put one picture above another. If one has smaller width then padding is added
     * @param that			the picture that goes underneath
     * @param position		proportion of whitespace to add (or columns to cut) from the left
     * @param fill			the char to use for padding
     * @return				the joined picture
     */
    private Picture above(Picture that, int position, Character fill) {
        if (this.isEmpty())
            return that;
        else if (that.isEmpty())
            return this;
        else if (this.width() < that.width())
            return this.fixWidth(that.width(), position, fill).aboveAligned(that);
        else
            return this.aboveAligned(that.fixWidth(this.width(), position, fill));
    }

    /**
     * Put one picture beside another. If one has smaller depth then padding is added
     * @param that			the picture that goes on the right
     * @param position		proportion of whitespace to add (or columns to cut) from the top
     * @param fill			the char to use for padding
     * @return				the joined picture
     */
    private Picture beside(Picture that, int position, Character fill) {
        if (this.isEmpty())
            return that;
        else if (that.isEmpty())
            return this;
        else if (this.depth() < that.depth())
            return this.fixDepth(that.depth(), position, fill).besideAligned(that);
        else
            return this.besideAligned(that.fixDepth(this.depth(), position, fill));
    }

    /**
     * Put one picture above another. If one has smaller width then space padding is added
     * @param that			the picture that goes underneath
     * @param position		proportion of whitespace to add (or columns to cut) from the left
     * @return				the joined picture
     */
    public Picture above(Picture that, int position) {
        return this.above(that, position, space);
    }

    /**
     * Put one picture beside another. If one has smaller depth then space padding is added
     * @param that			the picture that goes on the right
     * @param position		proportion of whitespace to add (or columns to cut) from the top
     * @return				the joined picture
     */
    public Picture beside(Picture that, int position) {
        return this.beside(that, position, space);
    }

    /**
     * Transpose a picture by making lines into rows and rows into lines
     * @return		the transposed picture
     */
    public Picture transpose() {
        return new Picture(List.transpose(this.lines()));
    }

    /**
     * Reflect a picture about its horizontal mid-axis
     * @return		the reflected picture
     */
    public Picture reflectHorizontal() {
        return new Picture(this.lines().reverse());
    }

    /**
     * Reflect a picture about its vertical mid-axis
     * @return		the reflected picture
     */
    public Picture reflectVertical() {
        return new Picture(this.lines().map(List::reverse));
    }

    /**
     * Rotate the picture a given number of quadrants:
     * @param quadrants		the rotation: 1=90 degrees; 2=180 degrees; 3=270 degrees
     * @return				the rotated picture
     */
    public Picture rotate(int quadrants) {
        switch (quadrants % 4) {
            case 1:	return this.transpose().reflectVertical();
            case 2: return this.reflectHorizontal().reflectVertical();
            case 3: return this.transpose().reflectHorizontal();
            default: return this;
        }
    }

    /**
     * Stack a list of pictures
     * @param pictures		the pictures to stack
     * @param position		the justification (percentage from left)
     * @param fill			the padding character
     * @return				the picture representing the stacked pictures
     */
    public static Picture stack(List<Picture> pictures, int position, Character fill) {
        return pictures.foldr((p,q) -> p.above(q, position, fill), emptyPicture());
    }

    /**
     * Stack a list of pictures using space for padding
     * @param pictures		the pictures to stack
     * @param position		the justification (percentage from left)
     * @return				the picture representing the stacked pictures
     */
    public static Picture stack(List<Picture> pictures, int position) {
        return stack(pictures, position, space);
    }

    /**
     * Spread a list of pictures (join them side by side)
     * @param pictures		the pictures to spread
     * @param position		the justification (percentage from top)
     * @param fill			the padding character
     * @return				the picture representing the spread pictures
     */
    public static Picture spread(List<Picture> pictures, int position, Character fill) {
        return pictures.foldr((p,q) -> p.beside(q, position, fill), emptyPicture());
    }

    /**
     * Spread a list of pictures (join them side by side) using space for padding
     * @param pictures		the pictures to spread
     * @param position		the justification (percentage from top)
     * @return				the picture representing the spread pictures
     */
    public static Picture spread(List<Picture> pictures, int position) {
        return pictures.foldr((p,q) -> p.beside(q, position, space), emptyPicture());
    }

    /**
     * Place a border to the left of the picture
     * @param fill		the character to form the border
     * @return		the picture with a border
     */
    public Picture leftBorder(Character fill) {
        return stack(repeat(this.depth(),box(1, 1, fill)), LFT).beside(this, TOP);
    }

    /**
     * Place a border to the right of the picture
     * @param fill		the character to form the border
     * @return		the picture with a border
     */
    public Picture rightBorder(Character fill) {
        return this.beside(stack(repeat(this.depth(), box(1, 1, fill)), RGT), TOP);
    }

    /**
     * Place a border to the top of the picture
     * @param fill		the character to form the border
     * @return		the picture with a border
     */
    public Picture topBorder(Character fill) {
        return spread(repeat(this.width(), box(1, 1, fill)), TOP).above(this, LFT);
    }

    /**
     * Place a border to the bottom of the picture
     * @param fill		the character to form the border
     * @return			the picture with a border
     */
    public Picture bottomBorder(Character fill) {
        return this.above(spread(repeat(this.width(), box(1, 1, fill)), TOP), LFT);
    }

    /**
     * Place a border to the around the picture
     * @param fill		the character to form the border
     * @return			the picture with a border
     */
    public Picture border(Character fill) {
        return this.topBorder(fill).bottomBorder(fill).leftBorder(fill).rightBorder(fill);
    }

    /**
     * Place a frame to the left of the picture
     * @return		the picture with a frame
     */
    public Picture leftFrame() {
        return leftBorder(vert);
    }

    /**
     * Place a frame to the right of the picture
     * @return		the picture with a frame
     */
    public Picture rightFrame() {
        return rightBorder(vert);
    }

    /**
     * Place a frame to the top of the picture
     * @return		the picture with a frame
     */
    public Picture topFrame() {
        return topBorder(horiz);
    }

    /**
     * Place a frame around the picture
     * @return		the picture with a frame
     */
    public Picture frame() {
        return this.leftFrame().rightFrame().topFrame().bottomFrame();
    }

    /**
     * Place a frame to the bottom of the picture
     * @return		the picture with a frame
     */
    public Picture bottomFrame() {
        return bottomBorder(horiz);
    }

    /**
     * Return the maximum width from a list of pictures
     * @param pictures		the pictures to be analysed
     * @return				the width of the widest picture
     */
    public static int maxWidth(List<Picture> pictures) {
        return pictures.listToStream().mapToInt(Picture::width).max().getAsInt();
    }

    /**
     * Return the maximum depth from a list of pictures
     * @param pictures		the pictures to be analysed
     * @return				the depth of the deepest picture
     */
    public static int maxDepth(List<Picture> pictures) {
        return pictures.listToStream().mapToInt(Picture::depth).max().getAsInt();
    }

    /**
     * Make all the pictures in a list the same width
     * @param pictures		the pictures to be normalised
     * @param position		the justification (percentage from left)
     * @param fill			the padding character
     * @return				the list of normalised pictures
     */
    public static List<Picture> normaliseCol(List<Picture> pictures, int position, Character fill) {
        int width = maxWidth(pictures);
        return pictures.map(p -> p.fixWidth(width, position, fill));
    }

    /**
     * Make all the pictures in a list the same width using space for padding
     * @param pictures		the pictures to be normalised
     * @param position		the justification (percentage from left)
     * @return				the list of normalised pictures
     */
    public static List<Picture> normaliseCol(List<Picture> pictures, int position) {
        return normaliseCol(pictures, position, space);
    }

    /**
     * Make all the pictures in a list the same depth
     * @param pictures		the pictures to be normalised
     * @param position		the justification (percentage from top)
     * @param fill			the padding character
     * @return				the list of normalised pictures
     */
    public static List<Picture> normaliseRow(List<Picture> pictures, int position, Character fill) {
        int depth = maxDepth(pictures);
        return pictures.map(p -> p.fixDepth(depth, position, fill));
    }

    /**
     * Make all the pictures in a list the same depth using space for padding
     * @param pictures		the pictures to be normalised
     * @param position		the justification (percentage from top)
     * @return				the list of normalised pictures
     */
    public static List<Picture> normaliseRow(List<Picture> pictures, int position) {
        return normaliseRow(pictures, position, space);
    }


    /**
     * Insert lines to transform a list of pictures to a table column		/p1/p2/.../pk/
     * @param pictures		the pictures to stack
     * @param position		the justification (percentage from left)
     * @param fill			the padding character
     * @return				the picture representing the stacked pictures
     */
    public static Picture tableCol(List<Picture> pictures, int position, Character fill) {
        return Picture.stack(normaliseCol(pictures, position, fill)
                .map(Picture::topFrame), position, fill).bottomFrame();
    }

    /**
     * Insert lines to transform a list of pictures to a table column		/p1/p2/.../pk/
     * using space for padding
     * @param pictures		the pictures to stack
     * @param position		the justification (percentage from left)
     * @return				the picture representing the stacked pictures
     */
    public static Picture tableCol(List<Picture> pictures, int position) {
        return tableCol(pictures, position, space);
    }

    /**
     * Insert lines to transform a list of pictures to a table row		|p1|p2|...|pk|
     * @param pictures		the pictures to spread
     * @param position		the justification (percentage from top)
     * @param fill			the padding character
     * @return				the picture representing the spread pictures
     */
    public static Picture tableRow(List<Picture> pictures, int position, Character fill) {
        return Picture.spread(normaliseRow(pictures, position, fill)
                .map(Picture::leftFrame), position, fill).rightFrame();
    }

    /**
     * Insert lines to transform a list of pictures to a table row		|p1|p2|...|pk|
     * using space for padding
     * @param pictures		the pictures to spread
     * @param position		the justification (percentage from top)
     * @return				the picture representing the spread pictures
     */
    public static Picture tableRow(List<Picture> pictures, int position) {
        return tableRow(pictures, position, space);
    }
}