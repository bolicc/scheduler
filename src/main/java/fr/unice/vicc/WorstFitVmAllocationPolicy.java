package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * worst fit:seek block with most resources, rule: max(mips*ram): ram stands for memory,mips stand for 
 * speed(potential memory)
 */

public class WorstFitVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;
    private Map<Double,Host> available;
    
    public WorstFitVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster = new HashMap<>();
        available = new HashMap<>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        hoster = new HashMap<>();
        available = new HashMap<>();
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> list) {
        return null;
    }
    
    @Override
    public boolean allocateHostForVm(Vm vm) { 
    	if (hoster.containsKey(vm))
    		return true;
    	for (Host h : getHostList()){
    		double mip = h.getAvailableMips();		
    		int ram = h.getRamProvisioner().getAvailableRam();
    		double metric = mip*ram;
    		available.put(metric,h);
    	}
    	Object[] key = available.keySet().toArray();
		Arrays.sort(key);
    	for (int j=key.length-1;j>=0;j--) {
    		Host host = available.get(key[j]);
    		if(allocateHostForVm(vm, host)) 
    			return true;	
    	}
    	return false;
    }
    	
    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
    	if (host!=null && host.vmCreate(vm)){
    		hoster.put(vm, host);
    		Log.formatLine("%.4f: VM #" + vm.getId() + " has been allocated to the host#" + host.getId() + 
					" datacenter #" + host.getDatacenter().getId() + "(" + host.getDatacenter().getName() + ") #", 
					CloudSim.clock());
    		return true;
    	}
        return false;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
    	Host host=hoster.remove(vm);
    	host.vmDestroy(vm); 
    }

    @Override
    public Host getHost(Vm vm) {
    	return hoster.get(vm);
    }

    @Override
    public Host getHost(int vmId, int userId) {//if id and userid of vm is the same to input
    	Host host = null;
    	for(Map.Entry<Vm, Host> entry : hoster.entrySet()){
    		Vm vm = entry.getKey();
    		if((vm.getUserId()==userId) && (vm.getId()==vmId)){
    	    	 host = hoster.get(vm);
    	    }
    	}
		return host;
    }
}
