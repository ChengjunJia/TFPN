package reachmatrix;

import java.io.*;
import java.util.ArrayList;

public class Pathset implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4952271025448000827L;
	
	ArrayList<MyPath> paths;
	public Pathset(ArrayList<MyPath> mps)
	{
		this.paths = mps;
	}
	
	public ArrayList<MyPath> getpaths()
	{
		return paths;
	}
	
	/**
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public static void saveToFile(String filename, ArrayList<MyPath> paths) throws IOException
	{
		Pathset ps = new Pathset(paths);
		File save = new File(filename);
		FileOutputStream out = new FileOutputStream(save);
		ObjectOutputStream oout = new ObjectOutputStream(out);
		oout.writeObject(ps);
		oout.close();
		out.close();
	}
	
	public static ArrayList<MyPath> readFromFile(String filename) throws IOException, ClassNotFoundException
	{
		FileInputStream in = new FileInputStream(new File(filename));
		ObjectInputStream oin = new ObjectInputStream(in);
		Pathset ps = (Pathset) oin.readObject();
		oin.close();
		in.close();
		return ps.paths;
	}
	
}
