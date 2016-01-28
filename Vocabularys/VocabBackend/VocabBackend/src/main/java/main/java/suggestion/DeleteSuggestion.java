package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class DeleteSuggestion extends Suggestion{
	
	@Override
	String parseSuggestion(String strSuggestion, EnumPredict predictType){
		String [] l = strSuggestion.split(" ");
		switch(predictType){
		case SingleRowDeleteEmpty:
			ProbabilityFile.increaseSingleRowProbability(predictType);
			String column = l[1];
			
			break;
		case SingleRowDeleteBasic:
			column = l[2];
			ProbabilityFile.increaseSingleRowProbability(predictType);
			break;
		case MultiRowDeleteBasic:
			ProbabilityFile.increaseMultiRowProbability(predictType);
			break;
		case SingleColumnDeleteBasic:
			ProbabilityFile.increaseSingleColumnProbability(predictType);
			column = l[1];
		case MultiColumnDeleteBasic:
			ProbabilityFile.increaseMultiColumnProbability(predictType);
			column = l[1];
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
			int index = 0;
			
			//delete empty rows
			if(isRowEmpty(selectedRowData, selection.getSelectedRow())){
				opStr = "Delete empty rows ";
				AddItem(oplist, opStr, EnumPredict.SingleRowDeleteEmpty);
			}
			
			//delete rows where column1 = "data in column1"
			while(index < columnhead.length){
				opStr = "Delete rows where " + columnhead[index] + " = " + selectedRowData[index];
				AddItem(oplist, opStr, EnumPredict.SingleRowDeleteBasic);
				index++;
			}
		}
		
		//delete rows 1,2
		if(selection.getType() == EnumType.rowMulti){
			
			opStr = "Delete rows " + getSelectedRows(selection);
			AddItem(oplist, opStr, EnumPredict.MultiRowDeleteBasic);
		}
		
		//delete column1
		if(selection.getType() == EnumType.colSingle){
			opStr = "Delete " + columnhead[selection.getSelectedColumn()];
			AddItem(oplist, opStr, EnumPredict.SingleColumnDeleteBasic);
		}
		
		//delete column1,column2
		if(selection.getType() == EnumType.colMulti){
			
			opStr = "Delete " + getSelectedColumns(selection, columnhead);
			AddItem(oplist, opStr, EnumPredict.MultiColumnDeleteBasic);
		}
		
		//delete rows where column1 contains selectedstring
		if(selection.getType() == EnumType.Text){
			opStr = "Delete rows where " + columnhead[selection.getTextcorY()] + " contains " + selection.getSelectedText();
		}
		
		return oplist;
	}
}
