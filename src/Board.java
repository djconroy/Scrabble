import java.util.ArrayList;

/**
 * Scrabble Board
 *
 * @author Chris Bleakley
 */
public class Board {

    public static final int SINGLE_LETTER = 1;
    public static final int DOUBLE_LETTER = 2;
    public static final int TRIPLE_LETTER = 3;
    public static final int DOUBLE_WORD = 4;
    public static final int TRIPLE_WORD = 5;
    public static final int[][] SQ_VALUE =
            //   A  B  C  D  E  F  G  H  I  J  K  L  M  N  O
            { {5, 1, 1, 2, 1, 1, 1, 5, 1, 1, 1, 2, 1, 1, 5},   // 1
                    {1, 4, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 4, 1},   // 2
                    {1, 1, 4, 1, 1, 1, 2, 1, 2, 1, 1, 1, 4, 1, 1},   // 3
                    {2, 1, 1, 4, 1, 1, 1, 2, 1, 1, 1, 4, 1, 1, 2},   // 4
                    {1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1},   // 5
                    {1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 1},   // 6
                    {1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1},   // 7
                    {5, 1, 1, 2, 1, 1, 1, 4, 1, 1, 1, 2, 1, 1, 5},   // 8
                    {1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1},   // 9
                    {1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 1},   // 10
                    {1, 1, 1, 1, 4, 1, 1, 1, 1, 1, 4, 1, 1, 1, 1},   // 11
                    {2, 1, 1, 4, 1, 1, 1, 2, 1, 1, 1, 4, 1, 1, 2},   // 12
                    {1, 1, 4, 1, 1, 1, 2, 1, 2, 1, 1, 1, 4, 1, 1},   // 13
                    {1, 4, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 4, 1},   // 14
                    {5, 1, 1, 2, 1, 1, 1, 5, 1, 1, 1, 2, 1, 1, 5} }; // 15

    public static final int SIZE  = 15;
    public static final int CENTRE = 7;
    public static final char EMPTY = ' ';

    private char[][] sqContents = new char[SIZE][SIZE];
    private int numPlays, wordScore;
    private ArrayList<Word> newWords = new ArrayList<Word>();
    private ArrayList<GridRef> newSquares = new ArrayList<GridRef>();


    Board () {
        numPlays = 0;
        for (int r=0; r<SIZE; r++)  {
            for (int c=0; c<SIZE; c++)   {
                sqContents[r][c] = EMPTY;
            }
        }
        return;
    }

    public int getWordScore () {
        return(wordScore);
    }

    public char getSqContents (int row, int column) {
        return(sqContents[row][column]);
    }

    public ArrayList<String> getWords () {
        // precondition: ran getNewWords
        ArrayList<String> justWords = new ArrayList<String>();
        for (int i=0; i<newWords.size(); i++) {
            justWords.add(newWords.get(i).getLetters());
        }
        return(justWords);
    }

    public boolean isFirstPlay () {
        return(numPlays == 0);
    }


    private boolean searchBox (int startRow, int endRow, int startColumn, int endColumn) {
        boolean found = false;
        for (int r=startRow; r<=endRow; r++) {
            for (int c=startColumn; c<=endColumn; c++) {
                if (sqContents[r][c] != EMPTY) {
                    found = true;
                }
            }
        }
        return(found);
    }


