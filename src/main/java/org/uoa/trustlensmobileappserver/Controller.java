package org.uoa.trustlensmobileappserver;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.servlet.annotation.WebFilter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.uoa.trustlensmobileappserver.data.*;
import org.uoa.trustlensmobileappserver.input.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.HttpHeaders;

// jena
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DC;

// json
import org.json.JSONObject;

@RestController
public class Controller {
	private static String prefixes = "PREFIX ep-plan:<https://w3id.org/ep-plan#> PREFIX prov:<http://www.w3.org/ns/prov#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";

	private static Question[] questions = { new Question("data_collection", "Data collection", 2, ""),
			new Question("identifiable", "Is collected data identifiable?", 2, ""),
			new Question("storage", "Duration of Storage", 1, ""),

			new Question("purpose", "Purpose of collected data", 3, prefixes
					+ "SELECT DISTINCT ?objective ?rationale ?objectiveDescription WHERE { <http://trustlens.org/test_dataset1#HomeMonitoringHighLevelPlan> a ep-plan:Plan; ep-plan:includesObjective ?objective. ?objective a ep-plan:Objective; rdfs:comment ?objectiveDescription; ep-plan:hasRationale ?rationale. } "),
			new Question("sharing", "Data Sharing", 3,
					prefixes + "SELECT  ?a ?b ?c  WHERE {     ?a ?b ?c . }  LIMIT 1"),

			new Question("access", "Data Access", 1, ""), new Question("contact", "Contact Information", 1, ""),
			new Question("privacy", "Privacy Policy", 2, ""),
			new Question("protected_privacy", "How your Privacy is protected", 3, ""),
			new Question("risk", "Risk indicator", 1, ""),

	};

	private static QuestionButton[] questionbuttons;

	private static String default_iot_system_uri = "<http://trustlens.org/test_dataset1#IoTSensingDevice1>";
	private static String default_sensor_system_overall = "<http://trustlens.org/test_dataset1#SensorSystemOverall>";

	private static IotSystem[] iotsystems = {
			new LocatedIotSystem("id1", "Meston Air quality sensor", "Sensor description", "Meston Buidling, Aberdeen",
					0, 0, "AB24 3EU", default_iot_system_uri, ""),
			new LocatedIotSystem("id2", "SDRL Motion sensor", "Sensor description", "Sir Duncan Rice Library, Aberdeen",
					0, 0, "AB24 3AA", default_iot_system_uri, ""),
			new QrcodeLocatedIotSystem("id3", "Tillydrone Ballard", "Sensor description", "Tillydrone, Aberdeen", 1, 1,
					"AB24 2UD", "AAA", "", ""),
			new QrcodeIotSystem("id4", "Kettle", "Kettle description", "BBB", "", ""),
			new QrcodeIotSystem("id5", "Samsung A40", "Samsung A40 description", "GH68-48395A", "", ""), };

	private static LocatedIotSystem[] locatediotsystems;
	private static QrcodeIotSystem[] qrcodeiotsystems;

