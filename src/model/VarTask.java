package model;

import logist.task.Task;

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
        return this.task.weight;
    }


	@Override
    public String toString() {
        return type + " " + task;
    }

}
