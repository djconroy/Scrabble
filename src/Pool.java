import java.util.ArrayList;
import java.util.Random;

/**
 * Pool of Scrabble Tiles
 *
 * @author Chris Bleakley
 */
public class Pool {

    private static final int NUM_BLANKS = 2;
    private static final int[] NUM_TILES = {9,2,2,4,12,2,3,2,9,1,1,4,2,6,8,2,1,6,4,6,4,2,2,1,2,1};  // A-Z

    private Random randomGenerator = new Random();

    private ArrayList<Tile> pool = new ArrayList<Tile>();

    Pool () {
        Tile tile;
        char face;
        for (int i=0; i<NUM_BLANKS; i++) {
            tile = new Tile(Tile.BLANK);
            pool.add(tile);
        }
        face = 'A';
        for (int i=0; i<NUM_TILES.length; i++) {
            for (int j=0; j<NUM_TILES[i]; j++) {
                tile = new Tile(face);
                pool.add(tile);
            }
            face++;
        }
        return;
    }

    public int size () {
        return(pool.size());
    }

    public boolean isEmpty () {
        return(pool.isEmpty());
    }

    public void add (Tile tile) {
        pool.add(tile);
    }

    public Tile getRandomTile () {
        int index = randomGenerator.nextInt(pool.size());
        return(pool.get(index));
    }

    public ArrayList<Tile> draw (int numRequested) {
        int numGiven;
        ArrayList<Tile> drawnTiles = new ArrayList<Tile>();
        Tile tile;

        if (numRequested > pool.size())
            numGiven = pool.size();
        else {
            numGiven = numRequested;
        }
        for (int i=0; i<numGiven; i++) {
            tile = getRandomTile();
            drawnTiles.add(tile);
            pool.remove(tile);
        }
        return(drawnTiles);
    }

}
