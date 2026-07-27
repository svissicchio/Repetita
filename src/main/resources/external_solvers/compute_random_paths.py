#!/opt/local/bin/python

import sys, random, time
import networkx as nx

class Parser:
    # constructor
    def __init__(self):
        self.topology = None
        self.demands = None

    # interface methods
    def get_topology(self):
        return self.topology

    def get_demands(self):
        return self.demands

    def load_repetita_data(self, topo_file, demand_file):
        self.topology = self._parse_repetita_topology(topo_file)
        self.demands = self._parse_repetita_demands(demand_file)

    # helper methods
    def _read_file(self,filename):
        f=open(filename, 'r')
        data=f.read()
        f.close()
        return data

    def _parse_repetita_topology(self, topo_file):
        graph=nx.Graph()
        data=self._read_file(topo_file)

        edges_data=data.split('label src dest weight bw delay\n')[1]
        for line in edges_data.split("\n"):
            if len(line)==0:
                continue
            (link_id,node1,node2,weight,bw,delay) = line.split()
            graph.add_edge(node1,node2,weight=weight,label=link_id)

        return graph

    def _parse_repetita_demands(self, demand_file):
        demand_list=[]
        data=self._read_file(demand_file)
        
        demands_data=data.split("label src dest bw\n")[1]
        for line in demands_data.split("\n"):
            if len(line)==0:
                continue
            demand_list.append(line.split())
        
        return demand_list

class RandomPathSolver:

    def __init__(self):
        pass

    def compute_paths(self, graph, demands, outfilename=None):
        new_paths=dict()
        start = time.time()
        for (id_demand,source,dest,volume) in demands:
            new_paths[id_demand]=self._compute_path(graph, source, dest)
        end = time.time()
        
        # output path information
        if outfilename!=None:
            sys.stdout=open(outfilename,'w')
        for demand in new_paths.keys():
            path_list = new_paths[demand]
            edge_string = ""
            for i in range(len(path_list)-1):
                edge_string += graph[path_list[i]][path_list[i+1]]['label']+"-"
            edge_string = edge_string[:-1]
            print "%s; %s; %s" %(demand,path_list,edge_string) 
        print "execution time (in ns): %s" %((end-start)*1000000000)

    def _compute_path(self, graph, source, dest):
        paths=list(nx.all_simple_paths(graph, source, dest))
        return random.choice(paths)


# command-line usage
def print_help():
    print "python compute_random_paths.py <topology filename> <demand filename> [<output path filename>]\n"

if __name__=="__main__":
    if len(sys.argv) < 3 or len(sys.argv) > 4:
        print_help()
        sys.exit()
    parser=Parser()
    parser.load_repetita_data(sys.argv[1],sys.argv[2])
    solver=RandomPathSolver()
    outfile=None
    if len(sys.argv) == 4:
        outfile=sys.argv[3]
    solver.compute_paths(parser.get_topology(),parser.get_demands(),outfilename=outfile)

