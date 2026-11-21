package org.mage.test.cards.single.usg;

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
public class IllGottenGainsTest extends CardTestPlayerBase {

    /**
     * {@link mage.cards.i.IllGottenGains Ill-Gotten Gains} {2}{B}{B}
     * Sorcery
     * Exile Ill-Gotten Gains. Each player discards their hand, then returns up to three cards from their graveyard to their hand.
     */
    private static final String gains = "Ill-Gotten Gains";

    @Test
    public void test_EachPlayerChoosesOrderOfOwnDiscardedCards() {
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 4);
        addCard(Zone.HAND, playerA, gains);
        addCard(Zone.HAND, playerA, "Grizzly Bears");
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        addCard(Zone.HAND, playerB, "Island");
        addCard(Zone.HAND, playerB, "Forest");
        playerA.getUserData().setAskMoveToGraveOrder(true);
        playerB.getUserData().setAskMoveToGraveOrder(true);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, gains);
        // last chosen will be on top
        setChoice(playerA, "Lightning Bolt");
        setChoice(playerB, "Forest");
        // return nothing
        setChoice(playerA, TestPlayer.CHOICE_SKIP);
        setChoice(playerB, TestPlayer.CHOICE_SKIP);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertExileCount(playerA, gains, 1);
        assertGraveyardOrder(playerA, "Lightning Bolt", "Grizzly Bears");
        assertGraveyardOrder(playerB, "Forest", "Island");
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
