package PlotTools;
// GraphViz.java - a simple API to call dot from Java programs

/*$Id$*/
/*
 ******************************************************************************
 *                                                                            *
 *              (c) Copyright 2003 Laszlo Szathmary                           *
 *                                                                            *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms of the GNU Lesser General Public License as published by   *
 * the Free Software Foundation; either version 2.1 of the License, or        *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY *
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public    *
 * License for more details.                                                  *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public License   *
 * along with this program; if not, write to the Free Software Foundation,    *
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.                              *
 *                                                                            *
 ******************************************************************************
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <dl>
 * <dt>Purpose: GraphViz Java API
 * <dd>
 *
 * <dt>Description:
 * <dd> With this Java class you can simply call dot
 *      from your Java programs
 * <dt>Example usage:
 * <dd>
 * <pre>
 *    GraphViz gv = new GraphViz();
 *    gv.addln(gv.start_graph());
 *    gv.addln("A -> B;");
 *    gv.addln("A -> C;");
 *    gv.addln(gv.end_graph());
 *    System.out.println(gv.getDotSource());
 *
 *    String type = "gif";
 *    File out = new File("out." + type);   // out.gif in this example
 *    gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
 * </pre>
 * </dd>
 *
 * </dl>
 *
 */
public class GraphViz
{
	/**
	 * The dir. where temporary files will be created.
	 */
	//private static String TEMP_DIR = "/tmp";	// Linux
	//private static String TEMP_DIR = "c:/temp";	// Windows

	/**
	 * Where is your dot program located? It will be called externally.
	 */
	//private static String DOT = "/usr/bin/dot";	// Linux
	private final static String DOT = "C:/Program Files (x86)/Graphviz 2.28/bin/dot.exe";	// Windows

	private StringBuilder graph = new StringBuilder();

	private String figName;

	private String figType; // gif, dot, fig, pdf, ps, svg, png, plain
	

	/**
	 * Constructor: creates a new GraphViz object that will contain
	 * a graph.
	 */

	//	String type = "gif";
	//  String type = "dot";
	//  String type = "fig";    // open with xfig
	//  String type = "pdf";
	//  String type = "ps";
	//  String type = "svg";    // open with inkscape
	//  String type = "png";
	//  String type = "plain";
	public GraphViz(String name, String type) {
		figName = name;
		figType = type;
	}

	/**
	 * Returns the graph's source description in dot language.
	 * @return Source of the graph in dot language.
	 */
	public String getDotSource() {
		return graph.toString();
	}

	/**
	 * Adds a string to the graph's source (without newline).
	 */
	public void add(String line) {
		graph.append(line);
	}

	/**
	 * Adds a string to the graph's source (with newline).
	 */
	public void addln(String line) {
		graph.append(line + "\n");
	}

	/**
	 * Adds a newline to the graph's source.
	 */
	public void addln() {
		graph.append('\n');
	}

	/*
	 * create figName.dot, figName.figType
	 */
	public boolean generateFig() throws IOException, InterruptedException
	{
		//do para check
		if(graph.length() == 0 || figName.length() == 0 || figType.length() == 0)
		{
			System.err.println("parameters are not prepared.");
			return false;
		}
		boolean result = generateFigPri();
		return result;
	}

	private boolean generateFigPri( ) throws IOException, InterruptedException 
	{
		File dotFile = writeDotSourceToFile();
		if(dotFile == null)
		{
			return false;
		}

		Runtime rt = Runtime.getRuntime();

		String[] args = {DOT, "-T"+figType, dotFile.getAbsolutePath(), "-o", figName + "." + figType};
		Process p = rt.exec(args);

		int retCode = p.waitFor();

		if(retCode == 0)
			return true;
		else
			return false;
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
			fout.write(graph.toString());
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
		addln("digraph G {");
	}

	/**
	 * Returns a string that is used to end a graph.
	 * @return A string to close a graph.
	 */
	public void end_graph() {
		addln("}");
	}

	/**
	 * Read a DOT graph from a text file.
	 * 
	 * @param input Input text file containing the DOT graph
	 * source.
	 */
	public void readSource(String input)
	{
		StringBuilder sb = new StringBuilder();

		try
		{
			FileInputStream fis = new FileInputStream(input);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			dis.close();
		} 
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		this.graph = sb;
	}

} // end of class GraphViz

