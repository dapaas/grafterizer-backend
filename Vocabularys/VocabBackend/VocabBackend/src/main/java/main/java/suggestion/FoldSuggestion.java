package main.java.suggestion;

import java.util.ArrayList;
import java.util.List;

public class FoldSuggestion extends Suggestion{
	
	@Override
	List<SuggestionItem> generateSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		List<SuggestionItem> oplist = new ArrayList<SuggestionItem>();
		
		String opStr = "";
		// fold using <row index> as a key.
		if(selection.getType() == EnumType.rowSingle){
			opStr = "Fold using " + selection.getSelectedRow() + " as a key";
			Parameters p = getParameter("fold", false, null, 
					null, selection.getSelectedRows(), null);
			AddItem(oplist, opStr, EnumPredict.SingleRowFoldUsingRow, p);
			
			//fold <column head> using <column index> as a key.
			int index = 0;
			while(index < columnhead.length){
				opStr = "Fold " + "\"" + columnhead[index] + "\"" + " using " + selection.getSelectedRow() + " as a key";
				p = getParameter("fold", false, columnhead[index], 
						null, selection.getSelectedRows(), null);
				AddItem(oplist, opStr, EnumPredict.SingleRowFoldUsingColumn, p);
				index++;
			}
		}
		
		// fold using <row index 1>, <row index 2>... as a key
		if(selection.getType() == EnumType.rowMulti){
			opStr = "Fold using " + getSelectedRows(selection) + " as a key";
			Parameters p = getParameter("fold", false, null, 
					null, selection.getSelectedRows(), null);
			AddItem(oplist, opStr, EnumPredict.MultiRowFoldUsingRow, p);
			int index = 0;
			// fold <column head> using <row index 1>, <row index 2>... as a key
			while(index < columnhead.length){
				opStr = "Fold " + "\"" + columnhead[index] + "\"" + " using " + getSelectedRows(selection) + " as a key";
				p = getParameter("fold", false, columnhead[index], 
						null, selection.getSelectedRows(), null);
				AddItem(oplist, opStr, EnumPredict.MultiRowFoldUsingColumn, p);
				index++;
			}
		}
		
		
		if(selection.getType() == EnumType.colSingle){
			// fold <column head> using header as a key.
			opStr = "Fold " + "\"" + columnhead[selection.getSelectedColumn()] + "\"" + " using header as a key";
			Parameters p = getParameter("fold", false, columnhead[selection.getSelectedColumn()], 
					null, null, null);
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, p);
			
			// fold <column head> using 1 as a key.
			opStr = "Fold " + "\"" + columnhead[selection.getSelectedColumn()] + "\"" + " using 1 as a key";
			Integer [] array = {1};
			p = getParameter("fold", false, columnhead[selection.getSelectedColumn()], 
					null, array, null);
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, p);
			
			// fold <column head> using 1,2 as keys.
			Integer [] twoRowArray = {1, 2};
			p = getParameter("fold", false, columnhead[selection.getSelectedColumn()], 
					null, twoRowArray, null);
			opStr = "Fold " + "\"" + columnhead[selection.getSelectedColumn()] + "\"" + " using 1,2 as keys";
			
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, p);
			
			// fold <column head> using 1,2,3 as keys.
			Integer [] threeRowArray = {1, 2, 3};
			p = getParameter("fold", false, columnhead[selection.getSelectedColumn()], 
					null, threeRowArray, null);
			opStr = "Fold " + "\"" + columnhead[selection.getSelectedColumn()] + "\"" + " using 1,2,3 as keys";
			
			AddItem(oplist, opStr, EnumPredict.SingleColumnFoldBasic, p);
		}
		
		if(selection.getType() == EnumType.colMulti){
			// fold <column head 1>, <column head 2>... using header as a key
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header as a key";
			Parameters p = getParameter("fold", false, null, 
					null, null, selection.getSelectedColumns());
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, p);
			
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header 1 as a key";
			Integer [] array = {1};
			p = getParameter("fold", false, null, 
					null, array, selection.getSelectedColumns());
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, p);
			
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header 1,2 as keys";
			Integer [] twoRowArray = {1, 2};
			p = getParameter("fold", false, null, 
					null, twoRowArray, selection.getSelectedColumns());
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, p);
			
			opStr = "Fold " + getSelectedColumns(selection, columnhead) + " using header 1,2,3 as keys";
			Integer [] threeRowArray = {1, 2, 3};
			p = getParameter("fold", false, null, 
					null, threeRowArray, selection.getSelectedColumns());
			AddItem(oplist, opStr, EnumPredict.MultiColumnFoldBasic, p);
		}
		
		return oplist;
	}
}
