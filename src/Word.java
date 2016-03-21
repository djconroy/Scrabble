/**
 * Word
 *
 * @author Chris Bleakley
 */
public class Word {

    private int startRow, startColumn, direction;
    private String letters;

    public static int HORIZONTAL = 0;
    public static int VERTICAL = 1;


    Word () {
        startRow = 0;
        startColumn = 0;
        direction = HORIZONTAL;
        letters = "";
        return;
    }

    Word (int row, int column, int orientation, String text) {
        setWord (row, column, orientation, text);
        return;
    }

    public void setWord (int row, int column, int orientation, String text) {
        startRow = row;
        startColumn = column;
        direction = orientation;
        letters = new String(text);
        return;
    }

    public int getStartRow () {
        return startRow;
    }

    public int getStartColumn () {
        return startColumn;
    }

    public String getLetters () {
        return letters;
    }

    public char getLetter (int i) {
        return letters.charAt(i);
    }

    public int getLength () {
        return letters.length();
    }

    public boolean isHorizontal () {
        return (direction == HORIZONTAL);
    }

    public boolean isVertical () {
        return (direction == VERTICAL);
    }

    public int getDirection () {
        return(direction);
    }

    public int getOppositeDirection () {
        int oppositeDirection;
        if (this.isVertical()) {
            oppositeDirection = HORIZONTAL;
        }
        else {
            oppositeDirection = VERTICAL;
        }
        return(oppositeDirection);
    }

}