	// should be PostMapping
	@RequestMapping("/question")
	@CrossOrigin(origins = "*")
	public ResponseEntity<String> question(@RequestBody questionPostParameters parameters) {

		System.out.println("/question POST parameters: " + parameters.toString());

		String response = queryexecutor.executeQuestion(parameters.getQuestion_id(),
				getIotSystemFromId(parameters.getSystem_id()));

		System.out.println(response);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	private static QueryExecutor queryexecutor = new QueryExecutor();

	private static float earth_radius = 6371000;

	public Controller() {
		questionbuttons = new QuestionButton[questions.length];
		for (int i = 0; i < questions.length; i++)
			questionbuttons[i] = questions[i].toQuestionButton();

		int locatediotsystems_count = 0;
		int qrcodeiotsystems_count = 0;
		for (int i = 0; i < iotsystems.length; i++) {
			IotSystem iotsys = iotsystems[i];
			if ((iotsys instanceof LocatedIotSystem) || (iotsys instanceof QrcodeLocatedIotSystem)) {
				locatediotsystems_count++;
			}
			if ((iotsys instanceof QrcodeIotSystem) || (iotsys instanceof QrcodeLocatedIotSystem)) {
				qrcodeiotsystems_count++;
			}
		}

		System.out.println(locatediotsystems_count + " " + qrcodeiotsystems_count);

		locatediotsystems = new LocatedIotSystem[locatediotsystems_count];
		qrcodeiotsystems = new QrcodeIotSystem[qrcodeiotsystems_count];
		for (int i = 0, j = 0, k = 0; i < iotsystems.length; i++) {
			IotSystem iotsys = iotsystems[i];
			if ((iotsys instanceof QrcodeLocatedIotSystem)) {
				locatediotsystems[j] = ((QrcodeLocatedIotSystem) iotsys);
				qrcodeiotsystems[k] = ((QrcodeLocatedIotSystem) iotsys).toQrcodeIotSystem();
				j++;
				k++;
			} else if ((iotsys instanceof LocatedIotSystem)) {
				locatediotsystems[j] = (LocatedIotSystem) iotsys;
				j++;
			} else if ((iotsys instanceof QrcodeIotSystem)) {
				qrcodeiotsystems[k] = (QrcodeIotSystem) iotsys;
				k++;
			}

		}
	}

	@RequestMapping("/greeting")
	public String greeting(@RequestParam(value = "parameter", defaultValue = "") String parameter) {

		String queryString = "SELECT DISTINCT ?step  WHERE { <http://trustlens.org/test_dataset1#HomeMonitoringHighLevelPlan> a ep-plan:Plan; ep-plan:includesStep ?step. ?step a ep-plan:Step.  } ";
		queryString = queryString.replace("\"", "").replace("\n", " ");
		Query query = QueryFactory.create(prefixes + queryString.replaceAll("\uFEFF", ""));
		// Query query = QueryFactory.create(queryString) ;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String response = "{}";

		try {
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:7200/repositories/Test",
					query);
			ResultSet results = qexec.execSelect();
			ResultSetFormatter.outputAsJSON(baos, results);
			qexec.close();
			response = baos.toString();
			JSONObject jo = new JSONObject(response);
			response = jo.toString(4);
		} catch (Exception e) {
			System.out.println("Exception");
			e.printStackTrace();
		}

		return response;
	}

	@RequestMapping("/greetingjena")
	public String greetinjena(@RequestParam(value = "parameter", defaultValue = "") String parameter) {

		String response = "{}";

		return response;

	}

	@RequestMapping("/old_question")
	@CrossOrigin(origins = "*")
	public ResponseEntity<String> old_question(@RequestBody questionPostParameters parameters) {

		System.out.println("/question POST parameters: " + parameters.toString());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// initialise response to error message
		String response = "{\"error_message\": \"This information is not available on TrustLens server\"}";

		String queryString = retrieveSparqlQueryString(parameters.getQuestion_id()).replace("\"", "").replace("\n",
				" ");

		System.out.println(queryString);

		if (queryString.equals(""))
			response = "{\"error_message\": \"This query has not been yet implemented on TrustLens server\"}";
		else {
			try {

				Query query = QueryFactory.create(queryString.replaceAll("\uFEFF", ""));
				// Query query = QueryFactory.create(queryString) ;

				QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:7200/repositories/Test",
						query);
				ResultSet results = qexec.execSelect();
				ResultSetFormatter.outputAsJSON(baos, results);
				qexec.close();
				response = baos.toString();
				JSONObject jo = new JSONObject(response);
				response = jo.toString(4);
			} catch (Exception e) {
				System.out.println("Exception");
				e.printStackTrace();
				response = "{\"error_message\": \"Error connecting to TrustLens graph database. Please try again later.\"}";
			}
		}

		// HttpHeaders hh = new HttpHeaders();
		// hh.add("Access-Control-Allow-Origin", "*");

		response = queryexecutor.executeQuestion(parameters.getQuestion_id(),
				getIotSystemFromId(parameters.getSystem_id()));

		System.out.println(response);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	private static String retrieveSparqlQueryString(String question_id) {
		String sparclQuery = new String("");
		for (int i = 0; i < questions.length; i++) {
			if (questions[i].getId().equals(question_id))
				sparclQuery = questions[i].getSparclQuery();
		}
		System.out.println("sparclQuery: " + sparclQuery);
		return sparclQuery;
	}

