package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import logist.plan.Plan;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import model.VarTask.Type;
import utils.Pair;

public class Solution {

    // For each vehicle store a list of tasks with the order they should get executed.
    // Each item of the list stores the task along with an index to its supplementary task
    // (if pickup the index points to the delivery and if delivery the index points to pickup).
    private HashMap<VarVehicle, List<Pair<VarTask, Integer>>> nextTask = new HashMap<>();

    HashMap<VarTask, VarVehicle> taskVehicles = new HashMap<>(); // Maps tasks to the vehicles that carry them.

    public Solution(List<VarVehicle> vehicles) {
        for (VarVehicle vehicle: vehicles) {
            this.nextTask.put(vehicle, new ArrayList<>());
        }
    }

    /** Copy constructor */
    public Solution(Solution toCopy) {
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: toCopy.nextTask.entrySet()) {
            List<Pair<VarTask, Integer>> copyTasks = new ArrayList<>();
            for (Pair<VarTask, Integer> pair: entry.getValue()) {
                copyTasks.add(new Pair<>(pair));
            }
            this.nextTask.put(entry.getKey(), copyTasks);
        }
    }

    /**
     * Return the first {@link VarTask} set to be executed for a {@link VarVehicle}
     */
    public VarTask getNextTask(VarVehicle v) {
        if (this.nextTask.get(v).isEmpty()) {
            return null;
        } else {
            return this.nextTask.get(v).get(0).getLeft();
        }
    }

    public Integer getTasksSize(VarVehicle v) {
        return this.nextTask.get(v).size();
    }

    /**
     * Check the if the stream of tasks in vehicle v satisfies its capacity constraint.
     *
     * @param v
     * @return
     */
    public boolean checkCapacityConstraint(VarVehicle v) {
        Integer currentWeight = 0;

        for (Pair<VarTask, Integer> pair: this.nextTask.get(v)) {
            if (pair.getLeft().type == Type.PickUp) {
                currentWeight += pair.getLeft().weight(); // Add for pickup
            }
            else {
                currentWeight -= pair.getLeft().weight(); // Subtract for deliver
            }

            if (currentWeight > v.capacity()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds the subTask to the end of the ordered list of the vehicles tasks
     *
     * @param v The vehicle
     * @param t The task
     */
    public void addVarTask(VarVehicle v, VarTask t) {
        List<Pair<VarTask, Integer>> tasks = this.nextTask.get(v);

        // If list empty create a new list
        if (tasks == null) {
            // ! Debug
            throw new AssertionError("Tasks null for: " + v);
        }

        // If task is pickUp simply append it in the list
        if (t.type == Type.PickUp) {
            tasks.add(new Pair<>(t, -1));  // -1 because we dont have the supplementary sub Task yet
        }
        // If delivery then search for the supplementary pick up and bind them
        else {
            // Loop all subTasks
            boolean foundPickUp = false;
            for (int idx = 0; idx < tasks.size(); idx++) {
                Pair<VarTask, Integer> pair = tasks.get(idx);

                // Find the one that holds the same task as t
                if (pair.getLeft().task.id == t.task.id) {
                    pair.setRight( tasks.size() );  // Add as index the size, which will be the new index of t
                    tasks.add(new Pair<>(t, idx));

                    // ! Debug
                    if (pair.getLeft().type == Type.Delivery)
                        throw new AssertionError("Adding two delivery tasks for: " + t.task);

                    // Update flag and break
                    foundPickUp = true;
                    break;
                }
            }

            // DEBUG:
            if (!foundPickUp)
                throw new AssertionError("Delivery Task not found for: " + t.task);
        }
    }


    /**
     * PickUp - Deliver order constraint
     *
     * Check if task1's t1 supplementary action is in interval (t1Idx, t2Idx] or
     * if task2's supplementary action is in interval [t1Idx, t2Idx).
     * If so return false
     *
     * @NOTE: Deliver is the supplementary of pickup and vice versa.
     * @param t1
     * @param t2
     */
    public boolean checkPickUpDeliverOrder(VarVehicle v,  int t1Idx, int t2Idx) {
        // Get the supplementary task indices
        Integer supT1Idx = this.nextTask.get(v).get(t1Idx).getRight();
        Integer supT2Idx = this.nextTask.get(v).get(t2Idx).getRight();

        // Check the intervals
        if ( (supT1Idx <= t2Idx && supT1Idx > t1Idx) || (supT2Idx >= t1Idx && supT2Idx < t2Idx)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Swaps the two indexes in the list of tasks for vehicle v.
     */
    public void swapVarTasksFor(VarVehicle v, int t1Idx, int t2Idx) {
        // before swapping remember to change the indexes of the supplementary tasks
        int supt1Idx = this.nextTask.get(v).get(t1Idx).getRight();
        int supt2Idx = this.nextTask.get(v).get(t2Idx).getRight();

        // Swap the indexes of the sup tasks to show to the new indices
        if (supt1Idx != -1) {
            // ! Debug
            if (this.nextTask.get(v).get(supt1Idx).getLeft().task.id != this.nextTask.get(v).get(t1Idx).getLeft().task.id) {
                throw new AssertionError("Bad mapping between pick up and delivery tasks");
            }

            this.nextTask.get(v).get(supt1Idx).setRight(t2Idx);
        }
        if (supt2Idx != -1) {
            // ! Debug
            if (this.nextTask.get(v).get(supt2Idx).getLeft().task.id != this.nextTask.get(v).get(t2Idx).getLeft().task.id) {
                throw new AssertionError("Bad mapping between pick up and delivery tasks");
            }

            this.nextTask.get(v).get(supt2Idx).setRight(t1Idx);
        }

        Collections.swap(this.nextTask.get(v), t1Idx, t2Idx);
    }

    // Returns a pair containing a task and its supplementary task for a vehicle.
    public Pair<VarTask, VarTask> getTaskAndSupplementaryAt(VarVehicle v, int index) {
        // Get the tasks of the vehicle
        List<Pair<VarTask, Integer>> tasks = nextTask.get(v);

        // Get the index of the supplementary task
        int supIndex = tasks.get(index).getRight();

        // Create a pair containing a task and its supplementary task
        return new Pair<>(tasks.get(index).getLeft(), tasks.get(supIndex).getLeft());
    }

    // Removes the given pair of task sfor the vehicle at the given index.
    public void removeTaskAndSupplementaryAt(VarVehicle v, Pair<VarTask, VarTask> pair, int index) {
        // Get the tasks of the vehicle
        List<Pair<VarTask, Integer>> tasks = nextTask.get(v);

        // Get the index of the supplementary task
        int supIndex = tasks.get(index).getRight();

        // Remove the task and its supplementary task
        tasks.remove(index);
        tasks.remove(supIndex-1); // -1 because we removed one element already

        // Update the supplementary indices of each task
        updateIndicesAfterRemove(tasks, index, supIndex-1);
    }

    // Inserts the given pair of tasks for the vehicle at the given index.
    public void addTaskAndSupplementaryAt(VarVehicle v, Pair<VarTask, VarTask> pair, int index) {
        // Add the two tasks back-to-back in the list of the vehicle
        nextTask.get(v).add(index, new Pair<VarTask, Integer>(pair.getLeft(), index + 1));
        nextTask.get(v).add(index + 1, new Pair<VarTask, Integer>(pair.getRight(), index));

        // Update the supplementary indices of each subsequent task
        updateIndicesAfterAdd(nextTask.get(v));
    }

    // Updates the supplementary index of each task after a removal.
    // The new values depend on where each task is located compared to t1idx and t2idx (indices of removed tasks).
    private void updateIndicesAfterRemove(List<Pair<VarTask, Integer>> tasks, int t1idx, int t2idx) {
        for (int idx = 0; idx < tasks.size(); idx++) {
            Pair<VarTask, Integer> pair = tasks.get(idx);

            if (pair.getRight() > t1idx && pair.getRight() < t2idx) {
                pair.setRight(pair.getRight()-1);
            }
            else if (pair.getRight() > t2idx) {
                pair.setRight(pair.getRight()-2);
            }
        }
    }

    // Updates the supplementary index of each task after an addition.
    // Basically, adds 2 to the index of each task except the first two (which were added).
    private void updateIndicesAfterAdd(List<Pair<VarTask, Integer>> tasks) {
        for (int idx = 2; idx < tasks.size(); idx++) {
            Pair<VarTask, Integer> pair = tasks.get(idx);
            pair.setRight(pair.getRight()+2);
        }
    }

    // Updates the vehicles of the given task in the map.
    public void updateTaskVehicle(VarTask task, VarVehicle vehicle) {
        taskVehicles.put(task, vehicle);
    }

    /**
     * Return the total cost of a solutions.
     *
     * For each vehicle count the total kms required to execute the VarTasks assigned to it.
     * Use as distance between two VarTasks the shortest distance between their cities.
     * In the score consider also the path from vehicle's task city to the first task's city.
     *
     * Then multiply the vehicle's distance with its costPerKm and sum up all the costs for all vehicles.
     */
    public double cost() {
        Double totalCost = 0D;
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: nextTask.entrySet()) {
            Double vehicleCost = 0D;

            // skip vehicles with no tasks
            if (this.getNextTask(entry.getKey()) == null) {
                continue;
            }

            // Add the starting cost from the vehicle's start city to the first task
            vehicleCost = entry.getKey().startCity().distanceTo( this.getNextTask(entry.getKey()).city());

            // Loop all the tasks in a vehicle
            for (int idx = 0; idx < entry.getValue().size() - 2; idx++) { // -2 because we dont want the last element
                VarTask task = entry.getValue().get(idx).getLeft();
                VarTask nextTask = entry.getValue().get(idx + 1).getLeft();
                vehicleCost += task.city().distanceTo(nextTask.city());
            }
            totalCost += vehicleCost * entry.getKey().costPerKm();
        }
        return totalCost;
    }


    /**
     * Create a plan for each vehicle and return a list of all the plans
     * with respect to the order of the list Vehicle passed as parameter
     * @return
     */
    public List<Plan> toPlans(List<VarVehicle> vehicles) {
        List<Plan> plans = new ArrayList<>();
        for (VarVehicle vehicle: vehicles) {
            // Create a plan for each vehicle
            if (this.getNextTask(vehicle) == null) {
                plans.add(Plan.EMPTY);
            }
            else {
                // Stat to the vehicle's start city
                Plan plan = new Plan(vehicle.startCity());

                // Move from vehicle's stat city to first task's city
                for (City cityInPath: vehicle.startCity().pathTo(this.getNextTask(vehicle).city())) {
                    plan.appendMove(cityInPath);
                }

                // Make all the plan actions
                for (int idx = 0; idx < this.getTasksSize(vehicle); idx++) {
                    VarTask task = this.nextTask.get(vehicle).get(idx).getLeft();

                    // First look if task is pickup / deliver and apply the action
                    if (task.type == Type.PickUp) {
                        plan.appendPickup(task.task);
                    }
                    else {
                        plan.appendDelivery(task.task);
                    }

                    // Then if task has next task in the list, move nextTask's city
                    if (idx < this.getTasksSize(vehicle) - 1) {
                        City nextTaskCity = this.nextTask.get(vehicle).get(idx + 1).getLeft().city();
                        for (City cityInPath: task.city().pathTo(nextTaskCity)) {
                            plan.appendMove(cityInPath);
                        }
                    }
                }

                plans.add(plan);
            }
        }

        return plans;
    }

    @Override
    public String toString() {
        String str = "Solution:\n";
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: this.nextTask.entrySet()) {
            str += entry.getKey() + ": " + entry.getValue() + "\n";
        }
        return str;
    }


    // ! Debug
    /**
     * Checks all the indexes in the pairs if they are correct
     */
    public void checkSupps() {
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: nextTask.entrySet()) {

            for (int idx = 0; idx < entry.getValue().size() - 1; idx++) {
                int supIdx = entry.getValue().get(idx).getRight();
                VarTask task    = entry.getValue().get(idx).getLeft();
                VarTask supTask = entry.getValue().get(supIdx).getLeft();

                if (task.task.id != supTask.task.id)
                    throw new AssertionError("Miss indexed tasks");
            }
        }
    }
}
