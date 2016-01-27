package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class CutSuggestion extends Suggestion{
	
	@Override
	String parseSuggestion(String strSuggestion, EnumPredict predictType){
		String [] l = strSuggestion.split(" ");
		switch(predictType){
		case SingleColumnCutBasic:
			String column = l[1];
			ProbabilityFile.increaseSingleColumnProbability(predictType);
			
			//return closure code 
			
			break;
		case MultiColumnCutBasic:
			column = l[1];
			ProbabilityFile.increaseMultiColumnProbability(predictType);
			
			//return closure code 
			
			break;
		default:
			break;
		}
		return "";
	}
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] tData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.colSingle){
			opStr = "Cut from " + columnhead[selection.getSelectedRow()];
			AddItem(oplist, opStr, EnumPredict.SingleColumnCutBasic);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Cut from " + getSelectedColumns(selection, columnhead);
			AddItem(oplist, opStr, EnumPredict.MultiColumnCutBasic);
		}
		
		return oplist;
	}
}
