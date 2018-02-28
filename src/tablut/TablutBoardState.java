package tablut;

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import coordinates.Coordinates.CoordinateDoesNotExistException;
import tablut.TablutMove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class TablutBoardState extends BoardState {

	/* Useful constants. */
	public static final int ILLEGAL = -1;
	public static final int SWEDE = 1;
	public static final int MUSCOVITE = 0;
	public static final int BOARD_SIZE = 9; // 9x9 board for tablut
	public static final int MAX_TURNS = 50;
	public static final boolean EASY_MOVEMENT = false;
	public static enum Piece { BLACK, WHITE, KING, EMPTY }
	
	private static HashMap<Piece, String> piecesToSymbols;
	private static HashMap<Piece, Integer> piecesToPlayer;
	static {
		piecesToSymbols = new HashMap<>();
		piecesToSymbols.put(Piece.BLACK, "B");
		piecesToSymbols.put(Piece.WHITE, "W");
		piecesToSymbols.put(Piece.KING, "K");
		piecesToSymbols.put(Piece.EMPTY, " ");
		
		piecesToPlayer = new HashMap<>();
		piecesToPlayer.put(Piece.BLACK, MUSCOVITE);
		piecesToPlayer.put(Piece.WHITE, SWEDE);
		piecesToPlayer.put(Piece.KING, SWEDE);
		piecesToPlayer.put(Piece.EMPTY, ILLEGAL);
		
		Coordinates.setAllCoordinates(BOARD_SIZE);
	}
	
	private static int FIRST_PLAYER = 0; // first player white, second player black
	
	/* These are our data storage things. */
	private Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
	private HashSet<Coord> muscoviteCoords; // we can use hashsets because
	private HashSet<Coord> swedeCoords;
	private Coord kingPosition;
	private Random rand = new Random(1917);
	private int turnPlayer;
	private int turnNumber;
	private int winner = Board.NOBODY;
	
	// Initial Board State creation. The genesis constructor.
	public TablutBoardState() {
		super();
		
		// Initialize to empty
		for (Coord c : Coordinates.iterCoordinates()) {
			board[c.x][c.y] = Piece.EMPTY;
		}
		
		/* We now place the pieces on the board */
		int middlePosition = 4;
		board[middlePosition][middlePosition] = Piece.KING;
		
		// Blacks. (Muscovites)
		List<Integer> blackSidePieces = Arrays.asList(3, 4, 5);
		List<Integer> axes = Arrays.asList(0, 8);
		for (Integer axis : axes) {
			for (Integer position : blackSidePieces) {
				board[position][axis] = Piece.BLACK;
				board[axis][position] = Piece.BLACK;
			}
		}
		axes = Arrays.asList(1, 7);
		for (Integer axis : axes) {
			board[axis][middlePosition] = Piece.BLACK;
			board[middlePosition][axis] = Piece.BLACK;
		}
		// Whites. (Swedes)
		axes = Arrays.asList(2, 3, 5, 6);
		for (Integer axis : axes) {
			board[axis][middlePosition] = Piece.WHITE;
			board[middlePosition][axis] = Piece.WHITE;
		}
		// Update the lists storing the coordinates of all the pieces.
		swedeCoords = new HashSet<>();
		muscoviteCoords = new HashSet<>();
		for (Coord c : Coordinates.iterCoordinates()) {
			Piece piece = getPieceAt(c);
			if (piecesToPlayer.get(piece) == SWEDE)
				swedeCoords.add(c);
			else if (piecesToPlayer.get(piece) == MUSCOVITE)
				muscoviteCoords.add(c);
		}
		kingPosition = Coordinates.get(4, 4);
	}
	
	/* The below method is for the purpose of cloning. */
	private TablutBoardState(TablutBoardState boardState) {
		for (Coord c : Coordinates.iterCoordinates())
			board[c.x][c.y] = boardState.board[c.x][c.y];
		swedeCoords = new HashSet<>(boardState.swedeCoords);
		muscoviteCoords = new HashSet<>(boardState.muscoviteCoords);
		turnPlayer = boardState.turnPlayer;
		turnNumber = boardState.turnNumber;
	}
	
	@Override
	public Object clone() {
		return new TablutBoardState(this);
	}
	
	/* ----- Here and below are for dealing with moves. ----- */
	public void processMove(TablutMove m) throws IllegalArgumentException {
        if(!isLegal(m)) { // isLegal checks if the player is the correct player.
            throw new IllegalArgumentException(
                "Invalid move for current context. " +
                "Move: " + m.toPrettyString());
        }
        
        int opponent = (turnPlayer == MUSCOVITE) ? SWEDE : MUSCOVITE;
        
        // Process move...
        Coord oldPos = m.getStartPosition();
        Coord newPos = m.getEndPosition();
        Piece movingPiece = getPieceAt(oldPos);
        
        // Get memory address to the list we are working on, then update it.
        HashSet<Coord> playerCoordSet = getPlayerCoordSet();
        playerCoordSet.remove(oldPos); // We can do this remove
        playerCoordSet.add(newPos);
        if (movingPiece == Piece.KING)
        	kingPosition = newPos;
        
        // Now update board.
        board[oldPos.x][oldPos.y] = Piece.EMPTY;
        board[newPos.x][newPos.y] = movingPiece;
        
        // Now check if a capture occurred. Only a piece next to the new position could have been captured.
        List<Coord> captured = new ArrayList<>();
        for (Coord enemy : Coordinates.getNeighbors(newPos)) {
        	if (isOpponentPieceAt(enemy)) {
        		boolean canCapture = true;
        		
    			// If the opponent is a king, we need to check if its at the center.
    			// If it is, then it can only be captured on all 4 sides.
    			if (getPieceAt(enemy) == Piece.KING && Coordinates.isCenter(kingPosition)) {
    				for (Coord possibleAlly : Coordinates.getNeighbors(enemy)) {
    					if (getPieceAt(possibleAlly) != Piece.BLACK) {
    						canCapture = false;
    						break;
    					}
    				}    				
    			} else { // Otherwise, check for the normal, sandwich-based capture rule.
    				try {
	    				Coord sandwichCord = Coordinates.getSandwichCoord(newPos, enemy);
	    				canCapture = canCaptureWithCoord(sandwichCord);
    				} catch (CoordinateDoesNotExistException e) {
    					canCapture = false;
    				}
    			}
    			if (canCapture) {
    				captured.add(enemy);
    			}
        	}
        }
        
        // Slaughter the captured enemies... like pigs. Or more like remove object memory addresses... same thing.
        // Note, it is possible for multiple pieces to be captured at once, so we have a list of them.
        for (Coord capturedCoord : captured) {
        	if (getPieceAt(capturedCoord) == Piece.KING) { 
        		kingPosition = null; // the king has been captured!
        	}	
        	getPlayerCoordSet(opponent).remove(capturedCoord);
        	board[capturedCoord.x][capturedCoord.y] = Piece.EMPTY;
        }
        
        // Update internal variables, winner, turn player, and turn number.
        if (turnPlayer != FIRST_PLAYER) turnNumber += 1;
        turnPlayer = opponent;
        updateWinner(); // Check if anybody won and update internal variables if so.
	}
	
	// Determines if a player has won by updating internal variable.
	private void updateWinner() {
		// Check if king is at corner -- SWEDES WIN!
		if (Coordinates.isCorner(kingPosition)) {
			winner = SWEDE;
		}
		// Check if the king was captured -- MUSCOVITES WIN!
		else if (kingPosition == null) {
			winner = MUSCOVITE;
		}
	}
	
	/**
	 * Get all legal moves for the player. This may be expensive, so it may
	 * be more desirable to select a subset of moves from specific positions. */
	public ArrayList<TablutMove> getAllLegalMoves() {
		ArrayList<TablutMove> allMoves = new ArrayList<>();
		for (Coord pos : getPlayerPieceCoordinates()) {
			allMoves.addAll(getLegalMovesForPosition(pos));
		}
		return allMoves;
	}
	
	/**
     * Get all legal moves for the passed position in the current board state.
     *
     * Returned moves are assumed to be moves for the player whose turn
     * it currently is. */
    public ArrayList<TablutMove> getLegalMovesForPosition(Coord start) {
        ArrayList<TablutMove> legalMoves = new ArrayList<>();
		
		// Check that the piece being requested actually belongs to the player.
        Piece piece = getPieceAt(start);
		if (piecesToPlayer.get(piece) != turnPlayer)
			return legalMoves;
        
        // Iterate along 4 directions.
        List<Coord> goodCoords = new ArrayList<>();
        for (int incr : Arrays.asList(-1, 1)) {
        	goodCoords.addAll(getLegalCoordsInDirection(start, incr, 0)); // move in x direction
        	goodCoords.addAll(getLegalCoordsInDirection(start, 0, incr)); // move in y direction
        }
        
        /* Add the real moves now. We do not call isLegal here; this is because we efficiently 
         * enforce legality by only adding those that are legal. This makes for a more efficient 
         * method so people aren't slowed down by just figuring out what they can do. */
        for (Coord end : goodCoords) {
        	if (pieceIsAllowedAt(end, piece)) // only king moves to corner or center, so need to check.
        		legalMoves.add(new TablutMove(start, end, this.turnPlayer));
        }
        return legalMoves;
    }
    
    private List<Coord> getLegalCoordsInDirection(Coord start, int x, int y) {
    	ArrayList<Coord> coords = new ArrayList<>();
    	assert(!(x!=0 && y!=0));
    	int startPos = (x!=0) ? start.x : start.y; //starting at x or y
    	int incr = (x!=0) ? x : y; // incrementing the x or y value
    	int endIdx = (incr == 1) ? BOARD_SIZE-1 : 0; // moving in the 0 or 8 direction
    	for(int i=startPos+incr; incr * i <= endIdx; i+=incr) { // increasing/decreasing functionality
    		// new coord is an x coord change or a y coord change
    		Coord coord = (x!=0) ? Coordinates.get(i, start.y) : Coordinates.get(start.x, i);
    		if (coordIsEmpty(coord))
    			coords.add(coord);
    		else
    			break;
    	}
    	return coords;
    }
    
    // Determines whether or not this coord is a valid coord we can sandwich with.
    private boolean canCaptureWithCoord(Coord c) {
    	if (Coordinates.isCorner(c) || Coordinates.isCenter(c))
    		return true;
    	if (piecesToPlayer.get(getPieceAt(c)) == turnPlayer)
    		return true;
    	return false;
    }
    
    // Returns all of the coordinates of pieces belonging to the current player.
    public List<Coord> getPlayerPieceCoordinates() {
		if (turnPlayer == MUSCOVITE)
			return new ArrayList<Coord>(muscoviteCoords); // copy the list so no funny business
		else if (turnPlayer == SWEDE)
			return new ArrayList<Coord>(swedeCoords);	
		return null;
    }
    
    private HashSet<Coord> getPlayerCoordSet() {
    	return (turnPlayer == MUSCOVITE) ? muscoviteCoords : swedeCoords;
    }
    
    private HashSet<Coord> getPlayerCoordSet(int player) {
    	return (player == MUSCOVITE) ? muscoviteCoords : swedeCoords;
    }
    
    public boolean isLegal(TablutMove move) {
    		// Make sure that this is the correct player.
    		if (turnPlayer != move.getPlayerID() || move.getPlayerID() == ILLEGAL) 
    			return false;
    		
    		// Get useful things.
    		Coord start = move.getStartPosition();
    		Coord end = move.getEndPosition();
    		Piece piece = getPieceAt(start); // this will check if the position is on the board
    		
    		// Check that the piece being requested actually belongs to the player.
    		if (piecesToPlayer.get(piece) != turnPlayer)
    			return false;
    		
    		// Next, make sure move doesn't end on a piece.
    		if (!coordIsEmpty(end))
    			return false;
    		
    		// Next, make sure the move is actually a move.
    		int coordDiff = start.maxDifference(end);
    		if (coordDiff == 0)
    			return false;
    		
    		// If we are doing step-movement, make sure the move is only one step.
    		if (EASY_MOVEMENT && (coordDiff > 1))
    			return false;
    		
    		// Now for the actual game logic. First we make sure it is moving like a rook.
    		if (!(start.x == end.x || start.y == end.y))
    			return false;
    		
    		// Now we make sure it isn't moving through any other pieces.
    		for (Coord throughCoordinate : start.getCoordsBetween(end)) {
    			if (!coordIsEmpty(throughCoordinate))
    				return false;
    		}
    		
    		// Make sure, if its a corner or center, that the king is the only one able to go there.
    		if (!pieceIsAllowedAt(end, piece))
    			return false;
    		
    		// All of the conditions have been satisfied, we have a legal move!
    		return true;
    }
    
    /* ----- Useful helper functions. ----- */
    public Piece getPieceAt(int xPosition, int yPosition) {
    		return board[xPosition][yPosition];
    }
    
    public Piece getPieceAt(Coord position) {
    		return getPieceAt(position.x, position.y);
    }
    
    public boolean isOpponentPieceAt(Coord position) {
    	return !(coordIsEmpty(position)) && piecesToPlayer.get(getPieceAt(position)) != turnPlayer;
    }
    
	public boolean coordIsEmpty(Coord c) {
		return getPieceAt(c) == Piece.EMPTY;
	}
	
	public int numberOfMuscovites() {
		return muscoviteCoords.size();
	}
	
	public int numberOfSwedes() {
		return swedeCoords.size();
	}	
    
	// If its a king, it can move anywhere. Otherwise, make sure it isn't trying to 
    // move to the center or a corner. 
    private boolean pieceIsAllowedAt(Coord pos, Piece piece) {
    	return piece == Piece.KING || !(Coordinates.isCorner(pos) || Coordinates.isCenter(pos));
    }
    
    // Need to check if the king has been captured.
	private boolean kingIsCaptured() {
		return false;
	}
    
    /* ----- Used by server. ----- */
	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public int getTurnPlayer() {
		return turnPlayer;
	}

	@Override
	public int getTurnNumber() {
		return turnNumber;
	}

	@Override
	public int getWinner() {
		return winner;
	}

	// Used by server to force a winner.
	@Override
	public void setWinner(int win) {
		winner = win;
	}

	@Override
	public int firstPlayer() {
		return FIRST_PLAYER;
	}

	@Override
	public boolean gameOver() {
		return turnNumber > MAX_TURNS;
	}

	@Override
	public Move getRandomMove() {
		ArrayList<TablutMove> moves = getAllLegalMoves();
        return moves.get(rand.nextInt(moves.size()));
	}
	
	/*** Debugging functionality is found below. ***/
	
	// Useful method to show the board.
	public void printBoard() {
		printSpecialBoard(new ArrayList<Coord>(), "");
	}
	
	// Very useful for printing any special positions (such as legal moves).
	// onto the board visualization.
	public void printSpecialBoard(List<Coord> positions, String specialChar) {
		int lastRow = 0;
		for (Coord c : Coordinates.iterCoordinates()) {
			if (c.x > lastRow) {
				lastRow = c.x;
				System.out.println();
			}
			String s = piecesToSymbols.get(this.getPieceAt(c));
			for (Coord special : positions) {
				if (special.equals(c)) {
					s = specialChar;
				}
			}
			System.out.print(s + " ");
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		TablutBoardState b = new TablutBoardState();
		
		// compute branching factors
		for (Integer player : Arrays.asList(MUSCOVITE, SWEDE)) {
			b.turnPlayer = player;
			int totalMoves = b.getAllLegalMoves().size();
			System.out.println(String.format("Player %d, %d possible moves.", player, totalMoves));
		}
		
		// Single coordinate observation for debugging.
		for (int i=0; i < BOARD_SIZE; i++) {
			b.turnPlayer = MUSCOVITE;
			Coord start = Coordinates.get(i, 0);
			System.out.println("------------------\n" + start.toString());
			List<TablutMove> moves = b.getLegalMovesForPosition(start);
			List<Coord> positions = new ArrayList<>();
			for (TablutMove move : moves)
				positions.add(move.getEndPosition());
			b.printSpecialBoard(positions, "*");	
		}
		
		System.out.println("------------------\nCorner check.");
		b.printSpecialBoard(Coordinates.getCorners(), "X");
		System.out.println("------------------\n\nRandom Movement check.");
		TablutMove move = (TablutMove) b.getRandomMove();
		b.printSpecialBoard(Arrays.asList(move.getStartPosition()), "S");
		System.out.println();
		b.printSpecialBoard(Arrays.asList(move.getEndPosition()), "E");
		b.processMove(move);
		System.out.println();
		b.printBoard();
		
		// Check capture
		System.out.println("------------------\n\n\nCapture check.");
		b = new TablutBoardState();
		TablutMove move1 = new TablutMove(0, 3, 2, 3, MUSCOVITE);
		TablutMove move2 = new TablutMove(6, 4, 6, 5, SWEDE);
		TablutMove move3 = new TablutMove(0, 5, 2, 5, MUSCOVITE);
		TablutMove move4 = new TablutMove(6, 5, 6, 6, SWEDE);
		TablutMove move5 = new TablutMove(1, 4, 2, 4, MUSCOVITE);
		List<TablutMove> moves = Arrays.asList(move1, move2, move3, move4, move5);
		for (TablutMove m : moves) {
			b.printSpecialBoard(Arrays.asList(m.getStartPosition()), "S");
			System.out.println();
			b.printSpecialBoard(Arrays.asList(m.getEndPosition()), "E");
			b.processMove(m);
			System.out.println();
			b.printBoard();
			System.out.println("MOVE COMPLETED "+m.toPrettyString()+"\n\n");
		}
		System.out.println();
	}
}