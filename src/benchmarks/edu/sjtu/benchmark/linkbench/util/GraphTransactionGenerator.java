package edu.sjtu.benchmark.linkbench.util;

import java.util.Random;

import org.voltdb.VoltTable;
import org.voltdb.client.Client;

import edu.brown.rand.RandomDistribution.FlatHistogram;
import edu.brown.statistics.ObjectHistogram;
import edu.sjtu.benchmark.linkbench.LinkbenchClient.Transaction;
import edu.sjtu.benchmark.linkbench.util.LinkbenchOperation;

public class GraphTransactionGenerator {
	private final FlatHistogram<Long> linkWeights;
	private final long num_nodes;
	private Client client;
	private Random rng = new Random();
	
	public GraphTransactionGenerator(long num_nodes, Client client) {
		this.num_nodes = num_nodes;
		this.client = client;
		try {	
			VoltTable[] result = this.client.callProcedure("GetLinkCount").getResults();

			ObjectHistogram<Long> linkCounts = new ObjectHistogram<Long>(); 
			for(int i = 0; i < result[0].getRowCount(); ++i) {
				long weight = (long) Math.ceil(Math.log(result[0].fetchRow(i).getLong(1))) + 1;
				linkCounts.put(result[0].fetchRow(i).getLong(0), weight);
			}

			this.linkWeights = new FlatHistogram<Long>(this.rng, linkCounts);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}
	public LinkbenchOperation nextTransaction(Transaction txn) {
		long nid = 0;
		switch(txn){
			case GET_NODE:
				nid = linkWeights.nextValue();
				break;
			case GET_LINK:
				nid = linkWeights.nextValue();
				break;
			case GET_NODES_FROM_LINK:
			    nid = linkWeights.nextValue();
			    break;
			default:
				assert(false):"should not come to this point";
		}
		return new LinkbenchOperation((int)nid);
	}
};