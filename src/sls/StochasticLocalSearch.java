package sls;

import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
import model.VarVehicle;
import model.VarTask.Type;
import model.Solution;
import model.VarTask;
import utils.OrderedList;
import utils.Pair;
import utils.OrderedList.OrderType;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.SplittableRandom;

public class StochasticLocalSearch {

    private Double choiceProbability;
    private int iterations;
    private long timeout;  // The time that the algorithm has available
    OrderedList<Pair<Solution, Integer>, Double> bestSolutions;

    public StochasticLocalSearch(double choiceProbability, int iterations, long timeout) {
        this.bestSolutions = new OrderedList<>(5, OrderType.Acceding);
        this.choiceProbability = choiceProbability;
        this.iterations = iterations;
        this.timeout = timeout;
    }

    /**
     * Apply the stochastic local search
     */
    public List<Plan> apply(List<VarVehicle> vehicles, TaskSet tasks) {
        // Start measuring time
        long startTime = System.currentTimeMillis();
        SplittableRandom randGen = new SplittableRandom(1);

        // Create the initial solution
        Solution solution = createShortestInitialSolution(vehicles, tasks);
        // Solution solution = createMaxInitialSolution(vehicles, tasks);
        System.out.println("[INF] Initial solution cost: " + solution.cost());

        // Loop until solution good enough
        int iterCounter = 0;
        do {
            List<Solution> neighbors = chooseNeighbors(solution, vehicles, randGen);
            solution = localChoice(neighbors, solution, randGen, iterCounter);
            iterCounter++;

            // Get the elapsed time from the beginning
            long elapsedTime = System.currentTimeMillis() - startTime;

            // Check termination condition
            if (terminationCondition(elapsedTime, iterCounter)) {
                break;
            }
        } while (true);


        System.out.println("[INF] Last solution cost: " + solution.cost());
        System.out.println("[INF] Best solution cost: " + bestSolutions.peekScore() + " on iter: " + bestSolutions.getTop().getRight());
        bestSolutions.getTop().getLeft().printCost();
        System.out.println("[INF] 2nd best solution cost: " + bestSolutions.peek2ndScore());
        return bestSolutions.getTop().getLeft().toPlans(vehicles);
    }

    /**
     * @return Return false if we must terminate the local search
     */
    private boolean terminationCondition(long elapsedTime, int iterCounter) {

        // Check time
        if (elapsedTime + 500 >= timeout) {  // give 500 ms to shutdown
            return true;
        }

        if (iterCounter >= iterations) {
            return true;
        }

        return false;
    }

    private Solution localChoice(List<Solution> neighbors, Solution oldSolution, SplittableRandom randGen, Integer iterCounter) {
        Double probability = randGen.nextDouble(1D);

        // With probability p return the best neighbor
        if (probability <= choiceProbability) {  // From 0.0 --to-> p: predict best
            Double minCost = oldSolution.cost();
            Solution bestSolution = oldSolution;

            for (Solution solution: neighbors) {
                Double cost = solution.cost();
                if (cost < minCost) {
                    minCost = cost;
                    bestSolution = solution;
                }
            }

            // Store the best solution
            bestSolutions.addElement(new Pair<>(bestSolution, iterCounter), minCost);

            // Return the best solution
            return bestSolution;
        }
        // Else return a random neighbor (or old)
        else {
            // neighbors.add(oldSolution);
            return neighbors.get(randGen.nextInt(neighbors.size()));
        }

    }

    private List<Solution> chooseNeighbors(Solution solution, List<VarVehicle> vehicles, SplittableRandom randGen) {
        List<Solution> neighbors = new ArrayList<>();

        // Find all vehicles with tasks
        // VarVehicle taskVehicles =

        // Get a random vehicle that holds a task (nextTask != Null)
        VarVehicle randVehicle = null;
        do {
            randVehicle = vehicles.get( randGen.nextInt(vehicles.size()) );
        } while (solution.getNextTask(randVehicle) == null);

        // Operation 1:
        // Create one new solution by transferring the randVehicles next task to all other vehicles
        // under the constraint that the can fit it (capacity constraint).
        for (VarVehicle vehicle: vehicles) {
            if (vehicle == randVehicle) continue; // Skip same vehicle

            // Create new solution from changeVehicle
            Solution newSolution = changeVehicle(solution, randVehicle, vehicle);

            // If constraints are met add it to neighbors
            if (newSolution.checkCapacityConstraint(vehicle)) {
                neighbors.add(newSolution);

                // Change the order of the deliver the task with every possible pickup after that and create a new neighbor
                // neighbors.addAll(swapDeliveryTask(newSolution, vehicle));
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

                    // Check if the weight constraints are satisfied
                    if (newSolution.checkCapacityConstraint(randVehicle)) {
                        neighbors.add(newSolution);
                    }
                }
            }
        }

        return neighbors;
    }

    /**
     * Creates the initial solution for the problem by assigning every task to the vehicle with the maximum capacity.
     */
    public Solution createShortestInitialSolution(List<VarVehicle> vehicles, TaskSet tasks) {
        Solution solution = new Solution(vehicles);

        // Find the vehicle with the maximum capacity
        VarVehicle maxV = Collections.max(vehicles, Comparator.comparing(s -> s.capacity()));

        // Assign all tasks to the vehicle closest to it
        for (Task t: tasks) {
            // Get random vehicle
            VarVehicle closestVehicle = null;
            Double minDistance = Double.MAX_VALUE;
            for (VarVehicle v: vehicles) {
                double dist = v.startCity().distanceTo(t.pickupCity);
                if (dist < minDistance) {
                    minDistance = dist;
                    closestVehicle = v;
                }
            }

            // Try to put it on the closestVehicle
            if (t.weight <= closestVehicle.capacity()) {
                solution.addVarTask(closestVehicle, new VarTask(t, Type.PickUp));
                solution.addVarTask(closestVehicle, new VarTask(t, Type.Delivery));
            }
            // If it doesn't fit try the max vehicle
            else if (t.weight <= maxV.capacity()) {
                solution.addVarTask(maxV, new VarTask(t, Type.PickUp));
                solution.addVarTask(maxV, new VarTask(t, Type.Delivery));
            }
            else {
                // The problem is unsolvable if the biggest vehicle cannot carry a task
                throw new AssertionError("The problem is unsolvable. Initial solution cannot be created.");
            }
        }

        return solution;
    }

    public Solution createMaxInitialSolution(List<VarVehicle> vehicles, TaskSet tasks) {
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



    /**
     * Swap the 1st delivery task with all pickUp task after it (care the weight).
     * @NOTE: The 1st delivery task must be at index 1!
     *
     * @param solution
     * @param vehicle
     * @return
     */
    private List<Solution> swapDeliveryTask(Solution solution, VarVehicle vehicle) {
        List<Solution> newSolutions = new ArrayList<>();
        int deliveryIdx = 1;

        // Loop every task from index = 2 and on
        for (int idx = 2; idx < solution.getTasksSize(vehicle); idx++) {

            if (solution.getTask(vehicle, idx).type() == Type.PickUp) {
                // Create a new solution for each swap
                Solution newSolution = new Solution(solution);
                newSolution.swapVarTasksFor(vehicle, deliveryIdx, idx);

                // Check if the weight constraints are satisfied
                if (newSolution.checkCapacityConstraint(vehicle)) {
                    newSolutions.add(newSolution);
                }
            }
        }

        return newSolutions;
    }
}
