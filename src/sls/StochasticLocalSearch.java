package sls;

import logist.plan.Plan;
import logist.simulation.Vehicle;
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
    private long timeout;  // The time that the algorithm has available
    OrderedList<Solution, Double> bestSolutions;

    public StochasticLocalSearch(double choiceProbability, long timeout) {
        this.bestSolutions = new OrderedList<>(5, OrderType.Acceding);
        this.choiceProbability = choiceProbability;
        this.timeout = timeout;
    }

    /**
     * Apply the stochastic local search
     */
    public List<Plan> apply(List<VarVehicle> vehicles, TaskSet tasks) {
        // Start measuring time
        long startTime = System.currentTimeMillis();
        SplittableRandom randGen = new SplittableRandom(startTime);

        // Create the initial solution
        Solution solution = createInitialSolution(vehicles, tasks, randGen);

        // Loop until solution good enough
        int iterCounter = 0;
        do {
            List<Solution> neighbors = chooseNeighbors(solution, vehicles, randGen);
            solution = localChoice(neighbors, solution, randGen);
            iterCounter++;

            // Get the elapsed time from the beginning
            long elapsedTime = System.currentTimeMillis() - startTime;

            // Check termination condition
            if (terminationCondition(elapsedTime, iterCounter)) {
                break;
            }
        } while (true);


        System.out.println("MinCost: " + bestSolutions.peekScore());
        return bestSolutions.getTop().toPlans(vehicles);
    }

    /**
     * @return Return false if we must terminate the local search
     */
    private boolean terminationCondition(long elapsedTime, int iterCounter) {

        // Check time
        if (elapsedTime + 500 >= timeout) {  // give 500 ms to shutdown
            return true;
        }

        if (iterCounter >= 100000) {
            return true;
        }

        // if time is not bothering us, then check the top 5 solutions
        // If not that different then break
        // Double topScore = bestSolutions.peekScore();
        // Double secondScore = bestSolutions.peek2ndScore();
        // if (topScore != null && secondScore != null && secondScore - topScore < 10e8) {
        //     return true;
        // }

        return false;
    }

    private Solution localChoice(List<Solution> neighbors, Solution oldSolution, SplittableRandom randGen) {
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
            bestSolutions.addElement(bestSolution, minCost);

            // Return the best solution
            return bestSolution;
        }
        else {
            return oldSolution;
        }

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
    public Solution createInitialSolution(List<VarVehicle> vehicles, TaskSet tasks, SplittableRandom randGen) {
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


    public Solution createInitialSolution2(List<VarVehicle> vehicles, TaskSet tasks) {
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
    public List<Plan> dumbTest(List<VarVehicle> vehicles, TaskSet tasks) {
        VarVehicle v = vehicles.get(0);

        // Change task order test
        // List<Task> ts = new ArrayList<>();
        // int counter = 0;
        // for (Task t: tasks) {
        //     counter++;
        //     if (counter < tasks.size() - 4)
        //         ts.add(t);
        // }
        // tasks.removeAll(ts);

        Solution s = new Solution(vehicles);
        for (Task t : tasks) {
            s.addVarTask(v, new VarTask(t, Type.PickUp));
        }
        for (Task t : tasks) {
            s.addVarTask(v, new VarTask(t, Type.Delivery));
        }

        // Loop until solution good enough
        // SplittableRandom randGen = new SplittableRandom(1);
        // List<Solution> neighbors = chooseNeighbors(s, vehicles, randGen);

        System.out.println(s);
        s = changeVehicle(s, vehicles.get(0), vehicles.get(1));
        System.out.println(s);
        s.checkSupps();

        s = changeVehicle(s, vehicles.get(0), vehicles.get(1));
        System.out.println(s);
        s.checkSupps();

        return s.toPlans(vehicles);
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
