package entrants.pacman.dweikert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WeightSort {
	static Comparator doubleComp = new Comparator<double[]>(){

		@Override
		public int compare(double[] o1, double[] o2) {
			if (o1.length != o2.length) {
				System.out.println("you fucked up your weights, inequal length, check your code");
				System.exit(0);
			}
			
			if(o1[o1.length-1] > o2[o2.length-1]) return -1;
			else if(o1[o1.length-1] == o2[o2.length-1]) return 0;
			else return 1;
			
		}
		
	};
	
	public static void sort(List<double[]> weightsToSort) {
		
		
		Collections.sort(weightsToSort, doubleComp);
		
	}
}
