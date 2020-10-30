package utils;

import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Utils {

    /**
     * Compute the shortest distance between all pairs of cities using the function `City.distanceTo()`
     *
     * @param topology The current topology of cities
     * @return A 2d-matrix of doubles storing the shortest distances between all pairs of cities (matrix[i][j] -> dist from i to j)
     */
    public static double[][] shortestDistances(Topology topology) {
        double[][] shortestDistances = new double[topology.size()][topology.size()];

        for (City c1: topology) {
            for (City c2: topology) {
                shortestDistances[c1.id][c2.id] = c1.distanceTo(c2);
            }
        }

        return shortestDistances;
    }

    /**
     * Find the maximum distance between all cities
     *
     * @param distances The distances of the cities
     * @return The maximum distance among all city pairs
     */
    public static double maxDistance(double[][] distances) {
        double maxDistance = -1D;

		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances[i].length; j++) {
                if (maxDistance < distances[i][j]) {
                    maxDistance = distances[i][j];
                }
            }
        }

        return maxDistance;
    }

    /**
     * Find the maximum reward of all tasks in the topology
     *
     * @param topology The current topology of cities
     * @return The maximum reward
     */
    public static int maxReward(Topology topology, TaskDistribution taskDistribution) {
        int maxReward = -1;

        for (City c1: topology) {
            for (City c2: topology) {
                if (c1 != c2 && maxReward < taskDistribution.reward(c1, c2)){
                    maxReward = taskDistribution.reward(c1, c2);
                }
            }
        }

        return maxReward;
    }

}
