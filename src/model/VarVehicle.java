package model;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;

/**
 * This class represents the vehicle variable in the COP
 */
public class VarVehicle {

    private Integer capacity;
    private Integer costPerKm;

    public VarVehicle(Integer capacity, Integer costPerKm) {
        this.capacity = capacity;
        this.costPerKm = costPerKm;
    }

    @Override
    public String toString() {
        return "V {cap= " + capacity + "}";
    }

    /**
     * @param vehicles
     * @return
     */
    public static List<VarVehicle> toVarVehicle(List<Vehicle> vehicles) {
        List<VarVehicle> ourVehicles = new ArrayList<>();
        for (Vehicle v: vehicles) {
            ourVehicles.add(new VarVehicle(v.capacity(), v.costPerKm()));
        }
        return ourVehicles;
    }

    public Integer capacity() {
        return capacity;
    }
}