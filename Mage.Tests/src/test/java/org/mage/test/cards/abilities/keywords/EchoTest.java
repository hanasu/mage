package org.mage.test.cards.abilities.keywords;

import mage.abilities.keyword.EchoAbility;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * @author LevelX2
 */

public class EchoTest extends CardTestPlayerBase {

    /*
     *    I flickered an Avalanche Riders with its Echo trigger on the stack with Restoration Angel.
     *    When the trigger resolved, my Riders was sacrificed, even though it should have been
     *    considered a new permanent.
     */

    @Test
    public void testEchoTriggerChecksIdentity() {
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        // Avalanche Riders  Creature - Human Nomad 2/2 {3}{R}
        // Haste
        // Echo (At the beginning of your upkeep, if this came under your control since the beginning of your last upkeep, sacrifice it unless you pay its echo cost.)
        // When Avalanche Riders enters the battlefield, destroy target land.
        addCard(Zone.HAND, playerA, "Avalanche Riders");

        addCard(Zone.BATTLEFIELD, playerA, "Plains", 4);
        // Restoration Angel  {3}{W}
        // Flash
        // Flying
        // When Restoration Angel enters the battlefield, you may exile target non-Angel creature you control,
        // then return that card to the battlefield under your control.
        addCard(Zone.HAND, playerA, "Restoration Angel");


        // cast Avalanche Riders and destroy forest
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Avalanche Riders");
        addTarget(playerA, "Forest");

        // Avalanche Riders go to echo, cast Restoration Angel to restore rider (do not apply echo with 4 mana)
        activateManaAbility(3, PhaseStep.UPKEEP, playerA, "{T}: Add {W}");
        activateManaAbility(3, PhaseStep.UPKEEP, playerA, "{T}: Add {W}");
        activateManaAbility(3, PhaseStep.UPKEEP, playerA, "{T}: Add {W}");
        activateManaAbility(3, PhaseStep.UPKEEP, playerA, "{T}: Add {W}");
        castSpell(3, PhaseStep.UPKEEP, playerA, "Restoration Angel");
        addTarget(playerA, "Avalanche Riders");
        setChoice(playerA, true); // raider do restore

        // Avalanche Riders triggered again
        addTarget(playerA, "Forest");

        // but no echo for blinked rider

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerA, 20);
        assertLife(playerB, 20);

        assertPermanentCount(playerA, "Avalanche Riders", 1);
        assertPermanentCount(playerA, "Restoration Angel", 1);

