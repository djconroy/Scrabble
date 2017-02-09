import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Scrabble Dictionary
 *
 * @author Chris Bleakley
 */
public class Dictionary {

    private String inputFileName = "res/sowpods.txt";
    private Node root;

    Dictionary () throws FileNotFoundException {
        String word;
        Node currentNode;
        char currentLetter;

        root = new Node();
        File inputFile = new File(inputFileName);
        Scanner in = new Scanner(inputFile);
        while (in.hasNextLine()) {
            word = in.nextLine();
            currentNode = root;
            for (int i=0; i<word.length(); i++) {
                currentLetter = word.charAt(i);
                if (currentNode.isChild(currentLetter)) {
                    currentNode = currentNode.getChild(currentLetter);
                }
                else {
                    currentNode = currentNode.addChild(currentLetter);
                }
            }
            currentNode.setEndOfWord();
        }
        in.close();
    }

    public boolean areWords (ArrayList<String> words) {
        Node currentNode;
        String currentWord;
        char currentLetter;
        boolean found = true;

        for (int w=0; (w<words.size()) && found; w++) {
            currentWord = words.get(w).toUpperCase();
            currentNode = root;
            for (int i=0; (i<currentWord.length()) && found; i++) {
                currentLetter = currentWord.charAt(i);
                if (currentNode.isChild(currentLetter)) {
                    currentNode = currentNode.getChild(currentLetter);
                }
                else {
                    found = false;
                }
            }
            if (!currentNode.isEndOfWord()) {
                found = false;
            }
        }

        return(found);
    }


}
