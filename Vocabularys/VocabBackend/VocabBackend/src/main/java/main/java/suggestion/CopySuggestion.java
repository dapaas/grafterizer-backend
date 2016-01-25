package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class CopySuggestion extends Suggestion {
	
	@Override
	String parseSuggestion(String strSuggestion, EnumPredict predictType){
		String [] l = strSuggestion.split(" ");
		switch(predictType){
		case SingleColumnCopyBasic:
			String column = l[2];
			ProbabilityFile.increaseSingleColumnProbability(predictType);
			
			
			//return closure code 
			
			
			break;
		case MultiColumnCopyBasic:
			column = l[2];
			ProbabilityFile.increaseMultiColumnProbability(predictType);
			
			
			//return closure code 
			
			
			break;
		default:
			break;
		}
		return "";
	}
	
	@Override
	List<SuggestionItem> generateSuggestion(String[][] tData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.colSingle){
			opStr = "Copy from " + columnhead[selection.getSelectedColumn()];
			AddItem(oplist, opStr, EnumPredict.SingleColumnCopyBasic);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Copy from " + getSelectedColumns(selection, columnhead);
			AddItem(oplist, opStr, EnumPredict.MultiColumnCopyBasic);
		}
		
		return oplist;
	}
}
