package sls;

import model.Vehicle;
import model.Solution;
import model.Task;

public class StochasticLocalSearch {

    // Assigns the first task of vehicle v1 to vehicle v2.
    private Solution changeVehicle(Solution solution, Vehicle v1, Vehicle v2) {
        // Create a copy of the given solution.
        newSolution = new Solution(solution);

        // Get the first task of vehicle v1.
        Task task = solution.getFirstTaskOf(v1);

        // Remove it from its list of tasks (in the new solution).
        newSolution.removeFirstTaskOf(v1);

        // Insert it as the first task of vehicle v2.
        newSolution.insertFirstTaskTo(v2, task);
        newSolution.updateTaskVehicle(task, vehicle);

        return newSolution;
    }

}
