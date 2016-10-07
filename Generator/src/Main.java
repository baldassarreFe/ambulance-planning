

/**
 * 
 * @author Mónica
 *
 */
public class Main {

	public static void main(String[] args) {
		
		/* Arguments control error */
		if(args.length != 1)
			throw new IllegalArgumentException();
		
		//Read file and check arguments
		double[] city = {0,0,0,0};
		double[] patients = {0,0,0,0};
		int amb = 0, hosp = 0;
		IO.readArguments(args[0], city, patients, amb, hosp);
		
		
		/* Generate random map */
		String s = "";
		Generator.generateMap(city, s); 
		
		/* Generate positions */
		Generator.genertePositions();
		
		/* Generate demands and priorities */
		
		
		/* Write output PDDL */
		
	
	}
	
	

}
