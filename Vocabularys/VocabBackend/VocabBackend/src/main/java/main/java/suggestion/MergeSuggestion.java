package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class MergeSuggestion extends Suggestion{
	
	@Override
	String parseSuggestion(String strSuggestion, EnumPredict predictType){
		String [] l = strSuggestion.split(" ");
		switch(predictType){
		case SingleRowMergeBasic:
			String column = l[2];
			ProbabilityFile.increaseSingleRowProbability(predictType);
			break;
		case MultiRowMergeBasic:
			column = l[2];
			ProbabilityFile.increaseMultiRowProbability(predictType);
			break;
		case SingleColumnMergeBasic:
			ProbabilityFile.increaseSingleColumnProbability(predictType);
			break;
		case MultiColumnMergeBasic:
			ProbabilityFile.increaseMultiColumnProbability(predictType);
			break;
		default:
			break;
		}
		return "";
	}
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			opStr = "Merge " + selection.getSelectedRow();
			AddItem(oplist, opStr, EnumPredict.SingleRowMergeBasic);
		}
		
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Merge " + getSelectedRows(selection);
			AddItem(oplist, opStr, EnumPredict.MultiRowMergeBasic);
		}
		
		if(selection.getType() == EnumType.colSingle){
			opStr = "Merge " + columnhead[selection.getSelectedColumn()];
			AddItem(oplist, opStr, EnumPredict.SingleColumnMergeBasic);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Merge " + getSelectedColumns(selection, columnhead);
			AddItem(oplist, opStr, EnumPredict.MultiColumnMergeBasic);
		}
		
		return oplist;
	}
}
