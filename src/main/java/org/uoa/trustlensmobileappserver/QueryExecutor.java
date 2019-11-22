package org.uoa.trustlensmobileappserver;

import java.io.ByteArrayOutputStream;
import java.util.*;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import org.apache.jena.rdf.model.RDFNode;

//json
import org.json.JSONObject;
import org.uoa.trustlensmobileappserver.data.*;

public class QueryExecutor {
	private static String iot_system_uri_tag = "<_IOT_SYSTEM_URI_>";
	private static String default_iot_system_uri = "<http://trustlens.org/test_dataset1#IoTSensingDevice1>";
	private static String iot_system_overall_uri_tag = "<_IOT_SYSTEM_OVERALL_URI_>";
	private static String default_sensor_system_overall = "<http://trustlens.org/test_dataset1#SensorSystemOverall>";

	private static String old_prefixes = "PREFIX ep-plan:<https://w3id.org/ep-plan#> "
			+ "PREFIX prov:<http://www.w3.org/ns/prov#> " + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";

	private static String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + "PREFIX gdpr: <https://w3id.org/GDPRtEXT#>"
			+ "PREFIX ssn: <http://www.w3.org/ns/ssn/>" + "PREFIX prov: <http://www.w3.org/ns/prov#>"
			+ "PREFIX ep-plan: <https://w3id.org/ep-plan#>" + "PREFIX tl: <https://w3id.org/trustlens#>"
			+ "PREFIX vcard:<http://www.w3.org/2006/vcard/ns#>";

	private static String error_msg_not_implemented = "{\"error_message\": \"This query has not been yet implemented on TrustLens server\"}";
	private static String error_msg_not_available = "{\"error_message\": \"This information is not available on TrustLens server\"}";
	private static String error_msg_connecting_database = "{\"error_message\": \"Error connecting to TrustLens graph database. Please try again later.\"}";
	private static String msg_not_available = "This information is not available";

	private static String br = "<br>";

	private Map<String, String[]> questionsSparclQueries;

