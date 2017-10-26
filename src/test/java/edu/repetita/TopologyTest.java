package edu.repetita;

import edu.repetita.core.Topology;
import org.junit.Test;

import java.util.Arrays;

public class TopologyTest {
    private Topology square = ToyTopologies.getSquare();

    @Test
    public void testFindSymmetricEdge_ExpectedEdgeFound_withSquareToyTopology(){
        assert square.findSymmetricEdge(2) == 3;
    }

    @Test
    public void testRemoveEdge_ExpectedEdgeRemoved_withSquareToyTopology(){
        String linkLabel = square.getEdgeLabel(2);
        String symmetricLabel = square.getEdgeLabel(square.findSymmetricEdge(2));

        Topology newSquare = square.removeUndirectedEdge(2);

        System.out.println(Arrays.toString(newSquare.edgeLabel));
        assert newSquare.edgeLabel.length == square.edgeLabel.length - 2;
        for(String l: newSquare.edgeLabel){
            assert !l.equals(linkLabel);
            assert !l.equals(symmetricLabel);
        }
    }
}
