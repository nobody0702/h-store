package edu.sjtu.benchmark.linkbench.procedures;

import java.util.HashSet;
import java.util.Set;

import org.voltdb.*;

import edu.brown.benchmark.ycsb.distributions.ZipfianGenerator;
import edu.sjtu.benchmark.linkbench.LinkbenchConstants;

public class GetLink extends VoltProcedure {
	
	 public final SQLStmt getLinks = new SQLStmt(
	        "SELECT * FROM " + LinkbenchConstants.TABLENAME_LINK +
	        " WHERE id1 = ?"
	    );
	 public VoltTable[] run(long nid) {
	    	voltQueueSQL(getLinks,nid);
	    	return voltExecuteSQL(true);
	    	/*
	    	final VoltTable result[] = voltExecuteSQL();
	    	assert result.length == 2;
	    	if(Math.min(result[0].getRowCount(), LinkbenchConstants.LIMIT_LINKS) > 0){
	    		long[] id2_nodes = new long[result[0].getRowCount()];
	    		long[] types = new long[result[1].getRowCount()];
	    		
	    		// get the list of users that nodes(id and its linktype) is linking 
	            for (int i = 0; i < result[0].getRowCount(); ++i) {
	                id2_nodes[i] = result[0].fetchRow(i).getLong(0);
	            }
	            for (int i = 0; i < result[0].getRowCount(); ++i) {
	                types[i] = result[0].fetchRow(i).getLong(0);
	            }
	            */
	            /*ZipGenerator links*/
	 /*           ZipfianGenerator r = new ZipfianGenerator(id2_nodes.length);
	            Set<Integer> indices = new HashSet<>();
	            for (int i = 0; i < LinkbenchConstants.LIMIT_LINKS && i < id2_nodes.length; ++i) {
	                Integer index = r.nextInt();
	                while (indices.contains(index)) {
	                    index = (index + 1) % id2_nodes.length;
	                }
	                indices.add(index);
	            }
	            for (Integer index : indices) {
	                voltQueueSQL(getLink,nid , types[index],id2_nodes[index]);
	            }
	            return voltExecuteSQL(true);
	    	}
	    	return null;*/
	    }
	
}