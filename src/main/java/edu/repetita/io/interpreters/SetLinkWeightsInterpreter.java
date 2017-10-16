package edu.repetita.io.interpreters;

import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.io.ExternalSolverInterpreter;

public class SetLinkWeightsInterpreter extends ExternalSolverInterpreter {
    @Override
    public String name() {
        return "setLinkWeights";
    }

    @Override
    public String getDescription(){
        return "Changes IGP link weights from the output of the run command " +
                "(the key field is the identifier of a link, the value field is " +
                "the new weight to be set on the given link)";
    }

    @Override
    public void elaborateRunCommandOutput(String outputString, Setting setting){
        Topology newTopology = setting.getTopology();
        int edgeField = this.getKeyField();
        int weightField = this.getValueField();

        String[] lines = outputString.split("\n");
        for (String l: lines){
            String[] edgeData = l.split(this.getFieldSeparator());
            if (edgeData.length > weightField && edgeData.length > edgeField) {
                newTopology.setWeight(edgeData[edgeField], Integer.parseInt(edgeData[weightField]));
            }
        }
    }
}