	public QueryExecutor() {
		questionsSparclQueries = new HashMap<String, String[]>();

		questionsSparclQueries.put("purpose",
				new String[] { prefixes + "SELECT DISTINCT ?objective  ?objectiveDescription ?system ?parentSystem ?parentSystemLabel WHERE { " + 
						"     {" + 
						"                ?system ssn:implements ?procedure." + 
						"    }" + 
						"    UNION {" + 
						"        ?parentSystem ssn:implements ?procedure;rdfs:label ?parentSystemLabel." + 
						"        ?system ssn:implements ?subProcedure." + 
						"        ?subProcedure ep-plan:isSubPlanOfPlan ?procedure." + 
						"    }" + 
						"				?procedure a ep-plan:Plan; ep-plan:includesObjective ?objective. " + 
						"    ?objective a ep-plan:Objective; rdfs:comment ?objectiveDescription.						" + 
						"    VALUES (?system) {" + 
						"        (<_IOT_SYSTEM_URI_>)" + 
						"    }" + 
						"}" });

		questionsSparclQueries.put("access", new String[] {
				prefixes + "SELECT DISTINCT ?agent ?role ?label ?rolelabel ?comment ?rolecomment " + "WHERE { "
						+ "?agent a ?role; rdfs:comment ?comment; rdfs:label ?label. "
						+ "?role rdfs:label ?rolelabel; rdfs:comment ?rolecomment. "
						+ "FILTER (?role=gdpr:Processor || ?role=gdpr:SubProcessor|| ?role=gdpr:Controller) " + "} ",
				prefixes + "SELECT DISTINCT ?agent ?label ?comment ?action ?commentAction ?labelAction " + "WHERE { "
						+ "?agent a gdpr:DataSubject;rdfs:comment ?comment; rdfs:label ?label; ep-plan:isPermittedAgentFor ?action. "
						+ "?action ep-plan:hasInputVariable ?data;rdfs:comment ?commentAction; rdfs:label ?labelAction. "
						+ "?data a gdpr:PersonalData. " + "} ",
				prefixes + "SELECT DISTINCT ?agent  ?label ?comment " + "WHERE { "
						+ "?agent rdfs:comment ?comment; rdfs:label ?label; ep-plan:isPermittedAgentFor ?action. "
						+ "{ " + "?action ep-plan:hasInputVariable ?data. " + "} " + "UNION" + "{ "
						+ "?action ep-plan:hasInputVariable ?data. " + "} " + "?data a gdpr:PersonalData "
						+ "FILTER NOT EXISTS { ?agent a ?role ."
						+ "FILTER (?role=gdpr:Processor || ?role=gdpr:SubProcessor|| ?role=gdpr:Controller|| ?role=gdpr:DataSubject) "
						+ "} } " });
		questionsSparclQueries.put("contact", new String[] { prefixes
				+ "SELECT DISTINCT ?name  ?hasContactOptionLabel ?organizationName ?contactvalue " + "" + "WHERE { "
				+ "" + "?system a ssn:System." + "?contactAgent tl:isContactPointForSystem ?system." + "" + "OPTIONAL {"
				+ "?contactAgent vcard:organization-name ?organizationName." + "}" + "" + "OPTIONAL {"
				+ "?contactAgent vcard:fn ?name." + "}" + "" + "?contactAgent ?hasContactOption ?contact."
				+ "?contact a vcard:Work; " + " vcard:value ?contactvalue." + ""
				+ "?hasContactOption rdfs:label ?hasContactOptionLabel." + "" + "VALUES (?hasContactOption ) {"
				+ "            ( vcard:email  )" + "            ( vcard:address  )" + "            ( vcard:tel  )"
				+ "            ( vcard:url  )" + "}" + " VALUES (?system)" + "    {" + "        (<_IOT_SYSTEM_URI_>)"
				+ "    }" + "}" });
		
		questionsSparclQueries.put("sharing", new String[] { prefixes
				+ "Select  Distinct ?shareComment ?objectiveComment " + 
				"" + 
				"WHERE{" + 
				"OPTIONAL {" + 
				"    ?system ssn:implements ?procedure." + 
				"    }" + 
				"    OPTIONAL {" + 
				"     ?system    ssn:implements ?subprocedure." + 
				"     ?subprocedure ep-plan:isSubPlanOfPlan ?procedure." + 
				"    }" + 
				"" + 
				"?procedure a ep-plan:Plan ." + 
				"" + 
				"?sharingStep ep-plan:isStepOfPlan ?procedure; a gdpr:ShareDataWithThirdParty; rdfs:comment ?shareComment." + 
				"" + 
				"OPTIONAL {" + 
				"?objective ep-plan:isAchievedBy ?sharingStep; rdfs:comment ?objectiveComment." + 
				"}" + 
				"" + 
				"OPTIONAL {" + 
				"?objective ep-plan:isAchievedBy ?output; rdfs:comment ?objectiveComment." + 
				"?sharingStep ep-plan:hasOutputVariable ?output." + 
				"}" + 
				"" + 
				"VALUES (?system) {" + 
				"(<_IOT_SYSTEM_URI_>)" + 
				"}" + 
			
				"}"

		});
		questionsSparclQueries.put("data_collection", new String[] { prefixes
				+ "SELECT  ?dataOutput ?outputLabel ?outputComment " + "WHERE{ " + "?system ssn:implements ?procedure. "
				+ "?procedure a ep-plan:Plan . "
				+ "?dataOutput ep-plan:isVariableOfPlan ?procedure; rdfs:comment ?outputComment; rdfs:label ?outputLabel. "
				+ "?objective ep-plan:isObjectiveOfPlan ?procedure; a ep-plan:Objective; ep-plan:isAchievedBy ?dataOutput.  "
				+ " VALUES (?system)" + "    {" + "        (<_IOT_SYSTEM_URI_>)" + "    }" + "}" });
		questionsSparclQueries.put("storage", new String[] { prefixes
				+ "SELECT DISTINCT ?storageStep  ?constraintLabel ?constraintComment  ?dataComment ?dataLabel "
				+ "Where { " + "<_IOT_SYSTEM_URI_> a ssn:System. " + "?system ssn:implements ?plan. "
				+ "?storageStep a ep-plan:Step; a gdpr:StoreData; ep-plan:hasConstraint ?constraint; ep-plan:isStepOfPlan ?plan. "
				+ "?constraint rdfs:label ?constraintLabel; rdfs:comment ?constraintComment. "
				+ "?storageStep ep-plan:hasInputVariable ?var. "
				+ "?var a gdpr:PersonalData ; rdfs:comment ?dataComment; rdfs:label ?dataLabel. " + "} " });
		
		questionsSparclQueries.put("identifiable", new String[] { prefixes
				+ "Select DIstinct ?variableType ?label  ?comment ?parentSystem " + 
				"WHERE{" + 
				"{    " + 
				"?system  ssn:implements ?procedure." + 
				"    }" + 
				"    UNION " + 
				"    {" + 
				"        ?system  ssn:implements ?subProcedure." + 
				"        ?parentSystem ssn:implements ?procedure. " + 
				"        ?subProcedure ep-plan:isSubPlanOfPlan ?procedure." + 
				"    }" + 
				"?procedure a ep-plan:Plan ." + 
				"?variable ep-plan:isVariableOfPlan ?procedure; a ?variableType." + 
				"" + 
				"OPTIONAL {" + 
				" ?variableType rdfs:label ?label;  rdfs:comment ?comment." + 
				"}" + 
				"" + 
				"FILTER (?variableType = gdpr:PersonalData || ?variableType = gdpr:AnonymousData || ?variableType = gdpr:PseudoAnonymousData ) " + 
				"" + 
				"    VALUES (?system)" + 
				"    " + 
				"    {" + 
				"        (<_IOT_SYSTEM_URI_>)" + 
				"    }" + 
				"" + 
				"}"

		});

		questionsSparclQueries.put("privacy", new String[] { prefixes
				+ "Select  ?policyLink ?policyComment ?policyLabel " + 
				"" + 
				"WHERE{" + 
				"?system ssn:implements ?procedure." + 
				"?procedure a ep-plan:Plan ." + 
				"" + 
				"?policyLink a tl:PrivacyPolicy;  rdfs:comment ?policyComment; rdfs:label ?policyLabel." + 
				"OPTIONAL {" + 
				"   ?policyLink   ep-plan:isPolicyOfPlan ?procedure." + 
				"         }" + 
				"OPTIONAL {" + 
				"     ?procedure ep-plan:isSubPlanOfPlan ?parentprocedure. " + 
				"     ?policyLink   ep-plan:isPolicyOfPlan ?parentprocedure." + 
				"}" + 
				"    VALUES (?system) {" + 
				"    (<_IOT_SYSTEM_URI_>)" + 
				"}" + 
				"" + 
				"} " });
		
		questionsSparclQueries.put("protected_privacy", new String[] { prefixes + "Select  Distinct ?label " + 
				"" + 
				"WHERE{" + 
				"    OPTIONAL {" + 
				"    ?system ssn:implements ?procedure." + 
				"    }" + 
				"    OPTIONAL {" + 
				"     ?system    ssn:implements ?subprocedure." + 
				"     ?subprocedure ep-plan:isSubPlanOfPlan ?procedure." + 
				"    }" + 
				"" + 
				"?procedure a ep-plan:Plan ." + 
				"" + 
				"?privacypolicy ep-plan:isPolicyOfPlan ?procedure; a tl:PrivacyPolicy. " + 
				"" + 
				"OPTIONAL {" + 
				"{" + 
				"?constraint ep-plan:hasRationale ?privacypolicy; ep-plan:isConstraintOfPlan ?procedure; rdfs:label ?label ." + 
				"}" + 
				"UNION" + 
				" {" + 
				"?step ep-plan:hasRationale ?privacypolicy; ep-plan:isStepOfPlan ?procedure; rdfs:label ?label." + 
				"}" + 
				"" + 
				"}" + 
				"    " + 
				"    VALUES (?system) {" + 
				"        (<http://trustlens.org/test_dataset1#IoTSensingDevice1>)" + 
				"    }" + 
				"" + 
				"}"});
		
		questionsSparclQueries.put("risk", new String[] { prefixes + "SELECT DISTINCT ?riskLabel ?riskComment "
				+ "WHERE { ?system a ssn:System. "
				+ "?risk tl:isRiskIndicatorForSystem ?system; rdfs:label ?riskLabel; rdfs:comment ?riskComment. "
				+ "VALUES (?system) {(<_IOT_SYSTEM_URI_>)}} " });
	}

