package edu.sjtu.benchmark.linkbench;


public abstract class LinkbenchConstants {
	/**table**/
    public static final String TABLENAME_NODE           = "nodetable";
    public static final String TABLENAME_LINK           = "linktable";
    
    /*transaction frequent*/
    public static final int FREQ_GET_NODE = 0;
    public static final int FREQ_GET_LINK = 0;
    public static final int FREQ_GET_NODES_FROM_LINK = 100;
    
    /*limit*/
    public static int LIMIT_LINKS = 5; 
    public static int LIMIT_MULTITOCH = 5; 
};