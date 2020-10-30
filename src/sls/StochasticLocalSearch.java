package sls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
import model.VVehicle;
import model.SubTask.Type;
import model.Solution;
import model.SubTask;

public class StochasticLocalSearch {

    /**
     * Apply the stochastic local search
     */
    public Plan apply() {
        // Initialize the solution
        Solution solution = createInitialSolution();
        Solution nextSolution = null;
        boolean[] isSame = new boolean[1];  // A boolean wrapper

        // Loop until solution good enough
        do {
            List<Solution> neighbors = chooseNeighbors(solution);
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

    private List<Solution> chooseNeighbors(Solution solution) {
        return null;
    }

    public Solution createInitialSolution() {
        return null;
    }


    /**
     * Run some tests
     */
    public void dumbTest(List<VVehicle> vehicles, TaskSet tasks) {
        VVehicle v = vehicles.get(0);

        Solution s = new Solution();
        for (Task t : tasks) {
            s.addSubTask(v, new SubTask(t, Type.PickUp));
            s.addSubTask(v, new SubTask(t, Type.Delivery));
        }


        System.out.println(s);
    }


    /**
     * Change the positions of two tasks with respect to their type {pickUp, Deliver}.
     */
    private Solution changeTaskOrder(Solution solution, VVehicle v, int t1Idx, int t2Idx) throws AssertionError{

        // Check if swap is violating the constraints
        if (solution.checkDeliverOrder(v, t1Idx, t2Idx)) {
            Solution newSolution = new Solution(solution);
            newSolution.swapSubTasksFor(v, t1Idx, t2Idx);
            return newSolution;
        }
        else {
            throw new AssertionError("Delivery of T1 before T2");
        }
    }

}
