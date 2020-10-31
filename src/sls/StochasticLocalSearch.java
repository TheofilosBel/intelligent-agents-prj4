package sls;

import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
import model.VarVehicle;
import model.VarTask.Type;
import model.Solution;
import model.VarTask;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.SplittableRandom;

public class StochasticLocalSearch {

    /**
     * Apply the stochastic local search
     */
    public Plan apply(List<VarVehicle> vehicles, TaskSet tasks) {
        // Create the initial solution
        Solution solution = createInitialSolution(vehicles, tasks);

        Solution nextSolution = null;
        boolean[] isSame = new boolean[1];  // A boolean wrapper
        SplittableRandom randGen = new SplittableRandom(1);

        // Loop until solution good enough
        do {
            List<Solution> neighbors = chooseNeighbors(solution, vehicles, randGen);
            nextSolution = localChoice(neighbors, isSame);
        }
        while (checkTermination(solution, nextSolution, isSame[0]));

        return null;
    }

    /**
     * @return Return false if we must terminate the local search
     */
    private boolean checkTermination(Solution solution, Solution nextSolution, boolean areEqual) {
        return false;
    }

    private Solution localChoice(List<Solution> neighbors, boolean[] isSame) {
        return null;
    }

    private List<Solution> chooseNeighbors(Solution solution, List<VarVehicle> vehicles, SplittableRandom randGen) {
        List<Solution> neighbors = new ArrayList<>();

        // Get a random vehicle that holds a task (nextTask != Null)
        VarVehicle randVehicle = null;
        do {
            randVehicle = vehicles.get( randGen.nextInt(vehicles.size()) );
        } while (solution.getNextTask(randVehicle) == null);

        // Operation 1:
        // Create one new solution by transferring the randVehicles next task to all other vehicles
        // under the constraint that the can fit it (capacity constraint).
        for (VarVehicle vehicle: vehicles) {
            Solution newSolution = changeVehicle(solution, randVehicle, vehicle);
            if (newSolution.checkCapacityConstraint(vehicle)) {
                neighbors.add(newSolution);
            }
        }

        // Operation 2:
        // Swap the order of the all tasks (if possible) in the randVehicle and create a new solution for each swap
        for (int outerIdx = 0; outerIdx < solution.getTasksSize(randVehicle) - 1; outerIdx++) { // Until previous of last
            for (int innerIdx = outerIdx + 1; innerIdx < solution.getTasksSize(randVehicle); innerIdx++) { // Until last

                 // Check if swap is violating the pickUp-Delivery order constraints
                if (solution.checkPickUpDeliverOrder(randVehicle, outerIdx, innerIdx)) {
                    Solution newSolution = new Solution(solution);
                    newSolution.swapVarTasksFor(randVehicle, outerIdx, innerIdx);
                    newSolution.checkSupps(); // ! Debug

                    // Check if the weight constraints are satisfied
                    if (newSolution.checkCapacityConstraint(randVehicle)) {
                        neighbors.add(newSolution);
                    }
                }
            }
        }

        return neighbors;
    }

    // Creates the initial solution for the problem by assigning every task to the vehicle with the maximum capacity.
    public Solution createInitialSolution(List<VarVehicle> vehicles, TaskSet tasks) {
        Solution solution = new Solution(vehicles);

        // Find the vehicle with the maximum capacity
        VarVehicle v = Collections.max(vehicles, Comparator.comparing(s -> s.capacity()));

        // Assign all tasks to the vehicle with the maximum capacity
        for (Task t: tasks) {
            if (t.weight <= v.capacity()) {
                solution.addVarTask(v, new VarTask(t, Type.PickUp));
                solution.addVarTask(v, new VarTask(t, Type.Delivery));
            }
            else {
                // The problem is unsolvable if the biggest vehicle cannot carry a task
                throw new AssertionError("The problem is unsolvable. Initial solution cannot be created.");
            }
        }

        return solution;
    }

    /**
     * Run some tests
     */
    public void dumbTest(List<VarVehicle> vehicles, TaskSet tasks) {
        Solution solution = createInitialSolution(vehicles, tasks);
        System.out.println(solution);

        // Change task order test

        // List<Task> ts = new ArrayList<>();
        // int counter = 0;
        // for (Task t: tasks) {
        //     counter++;
        //     if (counter < tasks.size() - 4)
        //         ts.add(t);
        // }
        // tasks.removeAll(ts);

        // Solution s = new Solution(vehicles);
        // for (Task t : tasks) {
        //     s.addVarTask(v, new VarTask(t, Type.PickUp));
        // }
        // for (Task t : tasks) {
        //     s.addVarTask(v, new VarTask(t, Type.Delivery));
        // }
    }

    /**
     * Assigns the first task of vehicle v1 to vehicle v2. Also moves its supplementary task.
     */
    private Solution changeVehicle(Solution solution, VarVehicle v1, VarVehicle v2) {
        // Create a copy of the old solution.
        Solution newSolution = new Solution(solution);

        // Get the first task of vehicle v1 and its supplementary task.
        Pair<VarTask, VarTask> taskPair = solution.getTaskAndSupplementaryAt(v1, 0);

        // Remove the first task and its supplementary
        newSolution.removeTaskAndSupplementaryAt(v1, taskPair, 0);

        // Insert the pickUp as the first task of vehicle v2 and the delivery as the second.
        newSolution.addTaskAndSupplementaryAt(v2, taskPair, 0);

        // Update the vehicles of the tasks
        newSolution.updateTaskVehicle(taskPair.getLeft(), v2);
        newSolution.updateTaskVehicle(taskPair.getRight(), v2);

        return newSolution;
    }

}
