REPETITA is a framework aimed at easing repeatable experiments on Traffic Engineering algorithms.
Our technical report provides more information about the perspective and ideas behind this framework, see https://arxiv.org/abs/1710.08665
Extended documentation is reported on the GitHub Wiki https://github.com/svissicchio/Repetita/wiki


# EXPERIMENT AND COMPARE WITH THE STATE OF THE ART

REPETITA currently features:
- a dataset with more than 250 real network topologies, from the Topology Zoo, Rocketfuel and DEFO projects, enhanced with synthetic traffic matrices
- a collection of traffic engineering algorithms, based on IGP weight optimization and Segment Routing
- automatic support to run and analyze experiments
- traffic engineering libraries, including ready-to-use code to compute Multi-Commodity Flow solutions, and to simulate traffic distribution induced by (i) equal-cost multipath routing, (ii) explicit paths as those installed by MPLS tunnels or OpenFlow, and (iii) Segment Routing paths.

For example, you can check effectiveness (e.g., link utilization), time efficiency, and control-plane overhead (number of performed changes) of your new traffic engineering algorithm after any single-link failure with a single CLI command: use a similar command to compare with existing algorithms!


# HOW DOES IT WORK?

Once installed, you can run experiments by simply typing commands on the CLI.

Typical usage: repetita -graph topology_file -demands demands_filename -solver algorithm_id -scenario scenario_id -t max_execution_time

Supported traffic engineering algorithms (solvers)
- SRLS                      A Segment Routing path optimizer approximating the Local Search algorithm described in "Gay et al., Expect the Unexpected: Sub-Second Optimization for Segment Routing. In INFOCOM, 2017."(full version at https://github.com/rhartert/defo-ls)
- defoCP                    A Segment Routing path optimizer implementing the Constraint Programming algorithm described in "Hartert et al., A Declarative and Expressive Approach to Control Forwarding Paths in Carrier-Grade Networks. In SIGCOMM, 2015."
- MIPTwoSRNoSplit           A Segment Routing path optimizer inspired by "Bhatia et al., Optimized network traffic engineering using segment routing. In INFOCOM, 2015." (it uses very similar Linear Programs but does not allow arbitrary split ratios)
- TabuIGPWO                 An IGP weight optimizer inspired by "B. Fortz and M. Thorup. Internet traffic engineering byoptimizing OSPF weights. In INFOCOM, 2000."
- CG4SR                     A Segment Routing path optimizer implementing a Column Generation algorithm described in "Mathieu Jadin, Francois Aubry, Pierre Schaus, and Olivier Bonaventure. CG4SR: Near Optimal Traffic Engineering for Segment Routing with Column Generation. In INFOCOM, 2019."
- ExternalSolvers           Any algorithm described in external_solvers/solvers-specs.txt

Scenarios to evaluate the solutions computed by the above algorithms
- SingleLinkFailureReoptimization      The solver is called to re-optimize the forwarding after every link failure in a randomly generated series
- SingleLinkFailureRobustness          Runs a solver on a topology, stores the routing configuration computed by the solver, and evaluates how single link failures affect the computed configuration
- SingleSolverRun                      Runs the given solver on an input setting
- DemandChangeReoptimization           Change demands and ask the configured solver to re-optimize

Performed analyses:
- maximum link utilization (possibly, including comparison with theoretical lower bound (MCF solution)
- number of demands with configured segment routing paths
- number of demands with configured explicit paths
- number of modified segment routing paths between two configurations
- number of modified explicit paths between two configurations


# JOIN THE PROJECT!

We are well aware that the current version of the framework has several limitations, from the number of supported algorithms to the dataset.
We have done our best to make the code as easy to extend as possible, in the hope to overcome current limitations over time. 
We would warmly welcome contributions to REPETITA: Any improvement can be a small step towards more fair and rigorous approach to research in traffic engineering!
