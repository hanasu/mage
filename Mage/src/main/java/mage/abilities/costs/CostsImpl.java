package mage.abilities.costs;

import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.costs.common.CyclingDiscardCost;
import mage.abilities.costs.common.DiscardHandCost;
import mage.abilities.costs.common.DiscardSourceCost;
import mage.abilities.costs.common.DiscardTargetCost;
import mage.abilities.costs.common.DiscardXTargetCost;
import mage.abilities.costs.common.ExileAttachmentCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.costs.mana.VariableManaCost;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.Outcome;
import mage.game.Game;
import mage.players.Player;
import mage.target.Targets;
import mage.util.CardUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @param <T>
 * @author BetaSteward_at_googlemail.com
 */
public class CostsImpl<T extends Cost> extends ArrayList<T> implements Costs<T> {

    protected String text = null;

    public CostsImpl() {
    }

    protected CostsImpl(final CostsImpl<T> costs) {
        this.ensureCapacity(costs.size());
        for (Cost cost : costs) {
            this.add((T) cost.copy());
        }
        this.text = costs.text;
    }

    @Override
    public UUID getId() {
        throw new RuntimeException("Not supported method");
    }

    @Override
    public CostsImpl<T> setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public String getText() {
        if (text != null) {
            return text;
        }
        if (this.isEmpty()) {
            return "";
        }

        StringBuilder sbText = new StringBuilder();
        for (T cost : this) {
            String textCost = cost.getText();
            if (textCost != null && !textCost.isEmpty()) {
                if (textCost.startsWith("and")) {
                    if (sbText.length() > 1) {
                        // Remove "," from previous cost
                        sbText.deleteCharAt(sbText.length() - 2);
                    }
                    sbText.append(textCost).append(", ");
                } else {
                    sbText.append(Character.toUpperCase(textCost.charAt(0))).append(textCost.substring(1)).append(", ");
                }
            }
        }
        if (sbText.length() > 1) {
            sbText.setLength(sbText.length() - 2);
        }
        return sbText.toString();
    }

    @Override
    public boolean canPay(Ability ability, Ability source, UUID controllerId, Game game) {
        for (T cost : this) {
            if (!cost.canPay(ability, source, controllerId, game)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean pay(Ability ability, Game game, Ability source, UUID controllerId, boolean noMana) {
        return pay(ability, game, source, controllerId, noMana, this);
    }

    @Override
    public boolean pay(Ability ability, Game game, Ability source, UUID controllerId, boolean noMana, Cost costToPay) {
        if (this.size() > 0) {
            while (!isPaid()) {
                T cost = chooseNextToPay(ability, game, controllerId);
                if (!cost.pay(ability, game, source, controllerId, noMana, costToPay)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Rule 601.2h: the player pays the costs in any order. The order shows in the graveyard when two or more of
     * the costs put cards there (Lion's Eye Diamond's "Discard your hand, Sacrifice Lion's Eye Diamond"), so then,
     * if the player orders the cards that go to their graveyard (askMoveToGraveOrder), they choose the cost to
     * pay next. Otherwise the costs are paid in their given order.
     */
    private T chooseNextToPay(Ability ability, Game game, UUID controllerId) {
        T first = getFirstUnpaid();
        List<T> graveyardCosts = new ArrayList<>();
        for (T cost : this) {
            if (!cost.isPaid() && putsCardsIntoGraveyard(cost)) {
                graveyardCosts.add(cost);
            }
        }
        Player player = game.getPlayer(controllerId);
        if (graveyardCosts.size() < 2 || player == null || player.getUserData() == null
                || !player.getUserData().askMoveToGraveOrder()) {
            return first;
        }
        MageObject sourceObject = ability == null ? null : game.getObject(ability.getSourceId());
        String sourceName = sourceObject == null ? "this" : sourceObject.getName();
        Map<String, String> choices = new LinkedHashMap<>();
        for (int i = 0; i < graveyardCosts.size(); i++) {
            choices.put(String.valueOf(i),
                    CardUtil.getTextWithFirstCharUpperCase(graveyardCosts.get(i).getText().replace("{this}", sourceName)));
        }
        Choice choice = new ChoiceImpl(true);
        choice.setMessage("Choose the cost to pay first");
        choice.setKeyChoices(choices);
        if (!player.choose(Outcome.Neutral, choice, game) || choice.getChoiceKey() == null
                || !choices.containsKey(choice.getChoiceKey())) {
            return first;
        }
        return graveyardCosts.get(Integer.parseInt(choice.getChoiceKey()));
    }

    // the costs that put cards into a graveyard: sacrifice and discard
    private static boolean putsCardsIntoGraveyard(Cost cost) {
        return (cost instanceof SacrificeCost && !(cost instanceof ExileAttachmentCost))
                || cost instanceof DiscardTargetCost || cost instanceof DiscardHandCost
                || cost instanceof DiscardSourceCost || cost instanceof DiscardXTargetCost
                || cost instanceof CyclingDiscardCost;
    }

    @Override
    public boolean isPaid() {
        for (T cost : this) {
            if (!(cost instanceof VariableManaCost) && !cost.isPaid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearPaid() {
        for (T cost : this) {
            cost.clearPaid();
        }
    }

    @Override
    public void setPaid() {
        for (T cost : this) {
            cost.setPaid();
        }
    }

    @Override
    public Costs<T> getUnpaid() {
        Costs<T> unpaid = new CostsImpl<>();
        for (T cost : this) {
            if (!cost.isPaid()) {
                unpaid.add(cost);
            }
        }
        return unpaid;
    }

    protected T getFirstUnpaid() {
        Costs<T> unpaid = getUnpaid();
        if (!unpaid.isEmpty()) {
            return unpaid.get(0);
        }
        return null;
    }

    @Override
    public List<VariableCost> getVariableCosts() {
        List<VariableCost> variableCosts = new ArrayList<>();
        for (T cost : this) {
            if (cost instanceof VariableCost) {
                variableCosts.add((VariableCost) cost);
            }
            if (cost instanceof ManaCosts) {
                variableCosts.addAll(((ManaCosts) cost).getVariableCosts());
            }
        }
        return variableCosts;
    }

    @Override
    public Targets getTargets() {
        Targets res = new Targets();
        for (T cost : this) {
            res.addAll(cost.getTargets());
        }
        return res.withReadOnly();
    }

    @Override
    public CostsImpl<T> copy() {
        return new CostsImpl(this);
    }
}
