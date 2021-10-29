package PlotTools;

import java.io.File;
import java.io.FileWriter;

public class GraphVizSimple {

	static final String undirectEdgeOp = "--";
	
	//gvs contains the content of the dot file
	private StringBuilder gvs;

	private String figName;
	
	private String edgeOp;

	/**
	 * Constructor: creates a new GraphViz object that will contain
	 * a graph.
	 */

	public GraphVizSimple(String name) {
		figName = name;
		gvs = new StringBuilder();
		edgeOp = undirectEdgeOp;
	}

	/**
	 * Returns the graph's source description in dot language.
	 * @return Source of the graph in dot language.
	 */
	public String getDotSource() {
		return gvs.toString();
	}

	/**
	 * Adds a string to the graph's source (without newline).
	 */
	public void add(String line) {
		gvs.append(line);
	}

	/**
	 * Adds a string to the graph's source (with newline).
	 */
	public void addln(String line) {
		gvs.append(line + "\n");
	}

	/**
	 * Adds a newline to the graph's source.
	 */
	public void addln() {
		gvs.append('\n');
	}
	
	// get a string nA--nB representing an edge in the dot file
	private String constructEdge(String nA, String nB)
	{
		return nA + edgeOp + nB;
	}
	
	//add an edge to the current graph
	public void addEdge(String nA, String nB)
	{
		String e = constructEdge(nA, nB);
		addln(e + ";");
	}

	/*
	 * write StringBuilder graph to a .dot file
	 */
	public File writeDotSourceToFile( ) 
	{
		File dotFile;
		try {
			dotFile = new File(figName + ".dot");
			FileWriter fout = new FileWriter(dotFile);
			fout.write(gvs.toString());
			fout.close();
		}
		catch (Exception e) {
			System.err.println("Error: I/O error while writing the dot source to temp file!");
			return null;
		}
		return dotFile;
	}


	/**
	 * Returns a string that is used to start a graph.
	 * @return A string to open a graph.
	 */
	public void start_graph() {
		addln("graph " + figName + " {");
	}

	/**
	 * Returns a string that is used to end a graph.
	 * @return A string to close a graph.
	 */
	public void end_graph() {
		addln("}");
	}


}
