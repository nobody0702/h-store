package edu.sjtu.benchmark.linkbench;
 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import org.apache.log4j.Logger;
import org.voltdb.CatalogContext;
import org.voltdb.catalog.*;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.Client;

import edu.brown.api.BenchmarkComponent;
import edu.brown.api.Loader;
import edu.brown.benchmark.wikipedia.util.TextGenerator;
import edu.brown.catalog.CatalogUtil;
import edu.brown.rand.RandomDistribution.FlatHistogram;
import edu.sjtu.benchmark.linkbench.util.GraphEdge;
import edu.sjtu.benchmark.linkbench.util.LinkbenchGraphLoader;
 
public class LinkbenchLoader extends Loader {
 
    public final static int configCommitCount = 1000;
    private final Random rng = new Random();
    private static final Logger LOG = Logger.getLogger(LinkbenchLoader.class);
    
    private int max_node_id = -1;
    private LinkbenchGraphLoader graph_loader;
    
    private HashSet<Integer> nodeset;
    
    public static void main(String args[]) throws Exception {
        BenchmarkComponent.main(LinkbenchLoader.class, args, true);
       
    }

    public LinkbenchLoader(String[] args) {
        super(args);
        for (String key : m_extraParams.keySet()) {
            String value = m_extraParams.get(key);

            if  (key.equalsIgnoreCase("network_file")) {
            	String filename = String.valueOf(value);
            	try{
            		graph_loader = new LinkbenchGraphLoader(filename);
            	}
            	catch(FileNotFoundException e){
            		throw new RuntimeException(e);
            	}
            }
            else if  (key.equalsIgnoreCase("max_node_id")) {
            	max_node_id = Integer.valueOf(value);
            }
        }
        
        graph_loader.SetMaxNodeId(max_node_id);
        this.nodeset = new HashSet<>();
    }
    protected void loadLinks(Database catalog_db) throws IOException {
    	Table catalog_tbl = catalog_db.getTables().getIgnoreCase(LinkbenchConstants.TABLENAME_LINK);
    	assert(catalog_tbl != null);
    	VoltTable vt = CatalogUtil.getVoltTable(catalog_tbl);
    	int num_cols = catalog_tbl.getColumns().size();
    	
    	int total = 0;
        int batchSize = 0;
    	
    	GraphEdge e = null;
    	while((e = this.graph_loader.readNextEdge()) !=null ){
    		Object row[] = new Object[num_cols];
        	int param = 0;
            String data = TextGenerator.randomStr(rng, rng.nextInt(64) + 10);
        	row[param++] = e.node0;
        	row[param++] = e.node1;
        	row[param++] = VoltType.NULL_BIGINT;
        	row[param++] = VoltType.NULL_TINYINT;
        	row[param++] = data;
        	row[param++] = VoltType.NULL_TIMESTAMP;
        	row[param++] = (int)1;
        	vt.addRow(row);
        	
        	batchSize++;
            total++;
            if ((batchSize % configCommitCount) == 0) {
                this.loadVoltTable(catalog_tbl.getName(), vt);
                vt.clearRowData();
                batchSize = 0;
            }
            
        	nodeset.add(e.node0);
        	nodeset.add(e.node1);
    	}
    	this.graph_loader.close();
    	
    	if (batchSize > 0) {
        	this.loadVoltTable(catalog_tbl.getName(), vt);
            vt.clearRowData();
        }
    	
    }
    
    protected void loadnodes(Database catalog_db) throws IOException {
    	if(nodeset.size() == 0) {
    		throw new RuntimeException("No users provided to loadUsersGraph()");
    	}
    	
    	Table catalog_tbl = catalog_db.getTables().getIgnoreCase(LinkbenchConstants.TABLENAME_NODE);
        assert(catalog_tbl != null);
        VoltTable vt = CatalogUtil.getVoltTable(catalog_tbl);
        int num_cols = catalog_tbl.getColumns().size();
        
        int total = 0;
        int batchSize = 0;
        
        for (Integer node : this.nodeset) {
        	Object row[] = new Object[num_cols];
            int param = 0;
            
            String data = TextGenerator.randomStr(rng, rng.nextInt(64) + 10);
            row[param++] = node.intValue(); // ID
            row[param++] = VoltType.NULL_INTEGER;
            row[param++] = VoltType.NULL_BIGINT;
            row[param++] = VoltType.NULL_TIMESTAMP;
            row[param++] = data;
            vt.addRow(row);
            
            batchSize++;
            total++;
            if ((batchSize % configCommitCount) == 0) {
                this.loadVoltTable(catalog_tbl.getName(), vt);
                vt.clearRowData();
                batchSize = 0;
            }
            
        }
        
        if (batchSize > 0) {
        	this.loadVoltTable(catalog_tbl.getName(), vt);
            vt.clearRowData();
        }
    	
    }
    @Override
    public void load() throws IOException {
    	final CatalogContext catalogContext = this.getCatalogContext();
    	this.loadLinks(catalogContext.database);
    	this.loadnodes(catalogContext.database);
    }
}
