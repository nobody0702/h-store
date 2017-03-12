package edu.sjtu.benchmark.linkbench.procedures;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.voltdb.*;

import edu.brown.benchmark.ycsb.distributions.ZipfianGenerator;
import edu.sjtu.benchmark.linkbench.LinkbenchConstants;

public class GetNodesFromLink extends VoltProcedure {
	 public final SQLStmt getLinks = new SQLStmt(
		        "SELECT id2 FROM " + LinkbenchConstants.TABLENAME_LINK +
		        " WHERE id1 = ?"
		    );
	 public final SQLStmt getNodes = new SQLStmt(
			 "SELECT * FROM " + LinkbenchConstants.TABLENAME_NODE +
			 " WHERE id = ?"
			 );
	 public VoltTable[] run(int nid) {
		 voltQueueSQL(getLinks, nid);
	     final VoltTable result[] = voltExecuteSQL();
	     assert result.length == 1;
	     
	     if(Math.min(result[0].getRowCount(),LinkbenchConstants.LIMIT_MULTITOCH) > 0){
	            long[] linking = new long[result[0].getRowCount()];
	         
	            // get the list of nodes that nid is linking
	            for (int i = 0; i < result[0].getRowCount(); ++i) {
	                linking[i] = result[0].fetchRow(i).getLong(0);
	            }
	            
	            // The chosen set of users will follow a zipfian distribution
	            // without replacement
	            Arrays.sort(linking);
	            ZipfianGenerator r = new ZipfianGenerator(linking.length);
	            Set<Integer> indices = new HashSet<>();
	            for(int i = 0;i < LinkbenchConstants.LIMIT_MULTITOCH && i < linking.length; i++){
	                Integer index = r.nextInt();
	                while (indices.contains(index)) {
	                    index = (index + 1) % linking.length;
	                }
	                indices.add(index);
	            }
	            for (Integer index : indices) {
	                voltQueueSQL(getNodes, linking[index]);
	            }
	            
	            return voltExecuteSQL(true);
	     }
	     
	     return null;
	 }
}