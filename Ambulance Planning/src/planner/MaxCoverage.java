package planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import model.CityMap;

/**
* @author Team 14
*
*/
public class MaxCoverage {
	public static Map<Integer, List<Integer>> findAmbulancesDestinations(CityMap map, int numAmbulances,
			int[] ambulanceLocations, int[] ambID, double[][] distance, List<List<Integer>> paths) {
		
		Map<Integer, List<Integer>> result = new HashMap<>();

		List<Double> demands = map.getDemands();
		double max = demands.stream().max(Double::compareTo).get();
		double[] demandNorm = demands.stream().mapToDouble(d -> d/max).toArray();

		// numAmbulances is the number of available ambulances
		if (numAmbulances >= 1) {
			int[] optLocations = findMaxCoverageLocations(numAmbulances, distance, demandNorm, demands.stream().mapToDouble(d->d).toArray());
			int[][] ambulancePlan = new int[optLocations.length][3];
			ambulancePlan = rearrangeHungarian(ambID, ambulanceLocations, optLocations, distance);
			
			for(int i = 0; i<optLocations.length; i++)
			{
				if(ambulancePlan[i][1]!=ambulancePlan[i][2])
				{
					int indPath = ambulancePlan[i][1]*map.nodesCount() + ambulancePlan[i][2]; 
					List<Integer> bstPath = paths.get(indPath);
					//System.out.println(Arrays.toString(bstPath.toArray()));
				}
			}
		}
		
		return result;
	}
	/*
	 * returns the plan for the ambulances
	 * @params ambID - array of ambulance IDS
	 * @params ambulanceLocations - array of ambulance locations
	 * @params optLocations - array of optimal locations found out by the algorithm
	 * @params distance - the shortest path distance matrix
	 * returns a plan for each available ambulance - NAIVE approach
	 */
	public static int[][] rearrange(int[] ambID, int[] ambulanceLocations, int[] optLocations, double[][] distance) {

		int[][] planRearrange = new int[optLocations.length][3];

		// List conversion
		List<Integer> ID = new ArrayList<Integer>();
		for (int i = 0; i < ambID.length; i++) {
			ID.add(ambID[i]);
		}

		List<Integer> ambLoc = new ArrayList<Integer>();
		for (int i = 0; i < ambulanceLocations.length; i++) {
			ambLoc.add(ambulanceLocations[i]);
		}

		List<Integer> optLoc = new ArrayList<Integer>();
		for (int i = 0; i < optLocations.length; i++) {
			optLoc.add(optLocations[i]);
		}

		int iter = 0;
		while (!ID.isEmpty()) {
			double[][] minArr = new double[ambLoc.size()][2];
			for (int i = 0; i < ambLoc.size(); i++) {
				double[] dist = new double[optLoc.size()];
				for (int j = 0; j < optLoc.size(); j++) {
					dist[j] = distance[ambLoc.get(i)][optLoc.get(j)];
				}
				minArr[i] = minArr(dist);
			}

			int ind2 = max2(minArr);
			int[] plan = new int[3];

			plan[0] = ID.get(ind2);
			plan[1] = ambLoc.get(ind2);
			plan[2] = optLoc.get((int) minArr[ind2][0]);

			ID.remove(ind2);
			ambLoc.remove(ind2);
			optLoc.remove((int) minArr[ind2][0]);

			planRearrange[iter] = plan;
			iter++;
		}
		return planRearrange;
	}

	/*
	 * returns the plan for the ambulances
	 * @params ambID - array of ambulance IDS
	 * @params ambulanceLocations - array of ambulance locations
	 * @params optLocations - array of optimal locations found out by the algorithm
	 * @params distance - the shortest path distance matrix
	 * returns a plan for each available ambulance - using the HUNGARIAN ALGO
	 */
	public static int[][] rearrangeHungarian(int[] ambID, int[] ambulanceLocations, int[] optLocations, double[][] distance) {
		
		int numAmb = ambID.length;
		int[][] costMat = new int[numAmb][numAmb];
		int[][] planRearrange = new int[optLocations.length][3];
		
		for(int i = 0; i<numAmb; i++)
		{
			for(int j = 0; j<numAmb; j++)
			{
				costMat[i][j] = (int)Math.round(distance[ambulanceLocations[i]][optLocations[j]]);
			}
		}
		
		int[] ambFinalLoc = AssignmentProblemSolver.solve(costMat);
		
		//construct plan rearrange
		for(int i = 0; i<numAmb; i++)
		{
			for(int j = 0; j<numAmb; j++)
			{
				if(j==0)
				{
					planRearrange[i][j] = ambID[i];
				}
				else if(j==1)
				{
					planRearrange[i][j] = ambulanceLocations[i];
				}
				else
				{
					planRearrange[i][j] = optLocations[ambFinalLoc[i]];
				}
			}
		}
		
		return planRearrange;
	}
	
	
	/*
	 * Utility fn for the rearrange function
	 */
	public static int max2(double[][] minArr) {
		double max = Double.NEGATIVE_INFINITY;
		int ind = -1;
		for (int i = 0; i < minArr.length; i++) {
			if (minArr[i][1] > max) {
				max = minArr[i][1];
				ind = i;
			}
		}
		return ind;
	}

