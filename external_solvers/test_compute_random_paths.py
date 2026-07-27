#!/usr/bin/env python3

import os
from compute_random_paths import Parser, RandomPathSolver

if __name__ == "__main__":
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, ".."))
    topo_path = os.path.join(project_root, "data", "2016TopologyZooUCL_inverseCapacity", "Airtel.graph")
    demand_path = os.path.join(project_root, "data", "2016TopologyZooUCL_inverseCapacity", "Airtel.0000.demands")

    parser = Parser()
    parser.load_repetita_data(topo_path, demand_path)
    solver = RandomPathSolver()
    solver.compute_paths(parser.get_topology(), parser.get_demands())
