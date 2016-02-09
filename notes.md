# Notes about the project

## The team

- Bo Li: bo_licc@hotmail.com
- Siqi Li: liuyiyi147@gmail.com

## Comments
### A naive scheduler to start

When a VM is coming, the algorithm scans each host of hostlist and finds the first host that can allocate enough resources to VM. Then put this VM on this host.

1. `hoster` is a hashmap with VM as key and host as value.
2. `getHost(Vm)`: use the key to get the value of this key.
`getHost(int, int)`: try to find a vm on the hostlist has the same vmId & userId as the input. Then return the host of this vm.
3. `allocateHostForVm(Vm, Host)`: first check if the host successfully allocates PEs and memory to this VM. If so, we can put this vm on this host.
4. `deallocateHostForVm`:remove the relationship between vm and host of this map. The result of `hoster.remove(vm)` returns the host of vm before the relationship is removed. Then destroy corresponding vm.
5. `optimizeAllocation` returns null.
6. `allocateHostForVm(Vm)`: achieve the above algorithm.

### Support for Highly-Available applications

All Vms with an id between [0-99] must be on distinct nodes, the same with Vms having an id between [100-199], [200-299]

1. In this algorithm, we implement two new methods, vmSort(int) and isSame.

Function vmSort(int): distinguish Vm from each other whose id is among[0-99], we use first. It can make VmId from [0-99]=>0,[100-199]=>100, etc.
Function isSame: check whether two vms share the same id. 

For each host in the hostlist, we check if there are already Vms on the host. If not, we directly check if this host successfully allocate resources to the vm and decide to put the vm on the list or not.
If yes, we need to get the vmlist on this host and use the new created functions to compare if the incoming vm has the same id as the exsiting ones. If it's the same, we move to next host and do the same. If it's different from all VmIds of the vmlist, we will put this vm on this host.

2.complexity=o(n2)

3.It requires huge cluster hosting capacity. For example, for vmid between [0-99],  in antiAffinity algorithm, it will need at least 100 hosts to host these vms.However, in Affinity algorithm, it needs way fewer hosts. In good aspects, this algorithm enhances falut tolerance.  

### Balance the load
1. Next fit: this algorithm is similar to the naive one. The only difference is that naive algorithm each time will go through the hostlist from the very beginning while next fit algorithm will start from the next host after the host used to allocate last time.

Basically, we define a local variable i to store current hostId. Most importanly, we have to set i to 0 after we finish going through the whole list so that we can achive to loop the hostlist. Otherwise, it's only one-time searching through hostlist, which is not correct.

nextFit penalties:360.59Ä
naive penalties:402.16Ä

2. To develop scheduler with `worst fit algorithm` algorithm, we set the metric as MIPS*RAM. When allocating a VM to a host, always choose the host with max{MIPS*RAM}.

Worst fit: allocate vms to the host who has the most resources.

In oder to measure resource of each host, we desgin a metric which is equal to metric=MIPS*RAM. As you know, MIPS stands for the speed of execution and Ram stands for memory. In any case, the more Ram is better. As well, if MIPS is large, then it can release more space when hadling the same task(time). So  max{MIPS*RAM} can stand for max resource of the host.

Basically, we define another hashmap<host,metric> and sort it in a descending order by metric. After that, we try to put each vm in each host following this order.

3. Penalties reveal SLA violation. Compare the Penalties between NextFit (penalties:360.59Ä)and WorstFit(penalties:6.06Ä).It causes SLA violation when the associated Vm is requiring more MIPS while the current searching host can't provide. It will also increase the waiting time of customers which increases penalities.   

In worstFit algorithm, we start from the host with most resources, which decreases the possibilities that current host can't provide sufficient MIPS for incoming vms. So penalities of this algorithm is realtively less than the nextFit algorithm.

4.Complexity: nextFit(o(n)),worstFit(o(n2))

### Get rid of SLA violations
No SLA violations means no penalties. To allocate a certain VM, we need to consider its RAM and MIPS. To generate no penalties, the host should have bigger RAM, and have at least one CPU unit (Presented by class Pe) that has more available MIPS than VM's need.

Basically, we check if the MIPS of vm is smaller than avaible MIPS of single Pe on the Pelist of each host.(Even though the total amount of available MIPS of each host is greater than the vm requests, it stil possibly can't fit in one single processor. As vm is suppose to be excuted in one single processor instead of sharded ones.)Meanwhile, we check if the Ram satisfies the requests. If these two conditions are satisfied, we will try to put the vm on this host.

### Energy-efficient schedulers
We tested 3 different metrics.
- min{MIPS*RAM}
- min{MIPS}
Interestingly, in our case, above two metrics have the same results as follow:
	
	Schedulers      |  Incomes  | Penalties |  Energy  | Revenue
	----------------|-----------|-----------|----------|----------
	Energy          | 12398.59€ | 1413.50€  | 2604.30€ | 8380.79€
  
- min{host_MIPS - vm_MIPS}
The third one in our case has slight advantage over above two metrics. Therefore, we choose this metrics in implementation. The implementations of other two metrics are in comments in method `allocateHostForVm(Vm vm)` of class `EnergyVmAllocationPolicy`.

	Schedulers      |  Incomes  | Penalties |  Energy  | Revenue
	----------------|-----------|-----------|----------|----------
	Energy          | 12398.59€ | 1395.49€  | 2604.12€ | 8398.98€

### Greedy scheduler
	Revenue  = Incomes - Penalties - Energy
As incomes are fixed, to maximize revenue, penalties and energy costs should be minimized. The importance is to provide a good trade-off between energy savings and penalties for SLA violation.  

Here we use `min{pe_MIPS - vm_MIPS}` as metric. `pe_MIPS` stands for available MIPS of a CPU unit in a host. `vm_MIPS` stands for MIPS the VM needs. For each host, we use the minimum pe_MIPS - vm_MIPS as a metric of this host. Among all the hosts, select the host with minimum metric that can load the VM. This kind of metric combines the algorithms we use in Energy-efficient scheduler with less energy cost and noViolations scheduler with 0 penalty.

### Results
Test different schedulers running on `all` day, the results are as follow.
	
	Schedulers      |  Incomes  | Penalties |  Energy  | Revenue
	----------------|-----------|-----------|----------|----------
	Naive           | 12398.59€ | 402.16€	| 2645.63€ | 9350.80€
	AntiAffinity    |           |           |          |
	NextFit         |           |           |          |
	WorstFit	    | 12398.59€ | 6.06€	    | 3286.62€ | 9105.92€
	NoViolations    | 12398.59€ | 0.00€	    | 2868.74€ | 9529.85€
	Energy          | 12398.59€ | 1395.49€  | 2604.12€ | 8398.98€
	Greedy          | 12398.59€ | 0.00€     | 2670.55€ | 9728.04€
	
	