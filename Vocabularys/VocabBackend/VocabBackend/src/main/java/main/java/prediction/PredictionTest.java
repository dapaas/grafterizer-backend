package main.java.prediction;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PredictionTest {
	
	private Prediction p = new Prediction();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseOperation() {
		Selection s = new Selection();
		s.setSelectedColumn(1);
		s.setSelectedRow(1);
		List<Integer> lcolumn = new ArrayList<Integer>();
		lcolumn.add(1);
		lcolumn.add(2);
		s.selectedColumns = lcolumn;
		
		List<Integer> lrow = new ArrayList<Integer>();
		lrow.add(1);
		lrow.add(2);
		s.selectedRows = lrow;
		
		//p.parseOperation("Delete rows where column1 = '123'", s);
		fail("Not yet implemented");
	}

	@Test
	public void testIsRowEmpty() {
		fail("Not yet implemented");
	}

	@Test
	public void testCopyOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testCutOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testFillOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testFoldOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testMergeOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testSplitOp() {
		fail("Not yet implemented");
	}

	@Test
	public void testGenerateOperations() {
		String [][] data = {
				{"2004", "4029.3"},
				{"2005", "3900"},
				{"2006", "3937"},
				{"2007", "3934.9"},
				{"2008", "4081.9"}
		};
		
		String [] columnHead = {
				"year",
				"rate"
		};
		
		int selectedRow = 2;
		int selectedColumn = 1;
		
		int [] selectedRows = {1,2};
		int [] selectedColumns = {0,1};
		
		Selection s = new Selection();
		
		s.setSelectedRow(1);
		s.setType(EnumType.rowSingle);
		
		p.generateOperations(data, s, columnHead);
		
	}

}
