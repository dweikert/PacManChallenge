package entrants.pacman.dweikert;



public class Network {

 

	protected int inputCount;
	protected int hiddenCount;
	protected int weightCount; 
	protected double fire[];
	protected double weights[];	
	protected double outputs[];




	/**
	 * Construct the neural network.
	 *
	 * @param inputCount The number of input neurons.
	 * @param hiddenCount The number of hidden neurons
	 * @param outputCount The number of output neurons
	 * @param learnRate The learning rate to be used when training.
	 * @param momentum The momentum to be used when training.
	 */
	 public Network(int inputCount, int hiddenCount, double _weights[])
	 {
	
		  this.inputCount = inputCount;
		  this.hiddenCount = hiddenCount;
		  this.weights = _weights;		  
		  weightCount = hiddenCount * (inputCount+1);
		  int neuronCount = inputCount + hiddenCount + 1;
		  this.outputs = new double[neuronCount];
		  fire    = new double[neuronCount];
		  
		  
		  
	 }
	
	
	
	 
	/**
	 * Using a hyerbolic tangent activation function
	 * 
	 * @param sum the sum of inputs to the neuron
	 * @return the output of the neuron
	 */
	 public double fire(double sum) {
		 return Math.tanh(sum);
	 }
	
	 
	 /**
	  * Compute the output for a given input to the neural network.
	  *
	  * @param input The input provide to the neural network.
	  * @return The results from the output neurons.
	  */
	 public double propagateNetwork(int input[]){
		 int i;
		 final int hiddenIndex = inputCount;
		 final int outIndex = inputCount + hiddenCount;
		 
		 //compute outputs of input layer (inputs are weightless)
		 
		 for( i = 0; i<inputCount; i++){
			 outputs[i] = fire(input[i]);
		 }
		 //compute outputs of hidden layer
		 int weightIndex = 0;
		 for(i = hiddenIndex; i<outIndex; i++){
			 double sum = 0;
			 for (int j=0; j<inputCount; j++) {
				 sum += outputs[j] * weights[weightIndex];
				 weightIndex++;
			 }
			 outputs[i]=fire(sum);
		 }
		 //compute final output
		 double sum = 0;
		 for(i= hiddenIndex; i<outIndex; i++) {
			sum+= outputs[i] * weights[weightIndex];
			weightIndex++;	
		 }
		 
		 outputs[outIndex] = fire(sum);
		 return outputs[outIndex];
	 }
}
 
 
