

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
		int[] objs = {0,0};
		IO.readArguments(args[0], city, patients, objs);
		
		
		/* Generate problem */
		String s = Generator.generateProblem(city, patients, objs[0], objs[1]);
		
		
		/* Write output PDDL */
		IO.printPDDL("C:\\Users\\Mónica\\Documents\\GitHub\\ambulance-planning\\Generator\\src\\output.txt", s);
		
	
	}
	
	

}
