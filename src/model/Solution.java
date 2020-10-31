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
            this.nextTask.put(entry.getKey(), new ArrayList<>(entry.getValue()) );
        }
    }

    /**
     * Return the 1st {@link VarTask} set to be executed for a {@link VarVehicle}
     */
    public VarTask getNextTask(VarVehicle v) {
        return this.nextTask.get(v).get(0).getLeft();
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

        // First check the intervals (faster)
        if ( (supT1Idx <= t2Idx && supT1Idx > t1Idx) || (supT2Idx >= t1Idx && supT2Idx < t2Idx)) {
            return false;
        }

        // Then check weights in those 3 cases:
        //  1.
        return false;
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
        return new Pair<>(tasks.get(index).getLeft(), tasks.get(supIndex).getLeft());
    }

    // Removes the given pair of task sfor the vehicle at the given index.
    public void removeTaskAndSupplementaryAt(VarVehicle v, Pair<VarTask, VarTask> pair, int index) {
        // Get the tasks of the vehicle
        List<Pair<VarTask, Integer>> tasks = this.nextTask.get(v);

        // Get the index of the supplementary task
        int supIndex = tasks.get(index).getRight();

        // Remove the task and its supplementary task
        tasks.remove(index);
        tasks.remove(supIndex-1); // -1 because we removed one element already
    }

    // Inserts the given pair of tasks for the vehicle at the given index.
    public void addTaskAndSupplementaryAt(VarVehicle v, Pair<VarTask, VarTask> pair, int index) {
        // Add the two tasks back-to-back in the list of the vehicle
        nextTask.get(v).add(index, new Pair<VarTask, Integer>(pair.getLeft(), index + 1));
        nextTask.get(v).add(index + 1, new Pair<VarTask, Integer>(pair.getRight(), index));
    }

    // Updates the vehicles of the given task in the map.
    public void updateTaskVehicle(VarTask task, VarVehicle vehicle) {
        taskVehicles.put(task, vehicle);
    }

    public double cost(){
        return 0d;
    }

    @Override
    public String toString() {
        String str = "Solution:\n";
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: this.nextTask.entrySet()) {
            str += entry.getKey() + ": " + entry.getValue() + "\n";
        }
        return str;
    }

}
