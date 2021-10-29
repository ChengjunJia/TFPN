package reachmatrix;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

import javax.swing.JApplet;

import org.jgraph.*;
import org.jgraph.graph.*;
import org.jgrapht.graph.DefaultEdge;

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;


/**
 * A demo applet that shows how to use JGraph to visualize JGraphT graphs.
 *
 * @author Barak Naveh
 *
 * @since Aug 3, 2003
 */
public class JGraphAdapterDemo extends JApplet {
	private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
	private static final int width = 1500;
	private static final int height = 1000;
	private static final Dimension DEFAULT_SIZE = new Dimension( width, height );

	static final Random generator = new Random(System.currentTimeMillis());// used to calculate positions
	static final int rangemin = 6;
	static final int rangemax = 30;
	static final int positive = 1;
	static final int negative = -1;
	GraphComponent gc;
	NeighborIndex<String, DefaultEdge> ni;
	String[] vertices;
	HashMap<String, Integer> vertexInvertInd;
	int[] positionx; // store the positions of vertices
	int[] positiony;
	int[] directx;
	int[] directy;

	// 
	private JGraphModelAdapter<String, DefaultEdge> m_jgAdapter;

	/**
	 * 
	 * @return - cluster Id
	 */
	private int initializepositions()
	{
		vertices = gc.getReducedGraph().vertexSet().toArray(new String[0]);
		vertexInvertInd = new HashMap<String, Integer>();
		positionx = new int[vertices.length];
		positiony = new int[vertices.length];
		directx = new int[vertices.length];
		directy = new int[vertices.length];
		int clusterid = 0;

		// find the cluster and initialize positions
		for(int i = 0; i < vertices.length; i ++)
		{
			vertexInvertInd.put(vertices[i], i);
			
			if(vertices[i].equals(gc.clusters.get(0)))
			{
				positionx[i] = width/2;
				positiony[i] = height/2;
				clusterid = i;
			}else{
				positionx[i] = 0;
				positiony[i] = 0;
			}

			directx[i] = 0;
			directy[i] = 0;
		}
		
		return clusterid;
	}
	
	private void calculatepositions()
	{
		int clusterid = initializepositions();
		List<String> clusterneighbors = ni.neighborListOf(vertices[clusterid]);
		int[] vIds = new int[clusterneighbors.size()];
		
		for(int i = 0; i < clusterneighbors.size(); i ++)
		{
			String v = clusterneighbors.get(i);
			vIds[i] = vertexInvertInd.get(v);
			// no direction requirement
			int[] xydir = new int[2];
			xydir[0] = convertToDir();
			xydir[1] = convertToDir();
			int[] xylen = genXYLen();
			setPosAndDir(clusterid, vIds[i], xylen[0]*xydir[0], xylen[1]*xydir[1]);
		}
		
		for(int i = 0; i < clusterneighbors.size(); i ++)
		{
			calculatepositions_R(vIds[i]); 
		}
	}
	
	private void calculatepositions_R(int rootId)
	{
		List<String> ns = ni.neighborListOf(vertices[rootId]);
		int [] vIds = new int[ns.size()];
		boolean [] alreadyset = new boolean[ns.size()];
		
		for(int i = 0; i < ns.size(); i ++)
		{
			vIds[i] = vertexInvertInd.get(ns.get(i));
			if(!isSetPos(vIds[i]))
			{
				setPos(rootId, vIds[i]);
				alreadyset[i] = false;
			}else
			{
				alreadyset[i] = true;
			}
		}
		
		for(int i = 0; i < vIds.length; i++)
		{
			if(!alreadyset[i])
			{
				calculatepositions_R(vIds[i]);
			}
		}
	}

	private boolean isSetPos(int vId)
	{
		if(positionx[vId]== 0 && positiony[vId] == 0)
		{
			return false;
		}else
		{
			return true;
		}
	}

	private void setPos(int fartherId, int vId)
	{
		int[] dir = genXYDir(fartherId);
		int[] len = genXYLen();
		setPosAndDir(fartherId, vId, len[0]*dir[0], len[1]*dir[1]);
	}

