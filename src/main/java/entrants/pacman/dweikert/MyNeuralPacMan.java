package entrants.pacman.dweikert;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import entrants.ghosts.dweikert.NeuralGhosts;
import examples.StarterGhost.Blinky;
import examples.StarterGhostComm.POCommGhost;
import pacman.Executor;
import pacman.controllers.IndividualGhostController;
import pacman.controllers.MASController;
import pacman.controllers.PacmanController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.util.Stats;
import pacman.game.Game;

/*
 *To start a game using neural PAcman and neural ghosts:
 *---------
 *po.runGame(new MyNeuralPacMan(), NeuralGhosts.neuralGhostController(), true, 40);
 *---------
 */


public class MyNeuralPacMan extends PacmanController {
    private MOVE myMove = MOVE.NEUTRAL;
    public static int currentWeightPacman = 0; 
    public static int currentWeightGhosts = 0;
    public static List<double[]> weightVectorsPacman;
    public static List<double[]> weightVectorsGhosts;
    static int generations;

    public static void main(String[] args) {
    
    	
    	
    	
    	Executor po = new Executor(true, true, true);
        po.setDaemon(true);
        
        
        /*
        //training routine pacman (not functional any longer as the best weights are now hardcoded in the respective methods.
         * simply here for documentational purposes
        generations = 0;
        weightVectorsPacman = WeightIO.readWeights("initialweights");
        weightVectorsGhosts = WeightIO.readWeights("initialweights");
        WeightIO.writeWeights(weightVectorsPacman, "initialweights");
        while(generations < 2000) {
	        double highest = 0;
	        currentWeightPacman = 0;
	        currentWeightGhosts = 0;
	        for(int i = 0; i<weightVectorsPacman.size();i++){
	        	Stats stats[];        	
	        	String s = "testing weight " + i + " gen " + generations;
	        	stats = po.runExperiment(new MyPacManVanilla(), new POCommGhosts(50), 10, s);
	        	System.out.println("Average: " + stats[0].getAverage());
	        	
	        	highest = (stats[0].getAverage() > highest) ? stats[0].getAverage() : highest;
	        	weightVectorsPacman.get(i)[weightVectorsPacman.get(i).length-1] =  stats[0].getAverage();
	        	currentWeightPacman++;
	        	System.out.println("highest: "  + highest);
	        }
	        System.out.println("generation " + generations);
	        WeightSort.sort(weightVectorsPacman);
	        GeneticOperators.evolve(weightVectorsPacman);
	        System.out.println("size: " + weightVectorsPacman.size());
	        try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        generations++;
        }
        WeightIO.writeWeights(weightVectorsPacman, "trainedweights");
        */
        
   	   /*
       //training routine ghosts(not functional any longer as the best weights are now hardcoded in the respective methods.
         * simply here for documentational purposes
        generations = 0;
        weightVectorsPacman = WeightIO.readWeights("trainedweights");
        weightVectorsGhosts = WeightIO.readWeights("trainedghostweights");
        while(generations < 100) {
	        double highest = -9999;
	        currentWeightPacman = 0;
	        currentWeightGhosts = 0;
	        for(int i = 0; i<weightVectorsGhosts.size();i++){
	        	Stats stats[];        	
	        	String s = "testing weight " + i + " gen " + generations;
	        	stats = po.runExperiment(new MyPacManVanilla(), NeuralGhosts.neuralGhostController(), 10, s);
	        	System.out.println("Average: " + -stats[0].getAverage());
	        	
	        	highest = (-stats[0].getAverage() > highest) ? -stats[0].getAverage() : highest;
	        	weightVectorsGhosts.get(i)[weightVectorsGhosts.get(i).length-1] =  -stats[0].getAverage();
	        	currentWeightGhosts++;
	        	System.out.println("highest: "  + highest);
	        }
	        System.out.println("generation " + generations);
	        WeightSort.sort(weightVectorsGhosts);
	        GeneticOperators.evolve(weightVectorsGhosts);
	        System.out.println("size: " + weightVectorsGhosts.size());
	        try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        generations++;
        }
        WeightIO.writeWeights(weightVectorsGhosts, "trainedGhostWeights");       
       */
       Stats stats[];    
       //stats = po.runExperiment(new MyNeuralPacMan(), new POCommGhosts(), 10, "");
       //System.out.println("Average: " + stats[0].getAverage());
       //run a game using both the neural net pacman and the neural net ghosts
       po.runGame(new MyNeuralPacMan(), NeuralGhosts.neuralGhostController(), true, 40);
       
      
       
    }
    

	
	
	
	
	
	
	
	
	
    
