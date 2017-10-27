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
        graph=nx.DiGraph()
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

class RandomWeightSolver:

    def __init__(self):
        pass

    def compute_weights(self, graph, demands, outfilename=None):
        new_graph = nx.DiGraph(graph)
        reweighted_edges = dict()           # to ensure that the new weights are symmetrical
        start = time.time()
        for (s,t) in graph.edges():
            if (s,t) in reweighted_edges:
                continue
            new_graph[s][t]['weight']=random.randint(1,100)     # we could do something better by extracting something in the range of the weights initially used in the topology...
            new_graph[t][s]['weight'] = new_graph[s][t]['weight']
            new_graph[s][t]['label']=graph[s][t]['label']
            new_graph[t][s]['label']=graph[t][s]['label']
            reweighted_edges[(s,t)] = 1
            reweighted_edges[(t,s)] = 1
        end = time.time()
        if outfilename!=None:
            sys.stdout=open(outfilename,'w')
        for (s,t) in new_graph.edges():
            print new_graph[s][t]['label'],s,t,new_graph[s][t]['weight']
        #print new_graph.edges(data=True)
        print "execution time: %s" %((end-start)*1000000000)

# command-line usage
def print_help():
    print "python compute_random_paths.py <topology filename> <demand filename> [<output path filename>]\n"

if __name__=="__main__":
    if len(sys.argv) < 3 or len(sys.argv) > 4:
        print_help()
        sys.exit()
    parser=Parser()
    parser.load_repetita_data(sys.argv[1],sys.argv[2])
    solver=RandomWeightSolver()
    outfile=None
    if len(sys.argv) == 4:
        outfile=sys.argv[3]
    solver.compute_weights(parser.get_topology(),parser.get_demands(),outfilename=outfile)