	// GET questionbuttons
	@RequestMapping(value = "/questionbuttons", method = RequestMethod.GET)
	@CrossOrigin(origins = "*")
	@ResponseBody
	public QuestionButton[] questionbuttons() {
		return questionbuttons;
	}

	// should be PostMapping
	@RequestMapping("/locatesystems")
	@CrossOrigin(origins = "*")
	public ResponseEntity<IotSystem[]> locatesystems(@RequestBody locatesystemsPostParameters parameters) {

		System.out.println("locatesystems POST parameters : " + parameters.toString());

		IotSystem[] locatediotsys;
		if (parameters.getMode() == 1) {
			locatediotsys = retrieveLocatedSystem(parameters.getPostcode(), parameters.getRadius());
		} else {
			locatediotsys = retrieveLocatedSystem(parameters.getLatitude(), parameters.getLongitude(),
					parameters.getRadius());
		}
		return new ResponseEntity<IotSystem[]>(locatediotsys, HttpStatus.OK);
	}

	public IotSystem[] retrieveLocatedSystem(String postcode, int radius) {
		String ordnancePostcode = "http://data.ordnancesurvey.co.uk/id/postcodeunit/" + postcode.replaceAll(" ", "");
		String[] latlon = getLatLonFromPostcodeOrdnancesurvey(ordnancePostcode);
		System.out.println(latlon[0] + latlon[1]);
		double lat = Double.parseDouble(trimApices(latlon[0]));
		double lon = Double.parseDouble(trimApices(latlon[1]));
		return retrieveLocatedSystem(lat, lon, radius);
		// return locatediotsystems;
	}

	public IotSystem[] retrieveLocatedSystem(double latitude, double longitude, int radius) {
		Set<String> locationPostcodes = getPostcodesUriOrdnancesurvey(latitude, longitude, radius);
		System.out.println(Arrays.toString(locationPostcodes.toArray()));

		Set<String> deploymentPostcodes = retrieveDeploymentPostcodes();

		Set<String> postcodes = new HashSet<String>();
		for (String s : locationPostcodes) {
			if (deploymentPostcodes.contains(s))
				postcodes.add(s);
		}

		System.out.println(Arrays.toString(postcodes.toArray()));

		Set<IotSystem> set = new HashSet<IotSystem>();

		for (String p : postcodes) {
			Set<IotSystem> partial_set = new HashSet<IotSystem>();
			partial_set = retrieveIotsytemByPostcode(p);
			for (IotSystem i : partial_set)
				set.add(i);
		}

		return set.toArray(new IotSystem[set.size()]); // locatediotsystems;
	}

	// should be PostMapping
	@RequestMapping("/systemdetails")
	@CrossOrigin(origins = "*")
	// change
	public ResponseEntity<IotSystem> systemdetails(@RequestBody systemdetailsParameters parameters) {

		System.out.println("/systemdetails POST parameters: " + parameters.toString());

		IotSystem iotsystemdetails = retrieveSystemDetails(parameters.getQrcode());

		return new ResponseEntity<IotSystem>(iotsystemdetails, HttpStatus.OK);
	}

	public IotSystem retrieveSystemDetails(String qr) {
		IotSystem iotsys = null;
		for (int i = 0; i < qrcodeiotsystems.length; i++) {
			System.out.println("qrcodes " + qrcodeiotsystems[i].getQrcode() + " " + qr);
			if (qrcodeiotsystems[i].getQrcode().equals(qr))
				iotsys = qrcodeiotsystems[i];
		}
		if (iotsys != null)
			System.out.println("iot system : " + iotsys.toString());
		return iotsys;
	}

	public IotSystem getIotSystemFromId(String id) {
		IotSystem iotsys = null;
		for (int i = 0; i < iotsystems.length; i++) {
			System.out.println("ids  " + iotsystems[i].getId() + " " + id);
			if (iotsystems[i].getId().equals(id))
				iotsys = iotsystems[i];
		}
		if (iotsys != null) {
			System.out.println("iot system : " + iotsys.toString());
		} else
			iotsys = new IotSystem(id, "", "", id, "");
		return iotsys;
	}