        assertPermanentCount(playerA, "Forest", 0);
        assertGraveyardCount(playerA, "Forest", 2);
        assertTappedCount("Plains", true, 4);
        assertTappedCount("Mountain", true, 0);
    }

    //Deranged Hermit has been cloned with Phantasmal Image.
    //The Phantasmal Image version of the Deranged Hermit had to pay the echo cost multiple times.
    @Test
    public void testEchoTriggerClone() {
        addCard(Zone.BATTLEFIELD, playerA, "Tropical Island", 15);
        // Deranged Hermit {3}{G}{G}
        // Echo
        addCard(Zone.HAND, playerA, "Deranged Hermit");
        addCard(Zone.HAND, playerA, "Phantasmal Image");
        addCard(Zone.HAND, playerA, "Double Major");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Deranged Hermit");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Double Major", "Deranged Hermit");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Phantasmal Image");
        setChoice(playerA, true);
        setChoice(playerA, "Deranged Hermit");

        setChoice(playerA, "Echo {3}{G}{G}", 2); // x3 triggers from Deranged Hermit

        setChoice(playerA, true); //Pay echo costs
        setChoice(playerA, true);
        setChoice(playerA, true);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertLife(playerA, 20);
        assertLife(playerB, 20);

        assertPermanentCount(playerA, "Deranged Hermit", 3);
        assertTappedCount("Tropical Island", true, 15);

    }

    /**
     * Echo triggers only if the permanent came under your control since the beginning of your last upkeep
     * (rule 702.30a). Turns are numbered for the game, so player A has turns 1, 3 and 5. Volrath's Shapeshifter is
     * cast in A's first turn and gets Karmic Guide's echo in A's second turn (turn 3). In A's third upkeep (turn 5)
     * it has been under A's control since before A's last upkeep, so echo doesn't trigger.
     */
    @Test
    public void testEchoAcquiredByPermanentControlledSinceBefore() {
        // As long as the top card of your graveyard is a creature card, Volrath's Shapeshifter has the full text
        // of that card and has the text "{2}: Discard a card."
        addCard(Zone.HAND, playerA, "Volrath's Shapeshifter"); // {1}{U}{U}
        // Flying, protection from black
        // Echo {3}{W}{W}
        // When Karmic Guide enters the battlefield, return target creature card from your graveyard to the battlefield.
        addCard(Zone.HAND, playerA, "Karmic Guide");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Volrath's Shapeshifter");
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}: Discard a card");
        setChoice(playerA, "Karmic Guide");

        // no echo trigger in turn 5 (strict mode would fail on its "pay echo?" choice)
        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Karmic Guide", 1);
        assertAbility(playerA, "Karmic Guide", new EchoAbility("{3}{W}{W}"), true);
        assertPermanentCount(playerA, "Island", 5);
    }

    /**
     * Same as above, but Volrath's Shapeshifter is cast in A's second turn (turn 3), after the upkeep: in A's next
     * upkeep (turn 5) it came under A's control since the beginning of A's last upkeep, so the echo it got from
     * Karmic Guide triggers.
     */
    @Test
    public void testEchoAcquiredByPermanentControlledSinceLastUpkeep() {
        addCard(Zone.HAND, playerA, "Volrath's Shapeshifter"); // {1}{U}{U}
        // Echo {3}{W}{W}
        addCard(Zone.HAND, playerA, "Karmic Guide");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);

        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Volrath's Shapeshifter");
        waitStackResolved(3, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(3, PhaseStep.PRECOMBAT_MAIN, playerA, "{2}: Discard a card");
        setChoice(playerA, "Karmic Guide");

        setChoice(playerA, false); // don't pay echo in turn 5

        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Karmic Guide", 0);
        assertGraveyardCount(playerA, "Volrath's Shapeshifter", 1);
    }

    /**
     * Echo triggers only once: Pouncing Jaguar cast in A's first turn, echo paid in A's next upkeep (turn 3),
     * no echo in A's upkeep after that (turn 5).
     */
    @Test
    public void testEchoTriggersOnlyOnce() {
        // Echo {G}
        addCard(Zone.HAND, playerA, "Pouncing Jaguar"); // {G}
        addCard(Zone.BATTLEFIELD, playerA, "Forest");

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Pouncing Jaguar");
        setChoice(playerA, true); // pay echo in turn 3

        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Pouncing Jaguar", 1);
    }

    /**
     * Echo paid in the first upkeep doesn't trigger again in an additional upkeep step of the same turn: the
     * permanent was under A's control since the beginning of the first upkeep (Paradox Haze ruling).
     */
    @Test
    public void testEchoNotAgainInAdditionalUpkeep() {
        // Echo {G}
        addCard(Zone.HAND, playerA, "Pouncing Jaguar"); // {G}
        // Enchant player
        // At the beginning of enchanted player's first upkeep each turn, that player gets an additional upkeep step
        // after this step.
        addCard(Zone.HAND, playerA, "Paradox Haze"); // {2}{U}
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        addCard(Zone.BATTLEFIELD, playerA, "Island", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Pouncing Jaguar");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Paradox Haze", playerA);

        setChoice(playerA, "Echo {G}"); // order of the triggers in turn 3
        setChoice(playerA, true); // pay echo

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Pouncing Jaguar", 1);
        assertTappedCount("Forest", true, 1);
    }

    /**
     * An additional beginning phase has an upkeep too: Pouncing Jaguar cast in A's second turn (turn 3) has to pay
     * echo in the upkeep of the additional beginning phase that turn, and not again in A's next turn (turn 5).
     */
    @Test
    public void testEchoInAdditionalBeginningPhase() {
        // Echo {G}
        addCard(Zone.HAND, playerA, "Pouncing Jaguar"); // {G}
        // At the beginning of your postcombat main phase, you get an additional beginning phase after this phase.
        addCard(Zone.BATTLEFIELD, playerA, "Sphinx of the Second Sun");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");

        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Pouncing Jaguar");
        setChoice(playerA, true); // pay echo in the additional upkeep of turn 3

        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Pouncing Jaguar", 1);
    }

    /**
     * Echo triggers again after a control change: B gains control of Pouncing Jaguar in turn 4 and it returns to A
     * at the end of that turn, so in A's next upkeep (turn 5) it came under A's control since A's last upkeep.
     */
    @Test
    public void testEchoAgainAfterControlChange() {
        // Echo {G}
        addCard(Zone.HAND, playerA, "Pouncing Jaguar"); // {G}
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        // Untap target creature and gain control of it until end of turn. That creature gains haste until end of turn.
        addCard(Zone.HAND, playerB, "Threaten"); // {2}{R}
        addCard(Zone.BATTLEFIELD, playerB, "Mountain", 3);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Pouncing Jaguar");
        setChoice(playerA, true); // pay echo in turn 3

        castSpell(4, PhaseStep.PRECOMBAT_MAIN, playerB, "Threaten", "Pouncing Jaguar");

        setChoice(playerA, false); // don't pay echo in turn 5

        setStrictChooseMode(true);
        setStopAt(5, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Pouncing Jaguar", 0);
        assertGraveyardCount(playerA, "Pouncing Jaguar", 1);
    }
}
