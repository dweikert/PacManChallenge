package entrants.pacman.dweikert;



public class Network {

 

	protected int inputCount;
	protected int hiddenCount;
	protected int weightCount; 
	protected double weights[];	
	protected double outputs[];




	/**
	 * Construct the neural network.
	 *
	 * @param inputCount The number of input neurons.
	 * @param hiddenCount The number of hidden neurons
	 * @param _weights[] The weight vector for the network
	 */
	 public Network(int inputCount, int hiddenCount, double _weights[])
	 {
	
		  this.inputCount = inputCount;
		  this.hiddenCount = hiddenCount;
		  this.weights = _weights;		  
		  weightCount = hiddenCount * (inputCount+1);
		  int neuronCount = inputCount + hiddenCount + 1;
		  this.outputs = new double[neuronCount];	  
		  
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
	  * Compute the output of the neural network.
	  *
	  * @param input The input to the neural network.
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
 
 
