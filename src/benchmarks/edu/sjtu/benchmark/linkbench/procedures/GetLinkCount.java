package edu.sjtu.benchmark.linkbench.procedures;

import org.voltdb.*;

import edu.sjtu.benchmark.linkbench.LinkbenchConstants;

public class GetLinkCount extends VoltProcedure {
	 
	public final SQLStmt getLinkCount = new SQLStmt(
            "SELECT id1, count(*) FROM " + LinkbenchConstants.TABLENAME_LINK +
    		" GROUP BY id1"
        );
    
    public VoltTable[] run() {
    	voltQueueSQL(getLinkCount);
    	return voltExecuteSQL(true);
    }
}