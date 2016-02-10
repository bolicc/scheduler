# Notes about the project

## The team

- Bo Li: bo_licc@hotmail.com
- Siqi Li: liuyiyi147@gmail.com

## Comments

### Results

Test different schedulers running on `all` day. The results are as follow.
	
	Schedulers      |  Incomes  | Penalties |  Energy  | Revenue
	----------------|-----------|-----------|----------|----------
	Naive           | 12398.59€ | 402.16€	| 2645.63€ | 9350.80€
	AntiAffinity    | 12398.59€ | 200.95€   | 2688.44€ | 9509.21€
	NextFit         | 12398.59€ | 360.59€   | 2715.80€ | 9322.21€
	WorstFit	    | 12398.59€ | 6.06€	    | 3286.62€ | 9105.92€
	NoViolations    | 12398.59€ | 0.00€	    | 2868.74€ | 9529.85€
	Energy          | 12398.59€ | 1395.49€  | 2604.12€ | 8398.98€
	Greedy          | 12398.59€ | 0.00€     | 2670.55€ | 9728.04€
	
### A naive scheduler to start

When a VM is coming, the algorithm scans each host of hostlist and finds the first host that can allocate enough resources to VM. Then put this VM on this host.

1. `hoster` is a hashmap with VM as key and host as value.
2. `getHost(Vm)`: use the key to get the value of this key.
`getHost(int, int)`: try to find a VM on the hostlist has the same vmId & userId as the input. Then return the host of this VM.
3. `allocateHostForVm(Vm, Host)`: first check if the host successfully allocates PEs and memory to this VM. If so, we can put this VM on this host.
4. `deallocateHostForVm`:remove the relationship between VM and host of this map. The result of `hoster.remove(vm)` returns the host of VM before the relationship is removed. Then destroy corresponding VM.
5. `optimizeAllocation` returns null.
6. `allocateHostForVm(Vm)`: achieve the above algorithm.

### Support for Highly-Available applications

All VMs with an id between [0-99] must be on distinct nodes, the same with VMs having an id between [100-199], [200-299]

* For each host in the hostlist, we check if there are already VMs on it. 
If not, we check directly if this host can allocate resources to the VM and put the VM on its list.
 
If there are already VMs on this host, we need to get its vmlist and compare if the incoming VM has the same id interval as the existing ones with the help of the method `isSame()`. If it's the same, we move to next host and do the same. If it's different from all other VM's id interval in the vmlist, we will allocate this VM on this host.

* Complexity = O(mn), where

m: number of all existing VMs on a host
n: number of all hosts


* Such an antiAffinity algorithm requires huge cluster hosting capacity. For example, for VM id between [0-99], it needs at least 100 hosts to load these VMs, while in Affinity algorithm, fewer hosts will be needed. However, In good aspects, antiAffinity  algorithm enhances fault tolerance.  

### Balance the load

1. Next fit: this algorithm is similar to the naive one. The only difference is that naive algorithm each time will go through the hostlist from the very beginning while next fit algorithm will start from the next host after the host used to allocate last time.

Basically, we define a private static variable i to store current hostId. The trick here is to set i to 0 after we finish going through the whole list so that we can achieve to loop the hostlist. Otherwise, it's only one-time searching through hostlist, which is not correct.

NextFit penalties: 360.59€

Naive penalties: 402.16€

We can observe that NextFit has fewer penalties with regards to the Naive scheduler.

2. To develop scheduler with `worst fit algorithm`, we set the metric as MIPS*RAM, which reveals the resources the host has. When allocating a VM to a host, always choose the host with max{MIPS*RAM}.
 
Here, MIPS stands for the speed of execution and RAM stands for memory. In any case, the more RAM is the better. Meanwhile, if MIPS is large, then it can release more space when handling the same task in the same time. So  max{MIPS*RAM} can stand for max resource of the host.

Basically, we declare another Hashmap<Host,Double> available to store the metric of a host. And then sort it in a descending order by metric. After that, we try to put each VM to host following this order.

3. WorstFit algorithm performs the best in terms of reducing the SLA violation.
WorstFit penalties: 6.06€

NextFit penalties: 360.59€

Penalties reveal SLA violation. It causes SLA violation when the associated VM is requiring more MIPS while the current searching host cannot provide.

In worstFit algorithm, we start from the host with most resources, which decreases the possibilities that current host can't provide sufficient MIPS for incoming VMs. So penalties of this algorithm is relatively less than the nextFit algorithm.

4. Complexity: nextFit(o(n)), worstFit(o(n)), where

n: number of all hosts

### Get rid of SLA violations

No SLA violations means no penalties. To allocate a certain VM, we need to consider its RAM and MIPS. To generate no penalties, the host should have bigger RAM, and have at least one CPU unit (Presented by class Pe) that has more available MIPS than VM's need.

Basically, we check if the MIPS of VM is smaller than available MIPS of single Pe on the Pelist of each host. (Even though the total amount of available MIPS of each host is greater than the VM requests, it still possibly can't fit in one single processor. As VM is suppose to be executed in one single processor instead of shared ones.) Meanwhile, we check if the RAM satisfies the requests. If these two conditions are satisfied, we will try to put the VM on this host.

### Energy-efficient schedulers
We tested 3 different metrics.
- min{MIPS*RAM}
- min{MIPS}

Interestingly, in our case, above two metrics have the same results as follow:
	
	Schedulers      |  Incomes  | Penalties |  Energy  | Revenue
	----------------|-----------|-----------|----------|----------
	Energy          | 12398.59€ | 1413.50€  | 2604.30€ | 8380.79€
  
- min{host_MIPS - vm_MIPS}

`host_MIPS - vm_MIPS` shows the suitability of a host. The third one in our case has slight advantage over above two metrics. Therefore, we choose this metrics in implementation. The implementations of other two metrics are commented in method `allocateHostForVm(Vm vm)` of class `EnergyVmAllocationPolicy`.

	Schedulers      |  Incomes  | Penalties |  Energy  | Revenue
	----------------|-----------|-----------|----------|----------
	Energy          | 12398.59€ | 1395.49€  | 2604.12€ | 8398.98€

### Greedy scheduler
	Revenue  = Incomes - Penalties - Energy
As incomes are fixed, to maximize revenue, penalties and energy costs should be minimized. The importance is to provide a good trade-off between energy savings and penalties for SLA violation.  

Here we use the following metric:
`min{pe_MIPS - vm_MIPS}` 

`pe_MIPS` stands for available MIPS of a CPU unit in a host. `vm_MIPS` stands for MIPS the VM needs. For each host, we use the minimum pe_MIPS - vm_MIPS as a metric of this host. Among all the hosts, select the host with minimum metric that can load the VM. This kind of metric combines the algorithms we use in Energy-efficient scheduler with less energy cost and noViolations scheduler with 0 penalty.


	
	