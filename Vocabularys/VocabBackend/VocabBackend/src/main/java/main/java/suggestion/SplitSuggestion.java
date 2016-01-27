package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class SplitSuggestion extends Suggestion{
	
	@Override
	String parseSuggestion(String strSuggestion, EnumPredict predictType){
		String [] l = strSuggestion.split(" ");
		switch(predictType){
		case SingleRowSplitBasic:
			ProbabilityFile.increaseSingleRowProbability(predictType);
			break;
		case MultiRowSplitBasic:
			ProbabilityFile.increaseMultiRowProbability(predictType);
			break;
		case SingleColumnSplitBasic:
			ProbabilityFile.increaseSingleColumnProbability(predictType);
			break;
		case MultiColumnSplitBasic:
			ProbabilityFile.increaseMultiColumnProbability(predictType);
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
		if(selection.getType() == EnumType.rowSingle){
			opStr = "Split " + selection.getSelectedRow();
			AddItem(oplist, opStr, EnumPredict.SingleRowSplitBasic);
		}
		
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Split " + getSelectedRows(selection);
			AddItem(oplist, opStr, EnumPredict.MultiRowSplitBasic);
		}
		
		if(selection.getType() == EnumType.colSingle){
			opStr = "Split " + columnhead[selection.getSelectedColumn()];
			AddItem(oplist, opStr, EnumPredict.SingleColumnSplitBasic);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Split " + getSelectedColumns(selection, columnhead);
			AddItem(oplist, opStr, EnumPredict.MultiColumnSplitBasic);
		}
		
		return oplist;
	}
}
