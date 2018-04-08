package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class noSearch {
	
	public static TablutMove search(TablutBoardState bs, int player_id) {
		// eventually do some logic for being offensive or defensive
		return defensiveMuscovite(bs);
	}
	
	private static TablutMove defensiveSwede(TablutBoardState bs) {
		final double p = 0.15;
		int piecesAliveOnCurrentBoard = bs.getNumberPlayerPieces(bs.SWEDE); 
		int piecesDead = 9;
		int oldOpponentPieceCount = bs.getNumberPlayerPieces(bs.getOpponent());
		boolean kingAlive = true;
		double kingToCorner = Coordinates.distanceToClosestCorner(bs.getKingPosition()); 
		TablutMove bestMove = null;
		
		for (TablutMove move : bs.getAllLegalMoves()) {
			TablutBoardState cloned = (TablutBoardState) bs.clone();
			cloned.processMove(move);
			int piecesDeadThisMove = 0;
			
			// check if a swede pawn can be killed in the next turn
			for (TablutMove opMove : getAllOpponentMoves(cloned)) {
				TablutBoardState opCloned = (TablutBoardState) cloned.clone();
				opCloned.processMove(opMove);
				int pieceDiff = piecesAliveOnCurrentBoard - opCloned.getNumberPlayerPieces(bs.SWEDE);
				if (pieceDiff < piecesDeadThisMove) {
					piecesDeadThisMove = pieceDiff;
				}
				// could also do a sum of possible pieces dead from future moves
				// piecesDeadThisMove += pieceDiff;
				// check if the king will be killed
				if (opCloned.getKingPosition() == null) {
					kingAlive = false;
				}
			}
			
			/////// MOVE LOGIC, in reverse order of importance
			// check if we can kill muscovite pawns
			int newOpponentPieceCount = cloned.getNumberPlayerPieces(cloned.getOpponent());
			if (newOpponentPieceCount < oldOpponentPieceCount && Math.random() > p) {
				bestMove = move;
			}

			// if we can move the king to a corner, try doing so
			Coord kingPos = cloned.getKingPosition();
			if (Coordinates.distanceToClosestCorner(kingPos) < kingToCorner && kingAlive && Math.random() > p) {
				bestMove = move;
			}
			
			// if we can avoid killing one of our pieces, do this move
			if (piecesDeadThisMove < piecesDead && Math.random() > p) {
				bestMove = move;
				piecesDead = piecesDeadThisMove;
			}
		}
		if (bestMove == null) {
			return (TablutMove) bs.getRandomMove();
			// this won't ever happen, but it's required for the compiler
		} else {
			return bestMove;
		}
	}
	
	private static TablutMove offensiveSwede(TablutBoardState bs) {
		final double p = 0.15;
		int piecesAliveOnCurrentBoard = bs.getNumberPlayerPieces(bs.SWEDE); 
		int piecesDead = 9;
		int oldOpponentPieceCount = bs.getNumberPlayerPieces(bs.MUSCOVITE);
		boolean kingAlive = true;
		double kingToCorner = Coordinates.distanceToClosestCorner(bs.getKingPosition()); 
		TablutMove bestMove = null;
		
		String reasonForMove = "";
		
		for (TablutMove move : bs.getAllLegalMoves()) {
			TablutBoardState cloned = (TablutBoardState) bs.clone();
			cloned.processMove(move);
			int piecesDeadThisMove = 0;
			
			// check if a swede pawn can be killed in the next turn
			for (TablutMove opMove : getAllOpponentMoves(cloned)) {
				TablutBoardState opCloned = (TablutBoardState) cloned.clone();
				opCloned.processMove(opMove);
				int pieceDiff = piecesAliveOnCurrentBoard - opCloned.getNumberPlayerPieces(bs.SWEDE);
				if (pieceDiff < piecesDeadThisMove) {
					piecesDeadThisMove = pieceDiff;
				}
				// could also do a sum of possible pieces dead from future moves
				// piecesDeadThisMove += pieceDiff;
				// check if the king will be killed
				if (opCloned.getKingPosition() == null) {
					kingAlive = false;
					System.out.print(" king killed ");
				}
			}
			
			/////// MOVE LOGIC, in reverse order of importance
			// if we can avoid killing one of our pieces, do this move
			if (piecesDeadThisMove < piecesDead && Math.random() > p) {
				bestMove = move;
				piecesDead = piecesDeadThisMove;
				reasonForMove = "saves some swedes: " + piecesDeadThisMove; 
			}
			
			// if we can move the king to a corner, try doing so
			Coord kingPos = cloned.getKingPosition();
			if (Coordinates.distanceToClosestCorner(kingPos) < kingToCorner && kingAlive && Math.random() > p) {
				bestMove = move;
				reasonForMove = "moved the king " + (kingToCorner - Coordinates.distanceToClosestCorner(kingPos)) + " spots closer to the corner";
			}
			
			// check if we can kill muscovite pawns
			int newOpponentPieceCount = cloned.getNumberPlayerPieces(bs.MUSCOVITE);
			if (newOpponentPieceCount < oldOpponentPieceCount && Math.random() > p) {
				bestMove = move;
				reasonForMove = "killed " + (oldOpponentPieceCount - newOpponentPieceCount) + " muscovites";
			}
		}
		System.out.println("Chose move " + bestMove.toPrettyString() + " because " + reasonForMove);
		if (bestMove == null) {
			return (TablutMove) bs.getRandomMove();
			// this won't ever happen, but it's required for the compiler
		} else {
			return bestMove;
		}
	}
	
	private static TablutMove defensiveMuscovite(TablutBoardState bs) {
		final double p = 0.15;
		int[][] tlCorners = {{0,2},{1,1},{2,0}};
		int[][] trCorners = {{6,0},{7,1},{0,8}};
		int[][] blCorners = {{2,8},{1,7},{0,6}};
		int[][] brCorners = {{8,6},{7,7},{6,8}};
		int piecesAliveOnCurrentBoard = bs.getNumberPlayerPieces(bs.MUSCOVITE); 
		int piecesDead = 16;
		int oldOpponentPieceCount = bs.getNumberPlayerPieces(bs.getOpponent());
		int oldPiecesDefendingCorner = 0;
		String reasonForMove = "";
		TablutMove bestMove = null;
		
		oldPiecesDefendingCorner = checkDefenders(bs);
		
		for (TablutMove move : bs.getAllLegalMoves()) {
			boolean newPiecesDefendingCorner = false;
			int piecesDeadThisMove = 0;
			TablutBoardState cloned = (TablutBoardState) bs.clone();
			cloned.processMove(move);
			
			// check if a muscovite pawn can be killed in the next turn
			for (TablutMove opMove : getAllOpponentMoves(cloned)) {
				TablutBoardState opCloned = (TablutBoardState) cloned.clone();
				opCloned.processMove(opMove);
				int pieceDiff = piecesAliveOnCurrentBoard - opCloned.getNumberPlayerPieces(bs.MUSCOVITE);
				if (pieceDiff < piecesDeadThisMove) {
					piecesDeadThisMove = pieceDiff;
				}
				// could also do a sum of possible pieces dead from future moves
				// piecesDeadThisMove += pieceDiff;
			}
			// check if this moves a piece to a defensive corner pos
			int[] coordAsArray = {move.getEndPosition().x, move.getEndPosition().y};
			if (in(tlCorners, coordAsArray) || in(trCorners, coordAsArray) || in(blCorners, coordAsArray) || in(brCorners, coordAsArray)) {
				int newPiecesDefendingCornerCount = checkDefenders(cloned);
				if (newPiecesDefendingCornerCount > oldPiecesDefendingCorner) {
					newPiecesDefendingCorner = true;
				}
			}
			
			/////// MOVE LOGIC, in reverse order of importance
			// check if we can kill some swedes
			int newOpponentPieceCount = cloned.getNumberPlayerPieces(bs.SWEDE);
			if (newOpponentPieceCount < oldOpponentPieceCount && Math.random() > p) {
				bestMove = move;
				reasonForMove = "killed " + (oldOpponentPieceCount - newOpponentPieceCount) + " swedes";
			}
						
			// check if we can kill the king
			if (cloned.getKingPosition() == null && Math.random() > p) {
				bestMove = move;
				reasonForMove = "killed the king";
			}
			
			// try to keep muscovite pawns alive
			if (piecesDeadThisMove < piecesDead && Math.random() > p) {
				bestMove = move;
				piecesDead = piecesDeadThisMove;
				reasonForMove = "saves some muscovites: " + piecesDeadThisMove; 
			}
			
			// move piece to corner defence position 
			if (newPiecesDefendingCorner && Math.random() > p) {
				bestMove = move;
				reasonForMove = "defending the corner"; 
			}
			
			// if king moves towards corner, cover that corner asap
			Coord kingPos = bs.getKingPosition();
			if (kingPos.x > 4) {
				if (kingPos.y > 4) {
					if (in(brCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the bottom right corner";
					}
				} else if (kingPos.y < 4) {
					if (in(trCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the top right corner";
					}
				} else {
					if (in(brCorners, coordAsArray) || in(trCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the right side";
					}
				}
			} else if (kingPos.x < 4) {
				if (kingPos.y > 4) {
					if (in(blCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the bottom left corner";
					}
				} else if (kingPos.y < 4) {
					if (in(tlCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the top left corner";
					}
				} else {
					if (in(blCorners, coordAsArray) || in(tlCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the left side";
					}
				}
			} else {
				if (kingPos.y > 4) {
					if (in(blCorners, coordAsArray) || in(brCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the bottom side";
					}
				} else if (kingPos.y < 4) {
					if (in(tlCorners, coordAsArray) || in(trCorners, coordAsArray) && Math.random() > p) {
						bestMove = move;
						reasonForMove = "defending the top side";
					}
				} 
			}
		}
		System.out.println("Chose move " + bestMove.toPrettyString() + " because " + reasonForMove);
		if (bestMove == null) {
			return (TablutMove) bs.getRandomMove();
			// this won't ever happen, but it's required for the compiler
		} else {
			return bestMove;
		}
	}
	
	private static float offensiveMuscovite(TablutBoardState bs) {
		return (float) 0;
	}
	
	private static int checkDefenders(TablutBoardState bs) {
		int[][] tlCorners = {{0,2},{1,1},{2,0}};
		int[][] trCorners = {{6,0},{7,1},{0,8}};
		int[][] blCorners = {{2,8},{1,7},{0,6}};
		int[][] brCorners = {{8,6},{7,7},{6,8}};
		int defenders = 0;
		for (Coord myPiece : bs.getPlayerPieceCoordinates()) {
			int[] coordAsArray = {myPiece.x, myPiece.y};
			if (in(tlCorners, coordAsArray) || in(trCorners, coordAsArray) || in(blCorners, coordAsArray) || in(brCorners, coordAsArray)) {
				defenders += 0;
			}
		}
		return defenders;
	}
	
	private static boolean in(int[][] source, int[] search) {
		for (int[] line : source) {
			if (line[0] == search[0] && line[1] == search[1]) {
				return true;
			}
		}
		return false;
	}
	
	private static ArrayList<TablutMove> getAllOpponentMoves(TablutBoardState bs) {
        ArrayList<TablutMove> allMoves = new ArrayList<>();
        for (Coord pos : bs.getOpponentPieceCoordinates()) {
            allMoves.addAll(bs.getLegalMovesForPosition(pos));
        }
        return allMoves;
    }
	
}