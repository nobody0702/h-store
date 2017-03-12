package edu.sjtu.benchmark.linkbench.procedures;

import org.voltdb.*;
import edu.sjtu.benchmark.linkbench.LinkbenchConstants;

public class GetNode extends VoltProcedure {
 
    public final SQLStmt GetNode = new SQLStmt("SELECT * FROM " + 
    											LinkbenchConstants.TABLENAME_NODE 
    											+ " WHERE id = ? ");
 
    public VoltTable[] run(long node_id) {
        voltQueueSQL(GetNode, node_id);
        return (voltExecuteSQL());
    }
}
