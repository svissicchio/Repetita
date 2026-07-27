package edu.repetita.simulators;

import edu.repetita.core.Demands;
import edu.repetita.core.Topology;
import edu.repetita.ToyTopologies;
import edu.repetita.Warehouse;
import edu.repetita.core.Setting;
import edu.repetita.simulators.specialized.ExplicitPathFlowSimulator;
import edu.repetita.paths.ExplicitPaths;
import org.junit.Test;

import java.util.Collection;

public class ExplicitPathFlowSimulatorTest {
    private Warehouse warehouse = new Warehouse();
    private ExplicitPathFlowSimulator simulator = new ExplicitPathFlowSimulator();

    @Test
    public void computeFlows_zeroUtilization_withNoExplicitPath(){
        Setting setting = warehouse.getDefaultSetting();
        setting.setExplicitPaths(new ExplicitPaths(setting.getTopology()));
        this.simulator.setup(setting);
        Collection<String> simulatedDemands = this.simulator.computeFlows();

        assert simulatedDemands.isEmpty();
        for (int e=0; e<setting.getTopology().nEdges; e++){
            assert this.simulator.getFlow()[e] == 0;
        }
    }

    @Test
    public void computeFlows_expectedLinkUtilization_withKnownTopologyOneDemandOneExplicitPath(){
        Topology square = ToyTopologies.getSquare();
        Demands demands = ToyTopologies.getBottomLinkUnitaryDemandOnSquare();
        ExplicitPaths paths = new ExplicitPaths(square);

        Setting setting = new Setting();
        setting.setTopology(square);
        setting.setDemands(demands);
        setting.setExplicitPaths(paths);
        this.simulator.setup(setting);

        // links (c,a), (a,b) and (b,d) fully loaded with an SR path (c,a,b,d)
        paths.setPath(0,new int[]{square.getEdgeId("ca"), square.getEdgeId("ab"),
                square.getEdgeId("bd")});
        this.simulator.computeFlows();
        double[] flow = this.simulator.getFlow();
        for (int e=0; e<square.nEdges; e++){
            if (e == square.getEdgeId("ca") || e == square.getEdgeId("ab") ||
                    e == square.getEdgeId("bd")) {
                assert flow[e] == 1.0;
            }
            else{
                assert flow[e] == 0.0;
            }
        }
    }

}
