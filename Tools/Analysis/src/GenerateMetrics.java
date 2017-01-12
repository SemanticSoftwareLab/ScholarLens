/**
 * ScholarLens - http://www.semanticsoftware.info/scholarlens
 *
 * This file is part of the ScholarLens component.
 *
 * Copyright (c) 2016, 2017, Semantic Software Lab, http://www.semanticsoftware.info,
 * Friedrich Schiller University Jena, http://fusion.cs.uni-jena.de
 *    Rene Witte
 *    Bahar Sateli
 *    Felicitas Loeffler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 *  a class that takes exported LimeSurvey Excel files as input 
 *  and produces Excel files with the following metrics:  
 *  Precision@10, Precision@25, Precision@50, MAP, nDCG
 *   
 * @author Felicitas Loeffler
 *
 */
public class GenerateMetrics {

	/**
	 * Levels of Expertise
	 */
	private static final String NOVICE = "General";
	private static final String INTERMEDIATE = "Technical";
	private static final String EXPERT = "Research";
	private static final String IRRELEVANT = "Irrelevant";
	
	/**
	 * Level weights of Expertise - Likert Scale
	 */
	private static final int NOVICE_WEIGHT = 1;
	private static final int INTERMEDIATE_WEIGHT = 2;
	private static final int EXPERT_WEIGHT = 3;
	private static final int IRRELEVANT_WEIGHT = 0;
	
	/**
	 * column numbers for formulas (Excel cell reference)
	 * 
	 */
	private static final String RANK = "Rank";
	private static final String COMPETENCE = "Competence";
	private static final String RELEVANT = "Relevant";
	private static final String AVG_At_10 = "AvPrec@10";
	private static final String AVG_At_25 = "AvPrec@25";
	private static final String AVG_At_50 = "AvPrec@50";
	private static final String DCG = "DCG";
	private static final String IDEAL_RANK = "Ideal Rank";
	private static final String IDCG = "IDCG";
	private static final String nDCG = "nDCG";
	
	
	/**
	 * column numbers for formulas (Excel cell reference)
	 * 
	 */
	private static final int COLUMN_RANK = 0;
	private static final int COLUMN_COMPETENCE = 1;
	private static final int COLUMN_NOVICE = 2;
	private static final int COLUMN_INTERMEDIATE = 3;
	private static final int COLUMN_EXPERT = 4;
	private static final int COLUMN_IRRELEVANT = 5;
	private static final int COLUMN_RELEVANT = 6;
	private static final int COLUMN_AVG_At_10 = 7;
	private static final int COLUMN_AVG_At_25 = 8;
	private static final int COLUMN_AVG_At_50 = 9;
	private static final int COLUMN_DCG = 10;
	private static final int COLUMN_IDEAL_RANK = 11;
	private static final int COLUMN_IDCG = 12;
	private static final int COLUMN_nDCG = 13;
	
	/**
	 * path to the input files (exported LimeSurvey files in xlsx format)
	 */
	private static final String INPUT_PATH ="data";

	/**
	 * path to the output files (Excel files in xlsx format)
	 */
	private static final String OUTPUT_PATH ="result";
	
	/**
	 * threshold for relevant ratings - what ratings should be considered as relevant?
	 * 0 - irrelevant
	 * 1 - general knowledge (e.g., broad terms such as 'Number')
	 * 2 - technical knowledge 
	 * 3 - expert knowledge
	 * 
	 * default value is 0, so all ratings greater than 0 are considered as relevant
	 * 
	 */
	private static int THRESHOLD = 0;
	
