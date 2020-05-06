package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Factory;
import cs131.pa2.Abstract.Log.Log;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

import java.util.Collection;

/**
 * The class implementing the Factory interface for creating instances of classes
 *
 * @author cs131a
 */
public class ConcreteFactory implements Factory {

    /**
     * create new basic tunnel
     *
     * @param name the name of the tunnel to create
     * @return the tunnel created
     */
    @Override
    public Tunnel createNewBasicTunnel(String name) {
        return new BasicTunnel(name);
    }

    /**
     * create new vehicle of type 'car'
     *
     * @param name      the name of the car to create
     * @param direction the direction of the car
     * @return new 'car' created
     */
    @Override
    public Vehicle createNewCar(String name, Direction direction) {
        return new Car(name, direction);
    }

    /**
     * create new vehicle of type 'sled'
     *
     * @param name      the name of the sled to create
     * @param direction the direction of the sled
     * @return new 'sled' created
     */
    @Override
    public Vehicle createNewSled(String name, Direction direction) {
        return new Sled(name, direction);
    }

    /**
     * create Priority Scheduler instance
     *
     * @param name    the name of the priority scheduler to create
     * @param tunnels the collection of tunnels that the scheduler should manage
     * @param log     the log for logging the operations
     * @return new PriorityScheduler instance
     */
    @Override
    public Tunnel createNewPriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
        return new PriorityScheduler(name, tunnels, log);
    }

    /**
     * create vehicle of type 'ambulance'
     *
     * @param name      the name of the ambulance to create
     * @param direction the direction of the ambulance
     * @return new 'ambulance' created
     */
    @Override
    public Vehicle createNewAmbulance(String name, Direction direction) {
        return new Ambulance(name, direction);

    }

    /**
     * create Preemptive Priority scheduler instance
     *
     * @param name    the name of the preemptive priority scheduler to create
     * @param tunnels the collection of tunnels that the scheduler should manage
     * @param log     the log for logging the operations
     * @return new PreemptivePriorityScheduler instance
     */
    @Override
    public Tunnel createNewPreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
        return new PreemptivePriorityScheduler(name, tunnels, log);
    }
}
