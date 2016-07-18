package main.java.suggestion;

import java.util.List;

import main.java.suggestion.Suggestion.SuggestionItem;

public class SuggestionMgr {
	
	Suggestion suggestion;
	
	public SuggestionMgr(){
		
	}	
	
	public Suggestion getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(Suggestion suggestion) {
		this.suggestion = suggestion;
	}

	public void setSuggestion(EnumOp type) {
		switch(type){
		case Copy:
			suggestion = new CopySuggestion();
			break;
		case Cut:
			suggestion = new CutSuggestion();
			break;
		case Delete:
			suggestion = new DeleteSuggestion();
			break;
		case Fill:
			suggestion = new FillSuggestion();
			break;
		case Fold:
			suggestion = new FoldSuggestion();
			break;
		case Merge:
			suggestion = new MergeSuggestion();
			break;
		case Split:
			suggestion = new SplitSuggestion();
			break;
		case Other:
			suggestion = new OtherSuggestion();
			break;
		default:
			throw new IllegalArgumentException("Incorrect suggestion type");	
		}
	}
	
	public void setSuggestion(String type) {
		switch(type){
		case "Copy":
			suggestion = new CopySuggestion();
			break;
		case "Cut":
			suggestion = new CutSuggestion();
			break;
		case "Delete":
			suggestion = new DeleteSuggestion();
			break;
		case "Fill":
			suggestion = new FillSuggestion();
			break;
		case "Fold":
			suggestion = new FoldSuggestion();
			break;
		case "Merge":
			suggestion = new MergeSuggestion();
			break;
		case "Split":
			suggestion = new SplitSuggestion();
			break;
		case "Other":
			suggestion = new OtherSuggestion();
			break;
		default:
			throw new IllegalArgumentException("Incorrect suggestion type");	
		}
	}
	
	public List<SuggestionItem> getSuggestion(String[] selectedRowData, String[] selectedColumnData, Selection selection, String [] columnhead){
		
		return suggestion.generateSuggestion(selectedRowData, selectedColumnData, selection, columnhead);
	}
	
	public void parseSuggestion(EnumPredict predictType){
		ProbabilityFile.increaseProbability(predictType);
	}
}
