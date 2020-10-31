package model;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

/**
 * This class represents the vehicle variable in the COP
 */
public class VarVehicle {

    private Integer capacity;
    private Integer costPerKm;
    private City startCity;

    public VarVehicle(Integer capacity, Integer costPerKm, City startCity) {
        this.capacity = capacity;
        this.costPerKm = costPerKm;
        this.startCity = startCity;
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
            ourVehicles.add(new VarVehicle(v.capacity(), v.costPerKm(), v.getCurrentCity()));
        }
        return ourVehicles;
    }

    public Integer capacity() {
        return capacity;
    }

    public Integer costPerKm() {
        return costPerKm;
    }

    public City startCity() {
        return startCity;
    }
}