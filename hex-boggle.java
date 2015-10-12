/*
 * QuantCast Coding Challenge
 * Honeycomb Word Search
 * ---------------------
 * Submission By: John Sholar
 * School: Stanford University
 * Completion Time: 6 hours
 */

package quantcast_Challenge_Package;

import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.io.FileNotFoundException;

/*
 * Program Overview and Methodology:
 * 	1. The program begins by reading in the dictionary text file, and parsing it
 * 		into a trie. The Trie data structure is implemented as a private class
 * 		found later in the method, and is perfectly suited to the recursive process
 * 		of forming words one letter at a time.
 * 	2. The program then reads in the data contained in the honeycomb text file, and
 * 		parses this data into a HexGraph. HexGraph is also implemented as a private
 * 		class, and implementation details can be found in that section.
 * 	3. The program runs the findWordsInGraph method to search the HexGraph for words
 * 		contained in the Trie dictionary. These found words are stored in a TreeSet
 * 		to maintain alphabetical sorting.
 *  4. The program prints out each found string in alphabetical order.
 */
public class Quantcast_Honeycomb {
   public static void main(String[] args) {
       String honeycombData = args[0];
       String dictionaryData = args[1];
       Trie dictionary = new Trie(dictionaryData);
       HexGraph graph = new HexGraph(honeycombData);
       TreeSet<String> foundWords = graph.findWordsInGraph(dictionary);
       for (String str : foundWords) {
    	   System.out.println(str);
       }
   }
   
   /*
    * private static class HexGraph
    * ----------------------------- 
    * Fields:
    * 	1. int depth - the number of hexagonal rings that form the graph. For example,
    * 		the sample problem has 5.
    * 	2. Cell[][] graph - stores all Cells (implementations of yet another private
    * 		class found within the HexGraph class) contained in the Graph. The parent
    * 		array is of size depth, and the child arrays are of variable size. The
    * 		first is of size 1 (the innermost Cell), and all subsequent arrays are of
    * 		size (i * 6), where i is the index of the child array within the parent.
    */
   private static class HexGraph {
       private int depth;
       private Cell[][] graph;
       
       /*
        * public HexGraph
        * ---------------
        * The HexGraph is provided a data file containing the contents of the cells
        * and reads them in, with the following steps.
        * 	1. A check is performed to make sure the file exists (unnecessary because
        * 		this is guaranteed by the spec, but nevertheless demanded by the Scanner
        * 		class.
        * 	2. The Cell[][] graph is initialized provided in the text file.
        * 	3. Graph is filled with Cells. Each cell is assigned a character value read
        * 		in from the text file.
        * 	4. The neighbors of each cell are initialized via the
        * 		initializeCellNeighbors method.
        */
       public HexGraph(String hexDataFile) {
           Scanner fileScanner = null;
           try {fileScanner = new Scanner(new File(hexDataFile));}
           catch (FileNotFoundException ex) {ex.printStackTrace();};
           depth = fileScanner.nextInt();
           fileScanner.nextLine();
           graph = new Cell[depth][];
           for (int i = 0; i < depth; i++) {
               if (i == 0) graph[i] = new Cell[1];
               else graph[i] = new Cell[i * 6];
               String currentLine = fileScanner.nextLine();
               for (int j = 0; j < currentLine.length(); j++) {
                   char currentChar = currentLine.charAt(j);
                   Cell newCell = new Cell(currentChar);
                   graph[i][j] = newCell;
               }
           }
           fileScanner.close();
           initializeCellNeighbors();
       }
       
       /*
        * public void initializeCellNeighbors
        * -----------------------------------
        * Initializing the respective neighbors of each Cell in the graph proves to be
        * tricky, and every attempt was made to sequester the hideous arithmetic that
        * results away in this method. Though the calculations are difficult to read,
        * their intent is laid out here.
        * 	1. The program iterates through every cell in the graph, moving from the
        * 		inside out. In this way, we consider only a small subset of the graph
        * 		at the beginning, essentially treating it as a progressively larger
        * 		hexagon for each iteration of the outermost loop.
        * 	2. Because we are working from the inside out, initially, each Cell is
        * 		treated as if it lies on the edge of the graph. Thus, there are two
        * 		cases: the cell lies on a "corner" or an "edge" of the graph.
        * 	3. The if and else cases within the loop handle these two cases, and have
        * 		been marked as such. The obfuscating arithmetic in each case grabs
        * 		the desired cell, but it is dark magic. Tread lightly.
        * 			3a.	In the case of a corner, the cell immediately between the
        * 				current cell and the center is the only inner neighbor.
        * 			3b. In the case of an edge, there are two inner neighbors.
        * 	4. Regardless of whether the cell lies on an edge or a corner, it has
        * 		two neighbors on either side of it. These are initialized after
        * 		the conditional.
        * 	5. Finally, we iterate through the Cells added as neighbors and declare
        * 		our current cell to be a neighbor of THOSE cells. We can see that,
        * 		as we move outward, we initialize the inner and same-row neighbors
        * 		of a given cell first, and then the outer neighbors in the next 
        * 		iteration, via this retroactive addition.
        */
       private void initializeCellNeighbors() {
           if (depth <= 1) return;
           for (int i = 1; i < graph.length; i++) {
               for (int j = 0; j < graph[i].length; j++) {
                   Cell currentCell = graph[i][j];
                   // Conditional: inner neighbors are initialized
                   if (j % i == 0) { // Case 1: the Cell lies on a corner
                       currentCell.addNeighbor(graph[i - 1][(j / i)*(i - 1)]);
                   }
                   else { // Case 2: the Cell lies on an edge
                       int jIndex1 = ((j-(j/i)-1)+graph[i-1].length)%graph[i-1].length;
                       int jIndex2 = (j - (j/i))%graph[i-1].length;
                       currentCell.addNeighbor(graph[i-1][jIndex1]);
                       currentCell.addNeighbor(graph[i-1][jIndex2]);
                   }
                   // Neighbors to either side are initialized
                   currentCell.addNeighbor(graph[i][(j+graph[i].length+1)%(graph[i].length)]);
                   currentCell.addNeighbor(graph[i][(j+graph[i].length-1)%(graph[i].length)]);
                   for (Cell c : currentCell.getNeighbors()) {
                       c.addNeighbor(currentCell);
                   }
               }
           }
       }
       
