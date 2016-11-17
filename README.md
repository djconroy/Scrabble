# Scrabble
[Scrabble](https://en.wikipedia.org/wiki/Scrabble) game playable by bots. This repository includes a bot I wrote called BruteForce. Except for BruteForce and BruteForceTest, all code in this repository was written by [Chris Bleakley](http://www.ucd.ie/research/people/computerscience/assoc%20professorchrisbleakley/).

BruteForce generates every possible word placement that can be made on the bot's turn and then plays the highest scoring legal one. To do this it first generates all anagrams of all subsets of letters in the player's frame, then generates all possible word placement positions and then applies all anagrams at all positions and scores the possible word placements that result.

To play BruteForce against itself run Scrabble.main() with the following program arguments: BruteForce BruteForce

Note: BruteForce runs slowly when there's a blank tile in the player's frame since each blank tile multiplies the number of anagrams by approximately 26.

To see an illustration of the possible word placement positions BruteForce generates for a particular board state run BruteForceTest.main().
