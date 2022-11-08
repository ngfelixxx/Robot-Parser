import java.util.*;
import java.io.*;

public class LempelZivDecompress {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please call this program with one argument which is the input file name.");
        } else {
            try {
                Scanner s = new Scanner(new File(args[0]));

                // Read the entire file into one String.
                StringBuilder fileText = new StringBuilder();
                while (s.hasNextLine()) {
                    fileText.append(s.nextLine() + "\n");
                }

                System.out.println(decompress(fileText.toString()));
            } catch (FileNotFoundException e) {
                System.out.println("Unable to find file called " + args[0]);
            }
        }
    }
    
    /**
     * Take compressed input as a text string, decompress it, and return it as a
     * text string.
     */
    public static String decompress(String compressed) {
        // TODO (currently just returns the argument).
        ArrayList<Tuple> tupleList = new ArrayList<>();
        int cursor = 0;
        String output = "";
        for(Tuple t : tupleList){
            if(t.length == 0 && t.offSet == 0){
                output = output + t.nextCharacter;
                cursor = cursor + 1;
            } 
            
        }
        return compressed;
    }

    class Tuple { //should be an individual public class?
        int offSet;
        int length; 
        Character nextCharacter;
        public Tuple(int offSet, int length, Character nextCharacter){
            this.offSet = offSet;
            this.length = length;
            this.nextCharacter = nextCharacter;
        }
    }
}
