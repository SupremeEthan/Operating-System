package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

/**
 * The priority scheduler assigns vehicles to tunnels based on their priority
 * It extends the Tunnel class.
 * @author cs131a
 *
 */
public class PriorityScheduler extends Tunnel{

	/**
	 * Creates a new instance of the class PriorityScheduler with given name by calling the constructor of the super class
	 * @param name the name of the priority scheduler to create
	 */
	public PriorityScheduler(String name) {
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
