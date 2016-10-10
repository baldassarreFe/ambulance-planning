import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Class in charge of generating random problems. 
 * It has to create cities dispositions, different types of patients and
 * assign locations to the patients, ambulances and hospitals.
 * 
 * @author Team 14
 */
public class Generator {
	
	static Random r = new Random();
	
	/* Constants */
	private static final double INIT_PATIENTS = .6;
	private static final int MAX_ROUNDS = 100;

	
	/**
	 * Generates a complete problem using the parameters from the input file.
	 * Stores the information in PDDL in a String to print into the output file.
	 * 
	 * @param map Information regarding number of nodes, number of roads, noise in the distances and demand
	 * @param patient Information regarding number of patients and prob of each priority
	 * @param amb Number of ambulances
	 * @param hosp Number of hospitals
	 * @return String to print generated map
	 */
	public static String generateProblem(double[] map, double[] patient, int amb, int hosp, String out) {
		
		String s = "";
		ArrayList<ArrayList<Pair<Integer, Integer>>> lDemand = new ArrayList<ArrayList<Pair<Integer, Integer>>>();
		for(int i = 0; i < map[3]; i++) {
			ArrayList<Pair<Integer, Integer>> demand = new ArrayList<>();
			lDemand.add(demand);
		}	
		
		/* Generate headers of the PDDL file */
		s = s.concat(generateHeaders(out, map, patient, amb, hosp));
		s = s.concat("(:init ");
		
		/* Generate random map */		
		s = s.concat(generateMap(map, lDemand)); 
		
		/* Definitions */
		s = s.concat(generateDefinitions(patient, amb, hosp));
		
		/* Generate positions */
		s = s.concat(generatePositions(patient, amb, hosp, lDemand));
		
		/* Generate goal */
		s = s.concat(generateGoal((int)patient[0]));

		return s;
	}
	
	
	
	/* Partial generators */
	/**
	 * Generates the definition of the problem, the domain and the objects.
	 * define (problem name)
	 * (:domain ambulance world)
	 * (:objects l0 l1 p0 p1 a0 h0...)
	 * 
	 * @param out Path of the output file
	 * @param map Information regarding number of nodes, number of roads, noise in the distances and demand
	 * @param patient Information regarding number of patients and prob of each priority
	 * @param amb Number of ambulances
	 * @param hosp Number of hospitals
	 * @return String to print headers
	 */
	private static String generateHeaders(String out, double[] map, double[] patient, int amb, int hosp) {
		
		/*Definition of problem and domain */
		int idx = out.lastIndexOf("\\");
		if(idx != -1){
			out = out.substring(idx, out.length());
		} else {
			idx = out.lastIndexOf(".");
			if (idx != -1)
				out = out.substring(0, idx);
		}
		
		String s = "(define (problem " + out + ")\n";
		s = s.concat("(:domain ambulance world)\n");
		
		
		/* Objects */
		s = s.concat("(:objects ");
		
		//Locations
		for(int i = 0; i < map[0]; i++) {
			s = s.concat("l" + i + " ");
		}
		//Patients
		for(int i = 0; i < patient[0]; i++) {
			s = s.concat("p" + i + " ");
		}
		//Ambulances
		for(int i = 0; i < amb; i++) {
			s = s.concat("a" + i + " ");
		}
		//Hospitals
		for(int i = 0; i < hosp; i++) {
			s = s.concat("h" + i + " ");
		}
		s = s.concat(")\n");		

		return s;
	}
	
