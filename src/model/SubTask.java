package model;

import logist.task.Task;

/**
 * This class represents the vehicle variable in the COP
 */
public class SubTask {
    public enum Type {PickUp, Delivery};

    Task task;   // The task Delivering or picking up
    Type type;   // The type of the Task

    public SubTask(Task task, Type type) {
        this.type = type;
        this.task = task;
    }


	@Override
    public String toString() {
        return type + " " + task;
    }

}
