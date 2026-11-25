package mage.watchers.common;

import mage.MageObjectReference;
import mage.constants.WatcherScope;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts the upkeep steps of its controller that began while a permanent was under that player's control, since it
 * entered the battlefield or last changed control (for echo, rule 702.30a). Additional upkeep steps count, skipped
 * upkeep steps don't. Not reset at the end of the turn.
 */
public class EchoWatcher extends Watcher {

    private final Map<MageObjectReference, Integer> controllerUpkeeps = new HashMap<>();

    public EchoWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        switch (event.getType()) {
            case UPKEEP_STEP_PRE:
                for (Permanent permanent : game.getBattlefield().getAllPermanents()) {
                    if (permanent.isControlledBy(event.getPlayerId())) {
                        controllerUpkeeps.compute(new MageObjectReference(permanent, game), CardUtil::setOrIncrementValue);
                    }
                }
                break;
            case GAINED_CONTROL:
                Permanent permanent = game.getPermanent(event.getTargetId());
                if (permanent != null) {
                    controllerUpkeeps.remove(new MageObjectReference(permanent, game));
                }
                break;
        }
    }

    /**
     * "came under your control since the beginning of your last upkeep": it wasn't under its controller's control
     * when that player's previous upkeep step began
     */
    public static boolean cameUnderControlSinceLastUpkeep(Permanent permanent, Game game) {
        EchoWatcher watcher = game.getState().getWatcher(EchoWatcher.class);
        return watcher != null
                && watcher.controllerUpkeeps.getOrDefault(new MageObjectReference(permanent, game), 0) < 2;
    }
}
