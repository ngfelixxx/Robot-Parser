import java.util.*;
import java.io.*;

public class KMP { //(15%)

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please call this program with " +
                               "two arguments which is the input file name " +
                               "and the string to search.");
        } else {
            try {
                Scanner s = new Scanner(new File(args[0]));

                // Read the entire file into one String.
                StringBuilder fileText = new StringBuilder();
                while (s.hasNextLine()) {
                    fileText.append(s.nextLine() + "\n");
                }

                System.out.println(search(fileText.toString(), args[1]));
            } catch (FileNotFoundException e) {
                System.out.println("Unable to find file called " + args[0]);
            }
        }
    }

    /**
     * Perform KMP substring search on the given text with the given pattern.
     * 
     * This should return the starting index of the first substring match if it
     * exists, or -1 if it doesn't.
     */
    public static int search(String text, String pattern) {
        // TODO
        int i = 0; //positionm of current character in string
        int j = 0; //start of current match in pattern
        while((j+1) < text.length()){  
            if(pattern.charAt(i) == text.charAt(j+i)){ //match at i
                i = i + 1;
                if(i == pattern.length()){ //found pattern
                    System.out.println("Found");
                    return j; 
                }
            } else if (getPieTable(text, pattern)[i] == -1){ //mis match so no self over lap 
                i = 0; 
                j = j+i+1; 
            } else { //mis match but with self overlap 
                j = j+i-getPieTable(text, pattern)[i]; //match position jumps forward 
                i = getPieTable(text, pattern)[i];
            }
        }
        return -1;
    }
    public static int[] getPieTable(String text, String pattern){ //added 
        int[] pieTable = new int[pattern.length()];
        pieTable[0] = -1; //initalize 
        pieTable[1] = 0;
        int pieTablePos = 2; //positions in table 
        int patternPos = 0; 
        while(pieTablePos < patternPos){ 
            if(pattern.charAt(pieTablePos-1) == pattern.charAt(patternPos)){ //substrings ...pos-1 and 0...j match
                pieTable[pieTablePos] = patternPos + 1;
                pieTablePos = pieTablePos + 1;
                patternPos = patternPos + 1;
            } else if (patternPos > 0){ //mis match so use table to update prefix position 
                patternPos = pieTable[patternPos];
            } else { //j=0 we have run out of candidate prefixes
                pieTable[pieTablePos] = 0;
                pieTablePos = pieTablePos + 1;
            }
        }
        return pieTable;
    }
}
