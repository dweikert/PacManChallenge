package entrants.pacman.dweikert;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import pacman.Executor;
import pacman.controllers.PacmanController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.util.Stats;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getMove() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., entrants.pacman.username).
 */


public class MyPacManVanilla extends PacmanController {
    private MOVE myMove = MOVE.NEUTRAL;
    static int currentWeight = 0; 
    static List<double[]> weightVectors;
    
    public static void main(String[] args) {
    	
    	
    	Executor po = new Executor(true, true, true);
        po.setDaemon(true);
       
        
        //training routine
        int generations = 0;
        weightVectors = readWeights("initialweights");
        writeWeights(weightVectors, "initialweights");
        while(generations < 50) {
	        double highest = 0;
	        currentWeight = 0;
	        for(int i = 0; i<weightVectors.size();i++){
	        	Stats stats[];        	
	        	String s = "testing weight " + i + " gen " + generations;
	        	stats = po.runExperiment(new MyPacManVanilla(), new POCommGhosts(50), 5, s);
	        	System.out.println("Average: " + stats[0].getAverage());
	        	
	        	highest = (stats[0].getAverage() > highest) ? stats[0].getAverage() : highest;
	        	weightVectors.get(i)[80] =  stats[0].getAverage();
	        	currentWeight++;
	        	System.out.println("highest: "  + highest);
	        }
	        System.out.println("generation " + generations);
	        WeightSort.sort(weightVectors);
	        GeneticOperators.evolve(weightVectors);
	        System.out.println("size: " + weightVectors.size());
	        try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        generations++;
        }
        writeWeights(weightVectors, "trainedweights");
        
       
   	       
        
       
       weightVectors = readWeights("trainedweights");
       currentWeight=0;
       System.out.println("current weight fitness: " + weightVectors.get(currentWeight)[80] );
       po.runGame(new MyPacManVanilla(), new POCommGhosts(50), true, 40);
       
      
        
    }
    
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

	
	
	
	
	
	
	
	
	
    
    @SuppressWarnings("incomplete-switch")
	public MOVE getMove(Game game, long timeDue) {  	

    	
    	// get the current position of PacMan (returns -1 in case you can't see PacMan)
    	int myNodeIndex = game.getPacmanCurrentNodeIndex();
    	
    	// get all possible moves at the queried position
    	MOVE[] myMoves = game.getPossibleMoves(myNodeIndex);
    	
    	double weights[] = MyPacManVanilla.weightVectors.get(currentWeight);
    	Network net = new Network(7, 10, weights);
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
    		boolean isFirstPill;
    		//get Ghost positions
    		
			for(GHOST ghost : GHOST.values()){
				ghostPositions[ghostIndex] = game.getGhostCurrentNodeIndex(ghost);
				ghostIndex++;
			}
    		switch(tmp) {
    			case UP:
    				nPillsInDir = 0;
    				distToJunct = 0;
    				nJunctsinDir = 0;
    				distToPowerPill = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
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
    					if (tmp == lastMove){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = 0;
					inputs[2] = distToPill;
					inputs[3] = distToPowerPill;
					inputs[4] = distToGhost;
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
    					if (tmp == lastMove){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = 0;
					inputs[2] = distToPill;
					inputs[3] = distToPowerPill;
					inputs[4] = distToGhost;
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
    					if (tmp == lastMove){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = 0;
					inputs[2] = distToPill;
					inputs[3] = distToPowerPill;
					inputs[4] = distToGhost;
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
    					if (tmp == lastMove){
            				turnAround = 1;
            			}	
        				
    				}
    				
    				inputs[0] = nPillsInDir;
					inputs[1] = 0;
					inputs[2] = distToPill;
					inputs[3] = distToPowerPill;
					inputs[4] = distToGhost;
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




