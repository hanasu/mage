package mage.abilities.keyword;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.costs.Cost;
import mage.abilities.costs.Costs;
import mage.abilities.costs.CostsImpl;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.keyword.EchoEffect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;

/**
 * @author Backfir3
 */
public class EchoAbility extends TriggeredAbilityImpl {

    protected Costs<Cost> echoCosts = new CostsImpl<>();
    private boolean manaEcho = true;
    private DynamicValue amount;
    private String rule;

    public EchoAbility(String manaString) {
        super(Zone.BATTLEFIELD, new EchoEffect(new ManaCostsImpl<>(manaString)), false);
        this.echoCosts.add(new ManaCostsImpl<>(manaString));
        this.rule = null;
    }

    public EchoAbility(DynamicValue amount, String rule) {
        super(Zone.BATTLEFIELD, new EchoEffect(amount), false);
        this.amount = amount;
        this.echoCosts.add(getCosts());
        this.manaEcho = true;
        this.rule = rule;
    }

    public EchoAbility(Cost echoCost) {
        super(Zone.BATTLEFIELD, new EchoEffect(echoCost), false);
        this.echoCosts.add(echoCost);
        this.manaEcho = false;
        this.rule = null;
    }

    protected EchoAbility(final EchoAbility ability) {
        super(ability);
        this.echoCosts = ability.echoCosts.copy();
        this.manaEcho = ability.manaEcho;
        this.amount = ability.amount;
        this.rule = ability.rule;
    }

    @Override
    public EchoAbility copy() {
        return new EchoAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.UPKEEP_STEP_PRE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        // rule 702.30a: "if this permanent came under your control since the beginning of your last upkeep", that
        // is, it wasn't under your control when your previous upkeep step began (additional upkeep steps count).
        // A property of the permanent, not of this ability object: an echo ability the permanent acquires later
        // (Volrath's Shapeshifter) is new.
        if (!event.getPlayerId().equals(this.controllerId)) {
            return false;
        }
        Permanent permanent = game.getPermanent(this.getSourceId());
        return permanent != null && permanent.getControllerUpkeepsBegun() < 2;
    }

    @Override
    public String getRule() {
        StringBuilder sb = new StringBuilder();
        if (rule != null) {
            sb.append(rule);
        } else {
            sb = new StringBuilder("Echo");
            if (manaEcho) {
                sb.append(' ');
            } else {
                sb.append("&mdash;");
            }
            if (echoCosts != null) {
                sb.append(echoCosts.getText());
            }
            if (!manaEcho) {
                sb.append('.');
            }
        }
        sb.append(" <i>(At the beginning of your upkeep, if this came under your control since the beginning of your last upkeep, sacrifice it unless you pay its echo cost.)</i>");
        return sb.toString();
    }
}

