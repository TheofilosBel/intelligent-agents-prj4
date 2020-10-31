package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
     * Return the 1st {@link VarTask} set to be executed for a {@link VarVehicle}
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
     * Check the if the stream of tasks in vehicle v satisfies it's capacity
     * constraint in each step.
     *
     * @param v
     * @return
     */
    public boolean checkCapacityConstraint(VarVehicle v) {
        Integer currentWeight = 0;
        for (Pair<VarTask, Integer> pair: this.nextTask.get(v)) {

            if (pair.getLeft().type == Type.PickUp) {
                currentWeight += pair.getLeft().weight();
            } else {
                currentWeight -= pair.getLeft().weight();
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
        List<Pair<VarTask, Integer>> tasks = this.nextTask.get(v);

        // Get the index of the supplementary task
        int supIndex = tasks.get(index).getRight();

        // Create a pair containing a task and its supplementary task
        Pair<VarTask, VarTask> pair = new Pair<>(tasks.get(index).getLeft(), tasks.get(supIndex).getLeft());

        // Remove the two tasks from the list
        removeTaskAt(v, index);
        removeTaskAt(v, supIndex);

        return pair;
    }

    // Removes the task of a vehicle at the given postition in the list.
    // TODO: update weights
    public void removeTaskAt(VarVehicle v, int position) {
        nextTask.get(v).remove(position);
    }

    // Insert the given task as the first one in the list of the given vehicle.
    // TODO: update weights
    public void addTaskAt(VarVehicle v, VarTask t, int position) {
        nextTask.get(v).add(position, new Pair<>(t, 0));
    }

    // Updates the vehicles of the given task in the map.
    public void updateTaskVehicle(VarTask task, VarVehicle vehicle) {
        taskVehicles.put(task, vehicle);
    }

    /**
     * Return the total cost of a solutions.
     *
     * For each vehicle count the total kms required to execute the VarTasks assigned to it. Use the
     * as distance between two VarTasks the shortest distance between their cities.
     *
     * Then multiply the vehicle's distance with its costPerKm and sum up all the costs for all vehicles.
     */
    public double cost() {
        Double totalCost = 0D;
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: nextTask.entrySet()) {
            // Loop all the tasks in a vehicle
            Double vehicleCost = 0D;
            for (int idx = 0; idx < entry.getValue().size() - 2; idx++) { // -2 because we dont want the last element
                VarTask task = entry.getValue().get(idx).getLeft();
                VarTask nextTask = entry.getValue().get(idx + 1).getLeft();
                vehicleCost += task.city().distanceTo(nextTask.city());
            }
            totalCost += vehicleCost * entry.getKey().costPerKm();
        }
        return totalCost;
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
