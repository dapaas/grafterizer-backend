package main.java.prediction;

public enum EnumPredict {
	SingleColumnCopyBasic,
	MultiColumnCopyBasic,
	
	SingleColumnCutBasic,
	MultiColumnCutBasic,
	
	SingleRowSplitBasic,
	MultiRowSplitBasic,
	SingleColumnSplitBasic,
	MultiColumnSplitBasic,
	
	SingleRowDeleteEmpty,   //delete empty rows
	SingleRowDeleteBasic,  //delete rows where column1 = "data in column1"
	MultiRowDeleteBasic,   //delete rows 1,2
	SingleColumnDeleteBasic,  //delete column1
	MultiColumnDeleteBasic,  //delete column1,column2
	
	SingleRowFillEmpty,
	SingleRowFillBasic,
	SingleRowFillAll,
	MultiRowFillBasic,
	SingleColumnFillBasic,
	MultiColumnFillBasic,
	
	SingleRowFoldUsingRow,
	SingleRowFoldUsingColumn,
	MultiRowFoldUsingRow,
	MultiRowFoldUsingColumn,
	SingleColumnFoldBasic,
	MultiColumnFoldBasic,
	
	SingleRowMergeBasic,
	MultiRowMergeBasic,
	SingleColumnMergeBasic,
	MultiColumnMergeBasic
}
