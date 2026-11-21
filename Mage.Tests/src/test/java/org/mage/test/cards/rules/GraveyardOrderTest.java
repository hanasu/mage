package org.mage.test.cards.rules;

import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.effects.OneShotEffect;
import mage.cards.Card;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.Game;
import mage.players.Player;
import org.junit.Assert;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cards that go to the graveyard at the same time: the owner chooses their order (if askMoveToGraveOrder is set)
 *
 * @author hanasu
 */
public class GraveyardOrderTest extends CardTestPlayerBase {

    @Test
    public void test_CardsFromBattlefield() {
        addCustomCardWithAbility("put to graveyard", playerA, new SimpleActivatedAbility(
                new PutCardsIntoGraveyardTestEffect("Grizzly Bears", "Ornithopter"), new TapSourceCost()));
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Ornithopter");
        playerA.getUserData().setAskMoveToGraveOrder(true);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Put");
        addTarget(playerA, "Ornithopter"); // last chosen will be on top

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardOrder(playerA, "Ornithopter", "Grizzly Bears");
    }

    @Test
    public void test_CardsFromDifferentZones() {
        addCustomCardWithAbility("put to graveyard", playerA, new SimpleActivatedAbility(
                new PutCardsIntoGraveyardTestEffect("Lightning Bolt", "Grizzly Bears"), new TapSourceCost()));
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        playerA.getUserData().setAskMoveToGraveOrder(true);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Put");
        addTarget(playerA, "Grizzly Bears"); // last chosen will be on top

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        assertGraveyardOrder(playerA, "Grizzly Bears", "Lightning Bolt");
        assertHandCount(playerA, "Lightning Bolt", 0);
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

/**
 * Puts the controller's cards with the given names (from the battlefield or hand) into the graveyard at the same time
 */
class PutCardsIntoGraveyardTestEffect extends OneShotEffect {

    private final List<String> cardNames;

    PutCardsIntoGraveyardTestEffect(String... cardNames) {
        super(Outcome.Benefit);
        this.cardNames = Arrays.asList(cardNames);
        staticText = "Put " + String.join(" and ", cardNames) + " into your graveyard";
    }

    private PutCardsIntoGraveyardTestEffect(final PutCardsIntoGraveyardTestEffect effect) {
        super(effect);
        this.cardNames = effect.cardNames;
    }

    @Override
    public PutCardsIntoGraveyardTestEffect copy() {
        return new PutCardsIntoGraveyardTestEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        // in the given order: the first card's zone is the one the order choice used
        Set<Card> cards = new LinkedHashSet<>();
        for (String cardName : cardNames) {
            game.getBattlefield().getAllActivePermanents(source.getControllerId()).stream()
                    .filter(permanent -> permanent.getName().equals(cardName))
                    .forEach(cards::add);
            controller.getHand().getCards(game).stream()
                    .filter(card -> card.getName().equals(cardName))
                    .forEach(cards::add);
        }
        return controller.moveCards(cards, Zone.GRAVEYARD, source, game);
    }
}