	public static void main(String[] args) throws Exception {
		
		parseCommandLine(args);
		
		//System.out.println("Threshold Cell Reference: "+THRESHOLD);
				
		File dir = new File(INPUT_PATH);
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		      // Do something with child
		    	//the original LimeSurvey file
				
		    
	
		try{
			
			//the original file from LimeSurvey
			OPCPackage pkg = OPCPackage.open(child);
			
			//OPCPackage pkg = OPCPackage.open(new File("resultsR2_3.xlsx"));
			XSSFWorkbook wb = new XSSFWorkbook(pkg);
			
			//create a new workbook for the formatted new output file
			XSSFWorkbook wbPR = new XSSFWorkbook();
			
			//create a new sheet in the new workbook
			Sheet prec_recall = wbPR.createSheet("Prec_Recall");
			
			//start writing into the file from the left upper corner row=0, column=0
			int rowNumPR= 0;
			
			
		    //create a new first Row
		    Row rowPR = prec_recall.getRow(rowNumPR);
		    Cell columnPR = null;
		    
            if(rowPR == null){
            	rowPR = prec_recall.createRow(rowNumPR);
            	
            	//create header columns 
                
            	//0-RANK
            	columnPR = rowPR.createCell(COLUMN_RANK, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(RANK);
               //1-COMPETENCE
            	columnPR = rowPR.createCell(COLUMN_COMPETENCE, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(COMPETENCE);
              //2-NOVICE
            	columnPR = rowPR.createCell(COLUMN_NOVICE, Cell.CELL_TYPE_NUMERIC);
                columnPR.setCellValue(NOVICE_WEIGHT);
              //3-INTERMEDIATE
            	columnPR = rowPR.createCell(COLUMN_INTERMEDIATE, Cell.CELL_TYPE_NUMERIC);
                columnPR.setCellValue(INTERMEDIATE_WEIGHT);
              //4-EXPERT
            	columnPR = rowPR.createCell(COLUMN_EXPERT, Cell.CELL_TYPE_NUMERIC);
                columnPR.setCellValue(EXPERT_WEIGHT);
              //5-IRRELEVANT
            	columnPR = rowPR.createCell(COLUMN_IRRELEVANT, Cell.CELL_TYPE_NUMERIC);
                columnPR.setCellValue(IRRELEVANT_WEIGHT);
              //6-RELEVANT
            	columnPR = rowPR.createCell(COLUMN_RELEVANT, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(RELEVANT);
              //7-AVG_At_10
            	columnPR = rowPR.createCell(COLUMN_AVG_At_10, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(AVG_At_10);
              //8-AVG_At_25
            	columnPR = rowPR.createCell(COLUMN_AVG_At_25, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(AVG_At_25);
              //9-AVG_At_50
            	columnPR = rowPR.createCell(COLUMN_AVG_At_50, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(AVG_At_50);
              //10-DCG
            	columnPR = rowPR.createCell(COLUMN_DCG, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(DCG);
              //11-IDEAL RANK
            	columnPR = rowPR.createCell(COLUMN_IDEAL_RANK, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(IDEAL_RANK);
                //12-IDCG
            	columnPR = rowPR.createCell(COLUMN_IDCG, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(IDCG);
              //13-nDCG
            	columnPR = rowPR.createCell(COLUMN_nDCG, Cell.CELL_TYPE_STRING);
                columnPR.setCellValue(nDCG);
              
              
                
            }
            
		  
		    //create a list with the rating per question(for sorting)
		    ArrayList<Rating> ratings = new ArrayList<Rating>();
    		

		    //create a list with the competences from the original Limesurvey export file
		    ArrayList<Competence> competences = new ArrayList<Competence>();
    		
		    generateCompetenceListFromLimeSurveyFile(wb, competences);
		    
		    //sort the list by rank
			Collections.sort(competences, new Comparator<Competence>() {

				@Override
				public int compare(Competence o1, Competence o2) {
					return Float.compare(o1.rank, o2.rank);
				}

			});
			
			
			//write into the new workbook
			rowNumPR = createPrecisionAtRankAndDCG(prec_recall, ratings, competences);
			
			
			//add new row with distribution and average precision values 
			rowPR = prec_recall.createRow(rowNumPR);
            
			//sum the ratings up
			sumRatingsUp(rowNumPR, rowPR);
         	
         	//compute the Average Precision (Top10, Top25, Top50)
			computeAveragePrecision(rowPR);
         	
         	//compute DCG, IDCG & nDCG
         	computeIDCG_nDCG(prec_recall, rowPR, ratings);
                      
        	
			//write workbook into the new output file
            String name = child.getName();
            
            System.out.println("*** successfully processed "+name+" ***");
            
            name = name.split(".xlsx")[0];
         	FileOutputStream fileOut = new FileOutputStream(OUTPUT_PATH+"/"+name+"_metrics.xlsx");
		    wbPR.write(fileOut);
		    fileOut.close();
				
			pkg.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		    }
		  } else {
		    // Handle the case where dir is not really a directory.
		    // Checking dir.isDirectory() above would not be sufficient
		    // to avoid race conditions with another process that deletes
		    // directories.
		  }
		  
		
	}

	
	/**
	 * takes a workbook as input and generates a list of competences,
	 * only the first sheet with its first two rows are analyzed,
	 * every LimeSurvey question group consists of 3 questions 
	 * 
	 * first row contains the following column headers per question group: 
	 * "original rank", competence question, "comment"
	 * second row contains the actual ranks, ratings and comment entries
	 * 
	 * @param XSSFWorkbook wb
	 * @param ArrayList<Competence> competences
	 */
	private static void generateCompetenceListFromLimeSurveyFile(XSSFWorkbook wb, ArrayList<Competence> competences) {
		for (Sheet sheet : wb ) {	
			for (Row row : sheet) {
		    	
		    	 
		    	int counter=0;
		    	int i=0;
		    	
		        for (Cell cell : row) {
		        	
		        	//first row in exported LimeSurvey file contains the competence question
		        	if(row.getRowNum()==0){
		        		
		        		//the questions start at index 9
		        		if(cell.getColumnIndex()>=9 && cell.getColumnIndex()<159 && cell.getColumnIndex()%3==0){
		        			String value = cell.getRichStringCellValue().getString();
			                
		        			Competence competence = new Competence(value);
		        			competences.add(competence);
			             
			               
		        		}
		        	}
		        	//fetch the original rank and ratings
		        	else{
		        		//get cellType
	        			int cellType = cell.getCellType();
	        			
	        			
	        			//if we have analyzed two consecutive cells 
	        			//it can be either a comment or a new hidden rank
	        			//if it is the hidden rank (cellType = NUMERIC), 
	        			//set counter = 0 and i++ (next competence) 
		        		if(counter>=2 && cellType == Cell.CELL_TYPE_NUMERIC ){
		        			counter=0;
		        			i++;
		        		}
		        		
		        		//ignore comment fields (counter <2), just take the hidden rank and the actual rating
		        		if(cell.getColumnIndex()>=8 && cell.getColumnIndex()<158 && counter<2 && i<50){
		        			
		        			Competence competence = competences.get(i);
		        			
		        			
		        			switch(cellType){
		        			
		        				case Cell.CELL_TYPE_NUMERIC:{
		        					
		        					//if it is numeric - it's the hidden original rank
				            		double numericValue = cell.getNumericCellValue();		         		
				            		competence.setRank(new Float(numericValue).intValue());
				            		
				            		
				            		
				            		break;
		        				}
		        				case Cell.CELL_TYPE_STRING:{
		        					String value = cell.getRichStringCellValue().getString();
				            		String[] values = value.split("\\s+");
				            		
				            		if(values[0]!=null){
				            			String rating = null;
				            			
				            			switch (values[0]){
				            				case NOVICE:{
				            					rating = NOVICE;
				            					break;
				            				}
				            				case INTERMEDIATE:{
				            					rating = INTERMEDIATE;
				            					break;
				            				}
				            				case EXPERT:{
				            					rating = EXPERT;
				            					break;
				            				}
				            				case IRRELEVANT:{
				            					rating = IRRELEVANT;
				            					break;
				            				}
				            				default:
				            					break;
				            					
				            				}
				            			
				            			if(rating!=null)
				            				competence.setRating(rating);
				            		}
				            		break;
				            		
		        				}//case STRING
		        			}//switch cellType
		        			
		        			counter++;
		        			
		        		}//if cell.index...
		        		
		        		
		        		
		        	}//2nd row
		        	
		        }//for each cell
		    }//for each row
			
			/*for(Iterator<Competence> it =competences.iterator(); it.hasNext();){
				Competence competence = it.next();
				System.out.println("Competence: Rank:"+ competence.getRank()+" Competence: "+competence.getCompetence()+" Rating: "+competence.getRating());
			}*/
			
		}// for each sheet
	}

	/**
	 * generates Precision@rank and DCG
	 * 
	 * @param Sheet prec_recall
	 * @param ArrayList<Rating> rating weights, needed for IDCG (sorted list of ratings:0-Irrelevant, 1-General, 2-technical,3-Research)		    	
	 * @param ArrayList<Competence> competences
	 * @return
	 */
	private static int createPrecisionAtRankAndDCG(Sheet prec_recall, ArrayList<Rating> ratings,
			ArrayList<Competence> competences) {
		int rowNumPR;
		Row rowPR;
		rowNumPR= 1;
		for(Iterator<Competence> it=competences.iterator();it.hasNext();){
			Competence competence = it.next();

			rowPR = prec_recall.getRow(rowNumPR);
			if(rowPR == null)
		    	rowPR = prec_recall.createRow(rowNumPR);
		   
		    
			Cell cellPR = rowPR.createCell(COLUMN_RANK,Cell.CELL_TYPE_STRING);
		    cellPR.setCellValue(competence.getRank());
		    
		    cellPR = rowPR.createCell(COLUMN_COMPETENCE,Cell.CELL_TYPE_STRING);
		    cellPR.setCellValue(competence.getCompetence());
		    
			int intValue=1;
			
			if(competence.getRating()!=null){
			    //System.out.println("Rating: "+competence.getRating());
				if(competence.getRating().equals(NOVICE)){
			    	
			    	cellPR = rowPR.createCell(COLUMN_NOVICE,Cell.CELL_TYPE_STRING);
			    	cellPR.setCellValue(intValue);
			    	
			    	ratings.add(new Rating(rowNumPR,NOVICE_WEIGHT));
			    }
			    else if(competence.getRating().equals(INTERMEDIATE)){
			    	cellPR = rowPR.createCell(COLUMN_INTERMEDIATE,Cell.CELL_TYPE_STRING);
			    	cellPR.setCellValue(intValue);
			    	
			    	ratings.add(new Rating(rowNumPR,INTERMEDIATE_WEIGHT));
			    }
			    else if(competence.getRating().equals(EXPERT)){
			    	cellPR = rowPR.createCell(COLUMN_EXPERT,Cell.CELL_TYPE_STRING);
			    	cellPR.setCellValue(intValue);
			    	
			    	ratings.add(new Rating(rowNumPR,EXPERT_WEIGHT));
			    }
			    else{
			    	cellPR = rowPR.createCell(COLUMN_IRRELEVANT,Cell.CELL_TYPE_STRING);
			    	cellPR.setCellValue(intValue);
			    	ratings.add(new Rating(rowNumPR,IRRELEVANT_WEIGHT));
			    }
			}
		    
		    //add formulas to AVG_PREC cells
		    CellReference cellRefRank = new CellReference(rowNumPR, COLUMN_RANK);
		    
		    CellReference cellRefThresholdFirst = new CellReference(1, THRESHOLD);
		    CellReference cellRefThreshold = new CellReference(rowNumPR, THRESHOLD);
	        
		    
		    CellReference cellRefNoviceFirst = new CellReference(1, COLUMN_NOVICE);
        
		    
		    CellReference cellRefNovice = new CellReference(rowNumPR, COLUMN_NOVICE);
		    CellReference cellRefExpert = new CellReference(rowNumPR, COLUMN_EXPERT);
		    
		    CellReference cellRefIrrelevant = new CellReference(rowNumPR, COLUMN_IRRELEVANT);
		    CellReference cellRefNoviceWeight = new CellReference(0, COLUMN_NOVICE);
		    CellReference cellRefIrrelevantWeight = new CellReference(0, COLUMN_IRRELEVANT);
		    
		    
		    //6 - Relevant
		    cellPR = rowPR.createCell(COLUMN_RELEVANT);
		    //cellPR.setCellFormula("IF(SUM("+cellRefNovice.formatAsString()+":"+cellRefExpert.formatAsString()+")>0,1,0)");
		    cellPR.setCellFormula("IF(SUM("+cellRefThreshold.formatAsString()+":"+cellRefExpert.formatAsString()+")>0,1,0)");
		    
		    
		    //7 - AVG_PREC@10  
		    if(rowNumPR<=10){
		    	cellPR = rowPR.createCell(COLUMN_AVG_At_10);
		    	//cellPR.setCellFormula("SUM("+cellRefNoviceFirst.formatAsString()+":"+cellRefExpert.formatAsString()+")/"+cellRefRank.formatAsString());
		    	cellPR.setCellFormula("SUM("+cellRefThresholdFirst.formatAsString()+":"+cellRefExpert.formatAsString()+")/"+cellRefRank.formatAsString());
			    
		    }
		    
		    //8 - AVG_PREC@25  
		    if(rowNumPR<=25){
		    	cellPR = rowPR.createCell(COLUMN_AVG_At_25);
		    	//cellPR.setCellFormula("SUM("+cellRefNoviceFirst.formatAsString()+":"+cellRefExpert.formatAsString()+")/"+cellRefRank.formatAsString());
		     	cellPR.setCellFormula("SUM("+cellRefThresholdFirst.formatAsString()+":"+cellRefExpert.formatAsString()+")/"+cellRefRank.formatAsString());
				   
		    }
		    //9 - AVG_PREC@25  
		    if(rowNumPR<=50){
		    	cellPR = rowPR.createCell(COLUMN_AVG_At_50);
		    //	cellPR.setCellFormula("SUM("+cellRefNoviceFirst.formatAsString()+":"+cellRefExpert.formatAsString()+")/"+cellRefRank.formatAsString());
		     	cellPR.setCellFormula("SUM("+cellRefThresholdFirst.formatAsString()+":"+cellRefExpert.formatAsString()+")/"+cellRefRank.formatAsString());
				   
		    }	
		    //10 - DCG
		    cellPR = rowPR.createCell(COLUMN_DCG);
		    
		    //first rank without log
		    if(rowPR.getCell(COLUMN_RANK).getNumericCellValue()==1){
		    	cellPR.setCellFormula("SUMIF("+cellRefNovice.formatAsString()+":"+cellRefIrrelevant.formatAsString()+",1,"+cellRefNoviceWeight.formatAsString()+":"+cellRefIrrelevantWeight.formatAsString()+")");
		   	   
		    }else{
		     	cellPR.setCellFormula("SUMIF("+cellRefNovice.formatAsString()+":"+cellRefIrrelevant.formatAsString()+",1,"+cellRefNoviceWeight.formatAsString()+":"+cellRefIrrelevantWeight.formatAsString()+")/LOG("+cellRefRank.formatAsString()+",2)");
		       
		    }
   
		    
		    rowNumPR++;
			
		}
		return rowNumPR;
	}


	/**
	 * generates the IDCG and nDCG
	 * @param prec_recall
	 * @param rowPR
	 * @param ratings
	 */
	private static void computeIDCG_nDCG(Sheet prec_recall, Row rowPR, ArrayList<Rating> ratings) {
		CellReference cellRefDCGFirst = new CellReference(1, COLUMN_DCG);
		CellReference cellRefDCGLast = new CellReference(50, COLUMN_DCG);
				
		
		Cell cellPR = rowPR.createCell(COLUMN_DCG);
		cellPR.setCellFormula("SUM("+cellRefDCGFirst.formatAsString()
				+":"+cellRefDCGLast.formatAsString()+")");
		
		
		
		//Get the List of ratings and sort them by relevance (rating)
		Collections.sort(ratings, new Comparator<Rating>() {

			@Override
			public int compare(Rating o1, Rating o2) {
				return Float.compare(o2.rating, o1.rating);
			}

		});
		
		
		//go through the sheet again and add the ideal rank    		
		int current_position = 1;
		
		for (Iterator<Rating> it = ratings.iterator(); it.hasNext();) {
			int rank = it.next().getCompetenceRank();
			
			Row rowPR2 = prec_recall.getRow(rank);
			
				
			//CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
		    cellPR = rowPR2.createCell(COLUMN_IDEAL_RANK, Cell.CELL_TYPE_NUMERIC);
		    cellPR.setCellValue(current_position);
		    
		    CellReference cellRefNovice = new CellReference(rowPR2.getRowNum(), COLUMN_NOVICE);
		    
		    CellReference cellRefIrrelevant = new CellReference(rowPR2.getRowNum(), COLUMN_IRRELEVANT);
		    CellReference cellRefNoviceWeight = new CellReference(0, COLUMN_NOVICE);
		    CellReference cellRefIrrelevantWeight = new CellReference(0, COLUMN_IRRELEVANT);
		    CellReference cellRefIdealRank = new CellReference(rowPR2.getRowNum(), COLUMN_IDEAL_RANK);
		    
		        
		        cellPR = rowPR2.createCell(COLUMN_IDCG, Cell.CELL_TYPE_NUMERIC);
		     	
		        //LOG 1 is not defined
		        if(current_position==1)
		        	cellPR.setCellFormula("SUMIF("+cellRefNovice.formatAsString()+":"+cellRefIrrelevant.formatAsString()+",1,"+cellRefNoviceWeight.formatAsString()+":"+cellRefIrrelevantWeight.formatAsString()+")");
		        else
		         	cellPR.setCellFormula("SUMIF("+cellRefNovice.formatAsString()+":"+cellRefIrrelevant.formatAsString()+",1,"+cellRefNoviceWeight.formatAsString()+":"+cellRefIrrelevantWeight.formatAsString()+")/LOG("+cellRefIdealRank.formatAsString()+",2)");
		      
		     current_position++;
		     	
			}
			
			//sum of iDCG
			cellPR = rowPR.createCell(COLUMN_IDCG, Cell.CELL_TYPE_NUMERIC);
		  
			CellReference cellRefIDCGFirst = new CellReference(1, COLUMN_IDCG);
			CellReference cellRefIDCGLast = new CellReference(50, COLUMN_IDCG);
			
		   	cellPR.setCellFormula("SUM("+cellRefIDCGFirst.formatAsString()+":"+cellRefIDCGLast.formatAsString()+")");
		
		   	//nDCG
		   	CellReference cellRefDCG = new CellReference(rowPR.getRowNum(), COLUMN_DCG);
			CellReference cellRefIDCG = new CellReference(rowPR.getRowNum(), COLUMN_IDCG);
			
		   	cellPR = rowPR.createCell(COLUMN_nDCG, Cell.CELL_TYPE_NUMERIC);
		   	cellPR.setCellFormula("SUM("+cellRefDCG.formatAsString()+"/"+cellRefIDCG.formatAsString()+")");
	}

	/**
	 * generates the average precisions
	 * @param rowPR
	 */
	private static void computeAveragePrecision(Row rowPR) {
		//AVG_PREC@10
		CellReference cellRefRelevant10First = new CellReference(1, COLUMN_RELEVANT);
		CellReference cellRefRelevant10Last = new CellReference(10, COLUMN_RELEVANT);
		
		CellReference cellRefAvgAt10First = new CellReference(1, COLUMN_AVG_At_10);
		CellReference cellRefAvgAt10Last = new CellReference(10, COLUMN_AVG_At_10);
		
		Cell cellPR = rowPR.createCell(COLUMN_AVG_At_10);
		cellPR.setCellFormula("SUMIF("+cellRefRelevant10First.formatAsString()
				+":"+cellRefRelevant10Last.formatAsString()+",1,"
				+cellRefAvgAt10First.formatAsString()
				+":"+cellRefAvgAt10Last.formatAsString()
				+")/SUM("+cellRefRelevant10First.formatAsString()
				+":"+cellRefRelevant10Last.formatAsString()+")");
		
		
		//AVG_PREC@25
		CellReference cellRefRelevant25First = new CellReference(1, COLUMN_RELEVANT);
		CellReference cellRefRelevant25Last = new CellReference(25, COLUMN_RELEVANT);
		
		CellReference cellRefAvgAt25First = new CellReference(1, COLUMN_AVG_At_25);
		CellReference cellRefAvgAt25Last = new CellReference(25, COLUMN_AVG_At_25);
		
		
		cellPR = rowPR.createCell(COLUMN_AVG_At_25);
		cellPR.setCellFormula("SUMIF("+cellRefRelevant25First.formatAsString()
				+":"+cellRefRelevant25Last.formatAsString()+",1,"
				+cellRefAvgAt25First.formatAsString()
				+":"+cellRefAvgAt25Last.formatAsString()
				+")/SUM("+cellRefRelevant25First.formatAsString()
				+":"+cellRefRelevant25Last.formatAsString()+")");
		
		//AVG_PREC@50
		CellReference cellRefRelevant50First = new CellReference(1, COLUMN_RELEVANT);
		CellReference cellRefRelevant50Last = new CellReference(50, COLUMN_RELEVANT);
		
		CellReference cellRefAvgAt50First = new CellReference(1, COLUMN_AVG_At_50);
		CellReference cellRefAvgAt50Last = new CellReference(50, COLUMN_AVG_At_50);
		
		
		cellPR = rowPR.createCell(COLUMN_AVG_At_50);
		cellPR.setCellFormula("SUMIF("+cellRefRelevant50First.formatAsString()
				+":"+cellRefRelevant50Last.formatAsString()+",1,"
				+cellRefAvgAt50First.formatAsString()
				+":"+cellRefAvgAt50Last.formatAsString()
				+")/SUM("+cellRefRelevant50First.formatAsString()
				+":"+cellRefRelevant50Last.formatAsString()+")");
	}

	/**
	 * generates the sum over all competences
	 * @param rowNumPR
	 * @param rowPR
	 */
	private static void sumRatingsUp(int rowNumPR, Row rowPR) {
		//SUM of all NOVICE ratings
		CellReference cellRefNoviceFirst = new CellReference(1, COLUMN_NOVICE);
		CellReference cellRefNoviceLast = new CellReference(rowNumPR-1, COLUMN_NOVICE);
		
		Cell cellPR = rowPR.createCell(COLUMN_NOVICE);
		cellPR.setCellFormula("SUM("+cellRefNoviceFirst.formatAsString()+":"+cellRefNoviceLast.formatAsString()+")");
   
		//SUM of all INTERMEDIATE ratings
		CellReference cellRefIntermediateFirst = new CellReference(1, COLUMN_INTERMEDIATE);
		CellReference cellRefIntermediateLast = new CellReference(rowNumPR-1, COLUMN_INTERMEDIATE);
		
		cellPR = rowPR.createCell(COLUMN_INTERMEDIATE);
		cellPR.setCellFormula("SUM("+cellRefIntermediateFirst.formatAsString()+":"+cellRefIntermediateLast.formatAsString()+")");
   
		//SUM of all EXPERT ratings
		CellReference cellRefExpertFirst = new CellReference(1, COLUMN_EXPERT);
		CellReference cellRefExpertLast = new CellReference(rowNumPR-1, COLUMN_EXPERT);
		
		cellPR = rowPR.createCell(COLUMN_EXPERT);
		cellPR.setCellFormula("SUM("+cellRefExpertFirst.formatAsString()+":"+cellRefExpertLast.formatAsString()+")");
   
		//SUM of all IRRELEVANT ratings
		CellReference cellRefIrrelevantFirst = new CellReference(1, COLUMN_IRRELEVANT);
		CellReference cellRefIrrelevantLast = new CellReference(rowNumPR-1, COLUMN_IRRELEVANT);
		
		cellPR = rowPR.createCell(COLUMN_IRRELEVANT);
		cellPR.setCellFormula("SUM("+cellRefIrrelevantFirst.formatAsString()+":"+cellRefIrrelevantLast.formatAsString()+")");
	}

	 /**
	   * Parse command line options.
	   */
	  @SuppressWarnings("resource")
	private static void parseCommandLine(String[] args) {
	    
		int threshold_int=0;
		  
	// input: user
      System.out.println("What ratings should be considered as relevant?\n"
      		+ "Please enter a threshold for relevant ratings in the "
      		+ "range from 0 to 3!\nFor example, a value of '0' means "
      		+ "that all ratings greater than 0 are considered as relevant. ");
      
	  System.out.println("Your threshold:");
      
	  // Initiate a new Scanner
	  Scanner userInputScanner = new Scanner(System.in);
	    
      String  threshold_user= userInputScanner.next();

      try{
      	threshold_int = new Integer(threshold_user);
      }catch(Exception e)
      {
      	//print message how to use it
      	printUsage();
      	System.exit(1);
      }
      
      //if user input is in the correct range - take it
      if(threshold_int>=0 && threshold_int<4 ){
      	 	
      	switch(threshold_int){
      	
      	case 0:
      		//default case
      		THRESHOLD = COLUMN_NOVICE;
      		break;
      	case 1:
      		//consider all ratings above 1 as relevant
      		//start at COLUMN_INTERMEDIATE
      		THRESHOLD = COLUMN_INTERMEDIATE;
      		break;
      	case 2:
      		//consider all ratings above 2 as relevant
      		//start at COLUMN_EXPERT
      		THRESHOLD = COLUMN_EXPERT;
      		break;
      		
      	//default value is '0' which means all ratings greater than 0 are considered as relevant
      	//start at COLUMN_NOVICE 
      	default: THRESHOLD = COLUMN_NOVICE;
      	
      	}
      }
      else{
      	//error
      	//print message how to use it
      	printUsage();
      	System.exit(1);
      }
    
	}
	 
	/**
	 * print message how to use this program
	 */
	private static void printUsage() {
		System.out.print("The system excepts only numbers in the range from 0 to 3."
				+ "\n0 - irrelevant"
				+ "\n1 - general knowledge (e.g. broad terms as 'Number')"
				+ "\n2 - technical knowledge"
				+ "\n3 - expert knowledge");
      	
	}

}