	public String executeQuestion(String question_id, IotSystem iot_system) {

		String response = error_msg_not_available;
System.out.println (question_id);
		String[] sparclQueries = questionsSparclQueries.get(question_id);

		System.out.println("sparclQueries [] ");
		System.out.println(Arrays.toString(sparclQueries));

		if (sparclQueries == null)
			response = error_msg_not_implemented;
		else {
			ResultSet[] resultsets = new ResultSet[sparclQueries.length];
			QueryExecution[] qes = new QueryExecution[sparclQueries.length];
			Pair[] pairs = new Pair[sparclQueries.length];

			try {
				for (int i = 0; i < sparclQueries.length; i++) {
					pairs[i] = executeSparclQuery(sparclQueries[i], iot_system);
					resultsets[i] = pairs[i].r;
					qes[i] = pairs[i].q;
				}
				response = formatResults(question_id, resultsets, iot_system);
				for (int i = 0; i < sparclQueries.length; i++) {
					qes[i].close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				response = error_msg_connecting_database;
			}
		}
		System.out.println("response: " + response);
		return response;
	}

	public Pair executeSparclQuery(String queryString, IotSystem iot_system) {

		System.out.println("iot uri: " + iot_system.getUri());

		System.out.println("System's high level system" + iot_system.getParentSystemURI() + "NEED TO FIX THIS ");

		queryString = queryString.replaceAll(iot_system_uri_tag, iot_system.getUri())
				.replaceAll(iot_system_overall_uri_tag, default_sensor_system_overall).replaceAll("\uFEFF", "");
		Query query = QueryFactory.create(queryString);

		System.out.println("Executing query:" + queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:7200/repositories/Test", query);
		ResultSet results = qexec.execSelect();
		// System.out.println("query" + " " + query);
		return new Pair(results, qexec);

	}

	public String formatResults(String question_id, ResultSet[] resultsets, IotSystem iot_system) {
		// System.out.println(getAllLiterals(resultsets));
		String response = new String();
		switch (question_id) {
		case "data_collection":
			response = formatResultsDatacollection(resultsets);
			break;
		case "identifiable":
			response = formatResultsIdentifiable(resultsets);
			break;
		case "storage":
			response = formatResultsStorage(resultsets);
			break;
		case "purpose":
			response = formatResultsPurpose(resultsets, iot_system);
			break;
		case "sharing":
			response = formatResultsSharing(resultsets);
			break;
		case "access":
			response = formatResultsAccess(resultsets);
			break;
		case "contact":
			response = formatResultsContact(resultsets);
			break;
		case "privacy":
			response = formatResultsPrivacy(resultsets);
			break;
		case "protected_privacy":
			response = formatResultsPrivacyProtected(resultsets);
			break;
		case "risk":
			response = formatResultsRisk(resultsets);
			break;
		default:
			response = error_msg_not_implemented;
		}

		return response;
	}

	

	

	public String formatResultsDatacollection(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		// List<String> purpose_ls = new ArrayList<String>();
		HashMap data_collection_results = new HashMap<String, String>();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode ol = r.get("outputLabel");
			RDFNode oc = r.get("outputComment");
			if ((ol != null) && (oc != null))
				// purpose_ls.add(ol.toString() + br + oc.toString() );
				data_collection_results.put(ol.toString(), oc.toString());
		}
		// s += makeP( "This device produces the following data: " +
		// makeUL(purpose_ls));
		return "<h4>This device produces the following data </h4><hr>"
				+ new SimpleCollapseCard("collection", data_collection_results, Constants.GREEN_COLOR).getHTML();
	}

	public String formatResultsIdentifiable(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		List<String> identifiable_ls = new ArrayList<String>();
		HashMap <String, String> currentSystem = new HashMap <String, String> ();
		HashMap <String, String> parentSystem = new HashMap <String, String> ();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode l = r.get("label");
			RDFNode c = r.get("comment");
			RDFNode parent = r.get("parentSystem");
			
			
			//handle current system 
			if ((l != null) && (c != null)&&(parent ==null)) {
				//identifiable_ls.add(l.toString() + ": " + trimQuotesAndApices(c.toString()));
				currentSystem.put(cleanLitteralValue(l.toString()),cleanLitteralValue(c.toString()));
			}
		   
			//handle parent system 
			if ((l != null) && (c != null)&&(parent!=null)) {
				System.out.println("here");
				//identifiable_ls.add(l.toString() + ": " + trimQuotesAndApices(c.toString()));
				parentSystem.put(cleanLitteralValue(l.toString()),cleanLitteralValue(c.toString()));
			}
		
		}
		
		//if (identifiable_ls.isEmpty()) {
		if (parentSystem.isEmpty()&&currentSystem.isEmpty()) {
			//identifiable_ls
			//		.add("TrustLens has not enough information on the type of data used and produced by this system");
			s+= "We do not have enough information on the type of data used and produced by this system";
		} else {
			// add system name !!!
			//s += makeP("The system works with the following types of data: : " + makeUL(identifiable_ls));
			if (!currentSystem.isEmpty()) {
			
				s+="<h4>The current system works with the following types of data</h4><hr>";	
				s += new SimpleCollapseCard("data_types_current", currentSystem, Constants.GREEN_COLOR).getHTML();
				/*
				// check the number of variables
				ResultSet rs1 = resultsets[1];
				ResultSet rs2 = resultsets[2];
				boolean same_count = false;
				while (rs1.hasNext() && rs2.hasNext()) {
					QuerySolution r1 = rs1.next();
					QuerySolution r2 = rs2.next();
					RDFNode c1 = r1.get("count");
					RDFNode c2 = r2.get("count");
					if ((c1 != null) && (c2 != null)) {
						System.out.println("c1: " + c1 + ". c2: " + c2);
						if (c1.equals(c2))
							same_count = true;
					}
				}
				if (!same_count)
					s += makeRed("Warning: We were not able to determine the type of some of the data used by this system");
					*/
			}
			
		
			if (!parentSystem.isEmpty()) {
				s+="<h4>The parent system works with the following types of data</h4><hr>";	
				s += new SimpleCollapseCard("data_types_parent", parentSystem, Constants.GREEN_COLOR).getHTML();
				
			}
		}

		// add super system !!!
		//s += makeP("The systems is also part of a bigger IoT system...");
/*
		// check the number of variables
		ResultSet rs1 = resultsets[1];
		ResultSet rs2 = resultsets[2];
		boolean same_count = false;
		while (rs1.hasNext() && rs2.hasNext()) {
			QuerySolution r1 = rs1.next();
			QuerySolution r2 = rs2.next();
			RDFNode c1 = r1.get("count");
			RDFNode c2 = r2.get("count");
			if ((c1 != null) && (c2 != null)) {
				System.out.println("c1: " + c1 + ". c2: " + c2);
				if (c1.equals(c2))
					same_count = true;
			}
		}
		if (!same_count)
			s += makeRed("Warning: We were not able to determine the type of some of the data used by this system");
*/
		return s;
	}

