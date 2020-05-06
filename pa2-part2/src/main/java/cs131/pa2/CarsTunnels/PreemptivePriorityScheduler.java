package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Log.Log;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The preemptive priority scheduler assigns vehicles to tunnels based on their priority and supports ambulances.
 * It extends the Tunnel class.
 *
 * @author cs131a
 */
public class PreemptivePriorityScheduler extends Tunnel {

    /**
     * collection of all tunnels
     */
    private Collection<Tunnel> tunnels;

    /**
     * priority queue of all vehicles
     */
    private PriorityQueue<Vehicle> pq;

    /**
     * lock that locks shared resources
     */
    private final ReentrantLock lock;

    /**
     * condition variable for the lock
     */
    private final Condition cond;

    /**
     * map between vehicle and tunnel, record which tunnel the vehicle enters
     */
    private Map<Vehicle, Tunnel> vehicleTunnelMap;

    /**
     * map between tunnel and vehicles as a set, record all cars in the same tunnel
     */
    private Map<Tunnel, HashSet<Vehicle>> tunnelHashSetMap;

    /**
     * Creates a new instance of the class PreemptivePriorityScheduler with given name by calling the constructor of the super class
     *
     * @param name     the name of the preemptive priority scheduler to create
     * @param tunnels  the collection of all tunnels
     * @param log      logs
     */
    public PreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
        super(name, log);
        this.tunnels = tunnels;
        Comparator<Vehicle> cmp = (o1, o2) -> o2.getPriority() - o1.getPriority();
        this.pq = new PriorityQueue<>(cmp);
        this.lock = new ReentrantLock();
        this.vehicleTunnelMap = new HashMap<>();
        this.tunnelHashSetMap = new HashMap<>();
        // initialize locks for tunnels
        for (Tunnel tunnel : tunnels) {
            ReentrantLock tunnelLock = new ReentrantLock();
            tunnel.setLock(tunnelLock);
            tunnel.setCond(tunnelLock.newCondition());
        }
        this.cond = this.lock.newCondition();
    }

    /**
     * Override tryToEnterInner method. Achieve synchronization using locks and condition variable
     * The vehicle can enter the tunnel if and only if it has the highest priority in the priority
     * queue and the meets the entry condition of one tunnel.
     * Or if the vehicle is an ambulance, it can enter the tunnel regardless of how many vehicles
     * are in the tunnel or the direction. When an ambulance enters the tunnel, all the vehicles in
     * the tunnel must stop and wait for the ambulance to exit.
     * The only condition that an ambulance cannot enter is when all the tunnels have ambulance in it.
     *
     * @param vehicle The vehicle that is attempting to enter
     * @return true if the vehicle successfully enter the tunnel
     */
    @Override
    public boolean tryToEnterInner(Vehicle vehicle) {
        while (true) {
            lock.lock();
            try {
                // is ambulance
                if (vehicle instanceof Ambulance) {
                    for (Tunnel tunnel : tunnels) {
                        // if no ambulance in tunnel
                        if (tunnel.tryToEnter(vehicle) && !tunnel.hasAmbulance()) {
                            // indicate that there is an ambulance in tunnel
                            tunnel.setHasAmbulance(true);
                            // record which tunnel the vehicle enters
                            vehicleTunnelMap.put(vehicle, tunnel);
                            // terminate all the vehicles inside the tunnel
                            HashSet<Vehicle> vehicleHashSet = tunnelHashSetMap.getOrDefault(tunnel, null);
                            if (vehicleHashSet != null) {
                                vehicleHashSet.add(vehicle);
                                for (Vehicle v : vehicleHashSet) {
                                    // signal the vehicles that needs to stop
                                    v.terminateVehicle();
                                }
                                // update the map
                                tunnelHashSetMap.put(tunnel, vehicleHashSet);
                            } else {
                                vehicleHashSet = new HashSet<>();
                                vehicleHashSet.add(vehicle);
                                // update the map
                                tunnelHashSetMap.put(tunnel, vehicleHashSet);
                            }
                            return true;
                        }
                    }
                }
                // not ambulance
                else {
                    // if pq does not have the vehicle, push current vehicle into the pq
                    if (!pq.contains(vehicle)) {
                        pq.offer(vehicle);
                    }
                    // entry condition
                    if (pq.element().getPriority() <= vehicle.getPriority()) {
                        for (Tunnel tunnel : tunnels) {
                            // if success
                            if (tunnel.tryToEnter(vehicle) && !tunnel.hasAmbulance()) {
                                pq.poll();
                                // record which tunnel did this vehicle enter
                                vehicleTunnelMap.put(vehicle, tunnel);
                                HashSet<Vehicle> vehicleHashSet = tunnelHashSetMap.getOrDefault(tunnel, null);
                                if (vehicleHashSet != null) {
                                    vehicleHashSet.add(vehicle);
                                    // update map
                                    tunnelHashSetMap.put(tunnel, vehicleHashSet);
                                } else {
                                    vehicleHashSet = new HashSet<>();
                                    vehicleHashSet.add(vehicle);
                                    // update map
                                    tunnelHashSetMap.put(tunnel, vehicleHashSet);
                                }
                                // set condition variable for vehicle
                                return true;
                            }
                        }
                    }
                }
                // if not success, set to await, wait for some vehicle to exit to signal this cond.
                this.cond.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.lock.unlock();
            }
        }
    }

    /**
     * let the vehicle exit the tunnel. Signal all awaiting threads to try to enter
     * the tunnel.
     * If the vehicle is an ambulance it will signal all the awaiting vehicles and
     * let them continue moving.
     * If the vehicle is not an ambulance, it leaves the tunnel regularly.
     *
     * @param vehicle The vehicle that is exiting the tunnel
     */
    @Override
    public void exitTunnelInner(Vehicle vehicle) {
        // get the tunnel that this vehicle enters
        Tunnel tunnel = vehicleTunnelMap.get(vehicle);
        // record to log
        tunnel.exitTunnel(vehicle);
        lock.lock();
        // if an ambulance exits
        if (vehicle instanceof Ambulance) {
            try {
                // indicate that the tunnel do not have ambulance
                tunnel.setHasAmbulance(false);
                // update the set
                HashSet<Vehicle> vehicleHashSet = tunnelHashSetMap.get(tunnel);
                if (vehicleHashSet != null) {
                    vehicleHashSet.remove(vehicle);
                    for (Vehicle v : vehicleHashSet) {
                        v.signalWaitForAmbulance();
                    }
                    // update map
                    tunnelHashSetMap.put(tunnel, vehicleHashSet);
                }
                // wake up all threads awaiting for this.cond
                this.cond.signalAll();
            } finally {
                lock.unlock();
            }
        } else {
            try {
                // update the set of vehicles associated with the tunnel
                HashSet<Vehicle> vehicleHashSet = tunnelHashSetMap.get(tunnel);
                if (vehicleHashSet != null) {
                    vehicleHashSet.remove(vehicle);
                    tunnelHashSetMap.put(tunnel, vehicleHashSet);
                }
                // wake up all threads awaiting for this.cond
                this.cond.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}