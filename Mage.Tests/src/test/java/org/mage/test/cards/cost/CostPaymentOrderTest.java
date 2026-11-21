package org.mage.test.cards.cost;

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
 * The player pays the costs in any order (rule 601.2h, 602.2b). The order shows in the graveyard when two or more
 * costs put cards there, so a player who orders the cards that go to their graveyard (askMoveToGraveOrder) chooses
 * the order of those costs.
 *
 * @author hanasu
 */
public class CostPaymentOrderTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.l.LionsEyeDiamond Lion's Eye Diamond} {0}
     * Artifact
     * Sacrifice Lion's Eye Diamond, Discard your hand: Add three mana of any one color. Activate this ability only any time you could cast an instant.
     */
    private static final String diamond = "Lion's Eye Diamond";

    @Test
    public void test_SacrificeFirst() {
        addCard(Zone.BATTLEFIELD, playerA, diamond);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        playerA.getUserData().setAskMoveToGraveOrder(true);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Discard your hand");
        setChoice(playerA, "Sacrifice Lion's Eye Diamond"); // cost to pay first
        setChoice(playerA, "Red");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardOrder(playerA, diamond, "Grizzly Bears");
    }

    @Test
    public void test_DiscardFirst() {
        addCard(Zone.BATTLEFIELD, playerA, diamond);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        playerA.getUserData().setAskMoveToGraveOrder(true);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Discard your hand");
        setChoice(playerA, "Discard your hand"); // cost to pay first
        setChoice(playerA, "Red");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardOrder(playerA, "Grizzly Bears", diamond);
    }

    @Test
    public void test_GivenOrderIfNotAsked() {
        addCard(Zone.BATTLEFIELD, playerA, diamond);
        addCard(Zone.HAND, playerA, "Grizzly Bears");

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Discard your hand");
        setChoice(playerA, "Red");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardOrder(playerA, "Grizzly Bears", diamond);
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
