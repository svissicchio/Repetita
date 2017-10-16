package edu.repetita.io.interpreters;

import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.io.ExternalSolverInterpreter;
import edu.repetita.paths.ExplicitPaths;

public class SetExplicitPathsInterpreter extends ExternalSolverInterpreter {

    @Override
    public String name() {
        return "setExplicitPaths";
    }

    @Override
    public String getDescription(){
        return "Sets explicit paths (e.g., tunnels) from the output of the run command " +
                "(the key field is the identifier of a demand, the value field is " +
                "the explicit path to be set for the given demand)";
    }

    @Override
    public void elaborateRunCommandOutput(String outputString, Setting setting){
        Demands demands = setting.getDemands();
        Topology topology = setting.getTopology();
        ExplicitPaths paths = new ExplicitPaths(setting.getTopology());

        int demandField = this.getKeyField();
        int pathField = this.getValueField();

        String[] lines = outputString.split("\n");
        for (String l: lines){
            String[] data = l.split(this.getFieldSeparator());
            if (data.length > demandField && data.length > pathField) {
                String[] edgesList = data[pathField].split("-");
                int[] edges = new int[edgesList.length];
                for (int i = 0; i < edgesList.length; i++){
                    edges[i] = topology.getEdgeId(edgesList[i]);
                }
                int demandIndex = demands.getDemandIndex(data[demandField]);
                paths.setPath(demandIndex,edges);
            }
        }
        setting.setExplicitPaths(paths);
    }
}