       /*
        * public TreeSet<String> findWordsInGraph
        * ---------------------------------------
        * The findWordsInGraph method acts as a wrapper for the recursive function
        * that actually performs the brunt of the work. This method iterates through
        * each Cell in the graph and calls the recursive function on it.
        */
       public TreeSet<String> findWordsInGraph(Trie dictionary) {
           TreeSet<String> result = new TreeSet<String>();
           for (int i = 0; i < graph.length; i++) {
               for (int j = 0; j < graph[i].length; j++) {
                   Cell currentCell = graph[i][j];
                   String word = "" + currentCell.getCharacter();
                   recursiveWordFind(word, currentCell, result, dictionary);
               }
           }
           return result;
       }
       
       /*
        * public void recursiveWordFind
        * -----------------------------
        * The recursiveWordFind method accepts four parameters: the string formed by
        * our current Cell path, the Cell we're currently investigating, a TreeSet
        * to which we add valid words, and a Trie dictionary which is used to determine
        * the validity of words. The method operates as follows:
        * 	1. It is determined if the given string is a valid prefix and a valid word.
        * 		A prefix is defined as a string that could, with further additions,
        * 		become a word. A word is an entry that exists in the given dictionary.
        * 		If the string is an unfound word, it is added to the set of found words. 
        * 		If the string is NOT a prefix, the method returns, to prevent further
        * 		unnecessary searching.
        * 	2. The "used" variable of the current cell is set to true, so that it won't
        * 		be used multiple times in the string.
        * 	3. The program iterates through the neighbors of the current cell. For those
        * 		which have not already been used, the method is recursively called with
        * 		the neighbor as the active cell and an updated string.
        * 	4. After all neighbors have been investigated, the "used" field of the cell
        * 		is set to false, so that it CAN be used by calls in the future.
        */
       public void recursiveWordFind(String str, Cell currentCell, 
                                     TreeSet<String> foundWords, Trie dictionary) {
           boolean containsPrefix = dictionary.containsPrefix(str);
           boolean containsWord = dictionary.containsWord(str);
           if (containsWord && !foundWords.contains(str)) foundWords.add(str);
           else if (!containsPrefix) return;
           
           currentCell.setUsedInWord(true);
           for (Cell neighbor : currentCell.getNeighbors()) {
               if (!neighbor.usedInWord()) {
                   recursiveWordFind(str + "" + neighbor.getCharacter(), neighbor, foundWords, dictionary);
               }
           }
           currentCell.setUsedInWord(false);
       }
       
       /*
        * private class Cell
        * ------------------
        * A Cell is a very basic class used to hold data for an individual component
        * of the graph. It contains a HashSet of neighbors, a character, and a boolean
        * usedInWord, which is used by the recursiveWordFind method to ensure that
        * Cells are not used multiple times in searching for strings.
        */
       private class Cell 
       {
           private HashSet<Cell> neighbors;
           private char character;
           private boolean usedInWord;
           
           public Cell(char c) {
               character = c;
               usedInWord = false;
               neighbors = new HashSet<Cell>();
           }
           
           public char getCharacter() {return character;}
           public boolean usedInWord() {return usedInWord;}
           public void setUsedInWord(boolean used) {usedInWord = used;}
           public void addNeighbor(Cell neighbor) {neighbors.add(neighbor);}
           public HashSet<Cell> getNeighbors() {return neighbors;}
       }
   }
   
   /*
    * private static class Trie
    * -------------------------
    * A trie is a modified tree, where every node contains only a single piece of data,
    * but a node's complete "value" is defined by its own data and also by the data of
    * its parent. Customarily, a the values of trie nodes are defined by their position
    * in the trie, and this certainly could have been done here, but this approach was
    * taken to avoid the tedium of converting from position to character and back again,
    * which doesn't save much time or memory to begin with.
    * 
    * The trie class contains only one field, a root node, which in turn points to all
    * other nodes. In the implementation above, the trie class is used to represent a
    * dictionary, and as such, each node has 26 children - one for each letter of the
    * alphabet.
    * 
    */
   private static class Trie {
	   private final int LETTERS_IN_ALPHABET = 26;
       private TrieNode rootNode;
       
