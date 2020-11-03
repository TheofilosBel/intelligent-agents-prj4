package model;

import logist.task.Task;
import logist.topology.Topology.City;

/**
 * This class represents the vehicle variable in the COP
 */
public class VarTask {

    public enum Type {PickUp, Delivery};

    Task task; // The task Delivering or picking up
    Type type; // The type of the Task

    public VarTask(Task task, Type type) {
        this.type = type;
        this.task = task;
    }

    /**
     * Return the task's weight
     * @return
     */
    public int weight() {
        return task.weight;
    }

    /**
     * Get the pickUp city if type is PickUp
     * or delivery city if type is Delivery
     */
    public City city() {
        if (type == Type.PickUp) {
            return task.pickupCity;
        } else {
            return task.deliveryCity;
        }
    }


	@Override
    public String toString() {
        return type + " " + task;
    }


    public Type type() {
        return type;
    }

}
