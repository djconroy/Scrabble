/**
 * Scrabble Tile
 *
 * @author Chris Bleakley
 */
public class Tile {

    public static final char  BLANK = '*';
    private static final char  VALUE_BLANK = 0;
    private static final int[] VALUE_TILES = {1,3,3,2,1,4,2,4,1,8,5,1,3,1,1,3,10,1,1,1,1,4,4,8,4,10};   // A-Z

    private char face;			// face is the what is on the physical tile, i.e. blank or upper case letter
    private int value;

    Tile (char inputFace) {
        // precondition: inputFace must be upper case letter or BLANK
        face = inputFace;
        value = getValue(inputFace);
        return;
    }

    public boolean isBlank () {
        return(face==BLANK);
    }

    public char getFace () {
        return(face);
    }

    public boolean matches (char inputChar) {
        boolean match;
        if ( ( (inputChar>='a') && (inputChar<='z') && (face == BLANK) ) ||
                ( (inputChar>='A') && (inputChar<='Z') && (face == inputChar) ) ||
                ( (inputChar == BLANK) && (face == BLANK) ) ) {
            match = true;
        }
        else {
            match = false;
        }
        return(match);
    }

    public int getValue () {
        return(value);
    }

    public static int getValue (char letter) {
        int score, index;
        if ( ((letter>='a') && (letter<='z')) || (letter==BLANK) ) {
            score = VALUE_BLANK;
        }
        else {
            index = ((int) letter) - ((int) 'A');
            score = VALUE_TILES[index];
        }
        return(score);
    }

}
