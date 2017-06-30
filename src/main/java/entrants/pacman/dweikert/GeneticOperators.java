package entrants.pacman.dweikert;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticOperators {
	
	
	public static void evolve(List<double[]> weightVectors) {
		
		//kill:
		System.out.println("Size before kill: " + weightVectors.size());
		for(int i = 99; i>=70; i--){
			weightVectors.remove(i);
		}
		
		
		
		// reproduce:
		System.out.println("Size before reproduce: " + weightVectors.size());
		for(int i = 0; i<30; i=i+2) {
			double[] child1 = new double[81];
			double[] child2 = new double[81];
			for (int j=0; j<40; j++){
				child1[j] = weightVectors.get(i)[j];
				child1[child1.length-(j+1)] = weightVectors.get(i+1)[child1.length-(j+1)];
				child2[j] = weightVectors.get(i+1)[j];
				child2[child1.length-(j+1)] = weightVectors.get(i)[child1.length-(j+1)];
			}
			weightVectors.add(child1);
			weightVectors.add(child2);
		}
		
		//mutate children:
		System.out.println("Size before mutation: " + weightVectors.size());
		for(int i = 70; i<weightVectors.size(); i++) {
			for (int j = i; j<80; j++){
				double mutationfactor = ThreadLocalRandom.current().nextDouble(0, 1);
				if(mutationfactor < 0.1) {
					double sign = ThreadLocalRandom.current().nextInt(-1, 1);
					double mutationValue = ThreadLocalRandom.current().nextDouble(0,1);
					weightVectors.get(i)[j] += (sign >= 0) ? mutationValue : -mutationValue;
				}
				
				
				
			}
		}
		
	}
}
