package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The class for the Basic Tunnel, extending Tunnel.
 *
 * @author cs131a
 */
public class BasicTunnel extends Tunnel {

    /**
     * a collection that contains all the vehicles in this tunnel
     */
    private Collection<Vehicle> vehicleList = new ArrayList<>();

    /**
     * Creates a new instance of a basic tunnel with the given name
     *
     * @param name the name of the basic tunnel
     */
    public BasicTunnel(String name) {
        super(name);
    }

    /**
     * Override tryToEnterInner method, returns true if the vehicle is allowed to enter
     * <p>
     * Restriction
     * • Each tunnel has only one lane so at any given time all vehicles must be traveling in the same direction.
     * • Only three cars may be inside a tunnel at any given time.
     * • Only one sled may be inside a tunnel at any given time.
     * • Cars and sleds cannot share a tunnel.
     *
     * @param vehicle The vehicle that is attempting to enter
     * @return true if the vehicle did not break the restrictions above
     */
    @Override
    protected synchronized boolean tryToEnterInner(Vehicle vehicle) {
        int totalCars = 0;
        int numOfSled = 0;
        int numOfCars = 0;
        Direction currentDirection;
        // if no vehicle is in the tunnel, we just add current vehicle
        // for task 2b, add second condition
        if (vehicleList.isEmpty() || vehicle instanceof Ambulance) {
            vehicleList.add(vehicle);
            return true;
        }
        // if at least one vehicle is in the tunnel, the direction should be the
        // same as previous all vehicles in the list
        for (Vehicle vehicle1 : this.vehicleList) {
            // for task 2b
            if (vehicle1 instanceof Ambulance) continue;
            currentDirection = vehicle1.getDirection();
            Direction vehicleDirection = vehicle.getDirection();
            if (!currentDirection.equals(vehicleDirection)) return false;
            // stats
            if (vehicle1 instanceof Car) {
                if (numOfSled > 0) return false;
                numOfCars++;
                totalCars++;
            } else if (vehicle1 instanceof Sled) {
                if (numOfSled > 0) return false;
                if (numOfCars > 0) return false;
                numOfSled++;
                totalCars++;
            }
            // num of vehicles restriction
            if (totalCars >= 3) return false;
        }
        // car restriction
        if (vehicle instanceof Car) {
            if (numOfSled > 0) return false;
        }
        // sled restriction
        else if (vehicle instanceof Sled) {
            if (numOfSled > 0) return false;
            if (numOfCars > 0) return false;
        }
        vehicleList.add(vehicle);
        return true;
    }

    @Override
    public synchronized void exitTunnelInner(Vehicle vehicle) {
        this.vehicleList.remove(vehicle);
    }

}
