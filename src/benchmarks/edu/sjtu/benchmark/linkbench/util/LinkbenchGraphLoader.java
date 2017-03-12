package edu.sjtu.benchmark.linkbench.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class LinkbenchGraphLoader{
	String filename;
	BufferedReader br = null;
	
	int max_node_id = Integer.MAX_VALUE;
	public LinkbenchGraphLoader(String filename) throws FileNotFoundException {
		this.filename = filename;
		if(filename==null || filename.isEmpty())
			throw new FileNotFoundException("You must specify a filename to instantiate the TwitterGraphLoader... (probably missing in your workload configuration?)");
		
		File file = new File(filename);
		FileReader fr = new FileReader(file);
		br = new BufferedReader(fr);
	}
	public void SetMaxNodeId(int max_node_id) {
		this.max_node_id = max_node_id;
	}
	public GraphEdge readNextEdge() throws IOException {
		int node0 = Integer.MAX_VALUE;
		int node1 = Integer.MAX_VALUE;
		while(node1 > max_node_id) {
			String line = br.readLine();
			if (line == null) return null;
			String[] sa = line.split("\\s+");
			node0 = Integer.parseInt(sa[0]);
			node1 = Integer.parseInt(sa[1]);
			//System.out.println(node0+" "+node1);
		}
		

		if(node0 > max_node_id) {
			return null;
		}
		
		return new GraphEdge(node0,node1);
	}
	
	public void close() throws IOException {
		br.close();
	}

};