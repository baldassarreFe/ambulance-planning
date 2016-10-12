package model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class CityParser {
	
	/* Constants */	
	private static final int L_PARAM = 19;
	private static final int L_NODE = 0;
	private static final int L_X = 2;
	private static final int L_Y = 3;
	private static final int L_DEMAND = 2;
	
	private static final int D_PARAM = 20;
	
	private static final int R_PARAM = 13;
	private static final int R_A = 1;
	private static final int R_B = 3;
	private static final int R_DISTANCE = 5;
	
	private static final int P_PARAM = 14;
	private static final int P_P = 0;
	private static final int P_PRIORITY = 2;
	
	private static final int A_PARAM = 12;
	private static final int A_A = 0; 
	
	private static final int H_PARAM = 11;
	private static final int H_H = 0; 
	
	private static final int AT_PARAM = 4;
	private static final int AT_OBJECT = 0;
	private static final int AT_LOCATION = 1;
	
	
	/* Kattis functions IO */		
    public CityParser(InputStream i) {
        r = new BufferedReader(new InputStreamReader(i));
    }

    public boolean hasMoreTokens() {
        return peekToken() != null;
    }

    public int getInt() {
        return Integer.parseInt(nextToken());
    }

    public double getDouble() {
        return Double.parseDouble(nextToken());
    }

    public float getFloat() {
        return Float.parseFloat(nextToken());
    }

    public long getLong() {
        return Long.parseLong(nextToken());
    }

    public String getWord() {
        return nextToken();
    }

    private BufferedReader r;
    private String line;
    private StringTokenizer st;
    private String token;

    private String peekToken() {
        if (token == null)
            try {
                while (st == null || !st.hasMoreTokens()) {
                    line = r.readLine();
                    if (line == null) return null;
                    st = new StringTokenizer(line);
                }
                token = st.nextToken();
            } catch (IOException e) { }
        return token;
    }
    
    private String nextToken() {
        String ans = peekToken();
        token = null;
        return ans;
    }
    
    private String nextLine() {
    	try {
			line = r.readLine();
			if (line == null) return null;
	        st = new StringTokenizer(line);
	              
		} catch (IOException e) {
		}
    	token = st.nextToken();	  
    	return line;
        
    	
    }
    
    
    /* Our parser */
	public static CityMap parse(String cityFileName) {
		
		InputStream input;
		CityMap c = null;
		
		try {
			/* Open input file */		
			input = new FileInputStream(cityFileName);
			CityParser cp = new CityParser(input);
			
			/* Go to objects */
			int l = 0, p = 0, a = 0, h = 0;
			do {
				cp.nextLine();
			} while (!cp.line.contains("(:objects"));

			while(!cp.nextToken().contains(")")) {
				String obj = cp.peekToken();
				
				if(obj.startsWith("l"))
					l++;
				else if(obj.startsWith("p"))
					p++;
				else if(obj.startsWith("a"))
					a++;
				else if(obj.startsWith("h"))
					h++;
			}		
			
			double[][] adjMatrix = new double[l][l];
			double[][] coordinates = new double[l][CityMap.NUM_COORD];
			double[] demands = new double[l];
			List<NodeContent>[] contents = new ArrayList[l];
			
			for (int node = 0; node < l; node++) {
				contents[node] = new ArrayList<>();
			}
			
			
			/* Go to initial state */
			do {
				cp.nextLine();
			} while (!cp.line.contains("(:init"));
			
			/* Read all initial states (until "(:goal")*/
			do {
				cp.parsePredicate(cp.line, adjMatrix, coordinates, demands, (ArrayList<NodeContent>[]) contents);
				cp.nextLine();
			} while (!cp.line.contains("(:goal"));
			
			c = new CityMap(adjMatrix, coordinates, Arrays.asList(contents), demands);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return c;
	}
	

	private void parsePredicate(String l, double[][] adjMatrix, double[][] coord, double[] demands, ArrayList<NodeContent>[] contents) {
		
		ArrayList<Patient> p = new ArrayList<>();
		ArrayList<Ambulance> a = new ArrayList<>();
		ArrayList<Hospital> h = new ArrayList<>();
		
		String pred = this.nextLine(); // remove init
		
		/* Fist time look for location */
		do {
			//
			if(pred.contains("LocationCoord"))
				createLocationCoord(pred, coord);
			else if(pred.contains("LocationDemand"))
				createLocationDemad(pred, demands);
			else if(pred.contains("Distance"))
				createRoad(pred, adjMatrix);
			else if(pred.contains("Priority"))
				createPatient(pred, p);
			else if(pred.contains("Ambulance"))
				createAmbulance(pred, a);
			else if(pred.contains("Hospital"))
				createHospital(pred, h);
			else if(pred.contains("At"))
				createAt(pred, p, a, h, contents);
			else if(pred.contains("Waiting"))
				createWaiting(pred);
			else if(pred.contains("Available"))
				createAvailable(pred);
			
			pred = this.nextLine(); 
						
		} while (!this.line.equals(")")); // the close of init (before the goal)
		
		/* Do we need to read the goal?? TODO
		 * else if(pred.contains("InHospital"))
				createInHospital(pred);
				*/		
	}

	
	/* Actions */
	private void createAt(String pred, ArrayList<Patient> patients, ArrayList<Ambulance> amb, ArrayList<Hospital> hosp, ArrayList<NodeContent>[] contents) {
		
		//(At(p0 l4))
		String params = pred.substring(AT_PARAM, pred.length());
		
		String[] p = params.split(" |\\)");

		String type = p[AT_OBJECT];
		int idx = Integer.parseInt(p[AT_OBJECT].substring(1));
		int node = Integer.parseInt(p[AT_LOCATION].substring(1));
		
		if(type.startsWith("p")) {
			Patient pat = patients.get(idx);
			pat.setNode(node);
			contents[node].add(pat);
		}
		else if(type.startsWith("a")) {
			Ambulance a = amb.get(idx);
			a.setNode(node);
			contents[node].add(a);
		}
		else if(type.startsWith("h")) {
			Hospital h = hosp.get(idx);
			h.setNode(node);
			contents[node].add(h);
		}
		
	}

	private void createAvailable(String pred) {
		// FIXME: it's fixed as available by default (no need to change now)
		
	}

	private void createWaiting(String pred) {
		// FIXME: there is no waiting status in Patient
		
	}

	private void createHospital(String pred, ArrayList<Hospital> hospitals) {

		// (Hospital(h1))
		String params = pred.substring(H_PARAM, pred.length());
		String[] p = params.split(",|\\)");
		
		int id = Integer.parseInt(p[H_H]);
		Hospital h = new Hospital(0, id, 3);
		
		hospitals.add(h);		
	}

	private void createAmbulance(String pred, ArrayList<Ambulance> ambulances) {
		
		//(Ambulance(a0))
		String params = pred.substring(A_PARAM, pred.length());
		String[] p = params.split(",|\\)");
		
		int id = Integer.parseInt(p[A_A]);
		Ambulance a = new Ambulance(0, id, null, true);
		
		ambulances.add(a);		
	}

	private void createPatient(String pred, ArrayList<Patient> patients) {
		
		//(= (Priority(p0) 1)))
		String params = pred.substring(P_PARAM, pred.length());
		String[] p = params.split(" |\\)");
		
		int id = Integer.parseInt(p[P_P]);
		int priority = Integer.parseInt(p[P_PRIORITY]);
		Patient patient = new Patient(0, id, priority);
		
		patients.add(patient);		
	}

	private void createRoad(String pred, double[][] adjMatrix) {
		
		// (= (Distance(l0 l1) 6.647737422253468)
		String params = pred.substring(R_PARAM, pred.length());
		
		String[] p = params.split("l| |\\)");
		int a = Integer.parseInt(p[R_A]);
		int b = Integer.parseInt(p[R_B]);
		double d = Double.parseDouble(p[R_DISTANCE]);
		
		adjMatrix[a][b] = d;
		adjMatrix[b][a] = d;
		
	}
	
	private void createLocationDemad(String pred, double[] demands) {
		
		//(= (LocationDemand(l0) 44))
		String params = pred.substring(D_PARAM, pred.length());
		
		String[] p = params.split(" |\\)");
		int node = Integer.parseInt(p[L_NODE]);
		int d = Integer.parseInt(p[L_DEMAND]);
		
		demands[node] = d;
		
	}

	private void createLocationCoord(String pred, double[][] coord) {
		
		//(= (LocationCoord(l0) 8 7))
		String params = pred.substring(L_PARAM, pred.length());
		
		String[] p = params.split(" |\\)");
		int node = Integer.parseInt(p[L_NODE]);
		int x = Integer.parseInt(p[L_X]);
		int y = Integer.parseInt(p[L_Y]);
		
		coord[node][CityMap.X] = x;
		coord[node][CityMap.Y] = y;
		
	}

}
