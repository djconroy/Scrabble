// TwoBots
// =======
// To run it, create a jar file using Eclipse File->Export->java->jar executable->name + directory
// Then, run the jar using terminal
// java -jar TwoBots.jar <bot_name> <bot_name>
//
//


import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Scrabble Game playable by Bots
 *
 * @author Chris Bleakley
 */
public class Scrabble {

    private static int NUM_PLAYERS = 2;
    static int BONUS = 50;

    private static String[] BOT_NAMES = {
            "SeventeenTwentyNine","BsWhyteFalcon","Brogrammers","CoffeeTime","EdEddnEddy","Encoka","GetRekt",
            "GroupPNK",	"JCJ","MilesOfTiles","MVP","NaVi","NorfolkNChance","PaTriCon","Quizzify",
            "Random1","Random2","Clabbers","RedfordRejects","ScrabbledEggs","Scrabulous","Scrbbl",
            "TauTitans","TriForce","yet_itCompiles", "MVPslow", "BruteForce"};


    public static void main (String[] args) throws FileNotFoundException, IOException {

        Board board = new Board();
        Pool pool = new Pool();
        Player[] players = new Player[NUM_PLAYERS];
        Bot[] bots = new Bot[NUM_PLAYERS];
        UI ui = new UI();
        int currentPlayerId = 0, prevPlayerId;
        Player currentPlayer, prevPlayer;
        Frame currentFrame, prevFrame;
        Bot currentBot;
        Word word;
        char lowestTile;
        boolean tileDraw = false;
        String letters;
        char[] tiles = new char[NUM_PLAYERS];
        int commandCode, checkCode, totalWordScore;
        boolean frameWasFull, turnOver = false, gameOver = false, allOverPassLimit;
        int[] unusedScore = new int [NUM_PLAYERS];
        int totalUnused;
        boolean prevWasPlay = false, challengeDone = false;
        Dictionary dictionary = new Dictionary();
        String botName = "";
        boolean found = false;

        // Initialize Bots and Players
        if (args.length<2) {
            System.out.println("Not enough Bot names");
            return;
        }
        for (int i=0; i<NUM_PLAYERS; i++) {
            botName = args[i];
            for (int j=0; j<BOT_NAMES.length; j++) {
                if (BOT_NAMES[j].equals(botName)) {
                    found = true;
                }
            }
            if (!found) {
                System.out.println("Bot name not found");
                return;
            }
            try {
                bots[i] = (Bot) Class.forName(botName).newInstance();
            } catch (IllegalAccessException ex) {
                Thread.currentThread().interrupt();
            } catch (InstantiationException ex) {
                Thread.currentThread().interrupt();
            } catch (ClassNotFoundException ex) {
                Thread.currentThread().interrupt();
            }
            bots[i].reset();
            players[i] = new Player();
            players[i].setName(botName);
        }

        // Decide who starts
        do {
            for (int i=0; i<NUM_PLAYERS; i++) {
                tiles[i] = pool.getRandomTile().getFace();
                ui.displayTile(players[i],tiles[i]);
            }
            lowestTile = tiles[0];
            currentPlayerId = 0;
            tileDraw = false;
            for (int i=1; i<NUM_PLAYERS; i++) {
                if (tiles[i] < lowestTile) {
                    lowestTile = tiles[i];
                    currentPlayerId = i;
                    tileDraw = false;
                }
                else if (tiles[i] == lowestTile) {
                    tileDraw = true;
                }
            }
            if (!tileDraw) {
                ui.displayStarter(players[currentPlayerId]);
            }
            else {
                ui.displayStarterDraw();
            }
        } while (tileDraw);

        // Play the game
        gameOver = false;
        do {
            currentPlayer = players[currentPlayerId];
            currentBot = bots[currentPlayerId];
            currentFrame = currentPlayer.getFrame();
            currentFrame.refill(pool);
            ui.displayBoard(board);
            ui.displayScores(players);
            ui.displayPoolSize(pool);
            challengeDone = false;
            do {
                commandCode = currentBot.getCommand(currentPlayer,board,dictionary);
                ui.displayCommand(players[currentPlayerId],commandCode,currentBot.getWord(),currentBot.getLetters());
                switch (commandCode) {
                    case UI.COMMAND_QUIT :
                        turnOver = true;
                        gameOver = true;
                        break;
                    case UI.COMMAND_PASS :
                        turnOver = true;
                        currentPlayer.pass();
                        allOverPassLimit = true;
                        for (int i=0; i<NUM_PLAYERS; i++) {
                            allOverPassLimit = allOverPassLimit && players[i].isOverPassLimit();
                        }
                        if (allOverPassLimit) {
                            gameOver = true;
                        }
                        prevWasPlay = false;
                        break;
                    case UI.COMMAND_HELP :
                        ui.displayHelp();
                        turnOver = false;
                        break;
                    case UI.COMMAND_EXCHANGE :
                        letters = currentBot.getLetters();
                        if (!currentFrame.isAvailable(letters)) {
                            ui.displayError(UI.EXCHANGE_NOT_AVAILABLE);
                            turnOver = false;
                        } else if ( pool.size() < letters.length()) {
                            ui.displayError(UI.EXCHANGE_NOT_ENOUGH_IN_POOL);
                            turnOver = false;
                        } else {
                            currentFrame.exchange(letters, pool);
                            currentPlayer.pass();
                            allOverPassLimit = true;
                            for (int i=0; i<NUM_PLAYERS; i++) {
                                allOverPassLimit = allOverPassLimit && players[i].isOverPassLimit();
                            }
                            if (allOverPassLimit) {
                                gameOver = true;
                            }
                            turnOver = true;
                            prevWasPlay = false;
                        }
                        break;
                    case UI.COMMAND_PLAY :
                        word = currentBot.getWord();
                        checkCode = board.checkWord(word, currentFrame);
                        if (checkCode != UI.WORD_OK) {
                            ui.displayError(checkCode);
                            turnOver = false;
                        }
                        else {
                            frameWasFull = currentFrame.isFull();
                            totalWordScore = board.setWord(word, currentFrame);
                            if	(!dictionary.areWords(board.getWords())) {
                                ui.displayError(UI.NOT_IN_THE_DICTIONARY);
                                board.undo();
                                currentPlayer.undo();
                                currentFrame.undo();
                            }
                            if (currentFrame.isEmpty() && frameWasFull) {
                                totalWordScore = totalWordScore + BONUS;
                            }
                            ui.displayWordScore(totalWordScore);
                            currentPlayer.addScore(totalWordScore);
                            turnOver = true;
                            prevWasPlay = true;
                            if (currentFrame.isEmpty() && pool.isEmpty()) {
                                gameOver = true;
                            }
                        }
                        break;
                    case UI.COMMAND_CHALLENGE :
                        if (challengeDone) {
                            ui.displayError(UI.CHALLENGE_REPEAT);
                            turnOver = false;
                        }
                        else if (board.isFirstPlay()) {
                            ui.displayError(UI.CHALLENGE_FIRST_PLAY);
                            turnOver = false;
                        }
                        else if (!prevWasPlay) {
                            ui.displayError(UI.CHALLENGE_PREV_NOT_PLAY);
                            turnOver = false;
                        }
                        else if	(!dictionary.areWords(board.getWords())) {
                            ui.displayChallengeSuccess();
                            prevPlayerId = currentPlayerId-1;
                            if (prevPlayerId < 0) {
                                prevPlayerId = NUM_PLAYERS-1;
                            }
                            prevPlayer = players[prevPlayerId];
                            prevFrame = prevPlayer.getFrame();
                            board.undo();
                            prevPlayer.undo();
                            prevFrame.undo();
                            ui.displayBoard(board);
                            ui.displayScores(players);
                            ui.displayPoolSize(pool);
                            challengeDone = true;
                            turnOver = false;
                        }
                        else {
                            ui.displayChallengeFail();
                            turnOver = true;
                            challengeDone = true;
                            prevWasPlay = false;
                        }
                        break;
                }
            }	while (!turnOver);
            if (!gameOver) {
                currentPlayerId++;
                if (currentPlayerId > NUM_PLAYERS-1) {
                    currentPlayerId = 0;
                }
            }
        } while (!gameOver);

        totalUnused = 0;
        for (int i=0; i<NUM_PLAYERS; i++) {
            unusedScore[i] = players[i].unusedLettersScore();
            players[i].addScore(-unusedScore[i]);
            totalUnused = totalUnused + unusedScore[i];
        }
        if (unusedScore[currentPlayerId] == 0) {
            players[currentPlayerId].addScore(totalUnused);
        }
        ui.displayBoard(board);
        ui.displayResult(players);

        System.out.println("GAME OVER");

        return;
    }

}
