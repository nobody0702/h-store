package edu.sjtu.benchmark.linkbench.util;

import edu.sjtu.benchmark.api.Operation;

/** Immutable class containing information about transactions. */
public final class LinkbenchOperation extends Operation {
	
	public int nid;
	public LinkbenchOperation(int nid) {
		super();
		this.nid = nid;
	}


}