package model;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;
import java.awt.Color;

/**
 * This class represents the vehicle variable in the COP
 */
public class VarVehicle {

    private Integer capacity;
    private Integer costPerKm;
    private City startCity;
    private Color color;

    public VarVehicle(Integer capacity, Integer costPerKm, City startCity, Color  color) {
        this.capacity = capacity;
        this.costPerKm = costPerKm;
        this.startCity = startCity;
        this.color = color;
    }

    @Override
    public String toString() {
        return "V {cap= " + capacity + " c= " + color + "}";
    }

    /**
     * @param vehicles
     * @return
     */
    public static List<VarVehicle> toVarVehicle(List<Vehicle> vehicles) {
        List<VarVehicle> ourVehicles = new ArrayList<>();
        for (Vehicle v: vehicles) {
            ourVehicles.add(new VarVehicle(v.capacity(), v.costPerKm(), v.getCurrentCity(), v.color()));
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