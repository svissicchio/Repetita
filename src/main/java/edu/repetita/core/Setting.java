package edu.repetita.core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.repetita.io.RepetitaParser;
import edu.repetita.paths.ExplicitPaths;
import edu.repetita.paths.SRPaths;

public class Setting {
	private String topologyFilename;
	private Topology topology = null;
	private String demandsFilename;
	private Demands demands = null;
    private List<Demands> demandChanges = new LinkedList<>();
    private int numberReoptimizations = 1;
    private RoutingConfiguration config;

	public Setting(){
        this.config = new RoutingConfiguration();
    }

	public void setTopologyFilename(String topologyFilename) {
		this.topologyFilename = topologyFilename;
		try {
			this.topology = RepetitaParser.parseTopology(this.topologyFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setDemandsFilename(String demandsFilename) {
		this.demandsFilename = demandsFilename;
		try {
			this.demands = RepetitaParser.parseDemands(this.demandsFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getTopologyFilename() {
		return this.topologyFilename;
	}
	
	public Topology getTopology() {
		return this.topology;
	}

    public void setTopology(Topology topology) {
        this.topology = topology;
    }
	
	public String getDemandsFilename() {
		return this.demandsFilename;
	}
	
	public Demands getDemands() { return this.demands; }

    public void setDemands(Demands newDemands) {
        this.demands = newDemands;
    }

    public List<Demands> getDemandChanges() {
        return this.demandChanges;
    }

    public void setDemandChangesFilenames (List<String> demandFilenames){
	    List<Demands> changes = new LinkedList<>();
	    for (String filename: demandFilenames){
            try {
                changes.add(RepetitaParser.parseDemands(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.setDemandChanges(changes);
    }

    public void setDemandChanges(List<Demands> demandChanges) {
        this.demandChanges = demandChanges;
    }

    public int getNumberReoptimizations() {
        return this.numberReoptimizations;
    }

    public void setNumberReoptimizations(int numberReoptimizations) {
        this.numberReoptimizations = numberReoptimizations;
    }

    public RoutingConfiguration getRoutingConfiguration() {
	    return this.config;
    }

    public void setRoutingConfiguration(RoutingConfiguration newConfig) {
	    this.config = newConfig;
    }

    public void setSRPaths(SRPaths paths) {
        this.config.setSRPaths(paths);
    }

    public SRPaths getSRPaths() {
        return this.config.getSRPaths();
    }

    public void setExplicitPaths(ExplicitPaths paths) {
        this.config.setExplicitPaths(paths);
    }

    public ExplicitPaths getExplicitPaths() {
        return this.config.getExplicitPaths();
    }

    public Setting clone(){
        Setting copy = new Setting();
        copy.setTopology(this.topology.clone());
        copy.setDemands(this.demands);
        copy.setDemandChanges(this.demandChanges);
        copy.setNumberReoptimizations(this.numberReoptimizations);
        copy.setRoutingConfiguration(this.getRoutingConfiguration().clone());
        return copy;
    }
}