    public int checkWord (Word word, Frame frame) {

        int checkCode = UI.WORD_OK;
        int startRow, startColumn, length, endRow, endColumn;
        int r, c;
        int frameLettersUsed = 0;
        char letter;
        Frame copyFrame = new Frame(frame);
        boolean foundExistingLetter = false, foundConnection = false;

        startRow = word.getStartRow();
        startColumn = word.getStartColumn();
        length = word.getLength();
        if (word.isVertical()) {
            endRow = startRow + length - 1;
            endColumn = startColumn;
        }
        else {
            endRow = startRow;
            endColumn = startColumn + length - 1;
        }

        // Check 1st move
        if ( (numPlays==0) &&
                ( ( word.isHorizontal() &&
                        ( (startRow!=CENTRE) || (startColumn>CENTRE) || (endColumn<CENTRE) ) ) ||
                        ( word.isVertical() &&
                                ( (startColumn!=CENTRE) || (startRow>CENTRE) || (endRow<CENTRE) ) ) ) ) {
            checkCode = UI.WORD_INCORRECT_FIRST_PLAY;
        }
        // Check in bounds
        else if ( (startRow<0) || (startRow>=SIZE) || (startColumn<0) || (startColumn>=SIZE) ||
                (word.isHorizontal() && (endColumn>=SIZE)) ||
                (word.isVertical() && (endRow>=SIZE)) ) {
            checkCode = UI.WORD_OUT_OF_BOUNDS;
        }
        else {
            // Try to put the letters on the board
            r = startRow;
            c = startColumn;
            for (int i=0; i<length; i++) {
                letter = word.getLetter(i);
                // Check empty square
                if (sqContents[r][c] == EMPTY) {
                    // Check letter available
                    if (copyFrame.isAvailable(letter)) {
                        copyFrame.removeChar(letter);
                        frameLettersUsed++;
                    }
                    else {
                        checkCode = UI.WORD_LETTER_NOT_IN_FRAME;
                    }
                }
                // Check no clash with existing letter
                else {
                    if (sqContents[r][c] != letter)  {
                        checkCode = UI.WORD_LETTER_CLASH;
                    } else {
                        foundExistingLetter = true;
                    }
                }
                // Next letter
                if (word.isHorizontal()) {
                    c++;
                }
                else {
                    r++;
                }
            }
            // Check at least 1 letter placed
            if ( (checkCode==UI.WORD_OK) && (frameLettersUsed==0) ) {
                checkCode = UI.WORD_NO_LETTER_FROM_FRAME;
            }
            else if ( (checkCode==UI.WORD_OK) && (numPlays>0) ) {

                // Check if word contains letters already on the board
                if (foundExistingLetter) {
                    foundConnection = true;
                }
                else {
                    // Check for connections at the perimeter of the word
                    if (word.isVertical()) {
                        if (startColumn > 0) {
                            foundConnection = searchBox (startRow, endRow, startColumn-1, endColumn-1);
                        }
                        if ( (startRow > 0) && (!foundConnection) ) {
                            foundConnection = searchBox (startRow-1, startRow-1, startColumn, endColumn);
                        }
                        if ( (endRow < Board.SIZE-1) && (!foundConnection) ) {
                            foundConnection = searchBox (endRow+1, endRow+1, startColumn, endColumn);
                        }
                        if ( (startColumn < Board.SIZE-1) && (!foundConnection) ) {
                            foundConnection = searchBox (startRow, endRow, startColumn+1, endColumn+1);
                        }
                    }
                    else {  // isHorizontal
                        if (startRow > 0) {
                            foundConnection = searchBox (startRow-1, endRow-1, startColumn, endColumn);
                        }
                        if ( (startColumn > 0) && (!foundConnection) ) {
                            foundConnection = searchBox (startRow, endRow, startColumn-1, startColumn-1);
                        }
                        if ( (endColumn < Board.SIZE-1) && (!foundConnection) ) {
                            foundConnection = searchBox (startRow, endRow, endColumn+1, endColumn+1);
                        }
                        if ( (startRow < Board.SIZE-1) && (!foundConnection) ) {
                            foundConnection = searchBox (startRow+1, endRow+1, startColumn, endColumn);
                        }
                    }
                }
                if (!foundConnection) {
                    checkCode = UI.WORD_NO_CONNECTION;
                }
            }
        }
        return(checkCode);
    }



    private int getWordScore (Word word) {
        // precondition: checkWord(word,frame) returns WORD_OK
        int score, wordValue, charValue, charMultiplier = 1, wordMultiplier = 1;
        int row, column;
        String letters;

        wordValue = 0;
        row = word.getStartRow();
        column = word.getStartColumn();
        letters = word.getLetters();
        for (int i=0; i<word.getLength(); i++) {
            charValue = Tile.getValue(letters.charAt(i));
            charMultiplier = 1;  // default
            if (sqContents[row][column] == EMPTY) {
                switch (SQ_VALUE[row][column]) {
                    case SINGLE_LETTER:
                        break;
                    case DOUBLE_LETTER:
                        charMultiplier = 2;
                        break;
                    case TRIPLE_LETTER:
                        charMultiplier = 3;
                        break;
                    case DOUBLE_WORD:
                        wordMultiplier = 2*wordMultiplier;
                        break;
                    case TRIPLE_WORD:
                        wordMultiplier = 3*wordMultiplier;
                }
            }
            if (word.isHorizontal()) {
                column++;
            }
            else {
                row++;
            }
            wordValue = wordValue + charValue * charMultiplier;
        }
        score = wordValue * wordMultiplier;
        return(score);
    }