	public String formatResultsStorage(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		List<String> ls = new ArrayList<String>();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode dl = r.get("dataLabel");
			RDFNode dc = r.get("dataComment");
			RDFNode cl = r.get("constraintLabel");
			RDFNode cc = r.get("constraintComment");
			if ((dl != null) && (dc != null) && (cl != null) && (cc != null))
				ls.add(trimQuotesAndApices(dl.toString()) + ":" + br + trimQuotesAndApices(dc.toString()));
			ls.add(trimQuotesAndApices(cl.toString()) + ":" + br + trimQuotesAndApices(cc.toString()));
		}
		s += makeP(makeUL(ls));
		return s;
	}

	public String formatResultsPurpose(ResultSet[] resultsets, IotSystem iot_system) {
		String s = new String();
		ResultSet rs = resultsets[0];
		List<String> purpose_ls = new ArrayList<String>();
		List<String> purpose_ls_parent = new ArrayList<String>();
		HashMap <String, ArrayList> objectives = new HashMap<String, ArrayList>();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode n = r.get("objectiveDescription");
			RDFNode parent = r.get("parentSystem");
			if (n != null&&parent!=null) {
				if (objectives.containsKey("Parent System")) {
				objectives.get("Parent System").add(cleanLitteralValue(n.toString()));
				}
				else {
					ArrayList objectiveItems = new ArrayList ();
					objectiveItems.add(cleanLitteralValue(n.toString()));
					objectives.put("Parent System",objectiveItems);
				}
			}
			
			if (n != null&&parent==null) {
				
				if (objectives.containsKey("Current System")) {
					objectives.get("Current System").add(cleanLitteralValue(n.toString()));
					}
					else {
						ArrayList objectiveItems = new ArrayList ();
						objectiveItems.add(cleanLitteralValue(n.toString()));
						objectives.put("Current System",objectiveItems);
					}
			}
			
			

		}
        
		//MM: this is a horrible way to do this but no time so we might fix it later
		HashMap <String, String>finalObjectives = new HashMap <String, String>  (); 
		
		Iterator it = objectives.keySet().iterator();
		
		while (it.hasNext()) {
			String key = (String) it.next();
			//prepare html list 
			ArrayList objectiveItemsList = objectives.get(key);
			String htmlObjectives = "<ul>"; 
			for (int i =0; i< objectiveItemsList.size();i++) {
				htmlObjectives+= "<li>"+objectiveItemsList.get(i)+"</li>";
			}
			htmlObjectives += "</ul>"; 
			finalObjectives.put(key, htmlObjectives);
		}
		
		s += "<h4>Purpose of the IoT system</h4><hr>";
		
		//System.out.println("Parent" + purpose_ls_parent.size() + "Sub system" + purpose_ls.size());

		// s += makeP( "Purposes of collected data:" + makeUL(purpose_ls));
		// return s;

		//return new CardSliderHTML((ArrayList<String>) purpose_ls, (ArrayList<String>) purpose_ls_parent).getHTML();
		
		s += new SimpleCollapseCard("purpose", finalObjectives, Constants.GREEN_COLOR).getHTML();
 		
		return s;
	}

	public String formatResultsAccess(ResultSet[] resultsets) {
		String s = new String();

		s += "<div class = \"alert alert-info\"> <strong> Info</strong> This section lists all entities that have access to personal data within the IoT system (including it's parent system) </div>";

		ResultSet rs1 = resultsets[1];
		List<String> userdataaccess_ls = new ArrayList<String>();
		HashMap userdataaccessMap = new HashMap<String, String>();
		while (rs1.hasNext()) {
			QuerySolution r = rs1.next();
			RDFNode n = r.get("commentAction");
			if (n != null)
				// userdataaccess_ls.add(n.toString());
				// Fix this this should be label of the action
				userdataaccessMap.put("User Data Acces", cleanLitteralValue(n.toString()));
		}
		// s += makeP( "<h3>User Actions</h3><hr>" + makeUL(userdataaccess_ls));

		s += "<h4>User Actions</h4><hr>"
				+ new SimpleCollapseCard("usraction", userdataaccessMap, Constants.GREEN_COLOR).getHTML() + "<hr>";

		ResultSet rs0 = resultsets[0];
		List<String> otherentities_ls = new ArrayList<String>();
		HashMap otherEntities = new HashMap<String, String>();
		while (rs0.hasNext()) {
			QuerySolution r = rs0.next();
			RDFNode l = r.get("label");
			RDFNode rl = r.get("rolelabel");
			RDFNode c = r.get("comment");
			RDFNode rc = r.get("rolecomment");
			/*
			 * if ( ( l!= null ) && ( rl!= null ) && ( c!= null ) && ( rc!= null ) ) //
			 * otherentities_ls.add(l.toString() + br + c.toString() + br + rl.toString() +
			 * " (" +trimQuotesAndApices(rc.toString()) + ")" ); otherentities_ls.add(
			 * makeItalic( "Organization: ") + l.toString() + br +
			 * makeItalic("Description: ") + c.toString() + br + makeItalic("GDPR Role: ") +
			 * rl.toString() + " (" +trimQuotesAndApices(rc.toString()) + ")" );
			 */

			if ((l != null) && (rl != null) && (c != null) && (rc != null)) {

				String label = cleanLitteralValue(l.toString()) + " (" + cleanLitteralValue(rl.toString()) + ")";
				otherEntities.put(label, cleanLitteralValue(c.toString()));
			}
		}

		/*
		 * if (!otherentities_ls.isEmpty()) { s += makeP(
		 * ("Other entities that can access personal data: " +
		 * makeUL(otherentities_ls))); }
		 */
		if (otherEntities.keySet().size() > 0) {
			s += " <h4>Other entities that can access personal data</h4><hr>"
					+ new SimpleCollapseCard("otherEntities", otherEntities, Constants.GREEN_COLOR).getHTML() + "<hr>";
		}

		ResultSet rs2 = resultsets[2];
		List<String> entitiesnotclear_li = new ArrayList<String>();
		HashMap noRoleEntities = new HashMap<String, String>();
		while (rs2.hasNext()) {
			QuerySolution r = rs2.next();
			RDFNode l = r.get("label");
			RDFNode c = r.get("comment");
			/*
			 * if ( ( l!= null ) && ( c!= null ) ) entitiesnotclear_li.add(l.toString() + br
			 * + c.toString() );
			 */
			if ((l != null) && (c != null)) {
				noRoleEntities.put(cleanLitteralValue(l.toString()), cleanLitteralValue(c.toString()));
			}
		}
		// s += makeRed(makeP( "Entities with access to personal data but no clear role
		// in terms of GDPR: "
		// + makeUL(entitiesnotclear_li)));

		s += "<h4>Entities with access to personal data but no clear GDPR role</h4><hr> "
				+ new SimpleCollapseCard("noRoleEntities", noRoleEntities, Constants.RED_COLOR).getHTML();
		return s;
	}

	public String formatResultsContact(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		// List<String[]> lsa = new ArrayList<String[]>();
		ArrayList<String> values = new ArrayList<String>();
		/*
		 * while ( rs.hasNext() ) { QuerySolution r = rs.next(); RDFNode n =
		 * r.get("name"); RDFNode cl = r.get("hasContactOptionLabel"); RDFNode on =
		 * r.get("organizationName"); RDFNode v = r.get("value"); if ( ( n!= null ) && (
		 * cl!= null ) && ( on!= null ) && ( v!= null ) ) { if (
		 * !values.contains(trimQuotesAndApices(v.toString())) &&
		 * !cl.toString().contains("has") ) { lsa.add(new String[] {on.toString(),
		 * trimQuotesAndApices(n.toString()), trimQuotesAndApices(cl.toString()) +": " +
		 * trimQuotesAndApices(v.toString()) } );
		 * values.add(trimQuotesAndApices(v.toString())); } }
		 * 
		 * }
		 */
		// s += makeP( compactListStringArray(lsa));

		// BIIIIG ASSUMPTION AS NO TIME - > WE KNOW WE WILL ONLY HAVE ONE CONTACT - FIX
		// NEEDED IF MORE CONTACTS POSSIBLE

		String name = "";
		String organisation = "";
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode n = r.get("name");
			RDFNode cl = r.get("hasContactOptionLabel");
			RDFNode on = r.get("organizationName");
			RDFNode v = r.get("contactvalue");

			name = cleanLitteralValue(n.toString());
			organisation = cleanLitteralValue(on.toString());
			String conntactValue = cleanLitteralValue(v.toString());
			if (cl.toString().contains("email")) {
				conntactValue = "<span class=\"glyphicon glyphicon-envelope\" aria-hidden=\"true\"></span> "
						+ conntactValue;
			}

			if (cl.toString().contains("telephone")) {
				conntactValue = "<span class=\"glyphicon glyphicon-earphone\" aria-hidden=\"true\"></span> "
						+ conntactValue;
			}
			values.add(conntactValue);

		}

		return new ContactCard(name, organisation, (ArrayList) values).getHTML();
	}

	public String formatResultsPrivacy(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		List<String> privacy_ls = new ArrayList<String>();
		HashMap <String, String> policies = new HashMap <String, String> ();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			
			RDFNode pl = r.get("policyLabel");
			RDFNode pc = r.get("policyComment");
			RDFNode plink = r.get("policyLink");
			if ( (pl != null) && (pc != null) && (plink != null)) {
				
				policies.put(cleanLitteralValue(pl.toString()), pc.toString() + " "
						+ "<a href='#' onclick=\"event.preventDefault();" + "cordova.InAppBrowser.open('"
						+ plink.toString() + "', '_system', 'location=yes');" + "\">Click to view</a>");
				/*
				privacy_ls.add(sl.toString() + " " + pl.toString() + " " + pc.toString() + " "
						+ "<a href='#' onclick=\"event.preventDefault();" + "cordova.InAppBrowser.open('"
						+ plink.toString() + "', '_system', 'location=yes');" + "\">Click to view</a>");
						*/
			}
		}
        
		s += "<div class = \"alert alert-info\"> <strong> Info</strong> This section lists all privacy policies associated with the IoT system (including it's parent system) </div>";

		
		s += new SimpleCollapseCard("policy", policies, Constants.GREEN_COLOR).getHTML();
		return s;
	}

	public String formatResultsRisk(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		List<String> ls = new ArrayList<String>();
		String riskLevel = "";
		String riskExplanation = "";
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode rl = r.get("riskLabel");
			RDFNode rc = r.get("riskComment");
			if ((rl != null) && (rc != null)) {
				//ls.add(cleanLitteralValue(rl.toString()) + br + cleanLitteralValue(rc.toString()));
				riskLevel= cleanLitteralValue(rl.toString());
				riskExplanation = cleanLitteralValue(rc.toString());
				
				String warning_level = ""; 
				if (riskLevel.equals("Low risk")) {
					warning_level = "alert-success";
				}
		if (riskLevel.equals("Medium risk")) {
			warning_level = "alert-warning";
				}
		if (riskLevel.equals("High risk")) {
			warning_level = "alert-danger";
			
		}
				System.out.println (riskLevel);
				s+= "<div class = \"alert "+warning_level+"\"> <strong> "+riskLevel+" </strong>  "+riskExplanation+" </div>";
			}
		}
		//s += makeP(makeUL(ls));	
		return s;
	}
	
	private String formatResultsPrivacyProtected(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		HashMap protected_privacy = new HashMap<String, String>();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode l = r.get("label");
			//RDFNode c = r.get("comment");
			if ((l != null) ) {
				protected_privacy.put(cleanLitteralValue(l.toString()), "no additional description found");
			}
		}
		
		s += "<div class = \"alert alert-info\"> <strong> Info</strong> This section lists all privacy protection mechanisms used by the IoT system (including it's parent system) </div>";

		
		s += "<h4>Privacy protection mechanisms used</h4><hr> "
				+ new SimpleCollapseCard("privacy_protection", protected_privacy, Constants.GREEN_COLOR).getHTML();
		
		return s;
	}
	
	private String formatResultsSharing(ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];
		HashMap sharing = new HashMap<String, String>();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			RDFNode l = r.get("shareComment");
			RDFNode c = r.get("objectiveComment");
			if ((l != null) ) {
				String comment = "no additional description found";
				if (c != null) {
					comment = cleanLitteralValue(c.toString());
				}
				
				sharing.put(cleanLitteralValue(l.toString()), comment);
			}
		}
		
		s += "<div class = \"alert alert-info\"> <strong> Info</strong> This section lists information related to data sharing with third parties  within the IoT system (including it's parent system) </div>";

		
		s += "<h4>Data sharing</h4><hr> "
				+ new SimpleCollapseCard("sharing", sharing, Constants.GREEN_COLOR).getHTML();
		
		return s;
		
	}
	

	public String formatOldResult(ResultSet[] resultsets) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String response = "{}";
		int random = (int) (Math.random() * resultsets.length);
		ResultSetFormatter.outputAsJSON(baos, resultsets[random]);
		response = baos.toString();
		JSONObject jo = new JSONObject(response);
		response = jo.toString(4);
		return response;
	}

	public String getAllLiterals(ResultSet[] rses) {
		String s = new String();
		for (int i = 0; i < rses.length; i++)
			s += getAllLiterals(rses[i]);
		return s;
	}

	public String getAllLiterals(ResultSet rs) {
		String s = new String();
		while (rs.hasNext()) {
			s += rs.next();
		}
		return s;
	}

	public class Pair {
		public final ResultSet r;
		public final QueryExecution q;

		public Pair(ResultSet r, QueryExecution q) {
			this.r = r;
			this.q = q;
		}
	}

	public String makeP(String s) {
		return "<p>" + s + "</p>";
	}

	public String makeUL(String s) {
		return "<ul>" + s + "</ul>";
	}

	public String makeUL(List<String> ls) {
		if (ls.size() == 0) {
			return makeUL(makeLI(msg_not_available));
		} else {
			String s = new String();
			for (String s1 : ls) {
				s += makeLI(s1);
			}
			return makeUL(s);
		}

	}

	public String makeLI(String s) {
		return "<li>" + s + "</li>";
	}

	public String makeBold(String s) {
		return "<span style=\"font-weight:bold\">" + s + "</span>";
	}

	public String makeItalic(String s) {
		return "<span style=\"font-style:italic\">" + s + "</span>";
	}

	public String makeRed(String s) {
		return "<span style=\"color:red\">" + s + "</span>";
	}

	public String trimQuotesAndApices(String s) {
		return s.replaceAll("^\"|\"$", "").replaceAll("@en", "").split("\\^\\^")[0];
	}

	public String compactListStringArray(List<String[]> lsa) {
		System.out.println("lsa len " + lsa.size() + " sa len " + lsa.get(0).length);

		List<String> ls = new ArrayList<String>();

		String lis = "";

		if (lsa.get(0).length == 1) {
			for (String[] sa : lsa)
				ls.add(sa[0]);
			return makeUL(ls);
		}

		List<String> firstvalues = new ArrayList<String>();
		for (String[] sa : lsa) {
			if (!firstvalues.contains(sa[0]))
				firstvalues.add(sa[0]);
		}

		for (String f : firstvalues) {
			List<String[]> eachfirstvalue_lsa = new ArrayList<String[]>();
			for (String[] sa : lsa) {
				if (f.equals(sa[0])) {
					eachfirstvalue_lsa.add(Arrays.copyOfRange(sa, 1, sa.length));
				}
			}
			lis += makeUL(makeLI(f + (compactListStringArray(eachfirstvalue_lsa))));

		}

		return lis;

	}

	/**
	 * remove parts after @ and ^^ indicating language and type method could be
	 * improved ...
	 **/
	private String cleanLitteralValue(String input) {
		String cleanString = input;
		for (int i = input.length() - 1; i > 0; i--) {
			if (input.charAt(i) == '^') {
				cleanString = input.substring(0, i - 1);
			}
			if (input.charAt(i) == '@' && input.length() - i == 3) {
				cleanString = input.substring(0, i);
			}
		}
		return cleanString;
	}
}
