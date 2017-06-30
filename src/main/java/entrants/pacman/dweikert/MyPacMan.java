package entrants.pacman.dweikert;

import java.util.Arrays;
import java.util.Random;

import pacman.Executor;
import pacman.controllers.PacmanController;
import pacman.controllers.examples.po.POCommGhosts;
import pacman.game.Constants.MOVE;
import pacman.game.Game;


public class MyPacMan extends PacmanController {

    public static void main(String[] args) {
        Executor po = new Executor(true, true, true);
        po.setDaemon(true);
        po.runGame(new MyPacMan(), new POCommGhosts(50), true, 40);
    }
    
    
    public MOVE getMove(Game game, long timeDue) {  	
    	
    	// get the current position of PacMan (returns -1 in case you can't see PacMan)
    	int myNodeIndex = game.getPacmanCurrentNodeIndex();
    	
    	// get all possible moves at the queried position
    	MOVE[] myMoves = game.getPossibleMoves(myNodeIndex);

    	// choose random direction at junction
    	if (game.isJunction(myNodeIndex))
    	{
        	// return a random available move
            int rndIdx = new Random().nextInt(myMoves.length);
            return myMoves[rndIdx];
            
    	} else {
    		// check if the lastMove is still available (hallways)
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

    	}

    	// default
		return game.getPacmanLastMoveMade().opposite();

    }
}