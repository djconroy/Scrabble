/**
 * Scrabble Player
 *
 * @author Chris Bleakley
 */
public class Player {

    private static int NUM_PASSES_LIMIT = 3;

    private String name;
    private int score, undoScore;
    private int numPasses, undoNumPasses;   // consecutive passes
    public Frame frame = new Frame();

    Player () {
        name = "";
        score = 0;
        undoScore = 0;
        numPasses = 0;
        undoNumPasses = 0;
    }

    public void setName (String text) {
        name = text;
        System.out.println(text);
        return;
    }

    public String getName () {
        return(name);
    }


    public void addScore (int increment) {
        undoScore = score;
        score = score + increment;
        undoNumPasses = numPasses;
        numPasses = 0;
        return;
    }

    public void pass() {
        numPasses++;
        return;
    }

    public boolean isOverPassLimit () {
        return(numPasses >= NUM_PASSES_LIMIT);
    }

    public int getScore() {
        return(score);
    }

    public Frame getFrame() {
        return(frame);
    }

    public int getNumPasses () {
        return(numPasses);
    }

    public int unusedLettersScore () {
        int unused = 0;
        for (Tile tile:frame.getAllTiles()) {
            unused = unused + tile.getValue();
        }
        return(unused);
    }

    public void undo () {
        score = undoScore;
        numPasses = undoNumPasses+1;
        return;
    }
}
