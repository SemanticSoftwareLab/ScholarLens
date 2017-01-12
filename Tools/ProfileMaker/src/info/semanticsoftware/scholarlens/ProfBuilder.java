package info.semanticsoftware.scholarlens;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * Semantic Scholarly Profile Builder.
 *
 * @author Rene Witte
 * @author Bahar Sateli
 *
 */
public class ProfBuilder {
	
	private static String userName = "error";
	private static String mode;
	private final static String LATEX_TEMPLATE_FILE_NAME = "template.tex";

	public static void main(String[] args) {

		/** The TDB directory where the triples are stored. */
		final String TDBdirectory = args[0];
		
		/** The running mode keyword:
		 * "survey" generates LimeSurvey templates
		 * otherwise generates LaTeX files
		 */
		mode = args[1];
		
		System.out.println("Dataset path: " + TDBdirectory);
		System.out.println("Export Mode: " + mode);

		/** The dataset read from TDB. */
		Dataset dataset = null;
		dataset = TDBFactory.createDataset(TDBdirectory);
		dataset.begin(ReadWrite.READ);

		/** The in-memory model of the triples. */
		Model model = dataset.getDefaultModel();
		
		StringBuffer sb = new StringBuffer();
		sb.append(getHeader());
		// all competences
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX um: <http://intelleo.eu/ontologies/user-model/ns/> PREFIX c: <http://www.intelleo.eu/ontologies/competences/ns/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pubo: <http://lod.semanticsoftware.info/pubo/pubo#> PREFIX dcterms: <http://purl.org/dc/terms/> SELECT DISTINCT ?uri (COUNT(?uri) AS ?count) WHERE { ?document pubo:hasAnnotation ?creator. ?creator rdfs:isDefinedBy <DUMBO> . ?creator um:hasCompetencyRecord ?competenceRecord. ?competenceRecord c:competenceFor ?competence. ?competence rdfs:isDefinedBy ?uri.} GROUP BY ?uri ORDER BY DESC(?count) LIMIT 50";
		sb.append(listCompetencies(queryString, model));
		sb.append("\\end{longtable}\\end{document}");
		BufferedWriter writer = null;
        try {
            File profile = new File(userName.substring(userName.lastIndexOf("/")+1) + ".tex");
            writer = new BufferedWriter(new FileWriter(profile));
            writer.write(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
        
        // clear the buffer
        sb.setLength(0);
        sb.append(getHeader());
		// all competences within REs
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX um: <http://intelleo.eu/ontologies/user-model/ns/> PREFIX c: <http://www.intelleo.eu/ontologies/competences/ns/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pubo: <http://lod.semanticsoftware.info/pubo/pubo#> PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX sro: <http://salt.semanticauthoring.org/ontologies/sro#> SELECT DISTINCT ?uri (COUNT(?uri) AS ?count) WHERE { ?creator rdfs:isDefinedBy <DUMBO> . ?creator um:hasCompetencyRecord ?competenceRecord. ?competenceRecord c:competenceFor ?competence. ?competence rdfs:isDefinedBy ?uri. ?rhetoricalEntity pubo:containsNE ?competence. ?rhetoricalEntity rdf:type sro:RhetoricalElement.} GROUP BY ?uri ORDER BY DESC(?count) LIMIT 50";
		sb.append(listCompetencies(queryString, model));		
		sb.append("\\end{longtable}\\end{document}");
		writer = null;
        try {
        	// by this line the userName is initialized
            File profile = new File(userName.substring(userName.lastIndexOf("/")+1) + "_re.tex");
            writer = new BufferedWriter(new FileWriter(profile));
            writer.write(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
		
		dataset.end();
	}

	/**
	 * Reads and returns the LaTeX table headers from the template file.
	 * 
	 * @return the LaTeX table headers as string
	 */
	private final static String getHeader() {
		String everything = null;
		try(BufferedReader br = new BufferedReader(new FileReader(LATEX_TEMPLATE_FILE_NAME))) {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();
		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    everything = sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return everything;
	}

	/** Returns the representation of competences in the given model.
	 * 
	 * @param SPARQLTemplate the query to select competences with a placeholder for a username
	 * @param model the dataset model containing the competence triples
	 * @return Either a LaTeX table source or a LimeSurvey template
	 */
	private final static String listCompetencies(final String SPARQLTemplate,final Model model) {
		List<Competency> competenceList = new ArrayList<Competency>();
		//int counter = 0;
		StringBuffer sb = new StringBuffer();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX um: <http://intelleo.eu/ontologies/user-model/ns/> PREFIX c: <http://www.intelleo.eu/ontologies/competences/ns/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX pubo: <http://lod.semanticsoftware.info/pubo/pubo#> SELECT DISTINCT ?uri WHERE {?document pubo:hasAnnotation ?author. ?author rdf:type um:User. ?author rdfs:isDefinedBy ?uri.}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				RDFNode user = soln.get("?uri");
				userName = user.toString();

				System.out.println("\nGenerating a semantic profile for  " + userName);
				System.out.println("=============================================================================================");

				RDFNode ne = null;

				// REPLACE THE PLACEHOLDER WITH USERNAME
				String SPARQL = SPARQLTemplate.replaceAll("DUMBO", userName);
				Query neQuery = QueryFactory.create(SPARQL);
				QueryExecution neQexec = QueryExecutionFactory.create(neQuery,model);
				ResultSet neResults = neQexec.execSelect();
				int rankCounter = 1;
				for (; neResults.hasNext();) {
					QuerySolution neSoln = neResults.nextSolution();
					ne = neSoln.get("?uri");
					if (ne != null) {
						Competency cmpt = getInfo(ne.toString(), rankCounter++);
						competenceList.add(cmpt);
					} else {
						System.out.println("No competencies found!");
					}
				}
				if(mode.equalsIgnoreCase("survey")){
					System.out.println("Generating LimeSurvey templates....");
					generateRandomizedSurvey(competenceList);
				}else{
					System.out.println("Generating LaTeX templates....");
					//generateLaTeXProfile(competenceList);
				}
			}
		} finally {
			qexec.close();
		}
		System.out.println("All done.");
		return sb.toString();
	}
	
	private static void generateRandomizedSurvey(final List<Competency> compList){
		/*System.out.println("Original list:");
		compList.stream().forEach(c -> System.out.print("(" + c.getRank() + ") " + c.getLabel() + ", "));*/
		
		List<Competency> shuffledList = new ArrayList<Competency>(compList);
		Collections.copy(shuffledList, compList);
		Collections.shuffle(shuffledList);
		
		/*System.out.println("\nShuffled list:");
		shuffledList.stream().forEach(c2 -> System.out.print("(" + c2.getRank() + ") " + c2.getLabel() + ", "));*/
		
		PrintWriter pw;
		try {
			if(new File("survey.txt").exists()){
				pw = new PrintWriter(new FileWriter("survey_re.txt", true));
			}else{
				pw = new PrintWriter(new FileWriter("survey.txt", true));
			}
			int groupCounter = 0;
			int questionCounter = 1;
			
			//preamble
			pw.printf("class	type/scale	name	relevance	text	help	language	validation	mandatory	other	default	same_default	allowed_filetypes	alphasort	answer_width	array_filter	array_filter_exclude	array_filter_style	assessment_value	category_separator	choice_title	code_filter	commented_checkbox	commented_checkbox_auto	date_format	date_max	date_min	display_columns	display_rows	dropdown_dates	dropdown_dates_minute_step	dropdown_dates_month_style	dropdown_prefix	dropdown_prepostfix	dropdown_separators	dropdown_size	dualscale_headerA	dualscale_headerB	em_validation_q	em_validation_q_tip	em_validation_sq	em_validation_sq_tip	equals_num_value	exclude_all_others	exclude_all_others_auto	hidden	hide_tip	input_boxes	location_city	location_country	location_defaultcoordinates	location_mapheight	location_mapservice	location_mapwidth	location_mapzoom	location_nodefaultfromip	location_postal	location_state	max_answers	max_filesize	max_num_of_files	max_num_value	max_num_value_n	maximum_chars	min_answers	min_num_of_files	min_num_value	min_num_value_n	multiflexible_checkbox	multiflexible_max	multiflexible_min	multiflexible_step	num_value_int_only	numbers_only	other_comment_mandatory	other_numbers_only	other_replace_text	page_break	parent_order	prefix	printable_help	public_statistics	random_group	random_order	rank_title	repeat_headings	reverse	samechoiceheight	samelistheight	scale_export	show_comment	show_grand_total	show_title	show_totals	showpopups	slider_accuracy	slider_default	slider_layout	slider_max	slider_middlestart	slider_min	slider_rating	slider_reset	slider_separator	slider_showminmax	statistics_graphtype	statistics_showgraph	statistics_showmap	suffix	text_input_width	time_limit	time_limit_action	time_limit_countdown_message	time_limit_disable_next	time_limit_disable_prev	time_limit_message	time_limit_message_delay	time_limit_message_style	time_limit_timer_style	time_limit_warning	time_limit_warning_2	time_limit_warning_2_display_time	time_limit_warning_2_message	time_limit_warning_2_style	time_limit_warning_display_time	time_limit_warning_message	time_limit_warning_style	use_dropdown%n" + 
					"S		sid		954679																																																																																																																											%n" + 
					"S		owner_id		1																																																																																																																											%n" + 
					"S		admin		Bahar Sateli																																																																																																																											%n" + 
					"S		active		N																																																																																																																											%n" + 
					"S		adminemail		sateli@semanticsoftware.info																																																																																																																											%n" + 
					"S		anonymized		N																																																																																																																											%n" + 
					"S		format		G																																																																																																																											%n" + 
					"S		savetimings		Y																																																																																																																											%n" + 
					"S		template		default																																																																																																																											%n" + 
					"S		language		en																																																																																																																											%n" + 
					"S		datestamp		Y																																																																																																																											%n" + 
					"S		usecookie		N																																																																																																																											%n" + 
					"S		allowregister		N																																																																																																																											%n" + 
					"S		allowsave		Y																																																																																																																											%n" + 
					"S		autonumber_start		0																																																																																																																											%n" + 
					"S		autoredirect		N																																																																																																																											%n" + 
					"S		allowprev		N																																																																																																																											%n" + 
					"S		printanswers		N																																																																																																																											%n" + 
					"S		ipaddr		Y																																																																																																																											%n" + 
					"S		refurl		Y																																																																																																																											%n" + 
					"S		datecreated		2016-07-08																																																																																																																											%n" + 
					"S		publicstatistics		N																																																																																																																											%n" + 
					"S		publicgraphs		N																																																																																																																											%n" + 
					"S		listpublic		N																																																																																																																											%n" + 
					"S		htmlemail		Y																																																																																																																											%n" + 
					"S		sendconfirmation		Y																																																																																																																											%n" + 
					"S		tokenanswerspersistence		N																																																																																																																											%n" + 
					"S		assessments		N																																																																																																																											%n" + 
					"S		usecaptcha		N																																																																																																																											%n" + 
					"S		usetokens		N																																																																																																																											%n" + 
					"S		bounce_email		sateli@semanticsoftware.info																																																																																																																											%n" + 
					"S		tokenlength		15																																																																																																																											%n" + 
					"S		showxquestions		N																																																																																																																											%n" + 
					"S		showgroupinfo		B																																																																																																																											%n" + 
					"S		shownoanswer		N																																																																																																																											%n" + 
					"S		showqnumcode		X																																																																																																																											%n" + 
					"S		bounceprocessing		N																																																																																																																											%n" + 
					"S		showwelcome		Y																																																																																																																											%n" + 
					"S		showprogress		Y																																																																																																																											%n" + 
					"S		questionindex		0																																																																																																																											%n" + 
					"S		navigationdelay		0																																																																																																																											%n" + 
					"S		nokeyboard		N																																																																																																																											%n" + 
					"S		alloweditaftercompletion		N																																																																																																																											%n" + 
					"S		googleanalyticsstyle		0																																																																																																																											%n" + 
					"SL		surveyls_title		Semantic Profiling of Scholars		en																																																																																																																									%n" + 
					"SL		surveyls_welcometext		<p>Hello,</p>    <p>Thanks for agreeing to participate in our study. Our goal is to evaluate <em>automatic profiling of scholars</em>. We believe that smarter scholarly applications require not just a semantically rich representation of research objects, but also of their users: By understanding your  interests, competences, projects and tasks, intelligent systems will be able to provide you improved, personalized results.</p>    <p>To avoid having to manually create these <em>user profiles</em>, we developed a method to automatically generate a scholarly profile for you. Your profile contains a set of so-called <em>competence topics</em>. For each topic, we ask you to select <span style=\"color:#B22222;\"><strong>one</strong></span> of the following levels that best applies to you:</p>    <ul>   <li><strong>Research</strong> – a topic that you know well and you are/have been doing research on;</li>   <li><strong>Technical</strong> – a topic that you know about and use in your tasks;</li>   <li><strong>General</strong> – a topic that you have general world knowledge about; or</li>   <li><strong>Irrelevant</strong> – a topic that you have no or very limited knowledge about.</li>  </ul>    <p>For example, for René, who is a researcher in Semantic Computing:</p>    <ul>   <li>\"<em>Linked Data</em>\" would fall into the <strong>Research</strong> level;</li>   <li>\"<em>HTTP (Hypertext Transfer Protocol)</em>\" would fall into the <strong>Technical </strong>level;</li>   <li>\"<em>User (computing)</em>\" would fall into the <strong>General</strong> level; and</li>   <li>\"<em>Figure Painting</em>\" would fall into the <strong>Irrelevant</strong> competency level.</li>  </ul>    <p>By participating in this survey, you will help us evaluate how accurate our system can represent your competence topics.<strong> Note: </strong>If you also participated in our previous study, please note that the competency levels have significantly changed.</p>    <p>Thanks a lot for participating in our study. Please note that we need your answers by the end of <strong>Monday, July 11th</strong> to be able to incorporate them into our paper (due a few days later :)</p>    <p>Kind regards,</p>    <p>Bahar, Felicitas, Birgitta and René</p>  		en																																																																																																																									%n" +
					"SL		surveyls_endtext		<p>Thank you for participating in our evaluation. If you would like to learn more about semantic user profiling of scholars, please refer to our latest publication [1].</p>    <p>Regards,<br />  Bahar, Felicitas, Birgitta and René</p>    <p>[1] <a href=\"http://www.semanticsoftware.info/system/files/savesd16.pdf\">Sateli, B., F. Löffler, B. König-Ries, and R. Witte, \"Semantic User Profiles: Learning Scholars' Competences by Analyzing their Publications\", Semantics, Analytics, Visualisation: Enhancing Scholarly Data (SAVE-SD 2015) Springer, 2016.</a></p>  		en																																																																																																																									%n" +
					"SL		surveyls_email_invite_subj		Invitation to participate in a survey		en																																																																																																																									%n" +
					"SL		surveyls_email_invite		Dear {FIRSTNAME},<br /> <br /> you have been invited to participate in a survey.<br /> <br /> The survey is titled:<br /> \"{SURVEYNAME}\"<br /> <br /> \"{SURVEYDESCRIPTION}\"<br /> <br /> To participate, please click on the link below.<br /> <br /> Sincerely,<br /> <br /> {ADMINNAME} ({ADMINEMAIL})<br /> <br /> ----------------------------------------------<br /> Click here to do the survey:<br /> {SURVEYURL}<br /> <br /> If you do not want to participate in this survey and don't want to receive any more invitations please click the following link:<br /> {OPTOUTURL}<br /> <br /> If you are blacklisted but want to participate in this survey and want to receive invitations please click the following link:<br /> {OPTINURL}		en																																																																																																																									%n" + 
					"SL		surveyls_email_remind_subj		Reminder to participate in a survey		en																																																																																																																									%n" + 
					"SL		surveyls_email_remind		Dear {FIRSTNAME},<br /> <br /> Recently we invited you to participate in a survey.<br /> <br /> We note that you have not yet completed the survey, and wish to remind you that the survey is still available should you wish to take part.<br /> <br /> The survey is titled:<br /> \"{SURVEYNAME}\"<br /> <br /> \"{SURVEYDESCRIPTION}\"<br /> <br /> To participate, please click on the link below.<br /> <br /> Sincerely,<br /> <br /> {ADMINNAME} ({ADMINEMAIL})<br /> <br /> ----------------------------------------------<br /> Click here to do the survey:<br /> {SURVEYURL}<br /> <br /> If you do not want to participate in this survey and don't want to receive any more invitations please click the following link:<br /> {OPTOUTURL}		en																																																																																																																									%n" +
					"SL		surveyls_email_register_subj		Survey registration confirmation		en																																																																																																																									%n" + 
					"SL		surveyls_email_register		Dear {FIRSTNAME},<br /> <br /> You, or someone using your email address, have registered to participate in an online survey titled {SURVEYNAME}.<br /> <br /> To complete this survey, click on the following URL:<br /> <br /> {SURVEYURL}<br /> <br /> If you have any questions about this survey, or if you did not register to participate and believe this email is in error, please contact {ADMINNAME} at {ADMINEMAIL}.		en																																																																																																																									%n" + 
					"SL		surveyls_email_confirm_subj		Confirmation of your participation in our survey		en																																																																																																																									%n" + 
					"SL		surveyls_email_confirm		Dear {FIRSTNAME},<br /> <br /> this email is to confirm that you have completed the survey titled {SURVEYNAME} and your response has been saved. Thank you for participating.<br /> <br /> If you have any further questions about this email, please contact {ADMINNAME} on {ADMINEMAIL}.<br /> <br /> Sincerely,<br /> <br /> {ADMINNAME}		en																																																																																																																									%n" + 
					"SL		surveyls_dateformat		2		en																																																																																																																									%n" + 
					"SL		email_admin_notification_subj		Response submission for survey {SURVEYNAME}		en																																																																																																																									%n" + 
					"SL		email_admin_notification		Hello,<br /> <br /> A new response was submitted for your survey '{SURVEYNAME}'.<br /> <br /> Click the following link to reload the survey:<br /> {RELOADURL}<br /> <br /> Click the following link to see the individual response:<br /> {VIEWRESPONSEURL}<br /> <br /> Click the following link to edit the individual response:<br /> {EDITRESPONSEURL}<br /> <br /> View statistics by clicking here:<br /> {STATISTICSURL}		en																																																																																																																									%n" + 
					"SL		email_admin_responses_subj		Response submission for survey {SURVEYNAME} with results		en																																																																																																																									%n" + 
					"SL		email_admin_responses		Hello,<br /> <br /> A new response was submitted for your survey '{SURVEYNAME}'.<br /> <br /> Click the following link to reload the survey:<br /> {RELOADURL}<br /> <br /> Click the following link to see the individual response:<br /> {VIEWRESPONSEURL}<br /> <br /> Click the following link to edit the individual response:<br /> {EDITRESPONSEURL}<br /> <br /> View statistics by clicking here:<br /> {STATISTICSURL}<br /> <br /> <br /> The following answers were given by the participant:<br /> {ANSWERTABLE}		en																																																																																																																									%n" + 
					"SL		surveyls_numberformat		0		en																																																																																																																									%n");
			for(Competency cmpt: shuffledList){
				pw.printf("G	G%d	%s	1	<em>%s</em>		en																																																																																																																									%n", groupCounter, cmpt.getLabel(), cmpt.getComment());
				// the hidden original rank of the competence topic
				pw.printf("Q	N	C%d	1	Original competence rank		en		Y	N	%d	1																																																																																															1			12																		%n",questionCounter, cmpt.getRank());
				questionCounter++;
				pw.printf("Q	L	C%d	1	What is your competency with respect to <strong>%s</strong>?		en		Y	N		1																1																																																																															1					1																%n" +
						"A	0	A1		<strong>General</strong> – a topic that you have general world knowledge about		en																																																																																																																										%n" + 
						"A	0	A2		<strong>Technical</strong> – a topic that you know about and use in your tasks		en																																																																																																																										%n" +
						"A	0	A3		<strong>Research</strong> – a topic that you know well and you are/have been doing research on		en																																																																																																																										%n" +
						"A	0	A4		<strong>Irrelevant</strong> – a topic that you have no or very limited knowledge about		en																																																																																																																										%n" +
						"Q	T	C%dComment	1	Comment		en			N		1																																																																																															1			12		1																	 %n", questionCounter, cmpt.getLabel(), questionCounter);
				questionCounter++;
				groupCounter++;
			}
			// demographics stuff
			pw.printf("G	G50	Demographics	1	Before we finish the survey, we would like to know a bit more about you.		en																																																																																																																									%n" + 
			"Q	T	D0	1	This is a hidden question		en			N		1																																																																																															1			12		1																	 %n" +
			"Q	L	D1	1	What is your gender?		en		Y	N		1																1																																																																															1					1																%n" + 
			"A	0	A1		Male		en																																																																																																																									%n" + 
			"A	0	A2		Female		en																																																																																																																									%n" +
			"Q	!	D2	1	What is your age?		en		Y	N		1																																																																																															1					1																%n" +
			"A	0	A1		20-24		en																																																																																																																									%n" +
			"A	0	A2		25-29		en																																																																																																																									%n" +
			"A	0	A3		30-34		en																																																																																																																									%n" +
			"A	0	A4		35-39		en																																																																																																																									%n" +
			"A	0	A5		40-44		en																																																																																																																									%n" +
			"A	0	A6		45-49		en																																																																																																																									%n" +
			"A	0	A7		50-54		en																																																																																																																									%n" +
			"A	0	A8		55-59		en																																																																																																																									%n" +
			"A	0	A9		60+		en																																																																																																																									%n" +
			"Q	L	D3	1	What is your highest level of education?		en		Y	N		1																1																																																																															1					1																%n" +
			"A	0	A1		Bachelor's degree		en																																																																																																																									%n" +
			"A	0	A2		Master's degree		en																																																																																																																									%n" +
			"A	0	A3		Doctorate degree		en																																																																																																																									%n" +
			"Q	L	D4	1	What is your current position?		en		Y	N		1																1																																																																															1					1																%n" +
			"A	0	A1		Student		en																																																																																																																									%n" +
			"A	0	A2		Postdoctoral Fellow		en																																																																																																																									%n" +
			"A	0	A3		Research Associate (Academia)		en																																																																																																																									%n" +
			"A	0	A4		Research Scientist (Industry)		en																																																																																																																									%n" +
			"A	0	A5		Professor or Principal Investigator		en																																																																																																																									%n" +
			"Q	T	D5	1	If you have any comments or suggestions about this survey, we would love to know.		en			N		1																																																																																															1			12		1																%n" );
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Competency getInfo(final String uri, final int rank) {
		String queryString = "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT  ?label ?comment WHERE{ OPTIONAL {<DUMBO> rdfs:label ?label . FILTER ( langMatches(lang(?label), \"en\"))} OPTIONAL {<DUMBO> rdfs:comment ?comment. FILTER ( langMatches(lang(?comment), \"en\"))} }";
		// REPLACE THE PLACEHOLDER WITH RESOURCE URI
		String SPARQL = queryString.replaceAll("DUMBO", uri);
		// System.out.println("DBpedia Q:" + SPARQL);

		Query query = QueryFactory.create(SPARQL);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		Competency cmp = null;
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution neSoln = results.nextSolution();
				RDFNode label = neSoln.get("?label");
				
				String lblStr = null;
				if (label == null) {
					//System.out.println("No label available for " + uri);
					lblStr = uri;
				}else {
					lblStr = label.asLiteral().getString();
				}
				// System.out.println(lblStr);
				RDFNode comment = neSoln.get("?comment");
				String commentStr = null;
				if (comment == null) {
					//System.out.println("No comment available for " + uri);
					commentStr = "No description available.";
				}else{
					commentStr = comment.asLiteral().getString();
					commentStr = commentStr.replace("&", "\\&");
					commentStr = commentStr.replace("%", "\\%");
					commentStr = commentStr.replace("#", "\\#");
                    commentStr = commentStr.replace("_", "\\_");
				}
				// System.out.println(comment.asLiteral().getString().substring(0,
				// comment.asLiteral().getString().indexOf(".")+1));
				//System.out.println("commentStr " + commentStr);
				cmp = new Competency(lblStr, commentStr, rank);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			qexec.close();
		}
		return cmp;
	}

}
