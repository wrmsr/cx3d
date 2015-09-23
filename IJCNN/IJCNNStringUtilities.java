package ini.cx3d.simulations.IJCNN;



/**
 * Some utility methods, for helping the parsing, and selecting
 * sub-strings in a string.
 * 
 * @author fredericzubler
 *
 */
public class IJCNNStringUtilities {
	
	

	//---------------------------------------------------
	//	Selecting the instruction strings for the daughter branches
	//---------------------------------------------------
	
	/** Returns the Instruction String for side branches. */
	public static String stringSideBranch(String instruction) {
		int lengthOfTheString = instruction.length();
		for (int index = 0; index < lengthOfTheString; index++) {
			char c = instruction.charAt(index);
			if(c == '['){
				int indexOfClosedBracket = findCorrespondingSquareBracket(instruction, index);
				String sideBranchInstruction = instruction.substring(index+1, indexOfClosedBracket);
				if(sideBranchInstruction.equals("_")){
					return instruction;
				}else{
					return sideBranchInstruction;
				}
			}
		}
		return null;
	}
	

	/** Returns the Instruction String for the daughter left branch . */
	public static String stringDaughterLeft(String instruction) {
		int lengthOfTheString = instruction.length();
		for (int index = 0; index < lengthOfTheString; index++) {
			char c = instruction.charAt(index);
			if(c == '['){
				int indexOfClosedBracket = findCorrespondingSquareBracket(instruction, index);
				index = indexOfClosedBracket+1;
				c = instruction.charAt(index);
			}
			if(c == '{'){
				int indexOfClosedBracket = findCorrespondingCurlyBracket(instruction, index);
				String FirstDaughterBranchInstruction = instruction.substring(index+1, indexOfClosedBracket);
				if(FirstDaughterBranchInstruction.equals("_")){
					System.out.println("*");
					return instruction;
				}else{
					System.out.println("**");
					return FirstDaughterBranchInstruction;
				}
			}
		}
		return "";
	}
	

	/** Returns the Instruction String for the daughter right branch . */
	public static  String stringDaughterRight(String instruction) {
		int lengthOfTheString = instruction.length();
		for (int index = 0; index < lengthOfTheString; index++) {
			char c = instruction.charAt(index); 
			if(c == '['){
				int indexOfClosedBracket = findCorrespondingSquareBracket(instruction, index);
				index = indexOfClosedBracket+1;
				c = instruction.charAt(index);
			}
			if(c == '{'){
				int indexOfClosedBracket = findCorrespondingCurlyBracket(instruction, index);
				index = indexOfClosedBracket + 1;
				indexOfClosedBracket = findCorrespondingCurlyBracket(instruction, index);
				String SecondDaughterBranchInstruction = instruction.substring(index+1, indexOfClosedBracket);
				if(SecondDaughterBranchInstruction.equals("_")){
					return instruction;
				}else{
					return SecondDaughterBranchInstruction;
				}
			}
		}
		return "";
	}
	
	
	// --------------------------------------------------------------------
	// Find closing (), [] or {}
	// --------------------------------------------------------------------
	
	/**
	 * Finds the closing curly bracket '}' closing the opening '{' at position "index"
	 * @param instructionString the String in which we look
	 * @param index the position of the {
	 * @return index of the closing }
	 */
	public static int findCorrespondingCurlyBracket(String instructionString, int index){
		int lengthOfTheString = instructionString.length();
		int level = 0; // sub brackets that can be opened
		for (index++ ; index < lengthOfTheString; index++) {
			char c = instructionString.charAt(index);
			if(c == '}'){ 				// there's a closing bracket :
				if(level == 0){ 		// if it is at the same level, we have what we want
					return index;
				}else{					// otherwise we update the level
					level +=1 ;
				}
			}else if(c == '{'){			// if it is another opening one, we close also change the level
				level -= 1;
			}
		}
		return -1;
	}
	
	
	/**
	 * Finds the closing square bracket ] closing the opening one [ at position index
	 * @param instructionString the String in which we look
	 * @param index the position of the [
	 * @return index of the closing ]
	 */
	public static int findCorrespondingSquareBracket(String instructionString, int index){
		int lengthOfTheString = instructionString.length();
		int level = 0; // sub brackets that can be opened
		for (index++ ; index < lengthOfTheString; index++) {
			char c = instructionString.charAt(index);
			if(c == ']'){ 				// there's a closing bracket :
				if(level == 0){ 		// if it is at the same level, we have what we want
					return index;
				}else{					// otherwise we update the level
					level +=1 ;
				}
			}else if(c == '['){			// if it is another opening one, we close also change the level
				level -= 1;
			}
		}
		return -1;
	}
	
	/**
	 * Finds the closing parenthesis ) closing the opening one ( at position index
	 * @param instructionString the String in which we look
	 * @param index the position of the (
	 * @return index of the closing )
	 */
	public static int findCorrespondingParenthesis(String instructionString, int index){
		int lengthOfTheString = instructionString.length();
		int level = 0; // sub brackets that can be opened
		for (index++ ; index < lengthOfTheString; index++) {
			char c = instructionString.charAt(index);
			if(c == ')'){ 				// there's a closing bracket :
				if(level == 0){ 		// if it is at the same level, we have what we want
					return index;
				}else{					// otherwise we update the level
					level +=1 ;
				}
			}else if(c == '('){			// if it is another opening one, we close also change the level
				level -= 1;
			}
		}
		return -1;
	}
}
