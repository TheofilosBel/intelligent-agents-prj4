package model;

import java.util.HashMap;
import java.util.List;

public class Solution {

    HashMap<Vehicle, List<Task>> nextTask; // Maps a vehicle to a list of tasks that need to be carried in order.
    HashMap<Task, Vehicle> taskVehicles; // Maps tasks to the vehicles that carry them.

    // Returns the first task of the given vehicle.
    public Task getFirstTaskOf(Vehicle vehicle) {
        return nextTask.get(vehicle).get(0);
    }

    // Removes the first task of the given vehicle.
    public void removeFirstTaskOf(Vehicle vehicle) {
        nextTask.get(vehicle).remove(0);
    }

    // Insert the given task as the first one in the list of the given vehicle.
    public void insertFirstTaskTo(Vehicle vehicle, Task task) {
        nextTask.get(vehicle).add(0, task);
    }

    // Updates the vehicles of the given task in the map.
    public void updateTaskVehicle(Task task, Vehicle vehicle) {
        taskVehicles.put(task, vehicle);
    }

}
