package edu.repetita.core;

import edu.repetita.paths.ExplicitPaths;
import edu.repetita.paths.SRPaths;

public class RoutingConfiguration {
    private SRPaths srpaths;
    private ExplicitPaths explicitPaths;

    public SRPaths getSRPaths() {
        return this.srpaths;
    }

    public void setSRPaths(SRPaths srpaths) {
        this.srpaths = srpaths;
    }

    public void setExplicitPaths(ExplicitPaths explicitPaths) {
        this.explicitPaths = explicitPaths;
    }

    public ExplicitPaths getExplicitPaths() {
        return this.explicitPaths;
    }

    public RoutingConfiguration clone(){
        RoutingConfiguration copy = new RoutingConfiguration();
        copy.setExplicitPaths(this.getExplicitPaths());
        copy.setSRPaths(this.getSRPaths());
        return copy;
    }
}
