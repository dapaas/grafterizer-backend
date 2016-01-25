package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class FoldSuggestion extends Suggestion{
	
	@Override
	String parseSuggestion(String strSuggestion, EnumPredict predictType){
		String [] l = strSuggestion.split(" ");
		switch(predictType){
		case SingleRowFoldUsingRow:

			break;
		case SingleRowFoldUsingColumn:

			break;
		case MultiRowFoldUsingRow:
		case MultiRowFoldUsingColumn:
		case SingleColumnFoldBasic:
		case MultiColumnFoldBasic:
		default:
			break;
		}
		return "";
	}
	
	@Override
	List<SuggestionItem> generateSuggestion(String[][] tData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		if(selection.getType() == EnumType.rowSingle){
			opStr = "Fold using " + selection.getSelectedRow() + " as a key";
			AddItem(oplist, opStr, EnumPredict.SingleRowFoldUsingRow);
			
			int index = 0;
			while(index < columnhead.length){
				opStr = "Fold " + columnhead[index] + " using " + selection.getSelectedRow() + "as a key";
				AddItem(oplist, opStr, EnumPredict.SingleRowFoldUsingColumn);
				index++;
			}
		}
		
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Fold using " + getSelectedRows(selection) + " as a key";
			AddItem(oplist, opStr, EnumPredict.MultiRowFoldUsingRow);
			int index = 0;
			while(index < columnhead.length){
				opStr = "Fold " + columnhead[index] + " using " + getSelectedRows(selection) + "as a key";
				AddItem(oplist, opStr, EnumPredict.MultiRowFoldUsingColumn);
				index++;
			}
		}
		
		if(selection.getType() == EnumType.colSingle){
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + "using header as a key";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic);
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + "using header 1 as a key";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic);
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + "using header 1,2 as a key";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic);
			opStr = "Fold " + columnhead[selection.getSelectedColumn()] + "using header 1,2,3 as a key";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + "using header as a key";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic);
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + "using header 1 as a key";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic);
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + "using header 1,2 as a key";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic);
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + "using header 1,2,3 as a key";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic);
		}
		
		return oplist;
	}
}
