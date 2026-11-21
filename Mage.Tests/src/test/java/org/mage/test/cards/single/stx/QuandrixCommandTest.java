package org.mage.test.cards.single.stx;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author hanasu
 */
public class QuandrixCommandTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.q.QuandrixCommand Quandrix Command} {1}{G}{U}
     * Instant
     * Choose two —
     * • Return target creature or planeswalker to its owner's hand.
     * • Counter target artifact or enchantment spell.
     * • Put two +1/+1 counters on target creature.
     * • Target player shuffles up to three target cards from their graveyard into their library.
     */
    private static final String command = "Quandrix Command";
    private static final String bears = "Grizzly Bears";
    private static final String elves = "Llanowar Elves";

    private void prepare() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, bears);
        addCard(Zone.HAND, playerA, command);
        addCard(Zone.GRAVEYARD, playerA, bears);
        addCard(Zone.GRAVEYARD, playerB, elves);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, command);
        setModeChoice(playerA, "3");
        setModeChoice(playerA, "4");
        addTarget(playerA, bears); // mode 3: creature on battlefield
        addTarget(playerA, playerB); // mode 4: player
    }

    @Test
    public void test_TargetPlayersGraveyard() {
        prepare();
        addTarget(playerA, elves); // mode 4: card in playerB's graveyard

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerA, bears, CounterType.P1P1, 2);
        assertGraveyardCount(playerB, elves, 0);
        assertLibraryCount(playerB, elves, 1);
        assertGraveyardCount(playerA, bears, 1);
    }

    // https://github.com/magefree/mage/issues/13363
    @Test
    public void test_CantTargetOtherPlayersGraveyard() {
        prepare();
        addTarget(playerA, bears); // mode 4: card in playerA's graveyard -- not allowed

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);

        try {
            execute();
        } catch (Throwable e) {
            if (!e.getMessage().contains("Targets list was setup by addTarget with [" + bears + "], but not used")) {
                Assert.fail("must throw error about bad targets, but got:\n" + e.getMessage());
            }
            return;
        }
        Assert.fail("must not be able to target a card in another player's graveyard");
    }
}
