import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scrabble bot which uses a brute-force search to find the highest scoring play on each turn.
 *
 * @author Daniel Conroy
 */
public class BruteForce implements Bot {
    class Position {
        final int startRow, startCol, direction;
        Position (int startRow, int startCol, int direction) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.direction = direction;
        }
    }
    private Word word = new Word();
    private String letters;
    private List<List<String>> anagrams = new ArrayList<>(Frame.MAX_TILES + 1);
    private List<List<Position>> possiblePositions = new ArrayList<>(Frame.MAX_TILES + 1);

    public BruteForce() {
        reset();
    }

    @Override
    public void reset() {
        word.setWord(0, 0, Word.HORIZONTAL, "");
        letters = "";
        anagrams.add(0, new ArrayList<>(1));
        for (int numLetters = 1, capacity = Frame.MAX_TILES; numLetters <= Frame.MAX_TILES;
             capacity *= Frame.MAX_TILES - numLetters, numLetters++) {
            anagrams.add(numLetters, new ArrayList<>(capacity));
        }
        for (int numLetters = 0; numLetters <= Frame.MAX_TILES; numLetters++) {
            possiblePositions.add(numLetters, new ArrayList<>());
        }
    }

    /**
     * Returns a read-only list of possible positions of word placements using 'numTiles' tiles
     * from the player's frame. These positions relate to the game state after the last call to
     * getCommand().
     *
     * @param numTiles the number of tiles to use
     * @return list of positions
     */
    List<Position> getPositions(int numTiles) {
        return Collections.unmodifiableList(possiblePositions.get(numTiles));
    }

    @Override
    public Word getWord() {
        return word;
    }

    @Override
    public String getLetters() {
        return letters;
    }

    @Override
    public int getCommand(Player player, Board board, Dictionary dictionary) {
        // make a decision on the play here
        // use board.getSqContents to check what is on the board
        // use Board.SQ_VALUE to check the multipliers
        // use frame.getAllTiles to check what letters you have
        // return the corresponding commandCode from UI
        // if a play, put the start position and letters into word
        // if an exchange, put the characters into letters
        StringBuilder lettersInFrame = new StringBuilder();
        int numTilesInFrame, commandCode;

        for (Tile tile: player.getFrame().getAllTiles()) {
            lettersInFrame.append(tile.getFace());
        }
        numTilesInFrame = lettersInFrame.length();
        genAnagrams(lettersInFrame.toString());
        if (board.isFirstPlay()) {
            genPossibleFirstPlay();
        } else {
            genPossible(board);
        }
        word = findBestWord(numTilesInFrame, board, dictionary);
        if (word.getLetters().length() > 0) { // Word placement found
            commandCode = UI.COMMAND_PLAY;
        } else {
            commandCode = UI.COMMAND_PASS;
        }
        return commandCode;
    }

    /**
     * Generates anagrams of all subsets of letters in the player's frame.
     *
     * @param letters letters in the player's frame
     */
    private void genAnagrams(String letters) {
        for (int numLetters = 0; numLetters <= Frame.MAX_TILES; numLetters++) {
            anagrams.get(numLetters).clear();
        }
        for (String subsetOfLetters: genPowerset(letters)) {
            anagrams.get(subsetOfLetters.length()).addAll(genPermutations(subsetOfLetters));
        }
    }

    public List<String> genPowerset(String letters) {
        List<String> powerset = new ArrayList<>();
        powerset.add("");
        for (int l = letters.length() - 1; l >= 0; l--) {
            String letter = letters.substring(l, l + 1);
            int sizeSoFar = powerset.size();
            for (int i = 0; i < sizeSoFar; i++) {
                powerset.add(letter + powerset.get(i));
            }
        }
        return powerset;
    }

    public List<String> genPermutations(String letters) {
        List<String> perms = new ArrayList<>();
        perms.add("");
        for (int l = 0; l < letters.length(); l++) {
            List<String> newPerms = new ArrayList<>();
            String letter = letters.substring(l, l + 1);
            for (String perm: perms) {
                for (int i = 0; i <= l; i++) {
                    newPerms.add(perm.substring(0, i) + letter + perm.substring(i, l));
                }
            }
            perms = newPerms;
        }
        return perms;
    }

    /**
     * Generates possible positions of word placements for the first play.
     */
    private void genPossibleFirstPlay() {
        // Record all possible positions of horizontal 5, 6 and 7 letter word placements which
        // cover the centre square. They all contain a square with a double letter score, so the
        // word score may vary depending on which letter is placed on this square.
        for (int col = 1; col <= Board.CENTRE; col++) {
            possiblePositions.get(7).add(new Position(Board.CENTRE, col, Word.HORIZONTAL));
        }
        for (int col = 2; col <= Board.CENTRE; col++) {
            possiblePositions.get(6).add(new Position(Board.CENTRE, col, Word.HORIZONTAL));
        }
        for (int col = 3; col <= Board.CENTRE; col++) {
            possiblePositions.get(5).add(new Position(Board.CENTRE, col, Word.HORIZONTAL));
        }
        // Record positions of horizontal 2, 3 and 4 letter word placements which cover the centre square.
        for (int numLetters = 4; numLetters > 1; numLetters--) {
            possiblePositions.get(numLetters).add(new Position(Board.CENTRE, Board.CENTRE, Word.HORIZONTAL));
        }
    }

    /**
     * Generates all possible positions of word placements.
     *
     * @param board the current game board
     */
    private void genPossible(Board board) {
        for (int numLetters = 0; numLetters <= Frame.MAX_TILES; numLetters++) {
            possiblePositions.get(numLetters).clear();
        }
        genPossibleHorizontal(board);
        genPossibleVertical(board);
        genPossibleHorizontalHooks(board);
        genPossibleVerticalHooks(board);
    }

    /**
     * Generates all possible positions of horizontal word placements formed by
     * 1. Adding one or more letter(s) to an existing horizontal word,
     * e.g. (JACK)S, HI(JACK), HI(JACK)ING, or
     * 2. Playing perpendicular to a vertical word,
     * e.g. (JACK), then YEU(K)Y through the K.
     *
     * @param board the current game board
     */
    private void genPossibleHorizontal(Board board) {
        for (int row = 0; row < Board.SIZE; row++) {
            genPossibleInRow(board, row, 0);
        }
    }

    private void genPossibleInRow(Board board, final int row, int col) {
        // OK squares are squares on which tiles may be placed to form a new word on the board
        int numPrecedingOKSqs, numOtherOKSqs, firstLetterOrOKSq;

        // Search for a letter
        while (col < Board.SIZE && board.getSqContents(row, col) == Board.EMPTY) {
            col++;
        }
        if (col == Board.SIZE) {
            return;
        }

        // Found a letter
        numPrecedingOKSqs = countPrecedingOKSqsInRow(board, row, col);
        firstLetterOrOKSq = col - numPrecedingOKSqs;

        // Skip letter(s)
        do {
            col++;
        } while (col < Board.SIZE && board.getSqContents(row, col) != Board.EMPTY);

        numOtherOKSqs = countOtherOKSqsInRow(board, row, col);
        recordPositions(numPrecedingOKSqs, numOtherOKSqs, firstLetterOrOKSq, row, Word.HORIZONTAL, false);

        // Search for possible positions which have a different first letter
        if (col < Board.SIZE) {
            genPossibleInRow(board, row, col);
        }
    }

    /**
     * Counts OK squares preceding a letter that are not adjacent to another letter in the same row.
     */
    private int countPrecedingOKSqsInRow(Board board, final int row, int col) {
        int numPrecedingOKSqs = 0;
        
        while (numPrecedingOKSqs < Frame.MAX_TILES && col > 0
                && board.getSqContents(row, col - 1) == Board.EMPTY
                && (col == 1 || board.getSqContents(row, col - 2) == Board.EMPTY)) {
            numPrecedingOKSqs++;
            col--;
        }
        return numPrecedingOKSqs;
    }

    /**
     * Counts the OK squares following a bunch of letters which may be encompassed by the possible
     * positions containing those letters as their first letters.
     */
    private int countOtherOKSqsInRow(Board board, final int row, int col) {
        int numOtherOKSqs = 0;
        
        while (numOtherOKSqs < Frame.MAX_TILES && col < Board.SIZE) {
            if (board.getSqContents(row, col) == Board.EMPTY) {
                numOtherOKSqs++;
            }
            col++;
        }
        return numOtherOKSqs;
    }

    /**
     * Add positions encompassing the OK squares preceding and following a bunch of letters or a hook square.
     */
    private void recordPositions(
                int numPrecedingOKSqs,
                int numOtherOKSqs,
                int firstSq,
                int rowOrCol,
                int direction,
                boolean hook) {
        for (int numOthers = hook ? 1 : 0; numOthers <= numOtherOKSqs; numOthers++) {
            for (int numPreceding = 1; numPreceding <= numPrecedingOKSqs && numPreceding + numOthers <= Frame.MAX_TILES;
                 numPreceding++) {
                possiblePositions.get(numPreceding + numOthers).add(new Position(
                    (direction == Word.HORIZONTAL) ? rowOrCol : firstSq + numPrecedingOKSqs - numPreceding,
                    (direction == Word.HORIZONTAL) ? firstSq + numPrecedingOKSqs - numPreceding : rowOrCol,
                    direction));
            }
        }
        // Add positions beginning with a hook square or a letter already on the board
        for (int numOthers = hook ? 2 : 1; numOthers <= numOtherOKSqs; numOthers++) {
            possiblePositions.get(numOthers).add(new Position(
                (direction == Word.HORIZONTAL) ? rowOrCol : firstSq + numPrecedingOKSqs,
                (direction == Word.HORIZONTAL) ? firstSq + numPrecedingOKSqs : rowOrCol,
                direction));
        }
    }

    /**
     * Generates all possible positions of vertical word placements formed by
     * 1. Adding one or more letter(s) to an existing vertical word,
     * e.g. (JACK)S, HI(JACK), HI(JACK)ING, or
     * 2. Playing perpendicular to a horizontal word,
     * e.g. (JACK), then YEU(K)Y through the K.
     *
     * @param board the current game board
     */
    private void genPossibleVertical(Board board) {
        for (int col = 0; col < Board.SIZE; col++) {
            genPossibleInColumn(board, 0, col);
        }
    }

    private void genPossibleInColumn(Board board, int row, final int col) {
        int numPrecedingOKSqs, numOtherOKSqs, firstLetterOrOKSq;
        
        while (row < Board.SIZE && board.getSqContents(row, col) == Board.EMPTY) {
            row++;
        }
        if (row == Board.SIZE) {
            return;
        }
        
        numPrecedingOKSqs = countPrecedingOKSqsInColumn(board, row, col);
        firstLetterOrOKSq = row - numPrecedingOKSqs;
        
        do {
            row++;
        } while (row < Board.SIZE && board.getSqContents(row, col) != Board.EMPTY);
        
        numOtherOKSqs = countOtherOKSqsInColumn(board, row, col);
        recordPositions(numPrecedingOKSqs, numOtherOKSqs, firstLetterOrOKSq, col, Word.VERTICAL, false);
        
        if (row < Board.SIZE) {
            genPossibleInColumn(board, row, col);
        }
    }

    /**
     * Counts OK squares preceding a letter that are not adjacent to another letter in the same column.
     */
    private int countPrecedingOKSqsInColumn(Board board, int row, final int col) {
        int numPrecedingOKSqs = 0;
        
        while (numPrecedingOKSqs < Frame.MAX_TILES && row > 0
                && board.getSqContents(row - 1, col) == Board.EMPTY
                && (row == 1 || board.getSqContents(row - 2, col) == Board.EMPTY)) {
            numPrecedingOKSqs++;
            row--;
        }
        return numPrecedingOKSqs;
    }

    /**
     * Counts the OK squares following a bunch of letters which may be encompassed by the possible
     * positions containing those letters as their first letters.
     */
    private int countOtherOKSqsInColumn(Board board, int row, final int col) {
        int numOtherOKSqs = 0;
        
        while (numOtherOKSqs < Frame.MAX_TILES && row < Board.SIZE) {
            if (board.getSqContents(row, col) == Board.EMPTY) {
                numOtherOKSqs++;
            }
            row++;
        }
        return numOtherOKSqs;
    }

    /**
     * Generates all possible positions of horizontal word placements formed by
     * 1. "Hooking" a vertical word and playing perpendicular to that word,
     * e.g. playing IONIZES with the S hooked on (JACK) to make (JACK)S, or
     * 2. Playing parallel to a horizontal word(s) forming several short words,
     * e.g. CON played under (JACK) that to make (J)O and (A)N.
     *
     * @param board the current game board
     */
    private void genPossibleHorizontalHooks(Board board) {
        for (int row = 0; row < Board.SIZE; row++) {
            genPossibleHooksInRow(board, row, 0);
        }
    }

    private void genPossibleHooksInRow(Board board, final int row, int col) {
        int numPrecedingOKSqs, numHookOrOtherOKSqs, firstHookOrOKSq;

        // Search for a horizontal hook square
        while (col < Board.SIZE && !isHorizontalHookSq(board, row, col)) {
            col++;
        }
        if (col == Board.SIZE) {
            return;
        }

        // Found a horizontal hook square
        numPrecedingOKSqs = countPrecedingHookOKSqsInRow(board, row, col);
        firstHookOrOKSq = col - numPrecedingOKSqs;
        numHookOrOtherOKSqs = countHookOrOtherOKSqsInRow(board, row, col);
        recordPositions(numPrecedingOKSqs, numHookOrOtherOKSqs, firstHookOrOKSq, row, Word.HORIZONTAL, true);

        // Search for possible positions which have a different first hook square
        if (++col < Board.SIZE) {
            genPossibleHooksInRow(board, row, col);
        }
    }

    /**
     * Determines whether a square is a horizontal hook square, i.e., an empty square which is not
     * adjacent to any letter in the same row but is adjacent to a letter in the same column.
     */
    private boolean isHorizontalHookSq(Board board, int row, int col) {
        return board.getSqContents(row, col) == Board.EMPTY
                && (col == 0 || board.getSqContents(row, col - 1) == Board.EMPTY)
                && (col + 1 == Board.SIZE || board.getSqContents(row, col + 1) == Board.EMPTY)
                && ((row > 0 && board.getSqContents(row - 1, col) != Board.EMPTY)
                    || (row + 1 < Board.SIZE && board.getSqContents(row + 1, col) != Board.EMPTY));
    }

    /**
     * Counts OK squares preceding a horizontal hook square that are not adjacent to a letter.
     */
    private int countPrecedingHookOKSqsInRow(Board board, final int row, int col) {
        int numPrecedingOKSqs = 0;
        
        while (numPrecedingOKSqs < Frame.MAX_TILES - 1 && col > 0
                && board.getSqContents(row, col - 1) == Board.EMPTY
                && (col == 1 || board.getSqContents(row, col - 2) == Board.EMPTY)
                && (row == 0 || board.getSqContents(row - 1, col - 1) == Board.EMPTY)
                && (row + 1 == Board.SIZE || board.getSqContents(row + 1, col - 1) == Board.EMPTY)) {
            numPrecedingOKSqs++;
            col--;
        }
        return numPrecedingOKSqs;
    }

    /**
     * Counts the hook and OK squares which follow a particular hook square and which may be encompassed
     * by the possible positions containing that hook square as their first hook square.
     */
    private int countHookOrOtherOKSqsInRow(Board board, final int row, int col) {
        int numHookOrOtherOKSqs = 0;
        
        while (numHookOrOtherOKSqs < Frame.MAX_TILES && col < Board.SIZE
                && board.getSqContents(row, col) == Board.EMPTY
                && (col == Board.SIZE - 1 || board.getSqContents(row, col + 1) == Board.EMPTY)) {
            numHookOrOtherOKSqs++;
            col++;
        }
        return numHookOrOtherOKSqs;
    }

    /**
     * Generates all possible positions of vertical word placements formed by
     * 1. "Hooking" a horizontal word and playing perpendicular to that word,
     * e.g. playing IONIZES with the S hooked on (JACK) to make (JACK)S, or
     * 2. Playing parallel to a vertical word(s) forming several short words,
     * e.g. CON played under (JACK) that to make (J)O and (A)N.
     *
     * @param board the current game board
     */
    private void genPossibleVerticalHooks(Board board) {
        for (int col = 0; col < Board.SIZE; col++) {
            genPossibleHooksInColumn(board, 0, col);
        }
    }

    private void genPossibleHooksInColumn(Board board, int row, final int col) {
        int numPrecedingOKSqs, numHookOrOtherOKSqs, firstHookOrOKSq;
        
        while (row < Board.SIZE && !isVerticalHookSq(board, row, col)) {
            row++;
        }
        if (row == Board.SIZE) {
            return;
        }
        
        numPrecedingOKSqs = countPrecedingHookOKSqsInColumn(board, row, col);
        firstHookOrOKSq = row - numPrecedingOKSqs;
        numHookOrOtherOKSqs = countHookOrOtherOKSqsInColumn(board, row, col);
        recordPositions(numPrecedingOKSqs, numHookOrOtherOKSqs, firstHookOrOKSq, col, Word.VERTICAL, true);
        
        if (++row < Board.SIZE) {
            genPossibleHooksInColumn(board, row, col);
        }
    }

    /**
     * Determines whether a square is a vertical hook square, i.e., an empty square which is not
     * adjacent to any letter in the same column but is adjacent to a letter in the same row.
     */
    private boolean isVerticalHookSq(Board board, int row, int col) {
        return board.getSqContents(row, col) == Board.EMPTY
                && (row == 0 || board.getSqContents(row - 1, col) == Board.EMPTY)
                && (row + 1 == Board.SIZE || board.getSqContents(row + 1, col) == Board.EMPTY)
                && ((col > 0 && board.getSqContents(row, col - 1) != Board.EMPTY)
                    || (col + 1 < Board.SIZE && board.getSqContents(row, col + 1) != Board.EMPTY));
    }

    /**
     * Counts OK squares preceding a vertical hook square that are not adjacent to a letter.
     */
    private int countPrecedingHookOKSqsInColumn(Board board, int row, final int col) {
        int numPrecedingOKSqs = 0;
        
        while (numPrecedingOKSqs < Frame.MAX_TILES - 1 && row > 0
                && board.getSqContents(row - 1, col) == Board.EMPTY
                && (row == 1 || board.getSqContents(row - 2, col) == Board.EMPTY)
                && (col == 0 || board.getSqContents(row - 1, col - 1) == Board.EMPTY)
                && (col + 1 == Board.SIZE || board.getSqContents(row - 1, col + 1) == Board.EMPTY)) {
            numPrecedingOKSqs++;
            row--;
        }
        return numPrecedingOKSqs;
    }

    /**
     * Counts the hook and OK squares which follow a particular hook square and which may be encompassed
     * by the possible positions containing that hook square as their first hook square.
     */
    private int countHookOrOtherOKSqsInColumn(Board board, int row, final int col) {
        int numHookOrOtherOKSqs = 0;
        
        while (numHookOrOtherOKSqs < Frame.MAX_TILES && row < Board.SIZE
                && board.getSqContents(row, col) == Board.EMPTY
                && (row == Board.SIZE - 1 || board.getSqContents(row + 1, col) == Board.EMPTY)) {
            numHookOrOtherOKSqs++;
            row++;
        }
        return numHookOrOtherOKSqs;
    }

    /**
     * Scores all possible word placements and returns the highest scoring word placement that only
     * forms legal words.
     *
     * @param numTilesInFrame number of tiles in the player's frame
     * @param board the current game board
     * @param dictionary dictionary of legal words
     * @return the highest scoring word placement
     */
    private Word findBestWord(int numTilesInFrame, Board board, Dictionary dictionary) {
        // Preconditions:
        // (1) ran genAnagrams
        // (2) possiblePositions[numTiles] contains possible word placement positions which
        // encompass 'numTiles' empty squares, i.e., in which numTiles tiles from the player's
        // frame would be used in a word placement at that position.
        Word bestWord = new Word(0, 0, Word.HORIZONTAL, "");
        int bestScore = 0;
        
        for (int numTiles = numTilesInFrame; numTiles > 0; numTiles--) {
            for (Position position: possiblePositions.get(numTiles)) {
                for (String anagram: anagrams.get(numTiles)) {
                    for (String newAnagram: assignLettersToBlankTiles(anagram)) {
                        Word word = new Word(position.startRow, position.startCol, position.direction,
                                mergeAnagramWithLettersOnBoard(newAnagram, board, numTiles, position));
                        int score = board.getTotalWordScore(word);
                        if (numTiles == Frame.MAX_TILES) {
                            score += Scrabble.BONUS;
                        }
                        if (score > bestScore) {
                            ArrayList<String> justWords = new ArrayList<>();
                            for (String aWord: board.getWords()) {
                                justWords.add(aWord.toUpperCase());
                            }
                            if (dictionary.areWords(justWords)) {
                                bestWord = word;
                                bestScore = score;
                            }
                        }
                    }
                }
            }
        }
        return bestWord;
    }

    private String mergeAnagramWithLettersOnBoard(String anagram, Board board, int numTiles, Position position) {
        StringBuilder justAWord = new StringBuilder();
        int row = position.startRow;
        int col = position.startCol;
        int tilesUsed = 0;
        
        while (tilesUsed < numTiles) {
            if (board.getSqContents(row, col) == Board.EMPTY) {
                justAWord.append(anagram.charAt(tilesUsed));
                tilesUsed++;
            } else {
                justAWord.append(board.getSqContents(row, col));
            }
            if (position.direction == Word.HORIZONTAL) {
                col++;
            } else { // position.direction == Word.VERTICAL
                row++;
            }
        }
        return justAWord.toString();
    }

    public List<String> assignLettersToBlankTiles(String anagram) {
        List<String> anagrams = new ArrayList<>();
        int blankTileFaceIndex = anagram.indexOf(Tile.BLANK);
        
        if (blankTileFaceIndex == -1) { // There is no blank tile face in anagram
            anagrams.add(anagram);
        } else {
            StringBuilder newAnagram = new StringBuilder(anagram);
            for (char letterAssigned = 'a'; letterAssigned <= 'z'; letterAssigned++) {
                newAnagram.setCharAt(blankTileFaceIndex, letterAssigned);
                anagrams.addAll(assignLettersToBlankTiles(newAnagram.toString()));
            }
        }
        return anagrams;
    }
}
