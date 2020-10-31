package sls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import model.VarVehicle;
import model.VarTask.Type;
import model.Solution;
import model.VarTask;
import java.util.SplittableRandom;

public class StochasticLocalSearch {

    /**
     * Apply the stochastic local search
     */
    public Plan apply(List<VarVehicle> vehicles, TaskSet tasks) {
        // Initialize the solution
        Solution solution = createInitialSolution();
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
        VarTask nexTaskRand = null;
        do {
            randVehicle = vehicles.get( randGen.nextInt(vehicles.size()) );
        } while ( (nexTaskRand = solution.getNextTaskFor(randVehicle)) != null );


        // Create one new solution by transferring the vehicles next task to all other vehicles
        // under the constraint that the can fit it (capacity constraint).



        return null;
    }

    public Solution createInitialSolution() {
        return null;
    }


    /**
     * Run some tests
     */
    public void dumbTest(List<VarVehicle> vehicles, TaskSet tasks) {
        VarVehicle v = vehicles.get(0);

        List<Task> ts = new ArrayList<>();
        int counter = 0;
        for (Task t: tasks) {
            counter++;
            if ( counter <tasks.size() - 4)
                ts.add(t);
        }
        tasks.removeAll(ts);

        Solution s = new Solution();
        for (Task t : tasks) {
            s.addSubTask(v, new VarTask(t, Type.PickUp));
        }
        for (Task t : tasks) {
            s.addSubTask(v, new VarTask(t, Type.Delivery));
        }



        System.out.println(s);
        s = changeTaskOrder(s, v, 0, 1);
        System.out.println(s);
    }


    /**
     * Change the positions of two tasks with respect to their type {pickUp, Deliver}.
     */
    private Solution changeTaskOrder(Solution solution, VarVehicle v, int t1Idx, int t2Idx) throws AssertionError{

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

    // Assigns the first task of vehicle v1 to vehicle v2.
    // private Solution changeVehicle(Solution solution, VarVehicle v1, VarVehicle v2) {
    //     // Create a copy of the given solution.
    //     Solution newSolution = new Solution(solution);

    //     // Get the first task of vehicle v1.
    //     Task task = solution.getFirstTaskOf(v1);

    //     // Remove it from its list of tasks (in the new solution).
    //     newSolution.removeFirstTaskOf(v1);

    //     // Insert it as the first task of vehicle v2.
    //     newSolution.insertFirstTaskTo(v2, task);
    //     newSolution.updateTaskVehicle(task, v2);

    //     return newSolution;
    // }


    public static void main(String[] args) {

    }

}
