# Notes about the project

## The team

- Bo Li: bo_licc@hotmail.com
- Siqi Li: liuyiyi147@gmail.com

## Comments
### A naive scheduler to start
### Support for Highly-Available applications
### Balance the load
1.
2. To develop scheduler with `worst fit algorithm` algorithm, we set the metric as MIPS*RAM. When allocating a VM to a host, always choose the host with max{MIPS*RAM}.
3. Penalties reveal SLA violation. Compare the Penalties between NextFit and WorstFit, ...

### Get rid of SLA violations
No SLA violations means no penalties. To allocate a certain VM, we need to consider its RAM and MIPS. To generate no penalties, the host should have bigger RAM, and have at least one CPU unit (Presented by class Pe) that has more available MIPS than VM's need.

### Energy-efficient schedulers


### Results
Test different schedulers running on `all` day, the results are as follow.

	Schedulers	Incomes	Penalties	Energy	Revenue
	Naive	12398.59€	402.16€	2645.63€	9350.80€
	AntiAffinity
	NextFit
	WorstFit	12398.59€	6.06€	3288.96€	9103.58€
	NoViolations	12398.59€	0.00€	2868.74€	9529.85€
	EnergyEfficient
	