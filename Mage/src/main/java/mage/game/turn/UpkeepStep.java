

package mage.game.turn;

import mage.constants.PhaseStep;
import mage.game.Game;
import mage.game.events.GameEvent.EventType;

import java.util.UUID;

/**
 * @author BetaSteward_at_googlemail.com
 */
public class UpkeepStep extends Step {

    public UpkeepStep() {
        super(PhaseStep.UPKEEP, true);
        this.stepEvent = EventType.UPKEEP_STEP;
        this.preStepEvent = EventType.UPKEEP_STEP_PRE;
        this.postStepEvent = EventType.UPKEEP_STEP_POST;
    }

    protected UpkeepStep(final UpkeepStep step) {
        super(step);
    }

    @Override
    public void beginStep(Game game, UUID activePlayerId) {
        // before the "at the beginning of upkeep" triggers (echo)
        game.getBattlefield().beginningOfUpkeep(game);
        super.beginStep(game, activePlayerId);
    }

    @Override
    public UpkeepStep copy() {
        return new UpkeepStep(this);
    }

}
