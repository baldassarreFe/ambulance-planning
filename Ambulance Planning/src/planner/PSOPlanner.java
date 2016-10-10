package planner;


import model.Action;
import model.CityMap;
import model.Node;
import utils.Pair;
import utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Planner that uses PSO as a tool to find better solution.
 */
public class PSOPlanner extends Planner {

	private PSO pso;
	private PSO.PSOEvaluator evaluator;

	private CityMap map;

	private List<Node> ambulances;
	private List<Node> patients;
	private List<Node> hospitals;
	private int ambCnt;
	private int patCnt;
	private int hosCnt;

	private int particleDims;
	private double[][] particleBounds;

	private int[][] optHospitals;
	private double[][] optHospitalsDist;
	private int[] singleOptHospitals;
	private double[] singleOptHospitalsDist;

	@Override
	public List<Action> solve(CityMap map) {
		this.map = map;

		ambulances = map.getAmbulancesLocation();
		patients = map.getPatientsLocation();
		hospitals = map.getHospitalsLocation();
		ambCnt = ambulances.size();
		patCnt = patients.size();
		hosCnt = hospitals.size();

		particleDims = ambCnt * 2 + patCnt;
		buildBounds(); // creates particleBounds

		precalcOptimalHospitals();

		evaluator = new VRPEvaluator();
		pso = new PSO(evaluator, particleDims, particleBounds);

		double[] particle = pso.run(Long.MAX_VALUE);
		List<Action> plan = decodePlan(particle);

		return plan;
	}

	/**
	 * Required for PSO to initialize first particles correctly.
	 * <p>
	 * Updates <code>particleBounds</code> array.
	 */
	private void buildBounds() {
		particleBounds = new double[particleDims][2];

		// todo: (!!!) get bounds for nodes' coordinates from map (currently unavailable)
		int minX = 0, maxX = 1;
		int minY = 0, maxY = 1;

		for (int i = 0; i < ambCnt * 2; i += 2) {
			particleBounds[i][0] = minX;
			particleBounds[i][1] = maxX;
			particleBounds[i + 1][0] = minY;
			particleBounds[i + 1][1] = maxY;
		}

		for (int i = ambCnt * 2; i < ambCnt * 2 + patCnt; i++) {
			particleBounds[i][0] = 0;
			particleBounds[i][1] = 1;
		}
	}

	/**
	 * Builds <code>optimalHospitals</code> array.
	 * Asymptotic: O(patCnt^2 * hosCnt).
	 * <p>
	 * <code>optHospitals[i][j] - the best hospital to go to with ith patient before going to jth</code>
	 * <code>singleOptHospitals[i] - the best hospital to go to with ith patient</code>
	 */
	private void precalcOptimalHospitals() {
		optHospitals = new int[patCnt][patCnt];
		optHospitalsDist = new double[patCnt][patCnt];
		singleOptHospitals = new int[patCnt];
		singleOptHospitalsDist = new double[patCnt];

		for (int i = 0; i < patCnt; i++) {
			for (int j = 0; j < patCnt; j++) {
				optHospitals[i][j] = -1;
				for (int k = 0; k < hosCnt; k++) {
					if (isValidHospital(patients.get(i), hospitals.get(k))) {
						double curDist = 0;  // todo: (!!!) map.shortestDist(i, k) + map.shortestDist(k, j);
						if (optHospitals[i][j] == -1 || optHospitalsDist[i][j] > curDist) {
							optHospitals[i][j] = k;
							optHospitalsDist[i][j] = curDist;
						}
					}
				}
			}
			singleOptHospitals[i] = -1;
			for (int k = 0; k < hosCnt; k++) {
				if (isValidHospital(patients.get(i), hospitals.get(k))) {
					double curDist = 0;  // todo: (!!!) map.shortestDist(i, k);
					if (singleOptHospitals[i] == -1 || singleOptHospitalsDist[i] > curDist) {
						singleOptHospitals[i] = k;
						singleOptHospitalsDist[i] = curDist;
					}
				}
			}
		}
	}

	/**
	 * Check if the given hospital can accept the given patient.
	 */
	private boolean isValidHospital(Node patient, Node hospital) {
		return true;
	}


