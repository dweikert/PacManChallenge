package entrants.ghosts.dweikert;

import pacman.controllers.IndividualGhostController;
import pacman.controllers.MASController;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.comms.BasicMessage;
import pacman.game.comms.Message;
import pacman.game.comms.Messenger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import entrants.pacman.dweikert.MyNeuralPacMan;
import entrants.pacman.dweikert.Network;
import entrants.pacman.dweikert.WeightIO;


public class NeuralGhosts extends IndividualGhostController {
	static int currentWeight = 0; 
    static List<double[]> weightVectors; //the list of weight vectors for the network
    private final static int PILL_PROXIMITY = 15; //proximity threshold for power pills
    Random rnd = new Random();
    private int TICK_THRESHOLD; //used to remove outdated data (e.g. pacman position)
    private int lastPacmanIndex = -1;
    private int tickSeen = -1;
    static EnumMap<Constants.GHOST, IndividualGhostController> map;
    
    public NeuralGhosts(Constants.GHOST ghost) {
        this(ghost, 5);
    }

    public NeuralGhosts(Constants.GHOST ghost, int TICK_THRESHOLD) {
        super(ghost);
        this.TICK_THRESHOLD = TICK_THRESHOLD;
    }
    
    /*
     *  use this function to create an MAScontroller for starting a game
     */
    public static MASController neuralGhostController() {
    	map = new EnumMap<>(GHOST.class);
        map.put(GHOST.BLINKY, new entrants.ghosts.dweikert.Blinky());
        map.put(GHOST.INKY, new entrants.ghosts.dweikert.Inky());
        map.put(GHOST.PINKY, new entrants.ghosts.dweikert.Pinky());
        map.put(GHOST.SUE, new entrants.ghosts.dweikert.Sue());
    	MASController neuralGhosts = new MASController(map);
    	return neuralGhosts;
    }
    
    
    @Override
    @SuppressWarnings("incomplete-switch")
	public MOVE getMove(Game game, long timeDue) {  	
    	 // Housekeeping - throw out old info
        int currentTick = game.getCurrentLevelTime();
        if (currentTick <= 2 || currentTick - tickSeen >= TICK_THRESHOLD) {
            lastPacmanIndex = -1;
            tickSeen = -1;
        }
    	
    	// get the current position of PacMan (returns -1 in case you can't see PacMan)
    	int myNodeIndex = game.getGhostCurrentNodeIndex(ghost);
    	
    	// get all possible moves at the queried position
    	MOVE[] myMoves = game.getPossibleMoves(myNodeIndex);
    	
    	double weights[] = WeightIO.readWeights("trainedGhostWeights").get(0);
    	Network net = new Network(7, 12, weights);
    	double moveValue[] = new double[myMoves.length];
    	//moveValue[moveValue.length-1] = Double.MIN_VALUE;
    	int currentMove = 0;
    	int inputs[] = new int[7]; 
    	for(MOVE tmp : myMoves){   		
    		   		
    		int nextNodeIndex;
    		int nNodesInDir;
    		int nPillsInDir;
    		int pillDensity;
    		int[] ghostPositions = new int[4];
    		int ghostIndex = 0;
    		int nGhostsInDir;
    		int distToGhost;
    		int ghostEdible;
    		int distToPill;
    		int turnAround;
    		int ghostState;
    		int pacManIndex;
    		int isInPacmanDir;
    		int distToPacman;
    		int pacmanCloseToPP;
    		int pacmanIndex;
    		boolean ghostIsPresent;
    		Messenger messenger = game.getMessenger();
			MOVE lastMove = game.getGhostLastMoveMade(ghost);
    		//get Ghost positions
    		
			for(GHOST ghost : GHOST.values()){
				ghostPositions[ghostIndex] = game.getGhostCurrentNodeIndex(ghost);
				ghostIndex++;
			}
    		switch(tmp) {
    			case UP:
    				nNodesInDir = 0;
    				nPillsInDir = 0;
    				pillDensity = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				distToPill = 0;
    				turnAround = 0;
    				isInPacmanDir = 0;
    				distToPacman = 0;
    				pacmanCloseToPP = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex,  MOVE.UP);
    				while(game.isNodeObservable(nextNodeIndex)) {    					
    					//count number of pills, calculate pill density 
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						nNodesInDir++;
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
    						}    						    						
    					}
    					if(nNodesInDir != 0) {
    						pillDensity = (int) Math.floor(10*(nPillsInDir/nNodesInDir));   
    					}   			        
    			        //is there a ghost already in that direction?
    					for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						ghostIsPresent = true;
	    					}
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.UP);    					
    				}    				
    				//get Pacman position and state, if seen    					 			        
					pacmanIndex = game.getPacmanCurrentNodeIndex();    			       
			        if (pacmanIndex != -1) {
			            lastPacmanIndex = pacmanIndex;
			            tickSeen = game.getCurrentLevelTime();
			            if (messenger != null) {
			                messenger.addMessage(new BasicMessage(ghost, null, BasicMessage.MessageType.PACMAN_SEEN, pacmanIndex, game.getCurrentLevelTime()));
			            }
			        }
			        // get pacman position and state, if someone else has seen 
			        if (pacmanIndex == -1 && game.getMessenger() != null) {
			            for (Message message : messenger.getMessages(ghost)) {
			                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN) {
			                    if (message.getTick() > tickSeen && message.getTick() < currentTick) { // Only if it is newer information
			                        lastPacmanIndex = message.getData();
			                        tickSeen = message.getTick();
			                    }
			                }
			            }
			        }
			        if (pacmanIndex == -1) {
			            pacmanIndex = lastPacmanIndex;
			        } 
			        //check if pacman is close to power pill,
    				pacmanCloseToPP = closeToPower(game);
			        if (pacmanIndex != -1) {
			        	if (tmp == game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
                                pacmanIndex, game.getGhostLastMoveMade(ghost), Constants.DM.PATH)) {
    			        	isInPacmanDir = 1;
    			        }
    			        distToPacman = (int) Math.floor(game.getDistance(myNodeIndex, pacmanIndex, DM.PATH));
			        }    	
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				//check if ghost is edible, and for how long
    				ghostEdible = game.getGhostEdibleTime(ghost);
    				if (game.isGhostEdible(ghost)){
    					ghostState = 1;
    				}		
    				inputs[0] = 0;
    				inputs[1] = isInPacmanDir;
					inputs[2] = distToPacman;
					inputs[3] = pacmanCloseToPP;
					inputs[4] = ghostEdible;
					inputs[5] = ghostState;
					inputs[6] = turnAround;					
					moveValue[currentMove] = net.propagateNetwork(inputs);
					currentMove++;			    	
					break;
    			case RIGHT:
    				nNodesInDir = 0;
    				nPillsInDir = 0;
    				pillDensity = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				distToPill = 0;
    				turnAround = 0;
    				isInPacmanDir = 0;
    				distToPacman = 0;
    				pacmanCloseToPP = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex,  MOVE.RIGHT);
    				while(game.isNodeObservable(nextNodeIndex)) {    					
    					//count number of pills, calculate pill density 
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						nNodesInDir++;
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
    						}    						    						
    					}
    					if(nNodesInDir != 0) {
    						pillDensity = (int) Math.floor(10*(nPillsInDir/nNodesInDir));   
    					}
    								        
    			        //is there a ghost already in that direction?
    					for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						ghostIsPresent = true;
	    					}
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.RIGHT);    					
    				}    				
    				//get Pacman position and state, if seen    					 			        
					pacmanIndex = game.getPacmanCurrentNodeIndex();    			       
			        if (pacmanIndex != -1) {
			            lastPacmanIndex = pacmanIndex;
			            tickSeen = game.getCurrentLevelTime();
			            if (messenger != null) {
			                messenger.addMessage(new BasicMessage(ghost, null, BasicMessage.MessageType.PACMAN_SEEN, pacmanIndex, game.getCurrentLevelTime()));
			            }
			        }
			        // get pacman position and state, if someone else has seen 
			        if (pacmanIndex == -1 && game.getMessenger() != null) {
			            for (Message message : messenger.getMessages(ghost)) {
			                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN) {
			                    if (message.getTick() > tickSeen && message.getTick() < currentTick) { // Only if it is newer information
			                        lastPacmanIndex = message.getData();
			                        tickSeen = message.getTick();
			                    }
			                }
			            }
			        }
			        if (pacmanIndex == -1) {
			            pacmanIndex = lastPacmanIndex;
			        } 
			        //check if pacman is close to power pill,
    				pacmanCloseToPP = closeToPower(game);
			        if (pacmanIndex != -1) {
			        	if (tmp == game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
                                pacmanIndex, game.getGhostLastMoveMade(ghost), Constants.DM.PATH)) {
    			        	isInPacmanDir = 1;
    			        }
    			        distToPacman = (int) Math.floor(game.getDistance(myNodeIndex, pacmanIndex, DM.PATH));
			        }    	
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				//check if ghost is edible, and for how long
    				ghostEdible = game.getGhostEdibleTime(ghost);
    				if (game.isGhostEdible(ghost)){
    					ghostState = 1;
    				}		
    				inputs[0] = 0;
    				inputs[1] = isInPacmanDir;
					inputs[2] = distToPacman;
					inputs[3] = pacmanCloseToPP;
					inputs[4] = ghostEdible;
					inputs[5] = ghostState;
					inputs[6] = turnAround;					
					moveValue[currentMove] = net.propagateNetwork(inputs);
					currentMove++;			    	
					break;
    			case LEFT:
    				nNodesInDir = 0;
    				nPillsInDir = 0;
    				pillDensity = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				distToPill = 0;
    				turnAround = 0;
    				isInPacmanDir = 0;
    				distToPacman = 0;
    				pacmanCloseToPP = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex,  MOVE.LEFT);
    				while(game.isNodeObservable(nextNodeIndex)) {    					
    					//count number of pills, calculate pill density 
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						nNodesInDir++;
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
    						}    						    						
    					}
    					if(nNodesInDir != 0) {
    						pillDensity = (int) Math.floor(10*(nPillsInDir/nNodesInDir));   
    					}	        
    			        //is there a ghost already in that direction?
    					for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						ghostIsPresent = true;
	    					}
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.LEFT);    					
    				}    				
    				//get Pacman position and state, if seen    					 			        
					pacmanIndex = game.getPacmanCurrentNodeIndex();    			       
			        if (pacmanIndex != -1) {
			            lastPacmanIndex = pacmanIndex;
			            tickSeen = game.getCurrentLevelTime();
			            if (messenger != null) {
			                messenger.addMessage(new BasicMessage(ghost, null, BasicMessage.MessageType.PACMAN_SEEN, pacmanIndex, game.getCurrentLevelTime()));
			            }
			        }
			        // get pacman position and state, if someone else has seen 
			        if (pacmanIndex == -1 && game.getMessenger() != null) {
			            for (Message message : messenger.getMessages(ghost)) {
			                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN) {
			                    if (message.getTick() > tickSeen && message.getTick() < currentTick) { // Only if it is newer information
			                        lastPacmanIndex = message.getData();
			                        tickSeen = message.getTick();
			                    }
			                }
			            }
			        }
			        if (pacmanIndex == -1) {
			            pacmanIndex = lastPacmanIndex;
			        } 
			        //check if pacman is close to power pill,
    				pacmanCloseToPP = closeToPower(game);
			        if (pacmanIndex != -1) {
			        	if (tmp == game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
                                pacmanIndex, game.getGhostLastMoveMade(ghost), Constants.DM.PATH)) {
    			        	isInPacmanDir = 1;
    			        }
    			        distToPacman = (int) Math.floor(game.getDistance(myNodeIndex, pacmanIndex, DM.PATH));
			        }    	
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				//check if ghost is edible, and for how long
    				ghostEdible = game.getGhostEdibleTime(ghost);
    				if (game.isGhostEdible(ghost)){
    					ghostState = 1;
    				}		
    				inputs[0] = 0;
    				inputs[1] = isInPacmanDir;
					inputs[2] = distToPacman;
					inputs[3] = pacmanCloseToPP;
					inputs[4] = ghostEdible;
					inputs[5] = ghostState;
					inputs[6] = turnAround;					
					moveValue[currentMove] = net.propagateNetwork(inputs);
					currentMove++;			    	
					break;
    			case DOWN:
    				nNodesInDir = 0;
    				nPillsInDir = 0;
    				pillDensity = 0;
    				nGhostsInDir = 0;
    				distToGhost = 0;
    				ghostEdible = 0;
    				ghostState = 0;
    				distToPill = 0;
    				turnAround = 0;
    				isInPacmanDir = 0;
    				distToPacman = 0;
    				pacmanCloseToPP = 0;
    				nextNodeIndex = game.getNeighbour(myNodeIndex,  MOVE.DOWN);
    				while(game.isNodeObservable(nextNodeIndex)) {    					
    					//count number of pills, calculate pill density 
    					int nextNodePillIndex = game.getPillIndex(nextNodeIndex);
    					if(nextNodePillIndex != -1){
    						nNodesInDir++;
    						if(game.isPillStillAvailable(nextNodePillIndex)){
    							nPillsInDir++;
    						}    						    						
    					}
    					if(nNodesInDir != 0) {
    						pillDensity = (int) Math.floor(10*(nPillsInDir/nNodesInDir));   
    					}  			        
    			        //is there a ghost already in that direction?
    					for(int i=0; i<ghostPositions.length;i++){
	    					if (nextNodeIndex == ghostPositions[i]) {
	    						ghostIsPresent = true;
	    					}
    					}
    					nextNodeIndex = game.getNeighbour(nextNodeIndex, MOVE.DOWN);    					
    				}    				
    				//get Pacman position and state, if seen    					 			        
					pacmanIndex = game.getPacmanCurrentNodeIndex();    			       
			        if (pacmanIndex != -1) {
			            lastPacmanIndex = pacmanIndex;
			            tickSeen = game.getCurrentLevelTime();
			            if (messenger != null) {
			                messenger.addMessage(new BasicMessage(ghost, null, BasicMessage.MessageType.PACMAN_SEEN, pacmanIndex, game.getCurrentLevelTime()));
			            }
			        }
			        // get pacman position and state, if someone else has seen 
			        if (pacmanIndex == -1 && game.getMessenger() != null) {
			            for (Message message : messenger.getMessages(ghost)) {
			                if (message.getType() == BasicMessage.MessageType.PACMAN_SEEN) {
			                    if (message.getTick() > tickSeen && message.getTick() < currentTick) { // Only if it is newer information
			                        lastPacmanIndex = message.getData();
			                        tickSeen = message.getTick();
			                    }
			                }
			            }
			        }
			        if (pacmanIndex == -1) {
			            pacmanIndex = lastPacmanIndex;
			        } 
			        //check if pacman is close to power pill,
    				pacmanCloseToPP = closeToPower(game);
			        if (pacmanIndex != -1) {
			        	if (tmp == game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
                                pacmanIndex, game.getGhostLastMoveMade(ghost), Constants.DM.PATH)) {
    			        	isInPacmanDir = 1;
    			        }
    			        distToPacman = (int) Math.floor(game.getDistance(myNodeIndex, pacmanIndex, DM.PATH));
			        }    	
    				if(lastMove!= null){
    					if (tmp == lastMove.opposite()){
            				turnAround = 1;
            			}	
        				
    				}
    				//check if ghost is edible, and for how long
    				ghostEdible = game.getGhostEdibleTime(ghost);
    				if (game.isGhostEdible(ghost)){
    					ghostState = 1;
    				}		
    				inputs[0] = 0;
    				inputs[1] = isInPacmanDir;
					inputs[2] = distToPacman;
					inputs[3] = pacmanCloseToPP;
					inputs[4] = ghostEdible;
					inputs[5] = ghostState;
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
    			indexOfBestMove = equalMoves.get(ThreadLocalRandom.current().nextInt(0,equalMoves.size()));
    			
    			//last move:
    			/*
    			for(int i: equalMoves){
    				if (myMoves[i] == game.getGhostLastMoveMade(ghost)) {
    					indexOfBestMove = i;
    				}
    			}*/
    		}
    		if(myMoves.length >0) {
    			return myMoves[indexOfBestMove];
    		}
    		return null;
    	}

    //This helper function checks if Ms Pac-Man is close to an available power pill
    private int closeToPower(Game game) {
        int[] powerPills = game.getPowerPillIndices();

        for (int i = 0; i < powerPills.length; i++) {
            Boolean powerPillStillAvailable = game.isPowerPillStillAvailable(i);
            int pacmanNodeIndex = game.getPacmanCurrentNodeIndex();
            if (pacmanNodeIndex == -1) {
                pacmanNodeIndex = lastPacmanIndex;
            }
            if (powerPillStillAvailable == null || pacmanNodeIndex == -1) {
                return 0;
            }
            if (powerPillStillAvailable && game.getShortestPathDistance(powerPills[i], pacmanNodeIndex) < PILL_PROXIMITY) {
                return 1;
            }
        }

        return 0;
    }
    
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

