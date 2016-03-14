package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class CutSuggestion extends Suggestion{
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.colSingle){
			opStr = "Cut from " + columnhead[selection.getSelectedRow()];
			String closure = "";
			AddItem(oplist, opStr, EnumPredict.SingleColumnCutBasic, closure);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Cut from " + getSelectedColumns(selection, columnhead);
			String closure = "";
			AddItem(oplist, opStr, EnumPredict.MultiColumnCutBasic, closure);
		}
		
		return oplist;
	}
}
