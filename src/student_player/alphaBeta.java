package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;


public class alphaBeta {
	public static final int SWEDE = 1;
    public static final int MUSCOVITE = 0;
    
    public static TablutMove search(TablutBoardState bs, int depth, int player_id) {
    	// return the best move of all possible moves given a board state
    	List<TablutMove> options = bs.getAllLegalMoves();
    	ArrayList<abMove> moveValues = new ArrayList<abMove>();
    	for (TablutMove move: options) {
    		moveValues.add( new abMove( move, maxValue(bs, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, depth, player_id) ) );
    	}
    	abMove maxMove = new abMove(new TablutMove(0, 0, 0, 0, 0), Float.NEGATIVE_INFINITY);
    	for (abMove move: moveValues) {
    		if (move.getValue() >= maxMove.getValue()) {
    			maxMove = move;
    		}
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
    		
    		System.out.println("##### STARTING A NEW TURN");
    		for (TablutMove m: options) {
    			System.out.println(m.toPrettyString());
    		}
    		
    		for (TablutMove move: options) {
    			TablutBoardState cloned = (TablutBoardState) bs.clone();
        		cloned.processMove(move);
    			alpha = Math.max(alpha, minValue(cloned, alpha, beta, depth-1, player_id));
    			if (alpha >= beta) {
    				return beta;
    			}
    			return alpha;
    		}
    	}
    	return (float) 0.0;
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
    			return beta;
    		}
    	}
    	return (float) 0.0;
    }
    
    private static float evaluateBoard(TablutBoardState bs, int player_id) {
    	float pieceDiff = (float) 1 / bs.getNumberPlayerPieces(bs.getOpponent());
    	float kingToCorner = (float) 0;
    	if (player_id == bs.SWEDE) {
    		kingToCorner = (float) 1 / Coordinates.distanceToClosestCorner(bs.getKingPosition());
    	}
    	float win = (float) 0;
    	if (bs.getWinner() == player_id) {
    		win = (float) 1000;
    	} 
    	
//    	System.out.print(kingToCorner);
//    	System.out.print(" ");
//    	System.out.println(pieceDiff);
    	
    	return 10*pieceDiff + 100*kingToCorner + win;
    }
}
