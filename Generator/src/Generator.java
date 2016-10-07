import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Generator {
	
	static Random r = new Random();
	

	/**
	 * Generate a random graph using Erdos-Renyi algorithm.
	 * @param map Information picked from input file
	 * @param s String to print generated map
	 */
	public static void generateMap(double[] map, String s){
		
		int nodes = (int) map[0];
		int roads = (int) map[1];		
		double noise = map[2];
		int demand = (int) map[3];
		
		ArrayList<Integer> createNodes = new ArrayList<>();
		createNodes.add(0);
		createNodes.add(1);
		ArrayList<Pair<Integer, Integer>> posCoord = generteCoord(nodes);
		Collections.shuffle(posCoord);
		
		
		/* Initialize Erdos */
		Pair<Integer, Integer> p = posCoord.get(0);
		locationString(0, p, demand);
		
		p = posCoord.get(1);
		locationString(1, p, demand);
		
		roadString(0,1, posCoord, noise);
		
		
		/* Erdos algorithm */
		for(int i = 2; i < nodes; i++) {
			p = posCoord.get(i);
			locationString(i, p, demand);
			
			// Choose random connection
			int randomNode = r.nextInt(i);
			roadString(randomNode,i, posCoord, noise);			
		}
		
	}
	
	
	
	public static void genertePositions(){
		
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
	private static String locationString(int node, Pair<Integer, Integer> coord, int demand) {
		// Location(A,x,y,w)
		
		// Random demand for the location
		int w = r.nextInt(demand);
		return "Location(" + node + "," + coord.toString() + "," + demand +")\n";
		
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
		
		return "Road(" + node1 + "," + node2 + "," + d +")\n";		
	}

}
