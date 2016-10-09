import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Generator {
	
	static Random r = new Random();
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
	 * @param s String in which store the output
	 */
	public static String generateProblem(double[] map, double[] patient, int amb, int hosp) {
		
		String s = "";
		ArrayList<ArrayList<Pair<Integer, Integer>>> lDemand = new ArrayList<ArrayList<Pair<Integer, Integer>>>();
		for(int i = 0; i < map[3]; i++) {
			ArrayList<Pair<Integer, Integer>> demand = new ArrayList<>();
			lDemand.add(demand);
		}		
		
		
		/* Generate random map */		
		s = s.concat(generateMap(map, lDemand)); 
		
		/* Definitions */
		s = s.concat(generateDefinitions(patient, amb, hosp));
		
		/* Generate positions */
		s = s.concat(generatePositions(patient, amb, hosp, lDemand));
		 
		 return s;
	}
	
	/**
	 * Generate a random graph using Erdos-Renyi algorithm.
	 * @param map Information picked from input file
	 * @param s String to print generated map
	 */
	private static String generateMap(double[] map, ArrayList<ArrayList<Pair<Integer, Integer>>> locationDemand){
		
		String s = "";
		int nodes = (int) map[0];
		// int roads = (int) map[1];	FIXME: with the erdos alg it is not needed
		double noise = map[2];
		int demand = (int) map[3];
		
		//locationDemand = new TreeMap<>();
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
	
	private static String generateDefinitions(double[] patient, int amb, int hosp) {
		// Patient(P1,priority,time_lapse)
		
		String s = "";

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
		
		// Ambulance(A1)
		for(int i = 0; i < amb; i++) {
			s = s.concat("(Ambulance(a" + i + "))\n");
		}
		
		// Hospital(H1)
		for(int i = 0; i < hosp; i++) {
			s = s.concat("(Hospital(h" + i + "))\n");
		}
		
		return s;
	}
	
	
	
	private static String generatePositions(double[] patient, int amb, int hosp, ArrayList<ArrayList<Pair<Integer, Integer>>> lDemand) {
				
		// At(Oi,x,y)
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
		
		// Retrieve all locations and remove patient location
		ArrayList<Pair<Integer, Integer>> loc = new ArrayList<>();
		for(int i = 0; i < lDemand.size(); i++) {
			
			if(lDemand.get(i).isEmpty())
				continue;
			
			for(int j = 0; j < lDemand.get(i).size(); j++) {
				loc.add(lDemand.get(i).get(j));
			}
		}
		
		loc.removeAll(patientLoc);
		
		
		
		/* Ambulances */
		Collections.shuffle(loc);
		for(int i = 0; i < amb; i++){
			
			int randLoc = r.nextInt(loc.size());			
			int x = loc.get(randLoc).x;
			int y = loc.get(randLoc).y;
			
			s = s.concat("(At(a" + i + "," + x + "," + y + "))\n");
		}
		
		/* Hospitals */
		Collections.shuffle(loc);
		for(int i = 0; i < hosp; i++){

			int randLoc = r.nextInt(loc.size());			
			int x = loc.get(randLoc).x;
			int y = loc.get(randLoc).y;
			
			s = s.concat("(At(h" + i + "," + x + "," + y + "))\n");
		}
		
		return s;
	}
	
	
	
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
	private static String locationString(int node, Pair<Integer, Integer> coord, int demand, ArrayList<ArrayList<Pair<Integer, Integer>>> locationDemand) {
		// Location(A,x,y,w)
		
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
		
		return "(Location(" + node + "," + coord.toString() + "," + w +"))\n";		
	}
	
	private static String roadString(int node1, int node2, ArrayList<Pair<Integer, Integer>> coord, double noise) {
		// Road(A,B,w)
		
		// Distance between nodes (euclidean distance)
		int x1 = coord.get(node1).x;
		int x2 = coord.get(node2).x;
		int y1 = coord.get(node1).y;
		int y2 = coord.get(node2).y;
		
		double d = Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
		
		// Add noise?
		d += noise * r.nextDouble();
		
		return "(Road(" + node1 + "," + node2 + "," + d +"))\n";		
	}

}
