Framework for repeatable experiments in Traffic Engineering.

Features:
- dataset with most instances from the Topology Zoo
- a collection of traffic engineering algorithms and analyses of their results
- libraries to simulate traffic distribution induced by ECMP, static (MPLS tunnels or OpenFlow rules) and Segment Routing paths, compute Multicommodity Flow solutions, and much more!

Typical usage: repetita -graph topology_file -demands demands_filename -demandchanges list_demands_filename -solver algorithm_id -scenario scenario_id -t max_execution_time -outpaths path_filename -out output_filename -verbose debugging_level

Supported traffic engineering algorithms (solvers)
- SRLS                      A Segment Routing path optimizer approximating the Local Search algorithm described in "Gay et al., Expect the Unexpected: Sub-Second Optimization for Segment Routing. In INFOCOM, 2017."(full version at https://github.com/rhartert/defo-ls)
- defoCP                    A Segment Routing path optimizer implementing the Constraint Programming algorithm described in "Hartert et al., A Declarative and Expressive Approach to Control Forwarding Paths in Carrier-Grade Networks. In SIGCOMM, 2015."
- MIPTwoSRNoSplit           A Segment Routing path optimizer inspired by "Bhatia et al., Optimized network traffic engineering using segment routing. In INFOCOM, 2015." (it uses very similar Linear Programs but does not allow arbitrary split ratios)
- TabuIGPWO                 An IGP weight optimizer inspired by "B. Fortz and M. Thorup. Internet traffic engineering byoptimizing OSPF weights. In INFOCOM, 2000."
- ExternalSolvers           Any algorithm described in external_solvers/solvers-specs.txt

Scenarios to evaluate the solutions computed by the above algorithms
- SingleLinkFailureReoptimization      The solver is called to re-optimize the forwarding after every link failure in a randomly generated series
- SingleLinkFailureRobustness          Runs a solver on a topology, stores the routing configuration computed by the solver, and evaluates how single link failures affect the computed configuration
- SingleSolverRun                      Runs the given solver on an input setting
- DemandChangeReoptimization           Change demands and ask the configured solver to re-optimize

Performed analyses:
- maximum link utilization, optionally including comparison with the theoretical lower bound (MCF solution)
- number of demands with configured segment routing paths
- number of demands with configured explicit paths
- number of modified segment routing paths between two configurations
- number of modified explicit paths between two configurations

