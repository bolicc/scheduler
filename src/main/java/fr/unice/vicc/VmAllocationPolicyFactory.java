package fr.unice.vicc;

import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class VmAllocationPolicyFactory {

    /**
     * Return the VMAllocationPolicy associated to id
     * @param id the algorithm identifier
     * @param hosts the host list
     * @return the selected algorithm
     */
	
    VmAllocationPolicy make(String id, List<PowerHost> hosts) {

        switch (id) {
            case "naive":  return new NaiveVmAllocationPolicy(hosts);
            case "antiAffinity":  return new AntiAffinityVmAllocationPolicy(hosts);
//          case "antiAffinity":  return new OptimizedAntiAffinityVmAllocationPolicy(hosts);
            case "nextFit": return new NextFitVmAllocationPolicy(hosts);
            case "worstFit": return new WorstFitVmAllocationPolicy(hosts);
            case "noViolations": return new NoViolationsVmAllocationPolicy(hosts);
            case "energy": return new EnergyVmAllocationPolicy(hosts);
            case "greedy": return new GreedyVmAllocationPolicy(hosts);
        }
        throw new IllegalArgumentException("No such policy '" + id + "'");
    }
}