	/*
	 * Normalizes the demand array
	 */
	public static double[] normDemand(double[] demand, double d) {
		for (int i = 0; i < demand.length; i++) {
			demand[i] = demand[i] / d;
		}
		return demand;
	}

	/*
	 * Finds optimal location for the ambulances to be placed
	 * @params numAmbulances - the number of available ambulances
	 * @params distance - the shortest distance matrix
	 * @params demandNorm - normalized demand array
	 * @params demand - demand array
	 * returns the optimal location for the ambulances
	 */
	public static int[] findMaxCoverageLocations(int numAmbulances, double[][] distance, double[] demandNorm,
			double[] demand) {
		int[] optLocation = new int[numAmbulances];
		int numNodes = demand.length;
		if (numAmbulances == 1) {
			int[] classArr = new int[demand.length];
			for (int i = 0; i < demand.length; i++) {
				classArr[i] = 0;
			}
			optLocation = centroidFinder(1, distance, demandNorm, demand, classArr);
		} else {
			int randInits = 19;
			int[][] init = new int[randInits][numAmbulances];
			Random rnd = new Random();

			double[] initConf = new double[randInits];
			double max = Double.NEGATIVE_INFINITY;
			int ind = -1;
			for (int i = 0; i < randInits; i++) {
				for (int j = 0; j < numAmbulances; j++) {
					init[i][j] = rnd.nextInt(numNodes);
				}
				initConf[i] = evalInit(init[i], distance);

				if (initConf[i] > max) {
					ind = i;
					max = initConf[i];
				}
			}
			int[] bestGuess = init[ind];

			int iter = 0;
			int[] annotateNodes = null;
			while (iter < 11) {
				annotateNodes = annotate(distance, bestGuess);
				// if(numAmbulances<numNodes)
				bestGuess = centroidFinder(numAmbulances, distance, demandNorm, demand, annotateNodes);
				iter++;
			}
		
			optLocation = bestGuess;
		}

		return optLocation;
	}

	/*
	 * classify each node to a cluster based on the shortest distances to the centroid of the cluster
	 * @params distance - the shortest distance matrix
	 * @params bestGuess - the centroids for various clusters
	 * returns the annotated cluster for each node
	 */
	public static int[] annotate(double[][] distance, int[] bestGuess) {
		int numNodes = distance.length;
		int numClusters = bestGuess.length;

		int[] annotatedNodes = new int[numNodes];
		for (int i = 0; i < numNodes; i++) {
			double min = Double.POSITIVE_INFINITY;
			int ind = -1;
			for (int j = 0; j < numClusters; j++) {
				if (distance[i][bestGuess[j]] < min) {
					min = distance[i][bestGuess[j]];
					ind = j;
				}
			}
			annotatedNodes[i] = ind;
		}
		return annotatedNodes;
	}

	/*
	 * Evaluates the random initialisations for clustering the nodes
	 * @params init - init nodes
	 * @params distance - the shortest distance matrix
	 */
	public static double evalInit(int[] init, double[][] distance) {
		// evaluates the random initialisations for the k-means
		double[] dist = new double[init.length];
		double eval = 0;

		for (int i = 0; i < init.length; i++) {
			double min = Double.POSITIVE_INFINITY;
			for (int j = 0; j < init.length; j++) {
				if (i != j) {
					if (distance[init[i]][init[j]] < min) {
						min = distance[init[i]][init[j]];
					}
				}
			}
			dist[i] = min;
		}

		eval = calcGM(dist);
		return eval;
	}

