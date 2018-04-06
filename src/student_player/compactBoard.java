package student_player;

import java.util.Arrays;

import coordinates.Coord;
import tablut.TablutBoardState;

public class compactBoard {
	
	public static void search(TablutBoardState bs) {
//		System.out.println(bs.getPlayerPieceCoordinates());
//		System.out.println(bs.getOpponentPieceCoordinates());
//		System.out.println(bs.getKingPosition());
		
		int[][] board = new int[9][9];
		
		for (Coord co: bs.getPlayerPieceCoordinates()) {
			board[co.x][co.y] = 1;
		}
		
		for (Coord co: bs.getOpponentPieceCoordinates()) {
			board[co.x][co.y] = -1;
		}
		
		Coord kingPos = bs.getKingPosition();
		if (kingPos != null) {
			board[kingPos.x][kingPos.y] = 2;
		}
		
		for (int[] line : board) {
			System.out.println(Arrays.toString(line));
		}
	}
	
}