    @SuppressWarnings("incomplete-switch")
	public MOVE getMove(Game game, long timeDue) {  	

    	
    	// get the current position of PacMan (returns -1 in case you can't see PacMan)
    	int myNodeIndex = game.getPacmanCurrentNodeIndex();
    	
    	// get all possible moves at the queried position
    	MOVE[] myMoves = game.getPossibleMoves(myNodeIndex);
    	
    	double weights[] = WeightIO.readWeights("trainedWeights").get(0);
    	Network net = new Network(7, 12, weights);
    	double moveValue[] = new double[myMoves.length];
    	//moveValue[moveValue.length-1] = Double.MIN_VALUE;
    	int currentMove = 0;
    	int inputs[] = new int[7]; 
    	for(MOVE tmp : myMoves){

    		
    		   		
    		int nextNodeIndex;
    		int nPillsInDir;
    		int distToJunct;
    		int nJunctsinDir;
    		int distToPowerPill;
    		int[] ghostPositions = new int[4];
    		int ghostIndex = 0;
    		int nGhostsInDir;
    		int distToGhost;
    		int ghostEdible;
    		int distToPill;
    		int turnAround;
    		int ghostState;
    		boolean isFirstPill;
			MOVE lastMove = game.getPacmanLastMoveMade();
    		//get Ghost positions
    		
			for(GHOST ghost : GHOST.values()){
				ghostPositions[ghostIndex] = game.getGhostCurrentNodeIndex(ghost);
				ghostIndex++;
			}
    		switch(tmp) {
    			//for each move, calculate input and evaluate
    			case UP:
    				//reset input variables for new move evaluation
    				nPillsInDir = 0;
    				distToJunct = 0;
    				nJunctsinDir = 0;
    				distToPowerPill = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				isFirstPill = true;
    				distToPill = 0;
    				turnAround = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex,  MOVE.UP);
    				while(game.isNodeObservable(nextNodeIndex)) {
    					//count number of pills and get ghost position and state, if any
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
	    						//get Distance to nearest Pill
	    						if(isFirstPill){
	    							distToPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.PATH));
	    							isFirstPill = false;
	    						}
    						}
    					}
	    				for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						//ghost at this node:
	    						nGhostsInDir++;
	    						//only care about state and distance to nearest ghost
	    						if(nGhostsInDir ==1) {
	    							distToGhost = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
	    							ghostEdible = game.getGhostEdibleTime(GHOST.values()[i]);
	    							if (game.isGhostEdible(GHOST.values()[i])) {
	    								ghostState = 1;
	    							}
	    							else {
	    								ghostState = -1;
	    							}
	    							
	    						}    								
	    								
	    					}
	    				} 				
    					
    					//check distance to power pill, if any
    					int nextPowerPillIndex=game.getPowerPillIndex(nextNodeIndex);
	    				if(nextPowerPillIndex !=-1){
    						if(game.isPowerPillStillAvailable(nextPowerPillIndex)){
    							distToPowerPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
    						}    						
    					}  					
    					//get distance to nearest junction, if any
    					if(game.isJunction(nextNodeIndex) && !(game.isJunction(myNodeIndex))){
    						nJunctsinDir++;
    						distToJunct = (int) ((nJunctsinDir == 1) ? game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN) : distToJunct);
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.UP);    					
    				}
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = distToPill;
					inputs[2] = distToPowerPill;
					inputs[3] = distToGhost;
					inputs[4] = ghostState;
					inputs[5] = ghostEdible;
					inputs[6] = turnAround;
					moveValue[currentMove] = net.propagateNetwork(inputs);
					currentMove++;

			    	
					break;
    			case RIGHT:
    				nPillsInDir = 0;
    				distToJunct = 0;
    				nJunctsinDir = 0;
    				distToPowerPill = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				isFirstPill = true;
    				distToPill = 0;
    				turnAround = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex, MOVE.RIGHT);
    				while(game.isNodeObservable(nextNodeIndex)) {
    					//count number of pills and get ghost position and state, if any
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
	    						//get Distance to nearest Pill
	    						if(isFirstPill){
	    							distToPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.PATH));
	    							isFirstPill = false;
	    						}
    						}
    					}
	    				for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						//ghost at this node:
	    						nGhostsInDir++;
	    						//only care about state and distance to nearest ghost
	    						if(nGhostsInDir ==1) {
	    							distToGhost = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
	    							ghostEdible = game.getGhostEdibleTime(GHOST.values()[i]);
	    							if (game.isGhostEdible(GHOST.values()[i])) {
	    								ghostState = 1;
	    							}
	    							else {
	    								ghostState = -1;
	    							}
	    						}    								
	    								
	    					}
	    				} 				
    					
    					//check distance to power pill, if any
    					int nextPowerPillIndex=game.getPowerPillIndex(nextNodeIndex);
	    				if(nextPowerPillIndex !=-1){
    						if(game.isPowerPillStillAvailable(nextPowerPillIndex)){
    							distToPowerPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
    						}    						
    					}  					
    					//get distance to nearest junction, if any
    					if(game.isJunction(nextNodeIndex) && !(game.isJunction(myNodeIndex))){
    						nJunctsinDir++;
    						distToJunct = (int) ((nJunctsinDir == 1) ? game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN) : distToJunct);
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.RIGHT);    					
    				}    
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = distToPill;
					inputs[2] = distToPowerPill;
					inputs[3] = distToGhost;
					inputs[4] = ghostState;
					inputs[5] = ghostEdible;
					inputs[6] = turnAround; 					
					moveValue[currentMove] = net.propagateNetwork(inputs);
					currentMove++;
					break;
    			case LEFT:
    				nPillsInDir = 0;
    				distToJunct = 0;
    				nJunctsinDir = 0;
    				distToPowerPill = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				isFirstPill = true;
    				distToPill = 0;
    				turnAround = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex, MOVE.LEFT);
    				while(game.isNodeObservable(nextNodeIndex)) {
    					//count number of pills and get ghost position and state, if any
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
	    						//get Distance to nearest Pill
	    						if(isFirstPill){
	    							distToPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.PATH));
	    							isFirstPill = false;
	    						}
    						}
    					}
	    				for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						//ghost at this node:
	    						nGhostsInDir++;
	    						//only care about state and distance to nearest ghost
	    						if(nGhostsInDir ==1) {
	    							distToGhost = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
	    							ghostEdible = game.getGhostEdibleTime(GHOST.values()[i]);
	    							if (game.isGhostEdible(GHOST.values()[i])) {
	    								ghostState = 1;
	    							}
	    							else {
	    								ghostState = -1;
	    							}
	    						}    								
	    								
	    					}
	    				} 				
    					
    					//check distance to power pill, if any
    					int nextPowerPillIndex=game.getPowerPillIndex(nextNodeIndex);
	    				if(nextPowerPillIndex !=-1){
    						if(game.isPowerPillStillAvailable(nextPowerPillIndex)){
    							distToPowerPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
    						}    						
    					}  					
    					//get distance to nearest junction, if any
    					if(game.isJunction(nextNodeIndex) && !(game.isJunction(myNodeIndex))){
    						nJunctsinDir++;
    						distToJunct = (int) ((nJunctsinDir == 1) ? game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN) : distToJunct);
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.LEFT);    					
    					
    				}
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = distToPill;
					inputs[2] = distToPowerPill;
					inputs[3] = distToGhost;
					inputs[4] = ghostState;
					inputs[5] = ghostEdible;
					inputs[6] = turnAround;					
					moveValue[currentMove] = net.propagateNetwork(inputs);
					currentMove++;
					break;
    			case DOWN:
    				nPillsInDir = 0;
    				distToJunct = 0;
    				nJunctsinDir = 0;
    				distToPowerPill = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				isFirstPill = true;
    				distToPill = 0;
    				turnAround = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex, MOVE.DOWN);
    				while(game.isNodeObservable(nextNodeIndex)) {
    					//count number of pills and get ghost position and state, if any
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
	    						//get Distance to nearest Pill
	    						if(isFirstPill){
	    							distToPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.PATH));
	    							isFirstPill = false;
	    						}
    						}
    					}
	    				for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						//ghost at this node:
	    						nGhostsInDir++;
	    						//only care about state and distance to nearest ghost
	    						if(nGhostsInDir ==1) {
	    							distToGhost = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
	    							ghostEdible = game.getGhostEdibleTime(GHOST.values()[i]);
	    							if (game.isGhostEdible(GHOST.values()[i])) {
	    								ghostState = 1;
	    							}
	    							else {
	    								ghostState = -1;
	    							}
	    						}    								
	    								
	    					}
	    				} 				
    					
    					//check distance to power pill, if any
    					int nextPowerPillIndex=game.getPowerPillIndex(nextNodeIndex);
	    				if(nextPowerPillIndex !=-1){
    						if(game.isPowerPillStillAvailable(nextPowerPillIndex)){
    							distToPowerPill = (int) Math.floor(game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN));
    						}    						
    					}  					
    					//get distance to nearest junction, if any
    					if(game.isJunction(nextNodeIndex)){
    						nJunctsinDir++;
    						distToJunct = (int) ((nJunctsinDir == 1) ? game.getDistance(myNodeIndex, nextNodeIndex, DM.MANHATTAN) : distToJunct);
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.DOWN);    					
    					}
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = distToPill;
					inputs[2] = distToPowerPill;
					inputs[3] = distToGhost;
					inputs[4] = ghostState;
					inputs[5] = ghostEdible;
					inputs[6] = turnAround;					
					moveValue[currentMove] = net.propagateNetwork(inputs);
					currentMove++;
					break;
    			}
    		}
    	

    		
    		
    		int indexOfBestMove = 0;
    		List<Integer> equalMoves = new ArrayList<Integer>();
    		equalMoves.add(0);
    		for (int i = 1; i<moveValue.length; i++) {
    			if (moveValue[i] > moveValue[indexOfBestMove]){
    				indexOfBestMove = i;
    				equalMoves.clear();
    			}
    			else if (moveValue[i] == moveValue[indexOfBestMove]){
    				equalMoves.add(i);
    			}
    			
    		}
    		
    		if (!equalMoves.isEmpty()){
    			//random move:
    			//indexOfBestMove = equalMoves.get(ThreadLocalRandom.current().nextInt(0,equalMoves.size()));
    			
    			//last move:
    			for(int i: equalMoves){
    				if (myMoves[i] == lastMove) {
    					indexOfBestMove = i;
    				}
    			}
    		}
    		return myMoves[indexOfBestMove];
    	}
	}




