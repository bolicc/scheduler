package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerHost;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//compile exec:java -Dsched=energy -Dday=all

/**
 * Created by fhermeni2 on 16/11/2015.
 */
public class EnergyVmAllocationPolicy extends VmAllocationPolicy {
    /** The map to track the server that host each running VM. */
    private Map<Vm,Host> hoster;
    private Map<Double,Host> pelister;
    public EnergyVmAllocationPolicy(List<? extends Host> list) {
        super(list);
        hoster =new HashMap<>();
        pelister =new HashMap<>();
    }

    @Override
    protected void setHostList(List<? extends Host> hostList) {
        super.setHostList(hostList);
        hoster = new HashMap<>();
        pelister =new HashMap<>();
    }
  
    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> list) {
        return null;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) { 
    	
    	for (Host h: getHostList()){
    		
    		double mip=h.getAvailableMips();
    		//System.out.println("hostid"+h.getId()+"mips"+mip);
    		double metric=0.0;
    		if(mip>=0)
    		{
    			metric=mip;
    		   pelister.put(metric,h);
    			}
    		}
    	/*List<Map.Entry<Double, Host>> infoIds = new ArrayList<Map.Entry<Double, Host>>(pelister.entrySet());
    	Collections.sort(infoIds, new Comparator<Map.Entry<Double, Host>>() {   
    	    public int compare(Map.Entry<Double, Host> o1, Map.Entry<Double, Host> o2) {      
    	        //return (o2.getValue() - o1.getValue()); 
    	        return (o1.getKey()).compareTo(o2.getKey());
    	    }
    	}); */
    	Object[] key=pelister.keySet().toArray();
		Arrays.sort(key);
		System.out.println("Key Length is:" +key.length);
		for (int j=0;j<key.length;j++) {
    		Host host=pelister.get(key[j]);
    		//System.out.println("hostID"+host.getId()+"  mips"+key[j]);
    		
    	}
   
    	for (int j=0;j<key.length;j++) {
    		Host host=pelister.get(key[j]);
    		//System.out.println("hostID"+host.getId()+"  mips"+key[j]);
    		if (host.vmCreate(vm)){   			
    			hoster.put(vm, host);
    			
    			return true;
    	   }
    	}
    		return false;
    	}
   
    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
    	if (host!=null&&host.vmCreate(vm)){
    		hoster.put(vm, host);
    		/*Log.formatLine("%.4f: VM #" + vm.getId() + " has been allocated to the host#" + host.getId() + 
					" datacenter #" + host.getDatacenter().getId() + "(" + host.getDatacenter().getName() + ") #", 
					CloudSim.clock());*/
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