	private void setPosAndDir(int fartherId, int vId, int xoff, int yoff)
	{
		positionx[vId] = positionx[fartherId] + xoff;
		positiony[vId] = positiony[fartherId] + yoff;
		setDir(fartherId, vId);
	}

	private void setDir(int fartherId, int vId)
	{
		directx[vId] = positionx[vId] - positionx[fartherId];
		directy[vId] = positiony[vId] - positiony[fartherId];

		if(directx[vId] > 0)
			directx[vId] = positive;
		else
			directx[vId] = negative;

		if(directy[vId] > 0)
			directy[vId] = positive;
		else
			directy[vId] = negative;
	}

	private int[] genXYDir(int fartherId)
	{
		int[] xydir = new int[2];

		int xdir = 0;
		int ydir = 0;
		
		while(xdir != directx[fartherId] && ydir != directy[fartherId])
		{
			xdir = convertToDir();
			ydir = convertToDir();
		}
		
		xydir[0] = xdir;
		xydir[1] = ydir;
		
		return xydir;
	}
	
	private int convertToDir()
	{
		int dir_tmp = generator.nextInt(2);
		if(dir_tmp == 0)
		{
			return positive;
		}else
		{
			return negative;
		}
	}

	private int[] genXYLen()
	{
		int[] xylen = new int[2];
		int xlen = 0;
		int ylen = 0;

		while((xlen + ylen) < rangemin || (xlen + ylen) > rangemax)
		{
			xlen = generator.nextInt(rangemax);
			ylen = generator.nextInt(rangemax);
		}

		xylen[0] = xlen;
		xylen[1] = ylen;
		return xylen;
	}
	
	public void showPos()
	{
		for(int i = 0; i < vertices.length; i ++)
		{
			System.out.println(vertices[i] + ": " + positionx[i] + " " + positiony[i]);
		}
	}

	/**
	 * @see java.applet.Applet#init().
	 */
	 public void init(  ) {
		//String folder = "/home/hongkun/Dropbox/workspace-ap/reach/";
		String folder = "/Users/hongkunyang/Dropbox/workspace-ap/reach/";
		 gc = new GraphComponent(folder + "purdue.ser", folder + "purdue-BDDAP.ser");
		gc.clusterGraph();
		gc.reduceGraph();
		ni = new NeighborIndex<String, DefaultEdge>(gc.getReducedGraph());


		// create a visualization using JGraph, via an adapter
		m_jgAdapter = new JGraphModelAdapter<String, DefaultEdge>( gc.getReducedGraph() );

		JGraph jgraph = new JGraph( m_jgAdapter );

		adjustDisplaySettings( jgraph );
		getContentPane(  ).add( jgraph );
		resize( DEFAULT_SIZE );
		
		calculatepositions();
		// showPos();


		// position vertices nicely within JGraph component

		for(int i = 0; i < vertices.length; i ++)
		{
			positionVertexAt(vertices[i], positionx[i], positiony[i]);
		}

		// that's all there is to it!...
	 }


	 private void adjustDisplaySettings( JGraph jg ) {
		 jg.setPreferredSize( DEFAULT_SIZE );

		 Color  c        = DEFAULT_BG_COLOR;
		 String colorStr = null;

		 try {
			 colorStr = getParameter( "bgcolor" );
		 }
		 catch( Exception e ) {}

		 if( colorStr != null ) {
			 c = Color.decode( colorStr );
		 }

		 jg.setBackground( c );
	 }


	 private void positionVertexAt( Object vertex, int x, int y ) {
		 DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
		 Map              attr = cell.getAttributes(  );
		 Rectangle2D        b    =  GraphConstants.getBounds( attr );

		 GraphConstants.setBounds( attr, new Rectangle( x, y, b.getBounds().width, b.getBounds().height ) );

		 Map cellAttr = new HashMap(  );
		 cellAttr.put( cell, attr );
		 m_jgAdapter.edit( cellAttr, null, null, null );
	 }
}
