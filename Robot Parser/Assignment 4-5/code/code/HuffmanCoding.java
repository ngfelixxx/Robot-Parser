import java.util.*;

import org.w3c.dom.css.Counter;

import java.io.*;
import java.security.KeyStore.Entry;

/**
 * A new instance of HuffmanCoding is created for every run. The constructor is
 * passed the full text to be encoded or decoded, so this is a good place to
 * construct the tree. You should store this tree in a field and then use it in
 * the encode and decode methods.
 */
public class HuffmanCoding { //(15%)
    //added feilds
    static HMNode root;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please call this program with two arguments, which are " +
                               "the input file name and either 0 for constructing tree and printing it, or " +
                               "1 for constructing the tree and encoding the file and printing it, or " +
                               "2 for constructing the tree, encoding the file, and then decoding it and " +
                               "printing the result which should be the same as the input file.");
        } else {
            try {
                Scanner s = new Scanner(new File(args[0]));

                // Read the entire file into one String.
                StringBuilder fileText = new StringBuilder();
                while (s.hasNextLine()) {
                    fileText.append(s.nextLine() + "\n");
                }
                
                if (args[1].equals("0")) {
                    System.out.println(constructTree(fileText.toString()));
                } else if (args[1].equals("1")) {
                    constructTree(fileText.toString()); // initialises the tree field.
                    System.out.println(encode(fileText.toString()));
                } else if (args[1].equals("2")) {
                    constructTree(fileText.toString()); // initialises the tree field.
                    String codedText = encode(fileText.toString());
                     // DO NOT just change this code to simply print fileText.toString() back. ;-)
                    System.out.println(decode(codedText));
                } else {
                    System.out.println("Unknown second argument: should be 0 or 1 or 2");
                }
            } catch (FileNotFoundException e) {
                System.out.println("Unable to find file called " + args[0]);
            }
        }
    }

    // TODO add a field with your ACTUAL HuffmanTree implementation.
    private static Map<Character, String> tree;// Change type from Object to HuffmanTree or appropriate type you design.

    /**
     * This would be a good place to compute and store the tree.
     */
    public static Map<Character, String> constructTree(String text) { //extend table???
        // TODO Construct the ACTUAL HuffmanTree here to use with both encode and decode below.
        // TODO fill this in.
        Queue<HMNode> q = new PriorityQueue<>(); //lowest to highest frequency 
        for(Map.Entry<Character, Integer> entry : buildTable(text).entrySet()){
            q.offer(new HMNode(entry.getKey(), entry.getValue())); 
        }
        while(q.size()>1){
            HMNode left = q.poll();
            HMNode right = q.poll(); //top two nodes 
            HMNode parent = new HMNode(left, right); //create new tree 
            parent.setLeft(left); //set children
            parent.setRight(right);
            parent.setCount(left, right); //set frequency/count
            q.offer(parent); //add node to queue 
        }
        root = q.peek(); //finished building tree 
        return buildMap(root);
    }
    public static Map<Character, Integer> buildTable(String text){ //used to help build tree 
        Map<Character, Integer> table = new HashMap<Character, Integer>();
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
			if (table.keySet().contains(character)) {
				table.put(character, table.get(character) + 1);
			} else {
				table.put(character, 1);
			}
        }
        return table; //calculates occurances of each unique character in the text 
    }
    public static Map<Character, String> buildMap(HMNode root){
        Stack<HMNode> s = new Stack<>();
        Map<Character, String> emptyMap = new HashMap<>();
        s.push(root); //after building tree 
        while(!s.isEmpty()){
            HMNode curr = s.pop();
            if(curr.getLeft() == null && curr.getRight() == null){ //if no childen then it is leaf node 
                emptyMap.put(curr.getLetter(), curr.getCode()); //add that to tree since it has the letters 
            }
            if(curr.getLeft() != null){
                s.push(curr.getLeft());
                curr.getLeft().setCode(curr.getCode()+"0"); //c0 to left child 
            }
            if(curr.getRight() != null){
                s.push(curr.getRight());
                curr.getRight().setCode(curr.getCode()+"1"); //c1 to right child 
            }
        }
        return emptyMap; //traverses tree to assign codes to each unqiue character 
    }

    
    /**
     * Take an input string, text, and encode it with the tree computed from the text. Should
     * return the encoded text as a binary string, that is, a string containing
     * only 1 and 0.
     */
    public static String encode(String text) { //needs to return string not integer 
        // TODO fill this in.
        String result = "";
        for(int i = 0; i<text.length(); i++){
            char c = text.charAt(i);
            result = result + c; 
        }
        return result;
    }
    /**
     * Take encoded input as a binary string, decode it using the stored tree,
     * and return the decoded text as a text string.
     */
    public static String decode(String encoded) { //uses built tree root
        // TODO fill this in.
        String output = "";
        HMNode cursor = root; //traverses tree using root 
        char[] array = encoded.toCharArray(); //seperate string into characters 
        for(int i = 0; i<encoded.length(); i++){ 
            char c = array[i];
            if(c == '0'){ //characters are use single quotations 
                cursor = cursor.getLeft(); //left child node 
                if(cursor.getLeft() == null){ //found a leaf node
                    output = output + cursor.getLetter(); //append the letter
                    cursor = root;
                }
            } else if (c == '1'){ //right child node 
                cursor = cursor.getRight();
                if(cursor.getRight() == null){ //found a leaf node 
                    output = output + cursor.getLetter();
                    cursor = root;
                }
            }
        }
        return output; //built string 
    }

    //added classes to construct Huffman Tree:
    class HMNode implements Comparable<HMNode>{
        Character letter;
        Integer count;
        String code;
        HMNode parent;
        HMNode left;
        HMNode right;
        public HMNode(HMNode left, HMNode right){ //each node has children
            this.left = left;
            this.right = right;
        }
        public HMNode(Character letter, Integer count){ //leaf nodes are the unique letters from the message 
            this.letter = letter; 
            this.count = count;
        }
        public char getLetter(){
            return letter;
        }
        public HMNode getLeft(){
            return left;
        }
        public HMNode getRight(){
            return right;
        }
        public HMNode getParent(){
            return parent;
        }
        public String getCode(){
            return code;
        }
        public int getCount(){
            return count;
        }
        public void setParent(HMNode parent){
            this.parent = parent;
        }
        public void setCode(String code){
            this.code = code;
        }
        public void setCount(HMNode left, HMNode right){
            this.count = left.getCount() + right.getCount(); 
        }
        public void setLeft(HMNode left){
            this.left = left;
        }
        public void setRight(HMNode right){
            this.right = right;
        }
        public int compareTo(HMNode n) {
            return this.getCount() - n.getCount(); 
        }
    }

}
