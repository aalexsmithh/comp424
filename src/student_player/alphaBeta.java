package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;


public class alphaBeta {
	public static final int SWEDE = 1;
    public static final int MUSCOVITE = 0;
    private static final float p = (float) 0.15;
    
    public static TablutMove search(TablutBoardState bs, int depth, int player_id) {
    	// return the best move of all possible moves given a board state
    	List<TablutMove> options = bs.getAllLegalMoves();
    	// init list to collect best move option
    	ArrayList<abMove> moveValues = new ArrayList<abMove>();
    	// iterate over moves and get maxValue for each
    	for (TablutMove move: options) {
    		TablutBoardState cloned = (TablutBoardState) bs.clone();
    		cloned.processMove(move);
    		// calc move val
			float moveVal = maxValue(cloned, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, depth, player_id);
			// debug print
			System.out.print("Max on ");
			System.out.print(move.toPrettyString());
			System.out.print(" with value ");
			System.out.println(moveVal);
//			compactBoard.search(cloned);
    		// add to list
			moveValues.add( new abMove( move, moveVal ) );
    	}
    	// Find the best move of all those looked at
    	abMove maxMove = new abMove(new TablutMove(0, 0, 0, 0, 0), Float.NEGATIVE_INFINITY);
    	for (abMove move: moveValues) {
    		if (move.getValue() >= maxMove.getValue()) {
    			maxMove = move;
    		}
    	}
    	
    	// with probability p (class static), choose a random move
    	if (Math.random() > p) {
    		Random rand = new Random();
    		int idx = rand.nextInt(moveValues.size());
    		maxMove = moveValues.get(idx);
    		System.out.println("Got a random move");
    	}
    	
    	System.out.print("Chose move ");
    	System.out.print(maxMove.getMove().toPrettyString());
    	System.out.print(" with value ");
    	System.out.println(maxMove.getValue());
    	return maxMove.getMove();
}
    
    public static float maxValue(TablutBoardState bs, float alpha, float beta, int depth, int player_id) {
    	if (depth == 0) {
    		return evaluateBoard(bs, player_id);
    	} else {
    		List<TablutMove> options = bs.getAllLegalMoves();
    	    // iterate over all moves to find the best one		
    		for (TablutMove move: options) {
    			// clone board to test move
    			TablutBoardState cloned = (TablutBoardState) bs.clone();
        		cloned.processMove(move);
        		// alpha beta
    			alpha = Math.max(alpha, minValue(cloned, alpha, beta, depth-1, player_id));
    			if (alpha >= beta) {
    				return beta;
    			}
    		}
    		return alpha;
    	}
    }
    
    public static float minValue(TablutBoardState bs, float alpha, float beta, int depth, int player_id) {
    	if (depth == 0) {
    		return evaluateBoard(bs, player_id);
    	} else {
    		List<TablutMove> options = bs.getAllLegalMoves();
    		for (TablutMove move: options) {
    			TablutBoardState cloned = (TablutBoardState) bs.clone();
        		cloned.processMove(move);
    			beta = Math.min(beta, maxValue(cloned, alpha, beta, depth-1, player_id));
    			if (alpha >= beta) {
    				return alpha;
    			}
    		}
    		return beta;
    	}
    }
    
    public static void evalVerbose(TablutBoardState bs, int player_id) {
    	if (player_id == bs.SWEDE) {
    		float opponentPieces = (float) 12 / bs.getNumberPlayerPieces(bs.getOpponent()); // lower the number of opponent pieces
    		float myPieces = bs.getNumberPlayerPieces(player_id); // reward for keeping my own pieces
        	float kingAlive = (bs.getKingPosition() != null) ? 1 : 0; // reward for keeping the king alive
        	float kingToCorner = (bs.getKingPosition() != null) ? 1/Coordinates.distanceToClosestCorner(bs.getKingPosition()) : 0; // give reward as king gets closer to the corner
        	System.out.print("SWEDE Value of state: ");
        	System.out.print(opponentPieces);
        	System.out.print(" (opponentPieces) + ");
        	System.out.print(myPieces);
        	System.out.print(" (myPieces) + ");
        	System.out.print(10*kingAlive);
        	System.out.print(" (kingAlive) + ");
        	System.out.print(kingToCorner);
        	System.out.println(" (kingToCorner)");
    	} else if (player_id == bs.MUSCOVITE) {
    		float opponentPieces = (float) 9 / bs.getNumberPlayerPieces(bs.getOpponent());
    		float myPieces = bs.getNumberPlayerPieces(player_id); // reward for keeping my own pieces
        	float kingDead = (bs.getKingPosition() == null) ? 1 : 0;
        	float kingToCorner = kingHasCleanCorner(bs);
        	System.out.print("MUSCOVITE Value of state: ");
        	System.out.print(opponentPieces);
        	System.out.print(" (opponentPieces) + ");
        	System.out.print(myPieces);
        	System.out.print(" (myPieces) + ");
        	System.out.print(10*kingDead);
        	System.out.print(" (kingDead) + ");
        	System.out.print(-10*kingToCorner);
        	System.out.println(" (kingToCorner)");
    	} else {
    		
    	}
    }
    
