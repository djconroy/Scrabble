import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Scrabble Bot interface
 *
 * @author Chris Bleakley
 */
public interface Bot {

    public void reset () throws FileNotFoundException;

    public int getCommand (Player player, Board board, Dictionary dictionary) throws FileNotFoundException, IOException;

    public Word getWord ();

    public String getLetters ();
}
