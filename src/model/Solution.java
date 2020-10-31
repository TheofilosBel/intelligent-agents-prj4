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
    private HashMap<VarVehicle, List<Integer>> vehicleLoad = new HashMap<>();

    HashMap<VarTask, VarVehicle> taskVehicles; // Maps tasks to the vehicles that carry them

    public Solution() {}

    /** Copy constructor */
    public Solution(Solution toCopy) {
        for (Entry<VarVehicle, List<Pair<VarTask, Integer>>> entry: toCopy.nextTask.entrySet()) {
            this.nextTask.put(entry.getKey(), new ArrayList<>(entry.getValue()) );
        }
    }

    /**
     * Return the 1st {@link VarTask} set to be executed for a {@link VarVehicle}
     */
    public VarTask getNextTaskFor(VarVehicle v) {
        return this.nextTask.get(v).get(0).getLeft();
    }

    /**
     * Adds the subTask to the end of the ordered list of the vehicles tasks
     *
     * @param v The vehicle
     * @param t The task
     */
    public void addSubTask(VarVehicle v, VarTask t) {
        List<Pair<VarTask, Integer>> tasks = this.nextTask.get(v);
        List<Integer> weightsInTime = vehicleLoad.get(v);

        // If list empty create a new list
        if (tasks == null) {
            tasks = new ArrayList<>();
            this.nextTask.put(v, tasks);
            weightsInTime = new ArrayList<>();
            this.vehicleLoad.put(v, weightsInTime);
        }

        // If task is pickUp simply append it in the list
        if (t.type == Type.PickUp) {
            tasks.add(new Pair<>(t, -1));  // -1 because we dont have the supplementary sub Task yet

            // Add the weight to the current time
            if (weightsInTime.isEmpty()) {
                weightsInTime.add(t.weight());
            } else {
                weightsInTime.add(weightsInTime.get(weightsInTime.size() - 1) + t.weight());  // There is idx - 1 for sure
            }

            // ! Debug

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

                    // Update the weight: Remove the tasks weight
                    weightsInTime.add(weightsInTime.get(weightsInTime.size() - 1) - t.weight());

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
     * Check 2 tings:
     * 1. If task1's t1 supplementary action is in interval (t1Idx, t2Idx] or
     *    if task2's supplementary action is in interval [t1Idx, t2Idx)
     * 2. if swapping will result in an overload of the capacity of the vehicle.
     *
     * If one of the above holds, return false
     *
     * @NOTE: Deliver is the supplementary of pickup and vice versa.
     * @param t1
     * @param t2
     */
    public boolean checkDeliverOrder(VarVehicle v,  int t1Idx, int t2Idx) {
        // Get the supplementary task indices
        Integer supT1Idx = this.nextTask.get(v).get(t1Idx).getRight();
        Integer supT2Idx = this.nextTask.get(v).get(t2Idx).getRight();

        // First check the intervals (faster)
        if (this.nextTask.get(v).get(t1Idx).getRight() <= t2Idx  ||
            this.nextTask.get(v).get(t2Idx).getRight() >= t1Idx) {
            return false;
        }

        // Then check weights in those 3 cases:
        //  1.
        // if ()
        return false;
    }

    /**
     * Swaps the two indexes in the list of tasks for vehicle v.
     */
    public void swapSubTasksFor(VarVehicle v, int t1Idx, int t2Idx) {
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

    // Returns the first task of the given vehicle.
    public VarTask getFirstTaskOf(VarVehicle v) {
        return nextTask.get(v).get(0).getLeft();
    }

    // Removes the task of a vehicle at the given postition in the list.
    public void removeTaskAt(VarVehicle v, int position) {
        nextTask.get(v).remove(position);
    }

    // Insert the given task as the first one in the list of the given vehicle.
    public void addTaskAt(VarVehicle v, VarTask t, int position) {
        nextTask.get(v).add(position, new Pair<>(t, 0));
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
