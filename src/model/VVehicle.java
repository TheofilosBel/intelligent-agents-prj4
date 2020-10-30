package model;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;

/**
 * This class represents the vehicle variable in the COP
 */
public class VVehicle {

    Integer capacity;
    Integer costPerKm;

    public VVehicle(Integer capacity, Integer costPerKm) {
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
    public static List<VVehicle> toVVehicle(List<Vehicle> vehicles) {
        List<VVehicle> ourVehicles = new ArrayList<>();
        for (Vehicle v: vehicles) {
            ourVehicles.add(new VVehicle(v.capacity(), v.costPerKm()));
        }
        return ourVehicles;
    }
}