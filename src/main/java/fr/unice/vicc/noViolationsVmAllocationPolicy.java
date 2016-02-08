package fr.unice.vicc;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//compile exec:java -Dsched=noViolations -Dday=all
//mips of host>requst of mips request of vms in this host

/**
 * Created by fhermeni2 on 16/11/2015.
 */
public class noViolationsVmAllocationPolicy extends VmAllocationPolicy {
	/** The map to track the server that host each running VM. */
	private Map<Vm,Host> hoster;

	public noViolationsVmAllocationPolicy(List<? extends Host> list) {
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



	private boolean canAllocateHost (Host host, Vm vm) {
		for (Pe pe : host.getPeList()) {
			if (vm.getMips()<=pe.getPeProvisioner().getAvailableMips()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean allocateHostForVm(Vm vm) { 
		if (hoster.containsKey(vm))
			return true;
		for (Host h: getHostList()){
			if (h.getRamProvisioner().getAvailableRam()>=vm.getRam() &&
					canAllocateHost(h,vm)) {
				return allocateHostForVm(vm, h);
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
