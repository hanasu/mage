package org.mage.test.cards.single.wth;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author hanasu
 */
public class GaeasBlessingTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.g.GaeasBlessing Gaea's Blessing} {1}{G}
     * Sorcery
     * Target player shuffles up to three target cards from their graveyard into their library.
     * Draw a card.
     * When Gaea's Blessing is put into your graveyard from your library, shuffle your graveyard into your library.
     */
    private static final String blessing = "Gaea's Blessing";
    private static final String bears = "Grizzly Bears";
    private static final String elves = "Llanowar Elves";

    @Test
    public void test_TargetPlayersGraveyard() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, blessing);
        addCard(Zone.GRAVEYARD, playerB, elves);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, blessing);
        addTarget(playerA, playerB);
        addTarget(playerA, elves);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertGraveyardCount(playerB, elves, 0);
        assertLibraryCount(playerB, elves, 1);
        assertGraveyardCount(playerA, blessing, 1);
    }

    // https://github.com/magefree/mage/issues/13363
    @Test
    public void test_CantTargetOtherPlayersGraveyard() {
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, blessing);
        addCard(Zone.GRAVEYARD, playerA, bears);
        addCard(Zone.GRAVEYARD, playerB, elves);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, blessing);
        addTarget(playerA, playerB);
        addTarget(playerA, bears); // owned by playerA, not the target player

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
