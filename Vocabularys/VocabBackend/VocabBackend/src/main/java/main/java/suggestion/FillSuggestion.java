package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

import main.java.prediction.EnumPredict;
import main.java.prediction.EnumType;
import main.java.prediction.ProbabilityFile;
import main.java.prediction.Selection;

public class FillSuggestion extends Suggestion{
	
	@Override
	String parseSuggestion(String strSuggestion, EnumPredict predictType){
		String [] l = strSuggestion.split(" ");
		switch(predictType){
		case SingleRowFillEmpty:
			ProbabilityFile.increaseSingleRowProbability(predictType);
			break;
		case SingleRowFillBasic:
			ProbabilityFile.increaseSingleRowProbability(predictType);
			break;
		case SingleRowFillAll:
			ProbabilityFile.increaseSingleRowProbability(predictType);
			break;
		case MultiRowFillBasic:
			ProbabilityFile.increaseMultiRowProbability(predictType);
			break;
		case SingleColumnFillBasic:
			ProbabilityFile.increaseSingleColumnProbability(predictType);
			String column = l[1];
			break;
		case MultiColumnFillBasic:
			ProbabilityFile.increaseMultiColumnProbability(predictType);
			column = l[1];
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
			//fill empty rows with value from ...
			if(isRowEmpty(tData, selection.getSelectedRow())){
				opStr = "Fill empty rows with value from left";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty);
				opStr = "Fill empty rows with value from below";	
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty);
				opStr = "Fill empty rows with value from above";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillEmpty);
			}
			
			//fill row X with value from ...
			opStr = "Fill row " + selection.getSelectedRow() + " with value from left";		
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic);
			opStr = "Fill row " + selection.getSelectedRow()  + " with value from below";
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic);
			opStr = "Fill row " + selection.getSelectedRow()  + " with value from above";
			AddItem(oplist, opStr, EnumPredict.SingleRowFillBasic);
			
			//fill row where column1='***' with value from left
			int index = 0;
			while(index < columnhead.length){
				opStr = "Fill rows where " + columnhead[index] + " = " + tData[index] + " with value from left";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll);
				opStr = "Fill rows where " + columnhead[index] + " = " + tData[index] + " with value from above";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll);
				opStr = "Fill rows where " + columnhead[index] + " = " + tData[index] + " with value from below";
				AddItem(oplist, opStr, EnumPredict.SingleRowFillAll);
				index++;
			}
		}
		
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Fill rows" + getSelectedColumns(selection, columnhead) + " with value from left";	
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic);
			opStr = "Fill rows" + getSelectedColumns(selection, columnhead) + " with value from below";	
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic);
			opStr = "Fill rows" + getSelectedColumns(selection, columnhead) + " with value from above";
			AddItem(oplist, opStr, EnumPredict.MultiRowFillBasic);
		}
		
		if(selection.getType() == EnumType.colSingle){
			opStr = "Fill " + columnhead[selection.getSelectedColumn()] + " with value from left";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic);
			opStr = "Fill " + columnhead[selection.getSelectedColumn()] + " with value from above";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic);
			opStr = "Fill " + columnhead[selection.getSelectedColumn()] + " with value from below";
			AddItem(oplist, opStr, EnumPredict.SingleColumnFillBasic);
		}
		
		if(selection.getType() == EnumType.colMulti){
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from left";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic);
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from above";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic);
			opStr = "Fill " + getSelectedColumns(selection, columnhead) + " with value from below";
			AddItem(oplist, opStr, EnumPredict.MultiColumnFillBasic);
		}

		return oplist;
	}
}
