# Analysis Tool Manual

## About
The Analysis Tool is a simple command-line tool written in Java to compute different precision metrics from a given Excel file. It was originally developed for analyzing exported result files from [LimeSurvey](https://www.limesurvey.org/). Additionally, it was adpated to a certain structure used for analyzing and evaluating user competences. The input Excel files need to be in this certain structure which is described under [Excel structure] (../Analysis#Excel-Structure). However, the tool can be easily adopted to other LimeSurvey structures. Supported metrics are: Precision@k, Mean Average Precision (MAP) and normalized Discounted Cumulative Gain (nDCG).

## Prerequistes
The analysis tool requires [Apache Ant](https://ant.apache.org/) and [Apache Ivy](https://ant.apache.org/ivy/) and has been developed and tested with JAVA 8.

## Download
You can download the Analysis Tool including the source code form this [Github Repository](../Analysis)

## Excel Structure
The Analysis tool comes with a _data_ folder where all Excel files have to be put in. (@ToDo: add data folder with sample file!) The first 8 columns are LimeSurvey specific columns:
* Response ID,	
* Date submitted,	
* Last page,	
* Start language,	
* Date started,	
* Date last action,	
* IP address,	
* Referrer URL 

These columns can be configured in LimeSurvey but the Analysis tool assumes that the full structures has been exported. Starting from column 9 the excel contains survey specific columns. In our case we had a survey with 50 question groups (competences) each consisting of 3 questions. While the first row displays the column headers, the second row contains the actual user responses.

| Original competence rank	| What is your competency with respect to Algorithm? | Comment |
| :------------------------:  | :-----------------------------------------------: |:------:|
|14	Research | Research - a topic that you know well and you are/have been doing research on | |


Thus, in our example file a competence always spans 3 columns where the first column contains the actual rank which is hidden for the user, the second column denotes the user rating for a given competence and the third column contains user comments. In our case, the first two columns are always filled (mandatory) but comments are optional. In total the example structure comprises 50 competences which results in 150 + 8 final columns. 

## How to run the Analysis Tool 
Start the tool with the Ant task 

```
ant run
```
from the folder where the build.xml is located. The program will ask for which threshold you want to run the analysis. The threshold is related to the computation of Precision@k and Mean Average Precision and can be in the range of 0-3 refering to the three possible competence ratings _Irrelevant_, _General_, _Technical_, _Research_.

## Result
The tool creates a _result_ folder and generates a new metrics file. It sorts the competences horizontally and computes the different metrics.

