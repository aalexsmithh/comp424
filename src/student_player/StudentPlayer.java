package student_player;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260635164");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState bs) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
        // MyTools.getSomething();
    	//TablutMove move = 
//    	TablutMove move = alphaBeta.search(bs, 3, this.getColor());
//    	alphaBeta.evalVerbose(bs, this.player_id);
    	TablutMove move = noSearch.search(bs, this.getColor());
//    	Move move = bs.getRandomMove();
        // Return your move to be processed by the server.
        return move;
    }
}