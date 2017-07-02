package entrants.pacman.dweikert;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WeightIO {
    /*
     * read the weight Vectors for the EA from file
     */
	@SuppressWarnings("unchecked")
	public static List<double[]> readWeights(String s) {
    	List<double[]> weightList= new ArrayList<double[]>();
        try
        {
            FileInputStream fis = new FileInputStream(s);
            ObjectInputStream ois = new ObjectInputStream(fis);
            weightList = (List<double[]>) ois.readObject();
            ois.close();
            fis.close();
         }catch(IOException ioe){
             ioe.printStackTrace();
             return null;
          }catch(ClassNotFoundException c){
             System.out.println("Class not found");
             c.printStackTrace();
             return null;
          }
       
        return weightList;
    }
	
	/*
	 * write the weight vectors to file
	 */
	public static void writeWeights(List<double[]> weightVectors, String s){
		
	    try{
	    FileOutputStream fos= new FileOutputStream(s);
	    ObjectOutputStream oos= new ObjectOutputStream(fos);
	    oos.writeObject(weightVectors);
	    System.out.println("writing weights");
	    oos.close();
	    fos.close();
	    }catch(IOException ioe){
	    	ioe.printStackTrace();
	    }
	 }


}
