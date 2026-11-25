package org.mage.test.cards.single.mir;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Lion's Eye Diamond {0}
 * Artifact
 * Discard your hand, Sacrifice Lion's Eye Diamond: Add three mana of any one color. Activate only as an instant.
 * <p>
 * "Activate only as an instant" means it can't be activated while casting a spell (rule 605.3a only allows that for
 * mana abilities without such a restriction), so its mana must be added before the spell is cast, and by then the hand
 * is discarded. A card in hand can't be cast with that mana, but a card in another zone can.
 */
public class LionsEyeDiamondPlayableTest extends CardTestPlayerBase {

    @Test
    public void test_CardInHandNotPlayableWithDiamondMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.HAND, playerA, "Lightning Bolt"); // {R}

        checkPlayableAbility("can't cast from hand with the Diamond", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Lightning Bolt", false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_CardInHandPlayableWithOtherMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");
        addCard(Zone.HAND, playerA, "Lightning Bolt"); // {R}

        checkPlayableAbility("can cast with the Mountain", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Lightning Bolt", true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_CardInGraveyardPlayableWithDiamondMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        // Flashback {2}{R}
        addCard(Zone.GRAVEYARD, playerA, "Faithless Looting");

        checkPlayableAbility("can flashback with the Diamond", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Flashback", true);
        checkPlayableAbility("can't cast from hand with the Diamond", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Lightning Bolt", false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_CardInRevealedHandNotPlayableWithDiamondMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        // Your opponents play with their hands revealed.
        addCard(Zone.BATTLEFIELD, playerB, "Telepathy");

        checkPlayableAbility("can't cast from revealed hand with the Diamond", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Lightning Bolt", false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_CardInHandNotPlayableIfOtherManaIsNotEnough() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain");
        addCard(Zone.HAND, playerA, "Arc Lightning"); // {2}{R}

        checkPlayableAbility("the Diamond can't add to the Mountain", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Arc Lightning", false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_CardInHandPlayableWithAlternativeCost() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");
        // If you control a Swamp, you may pay 4 life rather than pay Snuff Out's mana cost.
        addCard(Zone.HAND, playerA, "Snuff Out"); // {3}{B}
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");

        checkPlayableAbility("can pay life instead", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Snuff Out", true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_AbilityFromHandNotPlayableWithDiamondMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        // Cycling {U}
        addCard(Zone.HAND, playerA, "Lonely Sandbar");

        checkPlayableAbility("can't cycle with the Diamond", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cycling", false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_PermanentAbilityPlayableWithDiamondMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        // Equip {1}
        addCard(Zone.BATTLEFIELD, playerA, "Bonesplitter");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");

        checkPlayableAbility("can equip with the Diamond", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Equip", true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_CardInOpponentsHandPlayableWithDiamondMana() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        // At the beginning of your upkeep, choose target opponent. This turn, that player can't cast spells or
        // activate abilities and plays with their hand revealed. You may play cards from that player's hand this turn.
        addCard(Zone.BATTLEFIELD, playerA, "Sen Triplets");
        addCard(Zone.HAND, playerB, "Lightning Bolt");

        addTarget(playerA, playerB);
        checkPlayableAbility("the Diamond discards only its controller's hand", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Lightning Bolt", true);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();
    }

    @Test
    public void test_DiamondManaInPoolCanCastFromGraveyard() {
        addCard(Zone.BATTLEFIELD, playerA, "Lion's Eye Diamond");
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        // Flashback {2}{R}
        addCard(Zone.GRAVEYARD, playerA, "Faithless Looting");

        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Discard your hand");
        setChoice(playerA, "Red");
        checkPlayableAbility("can flashback with mana in pool", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Flashback", true);
        checkPlayableAbility("hand was discarded", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Lightning Bolt", false);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Lightning Bolt", 1);
        assertGraveyardCount(playerA, "Lion's Eye Diamond", 1);
    }
}
