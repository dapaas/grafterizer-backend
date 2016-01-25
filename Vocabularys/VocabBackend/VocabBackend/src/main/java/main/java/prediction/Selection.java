package main.java.prediction;

import java.util.ArrayList;
import java.util.List;

public class Selection {
	EnumType type;
	Integer selectedRow = 0;
	Integer selectedColumn = 0;
	List<Integer> selectedRows = new ArrayList<Integer>();
	List<Integer> selectedColumns = new ArrayList<Integer>();
	String selectedText;
	Integer TextcorX = 0;
	Integer TextcorY = 0;
	
	public EnumType getType() {
		return type;
	}
	public void setType(EnumType type) {
		this.type = type;
	}
	public Integer getSelectedRow() {
		return selectedRow;
	}
	public void setSelectedRow(Integer row) {
		this.selectedRow = row;
	}
	public Integer getSelectedColumn() {
		return selectedRow;
	}
	public void setSelectedColumn(Integer column) {
		this.selectedRow = column;
	}
	public List<Integer> getSelectedRows() {
		return selectedRows;
	}
	public void setSelectedRows(List<Integer> selectedRows) {
		this.selectedRows = selectedRows;
	}
	public List<Integer> getSelectedColumns() {
		return selectedColumns;
	}
	public void setSelectedColumns(List<Integer> selectedColumns) {
		this.selectedColumns = selectedColumns;
	}
	public String getSelectedText() {
		return selectedText;
	}
	public void setSelectedText(String selectedText) {
		this.selectedText = selectedText;
	}
	public Integer getTextcorX() {
		return TextcorX;
	}
	public void setTextcorX(Integer textcorX) {
		TextcorX = textcorX;
	}
	public Integer getTextcorY() {
		return TextcorY;
	}
	public void setTextcorY(Integer textcorY) {
		TextcorY = textcorY;
	}
	
}
