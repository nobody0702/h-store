package edu.sjtu.benchmark.linkbench;
 
import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.voltdb.client.*;
import edu.brown.api.BenchmarkComponent;
import edu.brown.rand.RandomDistribution.FlatHistogram;
import edu.brown.statistics.ObjectHistogram;
import edu.sjtu.benchmark.linkbench.util.GraphTransactionGenerator;
import edu.sjtu.benchmark.linkbench.util.LinkbenchOperation;
 
public class LinkbenchClient extends BenchmarkComponent {

    private static final Logger LOG = Logger.getLogger(LinkbenchClient.class);
    //private static final int counter[] = new int[100];
 
    
    
    
    private class LinkbenchCallback implements ProcedureCallback {
        private final int idx;
 
        public LinkbenchCallback(int idx) {
            this.idx = idx;
        }
        @Override
        public void clientCallback(ClientResponse clientResponse) {
            // Increment the BenchmarkComponent's internal counter on the
            // number of transactions that have been completed
            incrementTransactionCounter(clientResponse,this.idx);
        }
    } // END CLASS
   
    
    private final FlatHistogram<Transaction> txnWeights;
    
    //callback
    protected final LinkbenchCallback callbacks[];
    
    public static enum Transaction {
    	GET_NODE("Get Node", LinkbenchConstants.FREQ_GET_NODE),
    	GET_LINK("Get Link", LinkbenchConstants.FREQ_GET_LINK),
        GET_NODES_FROM_LINK("Get Nodes From Link", LinkbenchConstants.FREQ_GET_NODES_FROM_LINK);
        
        /**
         * Constructor
         */
        private Transaction(String displayName, int weight) {
            this.displayName = displayName;
            this.callName = displayName.replace(" ", "");
            this.weight = weight;
        }
        
        public final String displayName;
        public final String callName;
        public final int weight; // probability (in terms of percentage) the transaction gets executed
    
    } // TRANSCTION ENUM
    
    private int num_nodes;
    
    private Random rng = new Random();
    
    private GraphTransactionGenerator graph_txn_gen;
    
    public LinkbenchClient(String[] args) {
        super(args);
    	
    	// Initialize the sampling table
        ObjectHistogram<Transaction> txns =  new ObjectHistogram<Transaction>();
        for (Transaction t : Transaction.values()) {
            Integer weight = this.getTransactionWeight(t.callName);
            if (weight == null) weight = t.weight;
            txns.put(t, weight);
        } // FOR
        
        assert(txns.getSampleCount() == 100) : txns;
        this.txnWeights = new FlatHistogram<Transaction>(this.rng, txns);
        
        int num_txns = Transaction.values().length;
        this.callbacks = new LinkbenchCallback[num_txns];
        for (int i = 0; i < num_txns; i++) {
            this.callbacks[i] = new LinkbenchCallback(i);
        } // FOR
        
        for (String key : m_extraParams.keySet()) {
            // TODO: Retrieve extra configuration parameters
        	String value = m_extraParams.get(key);
            if (key.equalsIgnoreCase("max_node_id")){
            	this.num_nodes = Integer.valueOf(value);
            }
        } // FOR
        
        this.graph_txn_gen = new GraphTransactionGenerator(this.num_nodes,
    			this.getClientHandle());
    }
    
    public static void main(String args[]) {
        BenchmarkComponent.main(LinkbenchClient.class, args, false);
    }
    
    @Override
    public void runLoop() {
    	 Client client = this.getClientHandle();
         try {
             while (true) {
                 // Figure out what page they're going to update
                 this.runOnce();
                 client.backpressureBarrier();
             } // WHILE
         } catch (Exception ex) {
             ex.printStackTrace();
         }
    }
 
    @Override
    public boolean runOnce() throws IOException {
    	 Transaction target = this.selectTransaction();
         this.startComputeTime(target.displayName);
         Object params[] = null;
         try {
             params = this.generateParams(target);
         } catch (Throwable ex) {
             throw new RuntimeException("Unexpected error when generating params for " + target, ex);
         } finally {
             this.stopComputeTime(target.displayName);
         }
         assert(params != null);
         boolean ret = this.getClientHandle().callProcedure(this.callbacks[target.ordinal()],
                                                            target.callName,
                                                            params);

         //if (debug.val) LOG.debug("Executing txn:" + target.callName + ",with params:" + params);
         return ret;
    } 

    
    private Transaction selectTransaction() {
        return this.txnWeights.nextValue();
    }
    
    @Override
    public String[] getTransactionDisplayNames() {
        // Return an array of transaction names
    	// Return an array of transaction names
        String procNames[] = new String[Transaction.values().length];
        for (int i = 0; i < procNames.length; i++) {
            procNames[i] = Transaction.values()[i].displayName;
        }
        return (procNames);
    }
    
    protected Object[] generateParams(Transaction txn) {
    	int nid;
    	LinkbenchOperation t = graph_txn_gen.nextTransaction(txn);
    	nid = t.nid;
    	
    	Object params[] = null;
    	
    	switch (txn) {
    	case GET_NODE:
    		params = new Object[]{
        			nid
        	};
    		break;
    	case GET_LINK:
        	params = new Object[]{
        			nid
            };
            break;
    	case GET_NODES_FROM_LINK:
    		params = new Object[]{
    				nid	
    		};
    		break;
    	default:
    		assert(false): "should not come to this point";
    	}
    	assert(params != null);
    	
    	return params;
    	
    	
    }
}
