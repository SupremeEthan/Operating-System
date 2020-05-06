package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

/**
 * The preemptive priority scheduler assigns vehicles to tunnels based on their priority and supports ambulances.
 * It extends the Tunnel class.
 * @author cs131a
 *
 */
public class PreemptivePriorityScheduler extends Tunnel{

	/**
	 * Creates a new instance of the class PreemptivePriorityScheduler with given name by calling the constructor of the super class
	 * @param name the name of the preemptive priority scheduler to create
	 */
	public PreemptivePriorityScheduler(String name) {
		super(name);
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		return false;
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		
	}
	
}

