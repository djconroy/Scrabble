import java.util.Scanner;
import java.util.ArrayList;

/**
 * User Interface for Scrabble Game
 *
 * @author Chris Bleakley
 */
public class UI {

    public static final int COMMAND_QUIT = 0;
    public static final int COMMAND_PASS = 1;
    public static final int COMMAND_HELP = 2;
    public static final int COMMAND_PLAY = 3;
    public static final int COMMAND_EXCHANGE = 4;
    public static final int COMMAND_CHALLENGE = 5;

    public static final int WORD_OK = 1;
    public static final int WORD_OUT_OF_BOUNDS = 2;
    public static final int WORD_LETTER_NOT_IN_FRAME = 3;
    public static final int WORD_LETTER_CLASH = 4;
    public static final int WORD_NO_LETTER_FROM_FRAME = 5;
    public static final int WORD_NO_CONNECTION = 6;
    public static final int WORD_INCORRECT_FIRST_PLAY = 7;
    public static final int EXCHANGE_NOT_AVAILABLE = 100;
    public static final int EXCHANGE_NOT_ENOUGH_IN_POOL = 101;
    public static final int CHALLENGE_FIRST_PLAY = 201;
    public static final int CHALLENGE_PREV_NOT_PLAY = 202;
    public static final int CHALLENGE_REPEAT = 203;
    public static final int NOT_IN_THE_DICTIONARY = 300;

    private String command;
    private Word word = new Word();
    private String letters;

    UI () {
        return;
    }

    public void displayGameStart() {
        System.out.println("WELCOME TO SCRABBLE");
        return;
    }

    public void displayTile (Player player, char tile) {
        System.out.println(player.getName() + " draws a " + Character.toString(tile));
        return;
    }

    public void displayStarterDraw () {
        System.out.println("Draw");
        return;
    }

    public void displayStarter (Player player) {
        System.out.println(player.getName() + " starts");
        return;
    }

    void displayBoardHeader () {
        char column = 'A';
        System.out.print("    ");
        for (int c=0; c<Board.SIZE; c++) {
            System.out.printf("%c ",column);
            column++;
        }
        System.out.println();
        return;
    }

    public void displayBoard (Board board) {
        int row = 1;
        char sqContents;

        displayBoardHeader();
        System.out.println();

        for (int r=0; r<Board.SIZE; r++) {
            System.out.printf("%2d  ", row);
            for (int c=0; c<Board.SIZE; c++) {
                sqContents = board.getSqContents(r,c);
                if (sqContents == Board.EMPTY) {
                    switch (Board.SQ_VALUE[r][c]) {
                        case Board.SINGLE_LETTER : System.out.print("  "); break;
                        case Board.DOUBLE_LETTER : System.out.print("2 "); break;
                        case Board.TRIPLE_LETTER : System.out.print("3 "); break;
                        case Board.DOUBLE_WORD   : System.out.print("2."); break;
                        case Board.TRIPLE_WORD   : System.out.print("3."); break;
                    }
                }
                else {
                    System.out.printf("%c ",sqContents);
                }
            }
            System.out.printf("  %2d\n", row);
            row++;
        }

        System.out.println();
        displayBoardHeader();
        System.out.println();
        return;
    }

    public void displayScores (Player[] players) {
        for (int i=0; i<players.length; i++) {
            System.out.println(players[i].getName() + " \t " + Integer.toString(players[i].getScore()) + " points");
        }
        return;
    }

    public void displayWordScore (int wordScore) {
        System.out.println("Word score " + wordScore);
        return;
    }

    public void displayPoolSize (Pool pool) {
        System.out.println("Pool \t" + pool.size() + " tiles");
    }


    public void displayHelp () {
        System.out.println("Command options: Q (QUIT), P (PASS), X (EXCHANGE), C (CHALLENGE) or play");
        System.out.println("For an exchange, enter the letters that you wish to exchange. E.g. X ABC");
        System.out.println("For a play, enter the grid reference of the first letter, and A (across) or D (down) and the word optionally including any letters already on the board. E.g. A1 D HELLO");
        System.out.println("For a blank tile, type the lower case letter than you want to place. For all other tiles and commands, use upper case.");
    }


    public void displayError (int errCode) {
        String message = "";
        switch (errCode) {
            case WORD_OUT_OF_BOUNDS:
                message = "Error: Word does not fit on the board.";
                break;
            case WORD_LETTER_NOT_IN_FRAME:
                message = "Error: You do not have the necessary letters.";
                break;
            case WORD_LETTER_CLASH:
                message = "Error: The word entered does not fit with the letters on the board.";
                break;
            case WORD_NO_LETTER_FROM_FRAME:
                message = "Error: The word does not use any of your letters.";
                break;
            case WORD_NO_CONNECTION:
                message = "Error: The word is not connected with the words on the board. ";
                break;
            case WORD_INCORRECT_FIRST_PLAY:
                message = "Error: The first word must be in the centre of the board.";
                break;
            case EXCHANGE_NOT_AVAILABLE:
                message = "Error: Letter not available in the frame.";
                break;
            case EXCHANGE_NOT_ENOUGH_IN_POOL:
                message = "Error: Not enough tiles in the pool.";
                break;
            case CHALLENGE_FIRST_PLAY:
                message = "Error: Cannot challenge on the first play.";
                break;
            case CHALLENGE_PREV_NOT_PLAY:
                message = "Error: The previous play did not place any words.";
                break;
            case CHALLENGE_REPEAT:
                message = "Error: Challenge already done.";
                break;
            case NOT_IN_THE_DICTIONARY:
                message = "Error: Word not in the dictionary: Enforced pass.";
                break;
        }
        System.out.println(message);
        return;
    }