	private Set<String> getPostcodesUriOrdnancesurvey(double lat, double lon, int radius) {
		Set<String> postcodes = new HashSet<String>();

		radius = radius;
		double minlat = getNewLat(lat, radius * (-1));
		double maxlat = getNewLat(lat, radius * (+1));

		double minlon = getNewLon(lon, radius * (+1), lat);
		double maxlon = getNewLon(lon, radius * (-1), lat);

		String sparqlQueryString = "Select ?postcode WHERE{?postcode <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . ?postcode <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . ?postcode a <http://data.ordnancesurvey.co.uk/ontology/postcode/PostcodeUnit>.?postcode <http://data.ordnancesurvey.co.uk/ontology/postcode/district> <http://data.ordnancesurvey.co.uk/id/7000000000030421>"
				+ " FILTER (?lat > " + String.valueOf(minlat) + " && ?lat < " + String.valueOf(maxlat) + " && ?long < "
				+ String.valueOf(minlon) + " && ?long > " + String.valueOf(maxlon) + " ) } ";

		System.out.println(sparqlQueryString);
		// sparqlQueryString = "Select ?postcode WHERE{?postcode
		// <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . ?postcode
		// <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . ?postcode a
		// <http://data.ordnancesurvey.co.uk/ontology/postcode/PostcodeUnit>.?postcode
		// <http://data.ordnancesurvey.co.uk/ontology/postcode/district>
		// <http://data.ordnancesurvey.co.uk/id/7000000000030421>FILTER (?lat >
		// 57.15314438363484 && ?lat < 57.17113081636515 && ?long < -2.09208 && ?long >
		// -2.09209) }";
		System.out.println(sparqlQueryString);
		Query query = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory
				.sparqlService("http://data.ordnancesurvey.co.uk/datasets/os-linked-data/apis/sparql", query);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			RDFNode node = qs.get("postcode");
			if (node != null)
				postcodes.add(node.toString());
			// System.out.println(qs);
		}

