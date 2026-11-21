

package org.mage.test.cards.rules;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 *
 * @author LevelX2
 */

public class ActivateAbilityOnlyLimitedTimesTest extends CardTestPlayerBase {

    /**
     * Wall of Roots    {1}{G}
     * Creature - Plant Wall
     * 0/5
     * Defender
     * Put a -0/-1 counter on Wall of Roots: Add {G}. Activate this ability only once each turn.
     *
     */
    @Test
    public void testAbilityCanBeActivatedTwice() {

        addCard(Zone.HAND, playerA, "Runeclaw Bear");
        addCard(Zone.BATTLEFIELD, playerA, "Wall of Roots", 2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Runeclaw Bear");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, "Wall of Roots", 0, 4);
        assertPermanentCount(playerA, "Runeclaw Bear", 1);

        assertLife(playerA, 20);
        assertLife(playerB, 20);

    }

    @Test
    public void testAbilityCantBeActivatedTwice() {

        addCard(Zone.HAND, playerA, "Garruk's Companion");
        addCard(Zone.BATTLEFIELD, playerA, "Wall of Roots",2);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Garruk's Companion");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, "Wall of Roots", 0, 4);
        assertHandCount(playerA, "Garruk's Companion", 0);
        assertPermanentCount(playerA, "Garruk's Companion", 1);

        assertLife(playerA, 20);
        assertLife(playerB, 20);

    }

    /**
     * Momentary Blink {1}{W}
     * Instant
     * Exile target creature you control, then return it to the battlefield under its owner's control.
     * Flashback (You may cast this card from your graveyard for its flashback cost. Then exile it.)
     */
    @Test
    public void testAbilityCanBeActivatedTwiceIfBlinked() {

        addCard(Zone.HAND, playerA, "Wall of Wood",2);
        addCard(Zone.HAND, playerA, "Momentary Blink");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Wall of Roots");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wall of Wood");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Momentary Blink", "Wall of Roots", "Cast Wall of Wood");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wall of Wood");
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPowerToughness(playerA, "Wall of Roots", 0, 4);
        assertHandCount(playerA, "Momentary Blink", 0);
        assertPermanentCount(playerA, "Wall of Wood", 2);

        
        assertLife(playerA, 20);
        assertLife(playerB, 20);

    }

    @Test
    public void test_MaxActivationsIncrease() {
        // Each power-up ability of permanents you control can be activated an additional time.
        // Power-up -- {5}{R}{R}: Put two +1/+1 counters on Wonder Man.
        addCard(Zone.HAND, playerA, "Wonder Man, Hollywood Hero",1); // {3}{R}{R}
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 5 + 7);
        //
        // Power-up -- {5}{U}: Put three +1/+1 counters on this creature.
        addCard(Zone.BATTLEFIELD, playerA, "Aerial Doombot",1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 6 * 3);

        // once per game
        checkPlayableAbility("t1 - can activate due first usage", 1,  PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}", true);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, playerA);
        checkPlayableAbility("t1 - can't activate due x1 usage", 1,  PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}", false);

        // increase to x2 per game
        checkPlayableAbility("t3 - can't activate due x1 usage", 3,  PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}", false);
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Wonder Man, Hollywood Hero");
        waitStackResolved(3, PhaseStep.PRECOMBAT_MAIN, playerA);
        checkPlayableAbility("t3 - can activate due limit icrease", 3,  PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}", true);
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}");
        waitStackResolved(3, PhaseStep.PRECOMBAT_MAIN, playerA);
        checkPlayableAbility("t3 - can't activate due x2 usage", 3,  PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}", false);

        checkPlayableAbility("t5 - can't activate due x2 usage", 5,  PhaseStep.PRECOMBAT_MAIN, playerA, "Power-up &mdash; {5}{U}", false);
        
        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.END_TURN);
        execute();
    }

    /**
     * An activation limit applies only to the ability as acquired from that object (rule 602.5c). A card that
     * left the graveyard and came back is a new object (rule 400.7), so Volrath's Shapeshifter can use Wall of
     * Roots' "Activate only once each turn" ability again.
     */
    @Test
    public void testOnceEachTurnAcquiredAgainFromNewObject() {
        // As long as the top card of your graveyard is a creature card, Volrath's Shapeshifter has the full text
        // of that card and has the text "{2}: Discard a card."
        addCard(Zone.BATTLEFIELD, playerA, "Volrath's Shapeshifter");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");
        // Flashback-Sacrifice a creature.
        addCard(Zone.GRAVEYARD, playerA, "Cabal Therapy");
        // Defender
        // Put a -0/-1 counter on Wall of Roots: Add {G}. Activate only once each turn.
        addCard(Zone.GRAVEYARD, playerA, "Wall of Roots");
        addCard(Zone.HAND, playerA, "Unearth"); // {B}
        addCard(Zone.HAND, playerB, "Lightning Bolt");

        // a +1/+1 counter keeps the Shapeshifter alive while it has no form (0/1 with a -0/-1 counter)
        addCounters(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wall of Roots", CounterType.P1P1, 1);
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Put a -0/-1 counter");
        checkPermanentCounters("first activation", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wall of Roots", CounterType.M0M1, 1);

        // Wall of Roots leaves the graveyard and comes back as a new object
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Unearth", "Wall of Roots");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPT("no form, Unearth on top", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Volrath's Shapeshifter", 1, 1);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Flashback", playerB);
        setChoice(playerA, "Wall of Roots"); // sacrifice the returned Wall of Roots
        setChoice(playerA, "Lightning Bolt");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPT("Wall of Roots form again", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Wall of Roots", 1, 5);

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Put a -0/-1 counter");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertCounterCount(playerA, "Wall of Roots", CounterType.M0M1, 2);
        assertPowerToughness(playerA, "Wall of Roots", 1, 4);
    }
}