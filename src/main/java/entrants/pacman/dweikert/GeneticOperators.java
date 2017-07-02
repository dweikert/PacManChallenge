package entrants.pacman.dweikert;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Helper class for evolving Weights
 */
public class GeneticOperators {
	
	/*
	 * Evolves the given List of Weight Vector genomes by
	 * replacing the 30 worst individuals with the children of the 30 best individuals
	 * Mutates each gene of each child with 10% chance.
	 */
	public static void evolve(List<double[]> weightVectors) {
		int nWeightVectors = weightVectors.size();
		
		//kill 30 worst Vectors:
		System.out.println("Size before termination: " + nWeightVectors);
		for(int i = nWeightVectors-1; i>=nWeightVectors-30; i--){
			weightVectors.remove(i);
		}
		
		nWeightVectors = weightVectors.size();
		
		// reproduce:
		System.out.println("Size before reproduction: " + nWeightVectors);
		int weightLength = weightVectors.get(0).length;
		System.out.println("Size of each gene (incl. fitness: " + weightLength);
		for(int i = 0; i<30; i=i+2) {
			//from 2 parents generate 2 children by recombining the parents first and second halves
			double[] child1 = new double[weightLength];
			double[] child2 = new double[weightLength];
			for (int j=0; j<(weightLength-1)/2; j++){
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
		for(int i = 70; i<nWeightVectors; i++) {
			for (int j = i; j<weightLength-1; j++){
				//with a chance of 10% mutate each gene by adding/subtracting(equal chance) a random value between 0 and 1
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
