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
//compile exec:java -Dsched=nextFit -Dday=all

/**
 * Created by fhermeni2 on 16/11/2015.
 */
public class NextFitVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;

    public NextFitVmAllocationPolicy(List<? extends Host> list) {
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
    public static int i = 0; 
    @Override
    public boolean allocateHostForVm(Vm vm) { 
    	if (hoster.containsKey(vm))
    		return true;
    	//int i=0;
    	
    	while(i<(getHostList().size())){//list of hosts
    		//System.out.println("last host"+i);
    		if(i==(getHostList().size()-1)){
        		i=0;
        	}
    		Host h=getHostList().get(i);
    		
    		//System.out.println("i value"+i+"host ID"+h.getId());
    		if (h!=null&&h.vmCreate(vm)){
    			hoster.put(vm, h);
    			//System.out.println("host ID : " + h.getId() + " vm ID : " + vm.getId());
    			//System.out.println("    current search host"+i);
    			Log.formatLine("%.4f: VM #" + vm.getId() + " has been allocated to the host#" + h.getId() + 
    					" datacenter #" + h.getDatacenter().getId() + "(" + h.getDatacenter().getName() + ") #", 
    					CloudSim.clock());
    			return true;
    			
    	   }
    	   		
    		else{
    			i++;
    		}
    	}
    	
    		return false;
    	}
    	
   
	@Override
    public boolean allocateHostForVm(Vm vm, Host host) {
    	if (host!=null&&host.vmCreate(vm)){
    		hoster.put(vm, host);
    		//System.out.println("host ID : " + host.getId() + " vm ID : " + vm.getId());
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