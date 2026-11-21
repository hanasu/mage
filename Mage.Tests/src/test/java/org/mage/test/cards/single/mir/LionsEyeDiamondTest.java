package org.mage.test.cards.single.mir;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.players.Player;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hanasu
 */
public class LionsEyeDiamondTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.l.LionsEyeDiamond Lion's Eye Diamond} {0}
     * Artifact
     * Sacrifice Lion's Eye Diamond, Discard your hand: Add three mana of any one color. Activate this ability only any time you could cast an instant.
     */
    private static final String diamond = "Lion's Eye Diamond";

    private static final String bears = "Grizzly Bears";
    private static final String bolt = "Lightning Bolt";
    private static final String island = "Island";

    @Test
    public void test_DiscardedCardsGoToGraveyardInChosenOrder() {
        addCard(Zone.BATTLEFIELD, playerA, diamond);
        addCard(Zone.HAND, playerA, bears);
        addCard(Zone.HAND, playerA, bolt);
        addCard(Zone.HAND, playerA, island);
        playerA.getUserData().setAskMoveToGraveOrder(true);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Discard your hand");
        // last chosen will be on top
        setChoice(playerA, island);
        setChoice(playerA, bolt);
        setChoice(playerA, "Red");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardOrder(playerA, island, bolt, bears);
    }

    @Test
    public void test_DiscardedCardsKeepHandOrderIfNotAsked() {
        addCard(Zone.BATTLEFIELD, playerA, diamond);
        addCard(Zone.HAND, playerA, bears);
        addCard(Zone.HAND, playerA, bolt);
        addCard(Zone.HAND, playerA, island);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Discard your hand");
        setChoice(playerA, "Red");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardOrder(playerA, bears, bolt, island);
    }

    /**
     * Checks the order of the given cards in the player's graveyard, from bottom to top
     */
    private void assertGraveyardOrder(TestPlayer player, String... cardNames) {
        List<String> names = Arrays.asList(cardNames);
        Player realPlayer = currentGame.getPlayer(player.getId());
        List<String> graveyard = realPlayer.getGraveyard().getCards(currentGame).stream()
                .map(card -> card.getName())
                .filter(names::contains)
                .collect(Collectors.toList());
        Assert.assertEquals("graveyard order (bottom to top)", names, graveyard);
    }
}