    private Word growWord (Word word) {
        Word newWord;
        StringBuffer letters = new StringBuffer();
        int startRow, endRow, startColumn, endColumn, length;

        startRow = word.getStartRow();
        startColumn = word.getStartColumn();
        length = word.getLength();
        letters.append(word.getLetters());
        if (word.isVertical()) {
            endRow = startRow + length - 1;
            while ( (startRow > 0) && (sqContents[startRow-1][startColumn] != EMPTY) ) {
                startRow--;
                letters.insert(0,sqContents[startRow][startColumn]);
            }
            while ( (endRow < Board.SIZE-1) && (sqContents[endRow+1][startColumn] != EMPTY) ) {
                endRow++;
                letters.append(sqContents[endRow][startColumn]);
            }
        }
        else {  // isHorizontal
            endColumn = startColumn + length - 1;
            while ( (startColumn > 0) && (sqContents[startRow][startColumn-1] != EMPTY) ) {
                startColumn--;
                letters.insert(0,sqContents[startRow][startColumn]);
            }
            while ( (endColumn < Board.SIZE-1) && (sqContents[startRow][endColumn+1] != EMPTY) ) {
                endColumn++;
                letters.append(sqContents[startRow][endColumn]);
            }
        }
        newWord = new Word (startRow, startColumn, word.getDirection(), letters.toString());

        return(newWord);
    }


    private ArrayList<Word> getNewWords (Word word) {
        ArrayList<Word> newWords = new ArrayList<Word>();
        Word newWord, wordSeed;
        int row, column, length, oppositeDirection;
        char letter;

        // Extend the word placed to include all letters before and after the new letters
        newWord = growWord(word);
        newWords.add(newWord);

        // For each new letter, search in the opposite direction for new words
        row = word.getStartRow();
        column = word.getStartColumn();
        length = word.getLength();
        oppositeDirection = word.getOppositeDirection();
        for (int i=0; i<length; i++) {
            if (sqContents[row][column] == EMPTY) {
                letter = word.getLetter(i);
                wordSeed = new Word(row,column,oppositeDirection,Character.toString(letter));
                newWord = growWord(wordSeed);
                if (newWord.getLength() > 1) {
                    newWords.add(newWord);
                }
            }
            if (word.isVertical()) {
                row++;
            }
            else {
                column++;
            }
        }

        return(newWords);
    }


    public int getTotalWordScore (Word word) {
        int totalWordScore = 0;

        newWords = getNewWords(word);
        for (Word newWord : newWords) {
            totalWordScore = totalWordScore + getWordScore(newWord);
        }

        return(totalWordScore);
    }


    public int setWord (Word word, Frame frame) {
        // precondition: checkWord(word,frame) returns WORD_OK
        // postcondition: this method places the word on the board, removes the letters from
        // the frame, increments the number of plays and calculates the score
        int row, column, index;
        int totalWordScore;
        char currentLetter;

        frame.resetUndo();
        newSquares.clear();
        totalWordScore = getTotalWordScore (word);
        row = word.getStartRow();
        column = word.getStartColumn();
        for (int i=0; i<word.getLength(); i++) {
            currentLetter = word.getLetter(i);
            if (sqContents[row][column] == EMPTY) {
                newSquares.add(new GridRef(row,column));
                index = frame.find(currentLetter);
                frame.removeAt(index);
                sqContents[row][column] = currentLetter;
            }
            if (word.isHorizontal()) {
                column++;
            }
            else {
                row++;
            }
        }
        numPlays++;
        return(totalWordScore);
    }



    public void undo () {
        for (int i=0; i<newSquares.size(); i++) {
            sqContents[newSquares.get(i).getRow()][newSquares.get(i).getColumn()] = EMPTY;
        }
        numPlays--;
        return;
    }

}
