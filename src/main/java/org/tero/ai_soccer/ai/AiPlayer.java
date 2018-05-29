package org.tero.ai_soccer.ai;

import org.tero.ai_soccer.sim.Player;
import org.tero.ai_soccer.util.Context;
import org.tero.ai_soccer.util.Vector2D;

import not.my.code.Action;

public class AiPlayer extends Player {

    private NeuralNet brainzz;

    public AiPlayer(Vector2D pos, boolean flipped, int playerId, NeuralNet brain) {
	super(pos, flipped, playerId);
	brainzz = brain;
    }

    @Override
    public Action takeAction(Context ctx) {
	double[] output = brainzz.predict(ctx.getRelativeContext(leftTeam, number, transform));
	double norm = Vector2D.of(output[0], output[1]).magnitude();
	Action action = new Action(5.0 / norm * output[0], 5.0 / norm * output[1], output[2] < 0);
	return action.relativeToGlobal(transform);
    }

}
