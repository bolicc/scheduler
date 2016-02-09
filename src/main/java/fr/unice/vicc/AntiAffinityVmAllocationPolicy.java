package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// need to be optimized???

/** 
 * All Vms with an id between [0-99] must be on distinct nodes, 
 * the same with Vms having an id between [100-199], [200-299], ... .
 */
public class AntiAffinityVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public AntiAffinityVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster = new LinkedHashMap<>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        hoster = new LinkedHashMap<>();//order matters
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> list) {
        return null;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) { 
    	if (hoster.containsKey(vm))
    		return true;
    	boolean result = false;
    	for (Host h: getHostList()){//list of host
    		if ((h.getVmList().size()>0)){// a specific host, get list of vm
    			int n = 0;		
    			for(Vm tempVm : h.getVmList()){//get each vm of this list		
    				result = isSame(vm,tempVm);		  
    				if(!result){
    					n = n+1;				
    				}
    			}
    			//System.out.println("n : " + n + " hostlist size : " + (h.getVmList().size()));
    			if(n==h.getVmList().size() && h.vmCreate(vm)){		
    				hoster.put(vm, h);
					//System.out.println("host ID : " + h.getId() + " vm ID : " + vm.getId());
    				Log.formatLine("%.4f: VM #" + vm.getId() + " has been allocated to the host#" + h.getId() + 
    						" datacenter #" + h.getDatacenter().getId() + "(" + h.getDatacenter().getName() + ") #", 
    						CloudSim.clock());
					return true;
    			}
    			
    		}
    		else if(h.vmCreate(vm)){//there is no vm on the host in the beginning, this is the first vm
    			hoster.put(vm, h); 			
    			//System.out.println("host ID : " + h.getId() + " vm ID : " + vm.getId());
    			Log.formatLine("%.4f: VM #" + vm.getId() + " has been allocated to the host#" + h.getId() + 
    					" datacenter #" + h.getDatacenter().getId() + "(" + h.getDatacenter().getName() + ") #", 
    					CloudSim.clock());
    			return true;
    		}
    	}
    	return false;
    }
    	
    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
    	if (host!=null && host.vmCreate(vm)){
    		hoster.put(vm, host);
    		//System.out.println("host ID : " + host.getId() + " vm ID : " + vm.getId());
    		Log.formatLine("%.4f: VM #" + vm.getId() + " has been allocated to the host#" + host.getId() + 
					" datacenter #" + host.getDatacenter().getId() + "(" + host.getDatacenter().getName() + ") #", 
					CloudSim.clock());
    		return true;
    	}
        return false;
    }
    
    public int vmSort(int vmId){
    	int sort = vmId/100;
    	sort = sort*100;
    	return sort;	
    }
    
	public boolean isSame(Vm vm1, Vm vm2){	
		if(vmSort(vm1.getId()) == vmSort(vm2.getId()))
			return true;
		else 
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