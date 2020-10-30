package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import model.VarTask.Type;
import utils.Pair;

public class Solution {

    // For each vehicle store a list of subTasks with the order they should get executed.
    // Each item of the list stores the subTask along with an index to its supplementary subTask
    // (if pickup the index points to the delivery and if delivery the index points to pickup)
    private HashMap<VarVehicle, List<Pair<VarTask, Integer>>> nextTask = new HashMap<>();

    HashMap<VarTask, VarVehicle> taskVehicles; // Maps tasks to the vehicles that carry them.

    public Solution() {}

    /** Copy constructor */
    public Solution(Solution toCopy) {
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: toCopy.nextTask.entrySet()) {
            this.nextTask.put( entry.getKey(), new ArrayList<>(entry.getValue()) );
        }
    }

    /**
     * Adds the subTask to the end of the ordered list of the vehicles tasks
     *
     * @param v The vehicle
     * @param t The task
     */
    public void addSubTask(VarVehicle v, VarTask t) {
        List< Pair<VarTask, Integer>> tasks = this.nextTask.get(v);

        // If list empty create a new list
        if (tasks == null) {
            tasks = new ArrayList<>();
            this.nextTask.put(v, tasks);
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
                    pair.setRight( tasks.size() );  // Add as index the size, witch will be the new index of t
                    tasks.add(new Pair<>(t, idx));

                    // ! Debug
                    if (pair.getLeft().type == Type.Delivery)
                        throw new AssertionError("Adding two delivery tasks for: " + t.task);

                    // Update flag and break
                    foundPickUp = true;
                    break;
                }
            }

            // ! Debug
            if (!foundPickUp)
                throw new AssertionError("Delivery Task not found for: " + t.task);
        }
    }


    /**
     * Check if task1's t1 supplementary action is between
     * t1Idx and t2Idx and If so return false.
     *
     * @NOTE: Deliver is the supplementary of pickUp and visa versa.
     * @param t1
     * @param t2
     */
    public boolean checkDeliverOrder(VarVehicle v,  int t1Idx, int t2Idx) {
        if (this.nextTask.get(v).get(t1Idx).getRight() < t2Idx) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Swaps the two indexes in the list of tasks for vehicle v
     */
    public void swapSubTasksFor(VarVehicle v, int t1Idx, int t2Idx) {
        // before swapping remember to change the indexes of the supplementary tasks
        int supt1Idx = this.nextTask.get(v).get(t1Idx).getRight();
        int supt2Idx = this.nextTask.get(v).get(t1Idx).getRight();

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

        Collections.swap(this.nextTask.get(v), t1Idx, t1Idx);
    }

    // Returns the first task of the given vehicle.
    // public VarTask getFirstTaskOf(VarVehicle vehicle) {
    //     return nextTask.get(vehicle).get(0);
    // }

    // Removes the first task of the given vehicle.
    // public void removeFirstTaskOf(VarVehicle vehicle) {
    //     nextTask.get(vehicle).remove(0);
    // }

    // Insert the given task as the first one in the list of the given vehicle.
    // public void insertFirstTaskTo(VarVehicle vehicle, VarTask task) {
    //     nextTask.get(vehicle).add(0, task);
    // }

    // Updates the vehicles of the given task in the map.
    // public void updateTaskVehicle(VarTask task, VarVehicle vehicle) {
    //     taskVehicles.put(task, vehicle);
    // }

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
