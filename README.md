# Scrabble
[Scrabble](https://en.wikipedia.org/wiki/Scrabble) game playable by bots. This repository includes a bot I wrote called BruteForce. Except for BruteForce and BruteForceTest, all code in this repository was written by [Chris Bleakley](https://people.ucd.ie/chris.bleakley).

BruteForce does a brute force search to find the highest scoring legal word placement that can be made on the bot's turn. To do this it first generates all permutations of all subsets of letters in the player's frame, then it generates all possible word placement positions, and then it applies all these permutations at all positions and scores the possible word placements that result.

To play BruteForce against itself run Scrabble.main() with the following program arguments: BruteForce BruteForce

Note: BruteForce runs slowly when there's a blank tile in the player's frame since each blank tile multiplies the number of anagrams by approximately 26.

To see an illustration of the possible word placement positions BruteForce generates for a particular board state run BruteForceTest.main().
