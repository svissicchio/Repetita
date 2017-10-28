package edu.repetita;

import edu.repetita.utils.MCF;
import org.junit.Test;

public class MCFTest {
    private Warehouse warehouse = new Warehouse();

    @Test
    public void testMCF_expectedLoad_defaultSetting () throws Exception {
        MCF mcfComputer = new MCF(warehouse.getDefaultSetting().getTopology(), warehouse.getDefaultSetting().getDemands());
        double maxUtilization = mcfComputer.computeMaxUtilization();
        if (Math.abs(maxUtilization - 0.9) > 1e-3)
            System.out.printf("%s: Max utilization is %g\n", warehouse.getDefaultTopologyFile(), maxUtilization);
        assert Math.abs(maxUtilization - 0.9) <= 1e-3;
    }
}
