# Scrabble
[Scrabble](https://en.wikipedia.org/wiki/Scrabble) game playable by bots. This repository includes a bot I wrote called BruteForce. Except for BruteForce and BruteForceTest, all code in this repository was written by [Chris Bleakley](http://www.ucd.ie/research/people/computerscience/drchrisbleakley/).

BruteForce generates every possible word placement that can be made on the bot's turn and plays the highest scoring legal one. To do this it first generates all anagrams of any subset of letters in the player's frame and then generates all possible word placement positions. Then it applies all anagrams at all positions and scores the possible word placements that result.

To play BruteForce against itself run Scrabble.main() with the following program arguments: BruteForce BruteForce

To see an illustration of the possible word placement positions BruteForce generates for a particular board state run BruteForceTest.main().
