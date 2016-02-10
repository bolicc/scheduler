package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizedAntiAffinityVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public OptimizedAntiAffinityVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster = new HashMap<>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        hoster = new HashMap<>();
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
    		if(h.getVmList().size() > 0){ // host already has VMs on it
    			int id_existing = h.getVmList().get(0).getId(); // get the first VM's id
    			int id_vm = vm.getId();
    			if(!areInSameInterval(id_existing,id_vm) && h.isSuitableForVm(vm) && allocateHostForVm(vm,h)){
    				return true;
    			}
    		}
    		else if (h.isSuitableForVm(vm) && allocateHostForVm(vm,h)){
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean areInSameInterval(int id_existing, int id_vm){ 
    	if((int)id_existing/100 == (int)id_vm/100){
    		return true;
    	}
    	else return false;	
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
    	Host host = hoster.remove(vm);
    	host.vmDestroy(vm); 
    }

    @Override
    public Host getHost(Vm vm) {
    	return hoster.get(vm);
    }

    @Override
    public Host getHost(int vmId, int userId) {
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
