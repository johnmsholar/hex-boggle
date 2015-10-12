# hex-boggle

A custom implementation of Java HexGrid and Trie classes used to simulate games of hexagonal boggle.

## Summary

This project was originally a coding challenge which I completed as part of an application process for Quantcast, a Data-Driven Advertising company based in San Francisco. It's also near and dear to my heart, as I eventually ended up getting an offer from the company and taking it.

## Components

The program itself is 10 lines, but relies on two private classes (HexGraph and Trie) which constitute a majority of the program.

### HexGraph

The HexGraph class models a honeycomb-like grid as a set of Cells (another private class within this one), each of which is adjacent to other cells. Assembling this model from a text input file makes searches over the grid much more intuitive and convenient.

### Trie

The Trie class implements the trie data structure (described [here](https://en.wikipedia.org/wiki/Trie)), and allows for efficient storage and lookup over a lexicon.

## Improvements

It was later brought to my attention that a hexagonal grid can be modeled by a traditional two-dimensional array, in which every element is considered adjacent not only to its neighbors above, below, left, and right, but also to its bottom-left and top-right neighbors. The adjacencies are identical! Who knew? In the interest of preserving my original submission, I've decided not to modify the code. However, users will note the presence of one particularly ugly block of matrix arithmetic which could have been totally eliminated if I had been aware of this trick.