	/**
	 * Generates a random graph using Erdos-Renyi algorithm.
	 * 
	 * @param map Information picked from input file
	 * @param locationDemand Array that contains locations with demand = the position
	 * @return String to print generated map
	 */
	private static String generateMap(double[] map, ArrayList<ArrayList<Pair<Integer, Integer>>> locationDemand){
		
		String s = "";
		int nodes = (int) map[0];
		// int roads = (int) map[1];	FIXME: with the erdos alg it is not needed
		double noise = map[2];
		int demand = (int) map[3];

		ArrayList<Integer> createNodes = new ArrayList<>();		
		createNodes.add(0);
		createNodes.add(1);
		ArrayList<Pair<Integer, Integer>> posCoord = generteCoord(nodes);
		Collections.shuffle(posCoord);
		
		
		/* Initialize Erdos */
		Pair<Integer, Integer> p = posCoord.get(0);
		s = s.concat(locationString(0, p, demand, locationDemand));
		
		p = posCoord.get(1);
		s = s.concat(locationString(1, p, demand, locationDemand));
		
		s = s.concat(roadString(0,1, posCoord, noise));
		
		
		/* Erdos algorithm */
		for(int i = 2; i < nodes; i++) {
			p = posCoord.get(i);
			s = s.concat(locationString(i, p, demand, locationDemand));
			
			// Choose random connection
			int randomNode = r.nextInt(i);
			s = s.concat(roadString(randomNode,i, posCoord, noise));			
		}		
		
		return s;
	}
	
	/**
	 * Generates definitions of objects.
	 * Patients -> Patient(p1,priority,time_lapse)
	 * Ambulances -> Ambulance(a1)
	 * Hospitals -> Hospital(h1)
	 * 
	 * @param patient Information regarding number of patients and prob of each priority
	 * @param amb Number of ambulances
	 * @param hosp Number of hospitals
	 * @return String to print
	 */
	private static String generateDefinitions(double[] patient, int amb, int hosp) {
		
		String s = "";

		/* Patients */
		for(int i = 0; i < patient[0]; i++) {
			// Priority
			int priority = r.nextInt(3);
			if (priority < patient[1])
				priority = 1;
			else if (priority < patient[1] + patient[2])
				priority = 2;
			else
				priority = 3;
			
			// Time_lapse
			int time = 0;
			if (r.nextDouble() > INIT_PATIENTS) 
				time = r.nextInt(MAX_ROUNDS);			
			
			s = s.concat("(Patient(p" + i + "," + priority + "," + time +"))\n");
		}
		
		/* Ambulances */
		for(int i = 0; i < amb; i++) {
			s = s.concat("(Ambulance(a" + i + "))\n");
		}
		
		/* Hospitals */
		for(int i = 0; i < hosp; i++) {
			s = s.concat("(Hospital(h" + i + "))\n");
		}
		
		return s;
	}
	
	
	/**
	 * Generates initial positions for every object.
	 * At(Oi,x,y)
	 * 
	 * @param patient Information regarding number of patients and prob of each priority
	 * @param amb Number of ambulances
	 * @param hosp Number of hospitals
	 * @param lDemand Array that contains locations with demand = the position
	 * @return String to print
	 */
	private static String generatePositions(double[] patient, int amb, int hosp, ArrayList<ArrayList<Pair<Integer, Integer>>> lDemand) {
			
		String s = "";
		ArrayList<Pair<Integer, Integer>> patientLoc = new ArrayList<>();
		
		/* Patients: according to demand */
		int patients = (int)patient[0];
		
		// Counting for distribution
		int total = 0;
		for(int d = 1; d < lDemand.size(); d++) {
			total += d * lDemand.get(d).size();
		}
		
		// Fill max demand node until no more patients
		int p = 0;
		
		for(int d = lDemand.size() - 1; d >= 0 && p < patients; d--) { // max dem
			if(lDemand.get(d).isEmpty())
				continue;
			
			double w = (double)lDemand.get(d).size() / (double)total;			
			
			for(int l = 0; l < lDemand.get(d).size() && p < patients; l++) {
				
				int nPatients = (int)Math.ceil(patients * w);
				Pair<Integer, Integer> location = lDemand.get(d).get(l);
				patientLoc.add(location);
				
				for(int n = 0 ; n < nPatients && p < patients; n++, p++) {
					s = s.concat("(At(p" + p + "," + location.x + "," + location.y + "))\n");
				}
			}
		}
		
		// All patients must be waiting
		for(int i = 0; i < patients; i++){
			s = s.concat("(Waiting(p" + i + "))\n");
		}
		
		// Retrieve all locations and remove patient location
		ArrayList<Pair<Integer, Integer>> loc = new ArrayList<>();
		for(int i = 0; i < lDemand.size(); i++) {
			
			if(lDemand.get(i).isEmpty())
				continue;
			
			for(int j = 0; j < lDemand.get(i).size(); j++) {
				loc.add(lDemand.get(i).get(j));
			}
		}
		
		loc.removeAll(patientLoc); // Ambulance and hospitals should not be in the same place as patient
		
		
		
		/* Ambulances */
		Collections.shuffle(loc);
		for(int i = 0; i < amb; i++){
			
			int randLoc = r.nextInt(loc.size());			
			int x = loc.get(randLoc).x;
			int y = loc.get(randLoc).y;
			
			s = s.concat("(At(a" + i + "," + x + "," + y + "))\n");
			s = s.concat("(Available(a" + i + "))\n");
		}
		
		/* Hospitals */
		Collections.shuffle(loc);
		for(int i = 0; i < hosp; i++){

			int randLoc = r.nextInt(loc.size());			
			int x = loc.get(randLoc).x;
			int y = loc.get(randLoc).y;
			
			s = s.concat("(At(h" + i + "," + x + "," + y + "))\n");
		}
		s = s.concat(")\n"); // Close init
		
		return s;
	}
	
