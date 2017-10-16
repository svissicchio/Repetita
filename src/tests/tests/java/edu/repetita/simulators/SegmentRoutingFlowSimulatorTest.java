package tests.java.edu.repetita.simulators;

import edu.repetita.core.Demands;
import edu.repetita.core.Setting;
import edu.repetita.core.Topology;
import edu.repetita.paths.SRPaths;
import edu.repetita.simulators.specialized.ECMPFlowSimulator;
import edu.repetita.simulators.specialized.SegmentRoutingFlowSimulator;
import org.junit.Test;
import tests.java.edu.repetita.ToyTopologies;
import tests.java.edu.repetita.Warehouse;


public class SegmentRoutingFlowSimulatorTest {
    private Warehouse warehouse = new Warehouse();
    private SegmentRoutingFlowSimulator simulator = new SegmentRoutingFlowSimulator();

    @Test
    public void computeFlows_linkUtilizationEqualToIgpOnly_withZeroSRPath(){
        Setting setting = this.warehouse.getDefaultSetting();
        setting.setSRPaths(new SRPaths(setting.getDemands(),setting.getTopology().nNodes));

        // compute utilization according to the segment routing simulator
        this.simulator.setup(setting);
        this.simulator.computeFlows();

        // compute utilization according to the ECMP simulator
        ECMPFlowSimulator ecmp = new ECMPFlowSimulator();
        ecmp.setup(setting);
        ecmp.computeFlows();

        // compare the traffic assignments computed by the two simulators on every edge
        for (int e=0; e<setting.getTopology().nEdges; e++){
            assert this.simulator.getFlow()[e] == ecmp.getFlow()[e];
        }
    }

    @Test
    public void computeFlows_expectedLinkUtilization_withKnownTopologyOneDemandOneSRPath(){
        Topology square = ToyTopologies.getSquare();
        square.setWeight("cd",2);
        square.setWeight("dc",2);
        Demands demands = ToyTopologies.getBottomLinkUnitaryDemandOnSquare();
        SRPaths srpaths = new SRPaths(demands,square.nNodes);

        Setting setting = new Setting();
        setting.setTopology(square);
        setting.setDemands(demands);
        setting.setSRPaths(srpaths);
        this.simulator.setup(setting);

        // link (c,d) should be the one and only one which is fully loaded in the absence of SR paths
        this.simulator.computeFlows();
        double[] flow = this.simulator.getFlow();
        for (int e=0; e<square.nEdges; e++){
            if (e == square.getEdgeId("cd")) {
                assert flow[e] == 1.0;
            }
            else{
                assert flow[e] == 0.0;
            }
        }

        // links (c,a), (a,b) and (b,d) should be the only ones loaded with SR path [c,b,d]
        srpaths.setPath(0,new int[]{square.getEdgeSource("cd"),square.getEdgeDest("ab"),
                square.getEdgeDest("cd")});
        this.simulator.computeFlows();
        flow = this.simulator.getFlow();
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

    @Test
    public void computeFlows_expectedLinkUtilization_withKnownTopologyTwoDemandsTwoSRPaths(){
        Topology square = ToyTopologies.getSquare();
        square.setWeight("cd",2);
        square.setWeight("dc",2);
        square.setWeight("ab",2);
        square.setWeight("ba",2);
        Demands demands = ToyTopologies.getBottomAndTopLinkDemandsOnSquare();
        SRPaths srpaths = new SRPaths(demands,square.nNodes);

        Setting setting = new Setting();
        setting.setTopology(square);
        setting.setDemands(demands);
        setting.setSRPaths(srpaths);
        this.simulator.setup(setting);

        // links (c,d) and (a,b) should be fully loaded without SR paths
        this.simulator.computeFlows();
        double[] flow = this.simulator.getFlow();
        for (int e=0; e<square.nEdges; e++){
            if (e == square.getEdgeId("cd") || e == square.getEdgeId("ab")) {
                assert flow[e] == 1.0;
            }
            else{
                assert flow[e] == 0.0;
            }
        }

        // links (c,a), (a,b) and (b,d) should be fully loaded with an SR path [c,b,d]
        srpaths.setPath(0,new int[]{square.getEdgeSource("cd"),square.getEdgeSource("ab"),
                square.getEdgeDest("ab"), square.getEdgeDest("cd")});
        srpaths.setPath(1,new int[]{square.getEdgeSource("ab"),square.getEdgeSource("cd"),
                square.getEdgeDest("cd"), square.getEdgeDest("ab")});
        this.simulator.computeFlows();
        flow = this.simulator.getFlow();
        for (int e=0; e<square.nEdges; e++){
            if (e == square.getEdgeId("ca") || e == square.getEdgeId("ab") ||
                    e == square.getEdgeId("bd" ) || e == square.getEdgeId("ac") ||
                    e == square.getEdgeId("cd") || e == square.getEdgeId("db" )) {
                assert flow[e] == 1.0;
            }
            else{
                assert flow[e] == 0.0;
            }
        }
    }
}