		qexec.close();
		return postcodes;
	}

	private double getNewLat(double lat, int rad) {
		System.out.println(String.valueOf(lat + ((rad / earth_radius) * (180 / Math.PI))));
		return lat + ((rad / earth_radius) * (180 / Math.PI));

	}

	private double getNewLon(double lon, int rad, double lat) {
		System.out.println(lon + ((rad / earth_radius) * (180 / Math.PI) / Math.cos(lat * Math.PI / 180)));
		return lon + ((rad / earth_radius) * (180 / Math.PI) / Math.cos(lat * Math.PI / 180));
	}

	private Set<IotSystem> retrieveIotsytemByPostcode(String postcode) {
		String pcode = getPostcodeFromPostcodeUri(postcode);
		Set<IotSystem> set = new HashSet<IotSystem>();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " + "PREFIX gdpr: <https://w3id.org/GDPRtEXT#> "
				+ "PREFIX ssn: <http://www.w3.org/ns/ssn/> " + "PREFIX prov: <http://www.w3.org/ns/prov#> "
				+ "Select ?system ?label ?comment  ?highLevelsystem ?highLevelsystemLabel ?highLevelsystemComment "
				+ "WHERE{ " + "?system a ssn:System; rdfs:label ?label; rdfs:comment ?comment.  "
				+ "?deployment ssn:deployedSystem ?system; ssn:deployedOnPlatform ?platform. "
				+ "?platform prov:atLocation " + "<" + postcode + ">" + " . " + "OPTIONAL { "
				+ "?highLevelsystem ssn:hasSubSystem ?system;  rdfs:label ?highLevelsystemLabel; rdfs:comment ?highLevelsystemComment. "
				+ "} " + "} ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:7200/repositories/Test", query);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			RDFNode system = qs.get("system");
			RDFNode label = qs.get("label");
			RDFNode comment = qs.get("comment");
			RDFNode highLevelsystem = qs.get("highLevelsystem");
			RDFNode highLevelsystemLabel = qs.get("highLevelsystemLabel");
			RDFNode highLevelsystemComment = qs.get("highLevelsystemComment");
			if (system != null) {
				String uri = "<" + system.toString() + ">";
				if (highLevelsystem != null)
					set.add(new LocatedIotSystem(system.toString(), label.toString(), comment.toString(), "", 0, 0,
							pcode, uri, highLevelsystem.toString(), highLevelsystemLabel.toString(),
							highLevelsystemComment.toString(), ""));
				else
					set.add(new LocatedIotSystem(system.toString(), label.toString(), comment.toString(), "", 0, 0,
							pcode, uri, ""));
				System.out.println("postcode: " + postcode);
				System.out.println("system: " + system);
			}
			// System.out.println(qs);
		}

		qexec.close();

		// return set.toArray(new IotSystem[set.size()]);]
		return set;
	}

	private String[] getLatLonFromPostcodeOrdnancesurvey(String postcode) {
		String[] latlon = { "0", "0" };
		String sparqlQueryString = "Select ?lat ?long WHERE{?postcode <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . ?postcode <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . ?postcode a <http://data.ordnancesurvey.co.uk/ontology/postcode/PostcodeUnit>.?postcode <http://data.ordnancesurvey.co.uk/ontology/postcode/district> <http://data.ordnancesurvey.co.uk/id/7000000000030421>"
				+ " FILTER (STR(?postcode)=" + "\"" + postcode + "\"" + " ) } ";

		System.out.println(sparqlQueryString);
		// sparqlQueryString = "Select ?postcode WHERE{?postcode
		// <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . ?postcode
		// <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . ?postcode a
		// <http://data.ordnancesurvey.co.uk/ontology/postcode/PostcodeUnit>.?postcode
		// <http://data.ordnancesurvey.co.uk/ontology/postcode/district>
		// <http://data.ordnancesurvey.co.uk/id/7000000000030421>FILTER (?lat >
		// 57.15314438363484 && ?lat < 57.17113081636515 && ?long < -2.09208 && ?long >
		// -2.09209) }";
		// System.out.println(sparqlQueryString);
		Query query = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory
				.sparqlService("http://data.ordnancesurvey.co.uk/datasets/os-linked-data/apis/sparql", query);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			RDFNode node = qs.get("lat");
			if (node != null) {
				latlon[0] = qs.get("lat").toString();
				latlon[1] = qs.get("long").toString();
			}
			System.out.println(qs);
		}

		qexec.close();
		return latlon;
	}

	public String trimApices(String s) {
		return s.split("\\^\\^")[0];
	}

	private Set<String> retrieveDeploymentPostcodes() {
		Set<String> set = new HashSet<String>();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " + "PREFIX gdpr: <https://w3id.org/GDPRtEXT#> "
				+ "PREFIX ssn: <http://www.w3.org/ns/ssn/> " + "PREFIX prov: <http://www.w3.org/ns/prov#> "
				+ "Select ?postcodeuri " + "WHERE{ "
				+ "?system a ssn:System; rdfs:label ?label; rdfs:comment ?comment.  "
				+ "?deployment ssn:deployedSystem ?system; ssn:deployedOnPlatform ?platform. "
				+ "?platform prov:atLocation ?postcodeuri " + "} ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:7200/repositories/Test", query);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			RDFNode pc = qs.get("postcodeuri");
			if (pc != null) {
				set.add(pc.toString());
				System.out.println("postcodeuri: " + pc);
			}
			// System.out.println(qs);
		}

		qexec.close();
		// return set.toArray(new IotSystem[set.size()]);]
		return set;
	}

	private String getPostcodeFromPostcodeUri(String uri) {
		String[] uri_splitted = uri.split("http://data.ordnancesurvey.co.uk/id/postcodeunit/");
		if (uri_splitted.length == 2)
			return uri_splitted[1];
		else
			return uri;
	}
}

// "SELECT DISTINCT ?step WHERE {
// <http://trustlens.org/test_dataset1#HomeMonitoringHighLevelPlan> a
// ep-plan:Plan; ep-plan:includesStep ?step. ?step a ep-plan:Step. } "