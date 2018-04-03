package student_player;

import tablut.TablutMove;

public class abMove {
	TablutMove move;
	float value;
	
	public abMove(TablutMove move, float val) {
		this.move = move;
		this.value = val;
	}
	
	public TablutMove getMove() {
		return this.move;
	}
	
	public float getValue() {
		return this.value;
	}
}