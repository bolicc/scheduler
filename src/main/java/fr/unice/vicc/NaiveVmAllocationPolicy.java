package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by fhermeni2 on 16/11/2015.
 */
public class NaiveVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public NaiveVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster =new HashMap<>();
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
    	for (Host h: getHostList()){
    		if (h.vmCreate(vm)){
    			hoster.put(vm, h);
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
    	if (host!=null&&host.vmCreate(vm)){
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
    	Host host=null;
    	for(Map.Entry<Vm, Host> entry: hoster.entrySet()){
    		Vm vm=entry.getKey();
    	if((vm.getUserId()==userId)&&(vm.getId()==vmId)){
    	    	 host= hoster.get(vm);
    	    	}
    	     }
		return host;
    }
}