	/**
	 * Transform PSO particle into a valid plan.
	 * <p>
	 * It's the heart of algorithm. For example, the method can be used to generate a good valid plan
	 * from a random particle, without actual use of PSO.
	 */
	private List<Action> decodePlan(double[] particle) {
		/*
		 * Split particle into specific subarrays.
		 */
		double[] ambDims = Arrays.copyOfRange(particle, 0, ambCnt * 2); // todo: maybe try to remove this part and use current ambulances' coordinates
		double[] patDims = Arrays.copyOfRange(particle, ambCnt * 2, ambCnt * 2 + patCnt);

		/*
		 * Sort patients according to their priorities (here priorities are numbers from patDims, they are not
		 * the same as real patient priorities).
		 */
		int[] patientsSorted = Utils.getSortedIndices(patDims);

		/*
		 * Get matrix of ambulance priorities for each patient, according to ambulances "positions" from the particle.
		 */
		int[][] ambPriorities = getAmbPriorities(ambDims);

		/*
		 * Add patients to the plan.
		 */
		List<Integer>[] plan = new List[ambCnt];
		for (int i = 0; i < ambCnt; i++) {
			plan[i] = new ArrayList<>();
		}
		double curPlanCost = 0;
		for (int patient : patientsSorted) {
			// Insert the patient into the plan.
			// Test only first few ambulances (currently: at least 2, but not more than 33% or 10)
			int ambsToTest = Math.max(Math.min(ambCnt, 2), Math.min(ambCnt / 3, 10));
			double bestInsertionCost = Double.POSITIVE_INFINITY;
			int insertionAmbulance = -1;
			int insertionIndex = -1;
			for (int i = 0; i < ambsToTest; i++) {
				int ambulance = ambPriorities[patient][i];
				Pair<Integer, Double> curInsertion = tryInsert(plan[ambulance], patient);
				if (curInsertion.y < bestInsertionCost) {
					bestInsertionCost = curInsertion.y;
					insertionAmbulance = ambulance;
					insertionIndex = curInsertion.x;
				}
			}

			/*
			 * Insert patient into chosen position in the plan.
			 */
			curPlanCost += bestInsertionCost;
			plan[insertionAmbulance].add(insertionIndex, patient);

		}

		// todo: (!!!) reformat plan and return
		return null;
	}

	/**
	 *
	 * @param ambPlan
	 * @param patient
	 * @return pair of insertion index and cost delta
	 */
	private Pair<Integer, Double> tryInsert(List<Integer> ambPlan, int patient) {
		return null; // todo
	}

	/**
	 * Get matrix of ambulance priorities for each patient, according to ambulances "positions" from the particle.
	 */
	private int[][] getAmbPriorities(double[] ambDims) {
		/*
		 * Fill 'distances' array.
		 * dist[i][j] - squared distance from ith patient to jth ambulance's point.
		 */
		double[][] dist = new double[patCnt][ambCnt];
		for (int i = 0; i < patCnt; i++) {
			Node patient = patients.get(i);
			double px = patient.getX();
			double py = patient.getY();
			for (int j = 0; j < ambCnt; j++) {
				double ax = ambDims[j * 2];
				double ay = ambDims[j * 2 + 1];
				dist[i][j] = Utils.distSqr(px, py, ax, ay);
			}
		}

		/*
		 * Fill matrix of priorities.
		 * priorities[i] - array of ambulances' indices for ith patient.
		 */
		int[][] priorities = new int[patCnt][ambCnt];
		for (int i = 0; i < patCnt; i++) {
			priorities[i] = Utils.getSortedIndices(dist[i]);
		}

		return priorities;
	}

	/**
	 * Evaluate the plan.
	 *
	 * @param plan list of actions
	 * @return value to minimize
	 */
	public double evaluatePlan(List<Action> plan) {
		return Double.POSITIVE_INFINITY; // todo (!!!) probably will already be stored in the plan variable (of course cannot be stored just in the list)
	}

	/**
	 * Apply additional heuristics to enhance the existing plan.
	 *
	 * @param plan current plan
	 * @return improved plan
	 */
	public List<Action> applyOptimizations(List<Action> plan) {
		return plan;  // todo: when main part works, implement this (2-opt, for example) (it's an important part)
	}

	public class VRPEvaluator extends PSO.PSOEvaluator {
		@Override
		public double evaluate(double[] particle) {
			List<Action> plan = decodePlan(particle);
			return evaluatePlan(plan);
		}
	}

}
