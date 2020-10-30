package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Solution {
    HashMap<Vehicle, List<Task>> nextTask = new HashMap<>();

    public Solution() {}

    /** Copy constructor */
    public Solution(Solution toCopy) {
        for (Entry<Vehicle, List<Task>> entry: toCopy.nextTask.entrySet()) {
            this.nextTask.put( entry.getKey(), new ArrayList<>(entry.getValue()) );
        }
    }

}
