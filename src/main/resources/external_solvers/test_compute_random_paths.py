#!/opt/local/bin/python

from compute_random_paths import *

if __name__=="__main__":
    parser=Parser()
    parser.load_repetita_data("/Users/ste/OngoingWork/mercurial/repetita/code/data/2016TopologyZooUCL_inverseCapacity/Airtel.graph","/Users/ste/OngoingWork/mercurial/repetita/code/data/2016TopologyZooUCL_inverseCapacity/Airtel.0000.demands")
    solver=RandomPathSolver()
    solver.compute_paths(parser.get_topology(),parser.get_demands())

