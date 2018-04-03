package student_player;

import java.util.List;

import tablut.TablutBoardState;
import tablut.TablutMove;

public class mcts {
	
	private TablutBoardState bs;
	private List<TablutMove> children;
	
	
	public static TablutMove search(TablutBoardState bs, int depth) { 
		System.out.println(bs.getPlayerPieceCoordinates());
		System.out.println(bs.getOpponentPieceCoordinates());
		return new TablutMove(0,0,0,0,0);
	}
	
	private static float mcts(TablutBoardState bs, int depth) {
		if (depth != 0) {
			// evaluate the state
			return (float) 0;
		} else {
			// Create placeholder for 
			TablutMove maxMove = new TablutMove(0, 0, 0, 0, 0);
			float value = Float.NEGATIVE_INFINITY;
			
			List<TablutMove> options = bs.getAllLegalMoves();
			
			for (TablutMove move: options) {
				TablutBoardState cloned = (TablutBoardState) bs.clone();
        		cloned.processMove(move);
        		float moveVal = mcts(cloned, depth-1);
        		if (moveVal > value) {
        			value = moveVal;
        			maxMove = move;
        		}
			}
			return value;
		}
	}
	
}