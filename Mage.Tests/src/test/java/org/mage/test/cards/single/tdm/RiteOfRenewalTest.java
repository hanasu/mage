package org.mage.test.cards.single.tdm;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author hanasu
 */
public class RiteOfRenewalTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.r.RiteOfRenewal Rite of Renewal} {3}{G}
     * Sorcery
     * Return up to two target permanent cards from your graveyard to your hand.
     * Target player shuffles up to four target cards from their graveyard into their library.
     * Exile Rite of Renewal.
     */
    private static final String rite = "Rite of Renewal";
    private static final String bears = "Grizzly Bears";
    private static final String elves = "Llanowar Elves";
    private static final String shock = "Shock";

    private void prepare() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 4);
        addCard(Zone.HAND, playerA, rite);
        addCard(Zone.GRAVEYARD, playerA, bears);
        addCard(Zone.GRAVEYARD, playerA, shock);
        addCard(Zone.GRAVEYARD, playerB, elves);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, rite);
        addTarget(playerA, bears); // permanent card from your graveyard
        addTarget(playerA, playerB); // player
    }

    @Test
    public void test_TargetPlayersGraveyard() {
        prepare();
        addTarget(playerA, elves); // card in playerB's graveyard

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertHandCount(playerA, bears, 1);
        assertGraveyardCount(playerB, elves, 0);
        assertLibraryCount(playerB, elves, 1);
        assertGraveyardCount(playerA, shock, 1);
        assertExileCount(playerA, rite, 1);
    }

    // https://github.com/magefree/mage/issues/13363
    @Test
    public void test_CantTargetOtherPlayersGraveyard() {
        prepare();
        addTarget(playerA, shock); // card in playerA's graveyard -- not allowed

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);

        try {
            execute();
        } catch (Throwable e) {
            if (!e.getMessage().contains("Targets list was setup by addTarget with [" + shock + "], but not used")) {
                Assert.fail("must throw error about bad targets, but got:\n" + e.getMessage());
            }
            return;
        }
        Assert.fail("must not be able to target a card in another player's graveyard");
    }
}
