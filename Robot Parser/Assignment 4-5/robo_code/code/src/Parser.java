import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;
import javax.swing.JFileChooser;

import org.w3c.dom.css.RGBColor;

/**
 * The parser and interpreter. The top level parse function, a main method for
 * testing, and several utility methods are provided. You need to implement
 * parseProgram and all the rest of the parser.
 */
public class Parser {

	/**
	 * Top level parse method, called by the World
	 */
	static RobotProgramNode parseFile(File code) { 
		Scanner scan = null;
		try {
			scan = new Scanner(code);

			// the only time tokens can be next to each other is
			// when one of them is one of (){},;
			scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");

			RobotProgramNode n = parseProgram(scan); // You need to implement this!!!

			scan.close();
			return n;
		} catch (FileNotFoundException e) {
			System.out.println("Robot program source file not found");
		} catch (ParserFailureException e) {
			System.out.println("Parser error:");
			System.out.println(e.getMessage());
			scan.close();
		}
		return null;
	}

	/** For testing the parser without requiring the world */

	public static void main(String[] args) {
		if (args.length > 0) {
			for (String arg : args) {
				File f = new File(arg);
				if (f.exists()) {
					System.out.println("Parsing '" + f + "'");
					RobotProgramNode prog = parseFile(f);
					System.out.println("Parsing completed ");
					if (prog != null) {
						System.out.println("================\nProgram:");
						System.out.println(prog);
					}
					System.out.println("=================");
				} else {
					System.out.println("Can't find file '" + f + "'");
				}
			}
		} else {
			while (true) {
				JFileChooser chooser = new JFileChooser(".");
				int res = chooser.showOpenDialog(null);
				if (res != JFileChooser.APPROVE_OPTION) {
					break;
				}
				RobotProgramNode prog = parseFile(chooser.getSelectedFile());
				System.out.println("Parsing completed");
				if (prog != null) {
					System.out.println("Program: \n" + prog);
				}
				System.out.println("=================");
			}
		}
		System.out.println("Done");
	}

	// Useful Patterns

	static Pattern NUMPAT = Pattern.compile("-?\\d+"); 
	static Pattern OPENPAREN = Pattern.compile("\\(");
	static Pattern CLOSEPAREN = Pattern.compile("\\)");
	static Pattern OPENBRACE = Pattern.compile("\\{");
	static Pattern CLOSEBRACE = Pattern.compile("\\}");
	//ADD MORE GRAMMARS(Include only the terniminals)
	static Pattern PROG = Pattern.compile("STMT"); 
	static Pattern ACT = Pattern.compile("move|turnL|turnR|turnAround|shieldOn|shieldOff|takeFuel|wait");
	static Pattern LOOP = Pattern.compile("loop");
	static Pattern IF = Pattern.compile("if"); 
	static Pattern WHILE = Pattern.compile("while");
	static Pattern STMT = Pattern.compile(String.join("|",Arrays.asList(ACT.pattern(), LOOP.pattern(), IF.pattern(), WHILE.pattern()))); //look for the strings 
	static Pattern BLOCK = Pattern.compile("\\{"); 
	static Pattern EXP = Pattern.compile("NUM|SEN|OP"); 
	static Pattern SEN = Pattern.compile("fuelLeft|oppLR|oppFB|numBarrels|barrelLR|barrelFB|wallDist");
	static Pattern OP = Pattern.compile("add|sub|mul|div");
	static Pattern COND = Pattern.compile("\\(|SEN|NUM|\\)|RELOP");
	static Pattern RELOP = Pattern.compile("lt|gt|eq");
	static Pattern NUM = Pattern.compile("-?[1-9][0-9]*|0");
	//Extras
	static Pattern SEMICOLON = Pattern.compile(";");
	static Pattern COMMA = Pattern.compile(",");

	/**
	 * See assignment handout for the grammar.
	 */
	static RobotProgramNode parseProgram(Scanner s) { 
		// THE PARSER GOES HERE
		ArrayList<STMTNode> allStatements = new ArrayList<STMTNode>();
		while(s.hasNext()) {
			allStatements.add(parseStatement(s));
		}
		return new ProgNode(allStatements);
	}