	/*
	 * returns the geometric mean of a given sequence of distances
	 */
	public static double calcGM(double[] dist) {
		double GM = 1;
		for (int i = 0; i < dist.length; i++) {
			GM *= dist[i];
		}
		GM = Math.pow(GM, 1.0 / dist.length);
		return GM;
	}

	/*
	 * Finds the optimum centroids of given clusters of points
	 * @params numAmb - number of available ambulances
	 * @params distance - the shortest distance matrix
	 * @params demandNorm - the normalised demand at each node
	 * @params demand - the demand at each node
	 * @params classArr - to which cluster does each node belongs to
	 * returns - the node numbers where the centroids must be placed
	 */
	public static int[] centroidFinder(int numAmb, double[][] distance, double[] demandNorm, double[] demand,
			int[] classArr) {

		int[] optLocation = new int[numAmb];
		int numNodes = classArr.length;

		for (int i = 0; i < numAmb; i++) {
			List<Integer> classNow = new ArrayList<Integer>();
			for (int j = 0; j < numNodes; j++) {
				if (classArr[j] == i)
					classNow.add(j);
			} // end of list for

			int lstSize = classNow.size();

			if (lstSize == 1) {
				optLocation[i] = classNow.get(0);
				continue;
			}

			double max = Double.NEGATIVE_INFINITY;
			int bestCentroid = -1;

			for (int x = 0; x < lstSize; x++) {
				int iter = 0;
				double[][] weightDist = new double[lstSize - 1][2];

				for (int y = 0; y < lstSize; y++) {
					if (classNow.get(y) != classNow.get(x)) {
						weightDist[iter][0] = demand[classNow.get(y)];
						weightDist[iter][1] = distance[classNow.get(x)][classNow.get(y)];
						iter++;
					}
				}
				double currConf = penalty(weightDist, demand[classNow.get(x)]);
				// System.out.println("PENALTY = " + currConf);
				if (currConf > max) {
					max = currConf;
					bestCentroid = classNow.get(x);
				}
				// if else condn for min
			}
			optLocation[i] = bestCentroid;
		} // end of main class for

		return optLocation;
	}

	/*
	 * Calculates the confidence for a node to be an apt location for the ambulance to be placed in a cluster
	 * @params weightDist - weighted distance from other nodes
	 * @params demNode - demand at that node 
	 */
	public static double penalty(double[][] weightDist, double demNode) {
		double numerator = 0;
		double denominator = 0;
		for (int i = 0; i < weightDist.length; i++) {
			numerator += weightDist[i][0];
			denominator += weightDist[i][0] * (1 / weightDist[i][1]);
		}
		return 1.0 / (numerator / denominator);
	}

	/*
	 * utility fn for the dijkstra's algorithm
	 */
	public static int minDist(double[] dist, Set<Integer> Q) {
		int ind = 0;
		int lenDist = dist.length;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < lenDist; i++) {
			if (Q.contains(i)) {
				if (dist[i] < min) {
					min = dist[i];
					ind = i;
				}
			}
		}
		return ind;
	}

	/*
	 * calculates the min value and its ind in a 1D double array
	 * @params - 1D double array 
	 */
	public static double[] minArr(double[] arr) {
		double ind = 0;
		int lenDist = arr.length;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < lenDist; i++) {
			if (arr[i] < min) {
				min = arr[i];
				ind = i;
			}
		}
		double[] minDetails = new double[2];
		minDetails[0] = ind;
		minDetails[1] = min;
		return minDetails;
	}

	/*
	 * calculates the max value and its ind in a 1D double array
	 * @params - 1D double array 
	 */
	public static double[] maxArr(double[] arr) {
		double ind = 0;
		int lenDist = arr.length;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < lenDist; i++) {
			if (arr[i] > max) {
				max = arr[i];
				ind = i;
			}
		}
		double[] maxDetails = new double[2];
		maxDetails[0] = ind;
		maxDetails[1] = max;
		return maxDetails;
	}
	
	public static int[] findMaxCoverageLocations(int numCentroid, CityMap map) {
		double[][] distance = map.getShortestDistances();
		double[] demand = map.getDemands().stream().mapToDouble(d->d).toArray();
		double max = map.getDemands().stream().max(Double::compareTo).get();
		double[] demandNorm = map.getDemands().stream().mapToDouble(d -> d/max).toArray();
		
		return findMaxCoverageLocations(numCentroid, distance, demandNorm, demand);		
	}
}
