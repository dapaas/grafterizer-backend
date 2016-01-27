package main.java.prediction;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import main.java.prediction.Prediction.PredictionProbability;

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
		Integer[] lcolumn = {0, 1};
		s.selectedColumns = lcolumn;
		
		Integer[] lrow = {1, 2};
		s.selectedRows = lrow;
		
		p.parseOperation("Delete rows where column1 = '123'", EnumPredict.SingleColumnDeleteBasic);
	}

	@Test
	public void testIsRowEmpty() {
		//fail("Not yet implemented");
	}


	@Test
	public void testGenerateOperations() {
		String [][] data = {
				{"2004", "4029.3"},
				{"2005", "3900"},
				{"2006", "3937"},
				{"",""},
				{"2007", "3934.9"},
				{"2008", "4081.9"}
		};
		
		String [] columnHead = {
				"year",
				"rate"
		};
		
		//Test select row
		Selection s = new Selection();
		s.setSelectedRow(1);
		s.setType(EnumType.rowSingle);
		
		Iterator<PredictionProbability> it = p.generateOperations(data[1], s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.strOp);
		}
		
		System.out.println("------------------------------------------");
		//Test select row
		s.setSelectedRow(3);
		s.setType(EnumType.rowSingle);
				
		it = p.generateOperations(data[3], s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.strOp);
		}
				
		System.out.println("------------------------------------------");
		
		//Test select rows
		Integer[] selectedrow = {2,4};
		s.setSelectedRows(selectedrow);
		s.setType(EnumType.rowMulti);
				
		it = p.generateOperations(data[0], s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.strOp);
		}
		
		System.out.println("------------------------------------------");
		
		//Test select column
		s.setSelectedColumn(1);
		s.setType(EnumType.colSingle);
				
		it = p.generateOperations(data[0], s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.strOp);
		}
		
		System.out.println("------------------------------------------");
		
		//Test select columns
		Integer[] selectedcolumn = {2,4};
		s.setSelectedColumns(selectedcolumn);
		s.setType(EnumType.colMulti);
				
		it = p.generateOperations(data[0], s, columnHead).iterator();
		while(it.hasNext()){
			PredictionProbability p = it.next();
			System.out.println(p.strOp);
		}
	}

}