	/**
	 * Generate the goal string in PDDL. All patients must be at a hospital.
	 * InHospital(p0)
	 * 
	 * @param patients Number of patients
	 * @return String to write
	 */
	private static String generateGoal(int patients) {
		
		String s = "(:goal ";
		
		for(int i = 0; i < patients; i++) {
			s = s.concat("(InHospital(p" + i + "))\n");			
		}
		s = s.concat("))"); // Close goal and define
		
		return s;
	}
	
	/**
	 * Generates all possible coordinates of a grid NxN, having enough variety
	 * for each node
	 * 
	 * @param nodes Number of nodes
	 * @return Array with all possible coordinates
	 */
	private static ArrayList<Pair<Integer, Integer>> generteCoord(int nodes){
		
		ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
		
		for(int i = 0; i < nodes; i++) {
			for(int j = 0; j < nodes; j++) {
				Pair<Integer, Integer> p = new Pair<Integer, Integer>(i, j);
				list.add(p);
			}
		}
		
		return list;		
	}
	
	
	
	
	/* Generate Strings */
	/**
	 * Creates the locations as the name of the node, its coordinate and a demand
	 * Location(l1,x,y,w)
	 * 
	 * @param node Number of nodes
	 * @param coord Coordinate assigned to node
	 * @param demand Max demand
	 * @param locationDemand Array that contains locations with demand = the position
	 * @return String to print generated map
	 */
	private static String locationString(int node, Pair<Integer, Integer> coord, int demand, ArrayList<ArrayList<Pair<Integer, Integer>>> locationDemand) {
				
		// Random demand for the location
		int w = r.nextInt(demand);
		
		// Save location with its demand
		if(locationDemand.get(w).isEmpty()) {
			ArrayList<Pair<Integer, Integer>> locations = new ArrayList<>();
			locations.add(coord);
			locationDemand.set(w, locations);
		}
		else {
			locationDemand.get(w).add(coord);			
		}
		
		return "(Location(l" + node + "," + coord.toString() + "," + w +"))\n";		
	}
	
	/**
	 * Creates the road between two nodes with the distance between them
	 * Road(l1,l2,w)
	 * 
	 * @param node1 Number of the first node to join
	 * @param node2 Number of the second node to join
	 * @param coord Array containing all the nodes' coordinates
	 * @param noise Max noise to add to a path (Euclidean distance)
	 * @return String to print generated map
	 */
	private static String roadString(int node1, int node2, ArrayList<Pair<Integer, Integer>> coord, double noise) {

		// Distance between nodes (euclidean distance)
		int x1 = coord.get(node1).x;
		int x2 = coord.get(node2).x;
		int y1 = coord.get(node1).y;
		int y2 = coord.get(node2).y;
		
		double d = Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
		
		// Add noise (or not)
		d += noise * r.nextDouble();
		
		return "(Road(l" + node1 + ",l" + node2 + "," + d +"))\n";		
	}

}