       /*
        * public Trie
        * -----------
        * The Trie constructor is passed the address of a single textFile as an argument.
        * The constructor iterates through each word in the textFile and adds it to the
        * Trie via the addWord method.
        */
       public Trie(String dictionaryFile) {
    	   rootNode = new TrieNode('a', false, LETTERS_IN_ALPHABET);
    	   // the character stored in the root node is never referenced
    	   Scanner fileScanner = null;
           try {fileScanner = new Scanner(new File(dictionaryFile));}
           catch (FileNotFoundException ex) {ex.printStackTrace();};
           while (fileScanner.hasNextLine()) {
               String currentWord = fileScanner.nextLine();
               addWord(currentWord);
           }
           fileScanner.close();
       }
       
       /*
        * private void addWord
        * --------------------
        * The addWord method works like a trie traversal in reverse. It begins at
        * the root of the trie. For every given character in the string, it chooses
        * the appropriate child of the active node and follows the branch downward.
        * If no child exists, one is created via the addChild method. Finally,
        * the final node of the word is set to be a "word end", which means that a
        * traversal that ends on that node is derived from a valid word.
        */
       private void addWord(String str) {
           TrieNode currentNode = rootNode;
           for (int i = 0; i < str.length(); i++) {
               char currentChar = str.charAt(i);
               if (currentNode.getChild(currentChar) != null) {
                   currentNode = currentNode.getChild(currentChar);
               }
               else {
                   currentNode.addChild(new TrieNode(currentChar, false, LETTERS_IN_ALPHABET));
                   currentNode = currentNode.getChild(currentChar);
               }
           }
           currentNode.setWordEnd(true);
       }
       
       /*
        * public boolean containsPrefix
        * -----------------------------
        * The containsPrefix method traverses the tree, taking one letter at a time from
        * the given string. If, at any point, it attempts to access a child that does not
        * exist, this indicates that the given string is invalid. If all children are valid
        * for the entire length of the string, the string is a valid prefix.
        */
       public boolean containsPrefix(String str) {
    	   TrieNode currentNode = rootNode;
           if (str.equals("")) return true;
           for (int i = 0; i < str.length(); i++) {
               char currentChar = str.charAt(i);
               if (currentNode.getChild(currentChar) == null) return false;
               else currentNode = currentNode.getChild(currentChar);
           }
           return true;
       }
       
       /*
        * public boolean containsWord
        * ---------------------------
        * The containsWord method operates in a very similar word to the containsPrefix method,
        * but with the notable exception that the final node must be marked as a "word end". If
        * it is not, the given string is merely a prefix. I struggled for some time, trying to
        * decide whether or not to combine the containsWord and containsPrefix methods, and
        * ultimately decided not to. The code is much clearer this way, and very little time is
        * lost, because these lookup operations are essentially 0(1) [actually O(n), where n
        * is the length of the provided string - very fast either way].
        */
       public boolean containsWord(String str) {
           TrieNode currentNode = rootNode;
           if (str.equals("")) return false;
           for (int i = 0; i < str.length(); i++) {
               char currentChar = str.charAt(i);
               if (currentNode.getChild(currentChar) == null) return false;
               else currentNode = currentNode.getChild(currentChar);
           }
           if (currentNode.isWordEnd()) return true;
           else return false;
       }
       
       /*
        * private class TrieNode
        * ----------------------
        * The TrieNode class stores all data required for a given element of a Trie:
        * a single char, a boolean denoting whether or not this character is
        * potentially the end of a word, and a Hashmap holding Character Keys pointing
        * to TrieNode values - the children of this node.
        */
       private class TrieNode {
           private char character;
           private boolean isWordEnd;
           private HashMap<Character, TrieNode> children;
           
           /*
            * public TrieNode
            * ---------------
            * Initializes a TrieNode with the specified parameters, as described in the
            * class description.
            */
           public TrieNode(char c, boolean wordEnd, int numberOfChildren) {
               character = c;
               isWordEnd = wordEnd;
               children = new HashMap<Character, TrieNode>(numberOfChildren);
           }
           
           /*
            * Various mutator and accessor methods for fields of a TrieNode object.
            */
           public char getCharacter() { return character;}
           public boolean isWordEnd() { return isWordEnd;}
           public void setWordEnd(boolean wordEnd) {isWordEnd = wordEnd;}
           public TrieNode getChild(char c) { return children.get(new Character(c));}
           
           /*
            * public void addChild
            * --------------------
            * Adds a new TrieNode as a child of this node.
            */
           public void addChild(TrieNode newNode) {
               children.put(new Character(newNode.getCharacter()), newNode);
           }
       }
       
   }
}

// A big shout-out to Quantcast for putting out this cool challenge. It was a great
// mental exercise and a cool learning experience. I was especially proud of my
// self-constructed Trie and HexGraph implementations.