	// utility methods for the parser
	//ADD ALL METHODS WITHOUT DECLARING PUBLIC OR PRIVATE
	static STMTNode parseStatement(Scanner s){ //implemented grammar 
		if (s.hasNext(ACT)) { //scan for patterns(strings in command) 
			STMTNode statement = new STMTNode(parseAction(s)); //returns an action object which when executed will apply on robot 
			require(SEMICOLON, "Statement is un-finished", s); //all statements end with semi colons 
			return statement;
		}
		if(s.hasNext(LOOP)){
			STMTNode statement = new STMTNode(parseLoop(s)); //returns a loop node statement
			return statement;
		}
		fail("Invalid Statement", s); 
		return null;
	}
	static RobotProgramNode parseAction(Scanner s) {
		if (s.hasNext("move")) {
			s.next();
			MoveNode moveNode = new MoveNode();
			return moveNode;
		} else if (s.hasNext("turnL")) {
			s.next();
			return new TurnLNode();
		} else if (s.hasNext("turnR")) {
			s.next();
			return new TurnRNode();
		} else if (s.hasNext("takeFuel")) {
			s.next();
			return new TakeFuelnode();
		} else if (s.hasNext("wait")) {
			s.next();
			WaitNode waitNode = new WaitNode();
			return waitNode;
		}
		fail("Invalid action", s);
		return null;
	}
	static LoopNode parseLoop(Scanner s) {
		if(s.hasNext("loop")){
			s.next();
			return new LoopNode(parseBlock(s)); 
		}
		fail("Can't parse loop", s); 
		return null; 
	}
	static BlockNode parseBlock(Scanner s){ 
		require(OPENBRACE, "needs: {", s);
		ArrayList<STMTNode> allStatements = new ArrayList<STMTNode>();
		if(s.hasNext()){
			while(!s.hasNext(CLOSEBRACE)){
				allStatements.add(parseStatement(s));
			}
			BlockNode blocknode = new BlockNode(allStatements); 
			require(CLOSEBRACE, "needs: }",s);
			return blocknode;
		} else {
			fail("block is empty", s);
		}
		return new BlockNode(allStatements);
	}
	//Stage 1
	//While 
	//If 
	//Conditional
	static ConditionNode parseCond(Scanner s){ //example: greaterThan, fuelLeft, ANumber 
		String relop; 
		ExpNode sensor;
		ExpNode num;
		relop = s.next();
		require(OPENPAREN, "Needs: (", s);
		sensor = parseSensor(s);
		require(COMMA, "Needs: ,", s);
		num = parseNum(s);
		require(CLOSEPAREN, "Needs: )", s);
		return new CondNode(relop, sensor, num);
	}
	static ExpNode parseNum(Scanner s){
		if(s.hasNext(NUM)){
			return new NumNode(s.nextInt());
		} else {
			fail("Needs Numbers", s);
		}
		return null;
	}
	static ExpNode parseSensor(Scanner s){
		if(s.hasNext(SEN)){
			return new SensNode(s.next());
		} else {
			fail("Needs Sensors", s);
		}
		return null;
	}
	static RobotProgramNode parseIf(Scanner s){
		ConditionNode cond = null;
		require(IF, "Needs If", s); //needs to have If and bracket (can see this in sample text)
		require(OPENPAREN, "Needs (", s);
		if(s.hasNext(COND)){ //if we find a condition 
			cond = parseCond(s);
		} else {
			fail("No condition", s);
		}
		require(CLOSEPAREN, "Needs )", s);
		return new IfNode(cond); 
	}
	static RobotProgramNode parseWhile(Scanner s){
		ConditionNode cond = null;
		require(WHILE, "Needs While", s);
		require(OPENPAREN, "Needs (", s);
		if(s.hasNext(COND)){ //if we find a condition 
			cond = parseCond(s);
		} else {
			fail("No condition", s);
		}
		require(CLOSEPAREN, "expected ')'",s);
		return new WhileNode(cond);
	}

	/**
	 * Report a failure in the parser.
	 */
	static void fail(String message, Scanner s) {
		String msg = message + "\n   @ ...";
		for (int i = 0; i < 5 && s.hasNext(); i++) {
			msg += " " + s.next();
		}
		throw new ParserFailureException(msg + "...");
	}

	/**
	 * Requires that the next token matches a pattern if it matches, it consumes
	 * and returns the token, if not, it throws an exception with an error
	 * message
	 */
	static String require(String p, String message, Scanner s) {
		if (s.hasNext(p)) {
			return s.next();
		}
		fail(message, s);
		return null;
	}

	static String require(Pattern p, String message, Scanner s) {
		if (s.hasNext(p)) {
			return s.next();
		}
		fail(message, s);
		return null;
	}

	/**
	 * Requires that the next token matches a pattern (which should only match a
	 * number) if it matches, it consumes and returns the token as an integer if
	 * not, it throws an exception with an error message
	 */
	static int requireInt(String p, String message, Scanner s) {
		if (s.hasNext(p) && s.hasNextInt()) {
			return s.nextInt();
		}
		fail(message, s);
		return -1;
	}

