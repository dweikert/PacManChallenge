
/*
package entrants.pacman.dweikert;


import java.util.Arrays;

import pacman.Executor;
import pacman.controllers.PacmanController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.util.Stats;


public class MCTSAIPacMan extends PacmanController {

    public static void main(String[] args) {
        Executor po = new Executor(true, true, true);
        po.setDaemon(true);
        po.runGame(new MCTSAIPacMan(), new POCommGhosts(50), true, 40);
        
        
        Stats[] stats = po.runExperiment(new MCTSAIPacMan(), new POCommGhosts(50), 3, "test");
        System.out.println(stats[0].getAverage());
        
        stats = po.runExperiment(new RandomJunctionPacMan(), new POCommGhosts(50), 3, "test");
        System.out.println(stats[0].getAverage());
        
        stats = po.runExperiment(new MyPacMan(), new POCommGhosts(50), 3, "test");
        System.out.println(stats[0].getAverage());
    }
       
    
    public MOVE getMove(Game game, long timeDue) {  	
    	// we will only simulate the next moves in a junction, 
    	// otherwise we will walk straight along the hallway
    	int myNodeIndex = game.getPacmanCurrentNodeIndex();

    	// choose random direction at junction
    	if (game.isJunction(myNodeIndex))
    	{
        	// return best direction determined through MCTS
            return mcts(game, timeDue);
    	} else {
    		// follow along the path
    		return nonJunctionSim(game);
    	}

    }
    
    public static MOVE nonJunctionSim(Game game){
    	// get the current position of PacMan (returns -1 in case you can't see PacMan)
    	int myNodeIndex = game.getPacmanCurrentNodeIndex();

    	// get all possible moves at the queried position
    	MOVE[] myMoves = game.getPossibleMoves(myNodeIndex);
    	
    	MOVE lastMove = game.getPacmanLastMoveMade();
		if (Arrays.asList(myMoves).contains(lastMove)){
			return lastMove;
		}
		
		// don't go back (corner)
		for (MOVE move : myMoves){
			if (move != lastMove.opposite()){
				return move;
			}
		}
		
		// default
		return lastMove.opposite();
    }
    
    public MOVE atJunctionSim(){
    	
    	return MOVE.NEUTRAL;
    }
    
    
    public MOVE mcts(Game game, long timeDue){
    	// create MCTSTree object for simulation
        MCTSTree tree = new MCTSTree(game);
        tree.simulate(timeDue);
        
        return tree.getBestMove();
    }
    
}*/