package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import model.SubTask.Type;
import utils.Pair;

public class Solution {

    // For each vehicle store a list of subTasks with the order they should get executed.
    // Each item of the list stores the subTask along with an index to its supplementary subTask
    // (if pickup the index points to the delivery and if delivery the index points to pickup)
    private HashMap<VVehicle, List<Pair<SubTask, Integer>>> nextTask = new HashMap<>();

    public Solution() {}

    /** Copy constructor */
    public Solution(Solution toCopy) {
        for (Entry<VVehicle, List<Pair<SubTask, Integer>>> entry: toCopy.nextTask.entrySet()) {
            this.nextTask.put( entry.getKey(), new ArrayList<>(entry.getValue()) );
        }
    }

    /**
     * Adds the subTask to the end of the ordered list of the vehicles tasks
     *
     * @param v The vehicle
     * @param t The task
     */
    public void addSubTask(VVehicle v, SubTask t) {
        List< Pair<SubTask, Integer>> tasks = this.nextTask.get(v);

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
                Pair<SubTask, Integer> pair = tasks.get(idx);

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
    public boolean checkDeliverOrder(VVehicle v,  int t1Idx, int t2Idx) {
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
    public void swapSubTasksFor(VVehicle v, int t1Idx, int t2Idx) {
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

    public double cost(){
        return 0d;
    }

    @Override
    public String toString() {
        String str = "Solution:\n";
        for (Entry<VVehicle, List<Pair<SubTask, Integer>>> entry: this.nextTask.entrySet()) {
            str += entry.getKey() + ": " + entry.getValue() + "\n";
        }
        return str;
    }
}