	static int requireInt(Pattern p, String message, Scanner s) {
		if (s.hasNext(p) && s.hasNextInt()) {
			return s.nextInt();
		}
		fail(message, s);
		return -1;
	}

	/**
	 * Checks whether the next token in the scanner matches the specified
	 * pattern, if so, consumes the token and return true. Otherwise returns
	 * false without consuming anything.
	 */
	static boolean checkFor(String p, Scanner s) {
		if (s.hasNext(p)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}

	static boolean checkFor(Pattern p, Scanner s) {
		if (s.hasNext(p)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}

}

// You could add the node classes here, as long as they are not declared public (or private)
//ADD ALL CLASS FOR ALL THE NODE TYPES THAT MAKE UP THE ABSTRACT SYNTAX TREE (Note: Used tutors help)
//Logic: Program(nodes){Statements(actions){}, Conditions{Blocks{Statements{Num{}, Relop{}, Exp{}, Sen{}}}}}
//ONES WE NEED: Block node, Exp node, If Node, Loop Node, Num Node, Prog Node, STMTNode, While Node
//Actions: Move node, Shield off node, Sheild on node, Take fuel node, Turn around node, Turn left node, Turn right node, Wait node 
//Conditionals: And node, Conditional Node, Equal node, Greater than node, Less than node, Note node, Or node 
//Operators: Addition node, Division node, Multiplication node, Substraction node
//Sensors: Barrel Front-behind Node, Barrel left-right Node, Fuel-left node, Number of Barrels node, Opponent front-back node, Opponent right-left node, Wall Distance node
//Stage 0 - complete (40%)
class ProgNode implements RobotProgramNode{ //main program node: root 
	ArrayList<STMTNode> allStatements = new ArrayList<>();
	public ProgNode(ArrayList<STMTNode> allStatements){ //Root node with all nodes in program 
		this.allStatements = allStatements;
	}
	@Override 
	public void execute(Robot robot){
		for(STMTNode s : allStatements){ //go through all children
			s.execute(robot);
		}
	}
	public void addNode(STMTNode n){
		allStatements.add(n);
	}
	public void removeNode(STMTNode n){
		allStatements.remove(n);
	}
	public String toString(){ //all classes need toString method 
		String result = "";
		for(STMTNode n : allStatements){
			result = result + " "+n;
		}
		return result;
	}
}
class STMTNode implements RobotProgramNode{ //expressions:leaf nodes, statments:subtrees
	RobotProgramNode statement; //supports all subclasses (can have multiple kinds of statements)
	public STMTNode(RobotProgramNode rpn){ 
		statement = rpn;
	}
	public STMTNode(LoopNode loopNode){ //constructor for loop statements 
		statement = loopNode;
	}
	@Override
	public void execute(Robot robot) {//perform on the robot
		statement.execute(robot);
	}
	public String toString() {
		return statement.toString();
	}
}
class LoopNode implements RobotProgramNode{ //loop node: parent of block node 
	BlockNode blockNode;
	public LoopNode(BlockNode blockNode){ //inside a loop lies a block 
		this.blockNode = blockNode;
	}
	@Override
	public void execute(Robot robot) {
		while(true){ //run all children under the blockNode 
			blockNode.execute(robot);
		}
	}
	public String toString() {
		return "Loop statement"; 
	}
}
class BlockNode implements RobotProgramNode{
	ArrayList<STMTNode> allStatements;
	public BlockNode(ArrayList<STMTNode> allStatements) { //inside a block lies statements 
		this.allStatements = allStatements;
	}
	@Override
	public void execute(Robot robot) {
		for (STMTNode state : allStatements) {
			state.execute(robot);
		}
	}
	public void addSTMTNode(STMTNode n) {
		allStatements.add(n);
	}
	public void removeSTMTNode(STMTNode n){
		allStatements.remove(n);
	}
	public ArrayList<STMTNode> getAllStatements(){
		return allStatements;
	} 
	public String toString() {
		String result = "";
		for(STMTNode s : allStatements){
			result = result + s;
		}
		return result; 
	}
}
interface ExpNode { //interface for all actions 
	public int evaluate(Robot robot);
}
class MoveNode implements RobotProgramNode{
	ExpNode expNode; 
	public MoveNode(){ }
	@Override
	public void execute(Robot robot) { 
		if(expNode == null){
			robot.move(); 
		} else {
			int numOfMoves = expNode.evaluate(robot);
			for(int i = 0; i < numOfMoves; i++){
				robot.move();
			}
		}
	}
	public void setExpNode(ExpNode expNode) {
		this.expNode = expNode;
	}
	public ExpNode getExpNode(){
		return expNode;
	}
	public String toString() {
		return "Moving Node";
	}
}
class TurnLNode implements RobotProgramNode {
	public TurnLNode() { }
	@Override
	public void execute(Robot robot) {
		robot.turnLeft(); 
	}
	public String toString() {
		return "Turning left node";
	}
}
class TurnRNode implements RobotProgramNode {
	public TurnRNode() { }
	@Override
	public void execute(Robot robot) {
		robot.turnRight(); 
	}
	public String toString() {
		return "Turning right node";
	}
}
class TakeFuelnode implements RobotProgramNode {
	public TakeFuelnode() { }
	@Override
	public void execute(Robot robot) {
		robot.takeFuel();
	}
	public String toString() {
		return "Taking fuel node";
	}
}
class WaitNode implements RobotProgramNode {
	ExpNode expNode; 
	public WaitNode() { }
	@Override
	public void execute(Robot robot) {
		robot.idleWait();
	}
	public void setExpNode(ExpNode expNode) {
		this.expNode = expNode;
	}
	public ExpNode getExpNode(){
		return expNode;
	}
	public String toString() {
		return "Waiting Node";
	}
}
//Stage 1 - 
//need class for (IF, WHILE, COND, and SEN rules) - individual (total 4 classes)
//need class for (less than, greater than, and equal) - condition
//need class for ("fuelLeft" | "oppLR" | "oppFB" | "numBarrels" | "barrelLR" | "barrelFB" | "wallDist") - sensor
//sensor node: uses enum for each kind of action, use case switch
//cond node: less than, greater than, and equal operators in evaluate 
//relop inside condition
interface ConditionNode { //needed for making If and While work
	public boolean evaluate(Robot robot);
}
class IfNode implements RobotProgramNode {
	ConditionNode condNode;
	BlockNode ifNode;
	BlockNode elseNode;
	public IfNode(ConditionNode condNode){
		this.condNode = condNode;
	}
	@Override
	public void execute(Robot robot) {
		if (condNode.evaluate(robot)) {
			ifNode.execute(robot);
		} else {
			elseNode.execute(robot);
		}
	}
	public String toString(){
		return "If/Else: "+condNode.toString();
	}
}
class WhileNode implements RobotProgramNode{
	ConditionNode condNode; //a while loop contains condition and block of code
	BlockNode blockNode;
	public WhileNode(ConditionNode condNode){
		this.condNode = condNode;
	}
	@Override
	public void execute(Robot robot) {
		while(condNode.evaluate(robot)){
			blockNode.execute(robot);
		}
	}
	public String toString(){
		return "While: "+condNode.toString();
	}
}
class CondNode implements ConditionNode{
	String relop;
	ExpNode sensor;
	ExpNode num;
	public CondNode(String relop, ExpNode sensor, ExpNode num){
		this.relop = relop;
		this.sensor = sensor;
		this.num = num;
	}
	@Override 
	public String toString(){
		return relop+" "+sensor+" "+num;
	}
	public boolean evaluate(Robot robot){ 
		if(relop.equals("lt")){ //relop returns less than
			return sensor.evaluate(robot)<num.evaluate(robot);
		} else if (relop.equals("gt")){ //relop returns greater than
			return sensor.evaluate(robot)>num.evaluate(robot);
		} else if (relop.equals("eq")){ //equals
			return sensor.evaluate(robot)==num.evaluate(robot);
		} else {
			return false;
		}
	}
}
class SensNode implements ExpNode {
	String sensor = "";
	SensNode(String s){
		sensor = s;
	}
	@Override
	public String toString(){
		return sensor.toString();
	}
	public int evaluate(Robot robot){ 
		if(sensor == "fuelLeft"){
			return robot.getFuel();
		} else if (sensor == "oppLR"){
			return robot.getOpponentLR();
		} else if (sensor == "oppFB"){
			return robot.getOpponentFB();
		} else if (sensor == "numBarrels"){
			return robot.numBarrels();
		} else if (sensor == "barrelLR"){
			return robot.getClosestBarrelLR();
		} else if (sensor == "barrelFB"){
			return robot.getClosestBarrelFB();
		} else if (sensor == "wallDist"){
			return robot.getDistanceToWall();
		} else {
			return 0;
		}
	}
}
class NumNode implements ExpNode {
	int num;
	public NumNode(int num){
		this.num = num;
	}
	@Override
	public int evaluate(Robot robot) {
		return num;
	}
	public String toString(){
		return num+"";
	}
}







