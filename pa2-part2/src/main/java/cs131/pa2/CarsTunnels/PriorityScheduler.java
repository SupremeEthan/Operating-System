package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Log.Log;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * The priority scheduler assigns vehicles to tunnels based on their priority
 * It extends the Tunnel class.
 *
 * @author cs131a
 */
public class PriorityScheduler extends Tunnel {

	/**
	 * The collection of all tunnels
	 */
    private Collection<Tunnel> tunnels;
    
    /**
     * The priority queue of all vehicles
     */
    private PriorityQueue<Vehicle> pq;
    
    /**
     * lock that locks shared resources
     */
    private final ReentrantLock lock;
    
    /**
     * Map between vehicle and tunnel
     */
    private Map<Vehicle, Tunnel> vehicleTunnelMap;
    
    /**
     * Condition variable for the lock
     */
    private final Condition cond;

    /**
     * Creates a new instance of the class PriorityScheduler with given name by calling the constructor of the super class
     *
     * @param name    the name of the priority scheduler to create
     * @param tunnels tunnels
     * @param log     log
     */
    public PriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
        super(name, log);
        this.tunnels = tunnels;
        Comparator<Vehicle> cmp = (o1, o2) -> o2.getPriority() - o1.getPriority();
        this.pq = new PriorityQueue<>(cmp);
        this.lock = new ReentrantLock();
        this.vehicleTunnelMap = new HashMap<>();
        this.cond = this.lock.newCondition();
    }

    /**
     * Override tryToEnterInner method. Achieve synchronization using locks and condition variable
     * The vehicle can enter the tunnel if and only if it has the highest priority in the priority
     * queue and the meets the entry condition of one tunnel.
     *
     * @param vehicle The vehicle that is attempting to enter
     * @return true if the vehicle successfully enter the tunnel
     */
    @Override
    public boolean tryToEnterInner(Vehicle vehicle) {
        while (true) {
            lock.lock();
            try {
                // if pq does not have the vehicle, push current vehicle into the pq
                if (!pq.contains(vehicle)) {
                    pq.offer(vehicle);
                }
                // entry condition
                if (pq.element().getPriority() <= vehicle.getPriority()) {
                    for (Tunnel tunnel : tunnels) {
                        // if success
                        if (tunnel.tryToEnter(vehicle)) {
                            pq.poll();
                            // record which tunnel it enters
                            vehicleTunnelMap.put(vehicle, tunnel);
                            return true;
                        }
                    }
                }
                // if not success, release lock and relinquish the CPU
                this.cond.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * let the vehicle exit the tunnel. Signal all awaiting threads to try to enter
     * the tunnel.
     *
     * @param vehicle The vehicle that is exiting the tunnel
     */
    @Override
    public void exitTunnelInner(Vehicle vehicle) {
        Tunnel tunnel = vehicleTunnelMap.get(vehicle);
        tunnel.exitTunnel(vehicle);
        lock.lock();
        try {
            this.cond.signalAll();
        } finally {
            lock.unlock();
        }
    }
}