    private static float evaluateBoard(TablutBoardState bs, int player_id) {
    	if (player_id == bs.SWEDE) {
    		return evalSwede(bs);
    	} else if (player_id == bs.MUSCOVITE) {
    		return evalMuscovite(bs);
    	} else {
    		return (float) 0;
    	}
    	
//    	System.out.print(kingToCorner);
//    	System.out.print(" ");
//    	System.out.println(pieceDiff);
//    	if (10*pieceDiff + 100*kingToCorner + win + kingDead > 1000) {
//    		System.out.print("This is a good board - ");
//    		System.out.println(10*pieceDiff + 100*kingToCorner + win + kingDead);
//    		compactBoard.search(bs);
//    	}
    	
    }
    
    private static float evalSwede(TablutBoardState bs) {
    	float opponentPieces = (float) 16 / bs.getNumberPlayerPieces(bs.MUSCOVITE); // lower the number of opponent pieces
		float myPieces = bs.getNumberPlayerPieces(bs.SWEDE); // reward for keeping my own pieces
    	float kingAlive = (bs.getKingPosition() != null) ? 1 : 0; // reward for keeping the king alive
//    	float kingToCorner = (bs.getKingPosition() != null) ? 1/(Coordinates.distanceToClosestCorner(bs.getKingPosition()) + (float) 0.001) : 0; // give reward as king gets closer to the corner
    	return opponentPieces + myPieces + 10 * kingAlive;// + kingToCorner;
    }
    
    private static float evalMuscovite(TablutBoardState bs) {
    	float opponentPieces = (float) 9 / bs.getNumberPlayerPieces(bs.SWEDE); // lower the number of opponent pieces
		float myPieces = bs.getNumberPlayerPieces(bs.MUSCOVITE); // reward for keeping my own pieces
    	float kingDead = (bs.getKingPosition() == null) ? 1 : 0;
//    	float kingToCorner = kingHasCleanCorner(bs);
    	return 10 * opponentPieces + myPieces + 10 * kingDead;// + -10 * kingToCorner;
    }
    
    private static float kingHasCleanCorner(TablutBoardState bs) {
    	// check if king is along the side
    	Coord kingPos = bs.getKingPosition();
    	if (kingPos == null) {
    		return 0;
    	} else {
    		if (kingPos.x == 0) {
        		boolean above = false;
        		boolean below = false;
        		for (Coord playerPos: bs.getPlayerPieceCoordinates()) {
        			above = (playerPos.x == 0 && playerPos.y > kingPos.y) ? true : false;
        			below = (playerPos.x == 0 && playerPos.y < kingPos.y) ? true : false;
        		}
        		return (above && below) ? 1 : 0;
        	} else if (kingPos.x == 9) {
        		boolean above = false;
        		boolean below = false;
        		for (Coord playerPos: bs.getPlayerPieceCoordinates()) {
        			above = (playerPos.x == 9 && playerPos.y > kingPos.y) ? true : false;
        			below = (playerPos.x == 9 && playerPos.y < kingPos.y) ? true : false;
        		}
        		return (above && below) ? 1 : 0;
        	} else if (kingPos.y == 0) {
        		boolean above = false;
        		boolean below = false;
        		for (Coord playerPos: bs.getPlayerPieceCoordinates()) {
        			above = (playerPos.y == 0 && playerPos.x > kingPos.x) ? true : false;
        			below = (playerPos.y == 0 && playerPos.x < kingPos.x) ? true : false;
        		}
        		return (above && below) ? 1 : 0;
        	} else if (kingPos.y == 9) {
        		boolean above = false;
        		boolean below = false;
        		for (Coord playerPos: bs.getPlayerPieceCoordinates()) {
        			above = (playerPos.y == 9 && playerPos.x > kingPos.x) ? true : false;
        			below = (playerPos.y == 9 && playerPos.x < kingPos.x) ? true : false;
        		}
        		return (above && below) ? 1 : 0;
        	} else {
        		return 0;
        	}
    	}
    }
}