    public void displayResult (Player[] players) {
        int maxScore, winnerId;
        boolean draw = false;

        maxScore = players[0].getScore();
        winnerId = 0;
        for (int i=1; i<players.length; i++) {
            if (players[i].getScore() > maxScore) {
                maxScore = players[i].getScore();
                winnerId = i;
                draw = false;
            }
            else if (players[i].getScore() == maxScore) {
                draw = true;
            }
        }
        displayScores(players);
        if (!draw) {
            System.out.println(players[winnerId].getName() + " wins");
        }
        else {
            System.out.println("The game is a draw");
        }
        return;
    }

    public void displayChallengeSuccess () {
        System.out.println("Challenge successful. Removing tiles and updating scores.");
        return;
    }

    public void displayChallengeFail () {
        System.out.println("Challenge fail. Miss your turn.");
        return;
    }


    public void displayPrompt (Player player) {
        Frame frame = player.getFrame();
        String name = player.getName();
        ArrayList<Tile> tiles;
        System.out.print(name + ", your tiles are: ");
        tiles = frame.getAllTiles();
        for (Tile tile : tiles) {
            System.out.print(Character.toString(tile.getFace()) + Integer.toString(tile.getValue()) + " ");
        }
        System.out.println();
        return;
    }

    public void displayCommand (Player player, int commandCode, Word word, String letters) {
        System.out.print(player.getName() + " enters: ");
        switch (commandCode) {
            case COMMAND_QUIT :
                System.out.println("QUIT");
                break;
            case COMMAND_PASS :
                System.out.println("PASS");
                break;
            case COMMAND_HELP :
                System.out.println("HELP");
                break;
            case COMMAND_PLAY :
                System.out.print((char) ((int) word.getStartColumn() + (int) 'A'));
                System.out.print(Integer.toString(word.getStartRow() + 1));
                if (word.isHorizontal()) {
                    System.out.print(" A ");
                }
                else {
                    System.out.print(" D ");
                }
                System.out.println(word.getLetters());
                break;
            case COMMAND_EXCHANGE :
                System.out.println("EXCHANGE " + letters);
                break;
            case COMMAND_CHALLENGE :
                System.out.println("CHALLENGE");
                break;
        }
        return;
    }

    public String getName () {
        Scanner in = new Scanner(System.in);
        String playerName;

        do {
            System.out.println("Enter player name: ");
            playerName = in.nextLine();
            playerName = playerName.trim();
            if (playerName.equals("")) {
                System.out.println("Error: Must not be blank");
            }
        }
        while (playerName.equals(""));

        return(playerName);
    }

    private void parsePlay() {
        char columnText;
        String gridText, rowText, directionText, wordText;
        String[] parts;
        int row, column, direction;

        parts = command.split("( )+");
        gridText = parts[0];
        columnText = gridText.charAt(0);
        column = ((int) columnText) - ((int) 'A');
        rowText = parts[0].substring(1);
        row = Integer.parseInt(rowText)-1;
        directionText = parts[1];
        if (directionText.equals("A")) {
            direction = Word.HORIZONTAL;
        }
        else {
            direction = Word.VERTICAL;
        }
        wordText = parts[2];
        word.setWord(row,column,direction,wordText);
        return;
    }

    private void parseExchange() {
        String[] parts;

        parts = command.split("( )+");
        letters = parts[1];
        return;
    }

    public int getCommand (Player player) {
        Scanner in = new Scanner(System.in);
        boolean valid = false;
        int commandCode = 0;
        Frame frame;
        String name;
        ArrayList<Tile> tiles;

        frame = player.getFrame();
        name = player.getName();
        do {
            System.out.print(name + ", your tiles are: ");
            tiles = frame.getAllTiles();
            for (Tile tile : tiles) {
                System.out.print(Character.toString(tile.getFace()) + Integer.toString(tile.getValue()) + " ");
            }
            System.out.println();
            System.out.print("Enter your move: ");
            command = in.nextLine();
            command = command.trim();

            if ( command.equals("QUIT") || command.equals("quit") || command.equals("Q") || command.equals("q") ) {
                valid = true;
                commandCode = COMMAND_QUIT;
            }
            else if ( command.equals("PASS") || command.equals("pass") || command.equals("P") || command.equals("p") ) {
                valid = true;
                commandCode = COMMAND_PASS;
            }
            else if (command.equals("HELP") || command.equals("help") || command.equals("H") || command.equals("h") ) {
                valid = true;
                commandCode = COMMAND_HELP;
            }
            else if (command.matches("EXCHANGE( )+([A-Z*]){1,7}") || command.matches("exchange( )+([A-Z*]){1,7}") ||command.matches("X( )+([A-Z*]){1,7}") || command.matches("x( )+([A-Z*]){1,7}")) {
                valid = true;
                commandCode = COMMAND_EXCHANGE;
                parseExchange();
            }
            else if (command.matches("[A-O](\\d){1,2}( )+[A,D]( )+([A-Za-z]){1,15}")) {
                valid = true;
                commandCode = COMMAND_PLAY;
                parsePlay();
            }
            else if (command.matches("CHALLENGE") || command.matches("challenge") || command.matches("C") || command.matches("c")) {
                valid = true;
                commandCode = COMMAND_CHALLENGE;
            }
            else {
                valid = false;
            }

            if (!valid) {
                System.out.println("Error: syntax incorrect. See help.");
            }

        } while (!valid);


        return(commandCode);
    }


    public Word getWord () {
        return(word);
    }

    public String getLetters () {
        return(letters);
    }

}
