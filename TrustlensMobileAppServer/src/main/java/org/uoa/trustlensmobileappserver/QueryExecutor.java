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
	
	private static String old_prefixes = 		"PREFIX ep-plan:<https://w3id.org/ep-plan#> "
									  		+	"PREFIX prov:<http://www.w3.org/ns/prov#> "
									  		+ 	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";
	
	private static String prefixes =	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
									+	"PREFIX owl: <http://www.w3.org/2002/07/owl#>"
									+	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
									+	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
									+	"PREFIX gdpr: <https://w3id.org/GDPRtEXT#>"
									+   "PREFIX ssn: <http://www.w3.org/ns/ssn/>" 
									+ 	"PREFIX prov: <http://www.w3.org/ns/prov#>"
									+	"PREFIX ep-plan: <https://w3id.org/ep-plan#>"
									+	"PREFIX tl: <https://w3id.org/trustlens#>"
									+   "PREFIX vcard:<http://www.w3.org/2006/vcard/ns#>";
	    
	private static String error_msg_not_implemented = "{\"error_message\": \"This query has not been yet implemented on TrustLens server\"}";
	private static String error_msg_not_available = "{\"error_message\": \"This information is not available on TrustLens server\"}";
	private static String error_msg_connecting_database =  "{\"error_message\": \"Error connecting to TrustLens graph database. Please try again later.\"}";
	private static String msg_not_available = "This information is not available";
	
	private static String br = "<br>";
	
	private Map<String, String[]> questionsSparclQueries;
	
	public QueryExecutor() {
		questionsSparclQueries = new HashMap<String, String[]>();
		

		questionsSparclQueries.put("purpose", new String[] 	{	prefixes + "SELECT DISTINCT ?objective ?rationale ?objectiveDescription WHERE { <http://trustlens.org/test_dataset1#HomeMonitoringHighLevelPlan> a ep-plan:Plan; ep-plan:includesObjective ?objective. ?objective a ep-plan:Objective; rdfs:comment ?objectiveDescription; ep-plan:hasRationale ?rationale. } " 
															}	);
		
		questionsSparclQueries.put("access",new String[] 	{	prefixes + 	"SELECT DISTINCT ?agent ?role ?label ?rolelabel ?comment ?rolecomment " + 
																			"WHERE { " + 
																			"?agent a ?role; rdfs:comment ?comment; rdfs:label ?label. " + 
																			"?role rdfs:label ?rolelabel; rdfs:comment ?rolecomment. " + 
																			"FILTER (?role=gdpr:Processor || ?role=gdpr:SubProcessor|| ?role=gdpr:Controller) " + 
																			"} "
																,
																prefixes + 	"SELECT DISTINCT ?agent ?label ?comment ?action ?commentAction ?labelAction " + 
																			"WHERE { " + 
																			"?agent a gdpr:DataSubject;rdfs:comment ?comment; rdfs:label ?label; ep-plan:isPermittedAgentFor ?action. " + 
																			"?action ep-plan:hasInputVariable ?data;rdfs:comment ?commentAction; rdfs:label ?labelAction. " + 
																			"?data a gdpr:PersonalData. " +
																			"} "
																,
																prefixes + 	"SELECT DISTINCT ?agent  ?label ?comment " + 
																			"WHERE { " + 
																			"?agent rdfs:comment ?comment; rdfs:label ?label; ep-plan:isPermittedAgentFor ?action. " + 
																			"{ " + 
																			"?action ep-plan:hasInputVariable ?data. " + 
																			"} " + 
																			"UNION" + 
																			"{ " + 
																			"?action ep-plan:hasInputVariable ?data. " + 
																			"} " +
																			"?data a gdpr:PersonalData " + 
																			"FILTER NOT EXISTS { ?agent a ?role ." + 
																			"FILTER (?role=gdpr:Processor || ?role=gdpr:SubProcessor|| ?role=gdpr:Controller|| ?role=gdpr:DataSubject) " + 
																			"} } "
															});
		questionsSparclQueries.put("contact", new String[] 	{	prefixes + 	"SELECT DISTINCT ?name  ?hasContactOptionLabel ?organizationName ?value " + 
																			"WHERE {  " + 
																			"<_IOT_SYSTEM_URI_>  a ssn:System. " + 
																			"?contactAgent tl:isContactPointForSystem ?system. " +
																			"?contact vcard:value ?value. " +
																			"OPTIONAL { " + 
																			"?contactAgent vcard:organization-name ?organizationName. " + 
																			"} " + 
																			"OPTIONAL { " + 
																			"?contactAgent vcard:fn ?name. " + 
																			"} " + 
																			"?contactAgent ?hasContactOption ?contact. " + 
																			"?contact a vcard:Work.  " + 
																			"?hasContactOption rdfs:label ?hasContactOptionLabel. " + 
																			"VALUES (?typeContact ) { " + 
																			"            ( vcard:email  ) " + 
																			"            ( vcard:address  ) " + 
																			"            ( vcard:telephone  ) " + 
																			"            ( vcard:url  ) " + 
																			"} " + 
																			"} "
															}	);
		questionsSparclQueries.put("sharing",new String[] 	{	prefixes + 	"SELECT DISTINCT ?agent ?label ?comment ?action ?commentAction ?labelAction " + 
																			"WHERE { " + 
																			"?agent a gdpr:DataSubject;rdfs:comment ?comment; rdfs:label ?label; ep-plan:isPermittedAgentFor ?action. " + 
																			"?action ep-plan:hasInputVariable ?data;rdfs:comment ?commentAction; rdfs:label ?labelAction. " + 
																			"?data a gdpr:PersonalData. "
																			+ "} "
																
															});
		questionsSparclQueries.put("data_collection", new String[] 	{	prefixes +	"SELECT  ?dataOutput ?outputLabel ?outputComment " +
																			"WHERE{ " + 
																			"<http://trustlens.org/test_dataset1#IoTSensingDevice1> ssn:implements ?procedure. " + 
																			"?procedure a ep-plan:Plan . " + 
																			"?dataOutput ep-plan:isVariableOfPlan ?procedure; rdfs:comment ?outputComment; rdfs:label ?outputLabel. " + 
																			"?objective ep-plan:isObjectiveOfPlan ?procedure; a ep-plan:Objective; ep-plan:isAchievedBy ?dataOutput.  " + 
																			"}"
															});
		questionsSparclQueries.put("storage", new String[] 	{	prefixes +	"SELECT DISTINCT ?storageStep  ?constraintLabel ?constraintComment  ?dataComment ?dataLabel " + 
																			"Where { " + 
																			"<_IOT_SYSTEM_URI_> a ssn:System. " + 
																			"?system ssn:implements ?plan. " + 
																			"?storageStep a ep-plan:Step; a gdpr:StoreData; ep-plan:hasConstraint ?constraint; ep-plan:isStepOfPlan ?plan. " + 
																			"?constraint rdfs:label ?constraintLabel; rdfs:comment ?constraintComment. " + 
																			"?storageStep ep-plan:hasInputVariable ?var. " + 
																			"?var a gdpr:PersonalData ; rdfs:comment ?dataComment; rdfs:label ?dataLabel. " + 															 
																			"} "
																	});
		questionsSparclQueries.put("identifiable", new String[] 	{	prefixes + 	"Select DIstinct ?variableType ?label  ?comment " + 
																			"WHERE{ " + 
																			"<_IOT_SYSTEM_URI_> ssn:implements ?procedure. " + 
																			"?procedure a ep-plan:Plan . " + 
																			"?variable ep-plan:isVariableOfPlan ?procedure; a ?variableType. " + 
																			" " + 
																			"OPTIONAL { " + 
																			" ?variableType rdfs:label ?label; rdfs:comment ?comment. " + 
																			"} " + 
																			" " + 
																			"FILTER (?variableType = gdpr:PersonalData || ?variableType = gdpr:AnonymousData || ?variableType = gdpr:PseudoAnonymousData )  " + 
																			"} "
																,
																prefixes +	"Select   (count (distinct ?variable) as ?count) " + 
																			"WHERE{ " + 
																			"<_IOT_SYSTEM_URI_> ssn:implements ?procedure. " + 
																			"?procedure a ep-plan:Plan . " + 
																			"?variable ep-plan:isVariableOfPlan ?procedure; a ?variableType. " + 
																			"FILTER (?variableType = gdpr:PersonalData || ?variableType = gdpr:AnonymousData || ?variableType = gdpr:PseudoAnonymousData )  " + 
																			"}"
																,
																prefixes + 	"Select   (count (distinct ?variable) as ?count) " + 
																			"WHERE{ " + 
																			"<_IOT_SYSTEM_URI_> ssn:implements ?procedure. " + 
																			"?procedure a ep-plan:Plan . " + 
																			"?variable ep-plan:isVariableOfPlan ?procedure. " + 
																			"}"																			
																			
															}	);

		questionsSparclQueries.put("privacy", new String[] 	{	prefixes + 	"Select  ?policyLink ?policyComment ?policyLabel ?systemLabel " + 
																			"WHERE{" + 
																			"<_IOT_SYSTEM_OVERALL_URI_> ssn:implements ?procedure; rdfs:label ?systemLabel." + 
																			"?procedure a ep-plan:Plan ." + 
																			"?policyLink a tl:PrivacyPolicy; ep-plan:isPolicyOfPlan ?procedure; rdfs:comment ?policyComment; rdfs:label ?policyLabel." + 
																			"} " 
															}	);
		questionsSparclQueries.put("risk", new String[] 	{	prefixes + 	"SELECT DISTINCT ?riskLabel ?riskComment " +
																			"WHERE { <_IOT_SYSTEM_URI_> a ssn:System. " +
																			"?risk tl:isRiskIndicatorForSystem ?system; rdfs:label ?riskLabel; rdfs:comment ?riskComment. " +
																			"} " 
															}	);
	}
	
	public String executeQuestion(String question_id, IotSystem iot_system) {
			
		String response = error_msg_not_available;
		
		String[] sparclQueries = questionsSparclQueries.get(question_id);
		
		System.out.println("sparclQueries [] ");
		System.out.println(Arrays.toString(sparclQueries));
		
		if ( sparclQueries == null ) 
			response = error_msg_not_implemented;
		else {
			ResultSet[] resultsets =  new ResultSet[sparclQueries.length];
			QueryExecution[] qes = new QueryExecution[sparclQueries.length];
			Pair[] pairs = new Pair[sparclQueries.length];
			
			try {
				for (int i=0; i<sparclQueries.length ; i++) {
					pairs[i] = executeSparclQuery (sparclQueries[i], iot_system);
					resultsets[i] = pairs[i].r;
					qes[i] = pairs[i].q; 
				}
				response = formatResults(question_id, resultsets);
				for (int i=0; i<sparclQueries.length; i++) {
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
	
	public Pair executeSparclQuery (String queryString, IotSystem iot_system) {
		
		System.out.println("iot uri: " + iot_system.getUri());
		queryString = queryString	.replaceAll(iot_system_uri_tag, iot_system.getUri())
									.replaceAll(iot_system_overall_uri_tag, default_sensor_system_overall)
									.replaceAll("\uFEFF", "");
		Query query = QueryFactory.create(queryString);
													 
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:7200/repositories/Test", query);
		ResultSet results = qexec.execSelect();
		// System.out.println("query" + " " + query);
		return new Pair (results, qexec) ;
		
	}

	public String formatResults(String question_id, ResultSet[] resultsets) {
		//System.out.println(getAllLiterals(resultsets));
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
				response = formatResultsPurpose(resultsets);
				break;
			case "sharing": 
				response = getAllLiterals(resultsets);
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
			case "privacy_protected": 
				response = getAllLiterals(resultsets);
				break;	
			case "risk": 
				response = formatResultsRisk(resultsets);
				break;	
			default:
				response = error_msg_not_implemented;
		}

		return response;
	}
	
	public String formatResultsDatacollection( ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];  
		List<String> purpose_ls = new ArrayList<String>();
		while ( rs.hasNext() ) {
			QuerySolution r = rs.next();
			RDFNode ol = r.get("outputLabel");
			RDFNode oc = r.get("outputComment");
			if ( ( ol!= null ) && ( oc!= null ) )
				purpose_ls.add(ol.toString() + br + oc.toString() );
		} 
		s += makeP( "This device produces the following data: " + makeUL(purpose_ls));
		return s;
	}
	
	public String formatResultsIdentifiable( ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];  
		List<String> identifiable_ls = new ArrayList<String>();
		while ( rs.hasNext() ) {
			QuerySolution r = rs.next();
			RDFNode l = r.get("label");
			RDFNode c = r.get("comment");
			if ( ( l!= null ) && ( c!= null ) )
				identifiable_ls .add(l.toString() + ": "+ trimQuotesAndApices(c.toString()) );
		} 
		if ( identifiable_ls.isEmpty() ) {
			identifiable_ls.add("TrustLens has not enough information on the type of data used and produced by this system");
		}
		else {
			// add system name !!!
			s += makeP( "The system works with the following types of data: : " + makeUL(identifiable_ls ));
		}
		
		// add super system !!!
		s += makeP("The systems is also part of a bigger IoT system...");
		
		// check the number of variables
		ResultSet rs1 = resultsets[1];
		ResultSet rs2 = resultsets[2];
		boolean same_count = false;
		while ( rs1.hasNext() && rs2.hasNext() ) {
			QuerySolution r1 = rs1.next();
			QuerySolution r2 = rs2.next();
			RDFNode c1 = r1.get("count");
			RDFNode c2 = r2.get("count");
			if ( ( c1!= null ) && ( c2!= null ) ) {
				System.out.println("c1: " + c1 + ". c2: " +c2);
				if (c1.equals(c2))
					same_count=true;
			}
		} 
		if (!same_count)
			s += makeRed("Warning: We were not able to determine the type of some of the data used by this system");
		
		return s;
	}
	
	public String formatResultsStorage( ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];  
		List<String> ls = new ArrayList<String>();
		while ( rs.hasNext() ) {
			QuerySolution r = rs.next();
			RDFNode dl = r.get("dataLabel");
			RDFNode dc = r.get("dataComment");
			RDFNode cl = r.get("constraintLabel");
			RDFNode cc = r.get("constraintComment");
			if ( ( dl!= null ) && ( dc!= null ) && ( cl!= null ) && ( cc!= null ) )
				ls.add(trimQuotesAndApices(dl.toString()) + ":" + br + trimQuotesAndApices(dc.toString()));
				ls.add(trimQuotesAndApices(cl.toString()) + ":" + br + trimQuotesAndApices(cc.toString()));
		} 
		s += makeP(makeUL(ls));
		return s;
	}
	
	public String formatResultsPurpose( ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];  
		List<String> purpose_ls = new ArrayList<String>();
		while ( rs.hasNext() ) {
			QuerySolution r = rs.next();
			RDFNode n = r.get("objectiveDescription");
			if ( n!= null )
				purpose_ls.add(n.toString());
		} 
		s += makeP( "Purposes of collected data:" + makeUL(purpose_ls));
		return s;
	}
	
	public String formatResultsAccess( ResultSet[] resultsets) {
		String s = new String();
		
		ResultSet rs1 = resultsets[1];  
		List<String> userdataaccess_ls = new ArrayList<String>();
		while ( rs1.hasNext() ) {
			QuerySolution r = rs1.next();
			RDFNode n = r.get("commentAction");
			if ( n!= null )
				userdataaccess_ls.add(n.toString());
		}
		s += makeP( "User Data Access:" + makeUL(userdataaccess_ls));
		
		ResultSet rs0 = resultsets[0];  
		List<String> otherentities_ls = new ArrayList<String>();
		while ( rs0.hasNext() ) {
			QuerySolution r = rs0.next();
			RDFNode l = r.get("label");
			RDFNode rl= r.get("rolelabel");
			RDFNode c = r.get("comment");
			RDFNode rc = r.get("rolecomment");
			if ( ( l!= null ) && ( rl!= null ) && ( c!= null ) && ( rc!= null ) )
				// otherentities_ls.add(l.toString() + br + c.toString() + br + rl.toString() + " (" +trimQuotesAndApices(rc.toString()) + ")" );
				otherentities_ls.add( makeItalic(  "Organization: ") + l.toString() + br 
									+ makeItalic("Description: ") + c.toString() + br 
									+ makeItalic("GDPR Role: ") + rl.toString() + " (" +trimQuotesAndApices(rc.toString()) + ")" );
		}
		if (!otherentities_ls.isEmpty())
			s += makeP( ("Other entities that can access personal data: " + makeUL(otherentities_ls)));
		
		ResultSet rs2 = resultsets[2];  
		List<String> entitiesnotclear_li= new ArrayList<String>();
		while ( rs2.hasNext() ) {
			QuerySolution r = rs2.next();
			RDFNode l = r.get("label");
			RDFNode c = r.get("comment");
			if ( ( l!= null ) && ( c!= null ) )
				entitiesnotclear_li.add(l.toString() + br + c.toString() );
		}
		s += makeRed(makeP( "Entities that have access to your personal data but no clear role in terms of GDPR: "
					+ makeUL(entitiesnotclear_li)));
		
		
		return s;
	}
	
	public String formatResultsContact( ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];  
		List<String[]> lsa = new ArrayList<String[]>();
		List<String> values = new ArrayList<String>();
		while ( rs.hasNext() ) {
			QuerySolution r = rs.next();
			RDFNode n = r.get("name");
			RDFNode cl = r.get("hasContactOptionLabel");
			RDFNode on = r.get("organizationName");
			RDFNode v = r.get("value");
			if ( ( n!= null ) && ( cl!= null ) && ( on!= null ) && ( v!= null ) ) {
				if ( !values.contains(trimQuotesAndApices(v.toString())) && !cl.toString().contains("has") ) {
					lsa.add(new String[] {on.toString(), trimQuotesAndApices(n.toString()), trimQuotesAndApices(cl.toString()) +": " + trimQuotesAndApices(v.toString()) }  );
					values.add(trimQuotesAndApices(v.toString()));
				}
			}
			
		} 
		s += makeP( compactListStringArray(lsa));
		return s;
	}
	
	public String formatResultsPrivacy( ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];  
		List<String> privacy_ls = new ArrayList<String>();
		while ( rs.hasNext() ) {
			QuerySolution r = rs.next();
			RDFNode sl = r.get("systemLabel");
			RDFNode pl = r.get("policyLabel");
			RDFNode pc = r.get("policyComment");
			RDFNode plink = r.get("policyLink");
			if ( ( sl!= null ) && ( pl!= null ) && ( pc!= null ) && ( plink!= null ) )
				privacy_ls.add(sl.toString() + " " + pl.toString() + " " + pc.toString() 
								+ " "  + "<a href='#' onclick=\"event.preventDefault();"
										+ "cordova.InAppBrowser.open('"
										+ plink.toString() + "', '_system', 'location=yes');"
										+ "\">Click to view</a>");
		} 
		s += makeP( "The following associated privacy policies were found:  " + makeUL(privacy_ls));
		return s;
	}
	
	public String formatResultsRisk( ResultSet[] resultsets) {
		String s = new String();
		ResultSet rs = resultsets[0];  
		List<String> ls = new ArrayList<String>();
		while ( rs.hasNext() ) {
			QuerySolution r = rs.next();
			RDFNode rl = r.get("riskLabel");
			RDFNode rc = r.get("riskComment");
			if ( ( rl!= null ) && ( rc!= null ) )
				ls.add(trimQuotesAndApices(rl.toString()) + br + trimQuotesAndApices(rc.toString()) );
		} 
		s += makeP( makeUL(ls));
		return s;
	}
	
	public String formatOldResult( ResultSet[] resultsets) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	String response = "{}";
    	int random = (int)(Math.random() * resultsets.length);
	    ResultSetFormatter.outputAsJSON(baos, resultsets[random]);
	    response = baos.toString();
    	JSONObject jo = new JSONObject(response);
    	response = jo.toString(4);
    	return response;
	}

	public String getAllLiterals(ResultSet[] rses) {
		String s = new String();
		for(int i=0; i<rses.length;i++)
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
		        this.r= r;
		        this.q= q;
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
		 }
		 else {
			 String s = new String();
			 for (String s1 : ls){
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
	 	
	 public String trimQuotesAndApices (String s) {
		 return s.replaceAll("^\"|\"$", "").replaceAll("@en", "").split("\\^\\^")[0];
	 } 
	 	 
	 public String compactListStringArray (List<String[]> lsa) {
		 System.out.println("lsa len " + lsa.size() + " sa len " + lsa.get(0).length);
		 
		 List<String> ls = new ArrayList<String>();
		
		 String lis = "";
		 
		 if ( lsa.get(0).length == 1) {
			 for (String[] sa : lsa)
				 ls.add(sa[0]);
			 return makeUL(ls);
		 }
		 
		 List<String> firstvalues = new ArrayList<String>();
		 for (String[] sa : lsa) {
			 if ( ! firstvalues.contains(sa[0]) )
					 firstvalues.add(sa[0]);
		 }
		 
		 for (String f : firstvalues) {
			 List<String[]> eachfirstvalue_lsa = new ArrayList<String[]>();
			 for (String[] sa : lsa) { 
				 if ( f.equals(sa[0]) ) {
				 	eachfirstvalue_lsa.add(Arrays.copyOfRange(sa, 1, sa.length));					 
				 }
			 }
			 lis += makeUL(makeLI(f + (compactListStringArray(eachfirstvalue_lsa))) );
			 
		 }
		 
		 return lis;
		 
	 }
}

