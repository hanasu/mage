package mage.target.common;

import mage.abilities.Ability;
import mage.cards.Card;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Cards in the graveyard of the player chosen by another target of the same ability
 * (example: "target player shuffles up to three target cards from their graveyard into their library")
 *
 * @author TheElk801
 */
public class TargetCardInTargetPlayersGraveyard extends TargetCardInGraveyard {

    private final int targetPlayerIndex;

    public TargetCardInTargetPlayersGraveyard(int targets) {
        this(targets, 0);
    }

    public TargetCardInTargetPlayersGraveyard(int targets, int targetPlayerIndex) {
        super(0, targets, StaticFilters.FILTER_CARD);
        this.targetPlayerIndex = targetPlayerIndex;
    }

    private TargetCardInTargetPlayersGraveyard(final TargetCardInTargetPlayersGraveyard target) {
        super(target);
        this.targetPlayerIndex = target.targetPlayerIndex;
    }

    /**
     * Player chosen by the player target, or null if it isn't chosen yet
     */
    private UUID getTargetPlayerId(Ability source) {
        if (source == null || source.getTargets().size() <= targetPlayerIndex) {
            return null;
        }
        return source.getTargets().get(targetPlayerIndex).getFirstTarget();
    }

    private boolean isOwnedByTargetPlayer(UUID id, Ability source, Game game) {
        Card card = game.getCard(id);
        return card != null && card.isOwnedBy(getTargetPlayerId(source));
    }

    @Override
    public boolean canTarget(UUID id, Ability source, Game game) {
        return super.canTarget(id, source, game) && isOwnedByTargetPlayer(id, source, game);
    }

    @Override
    public boolean canTarget(UUID playerId, UUID id, Ability source, Game game) {
        return super.canTarget(playerId, id, source, game) && isOwnedByTargetPlayer(id, source, game);
    }

    @Override
    public Set<UUID> possibleTargets(UUID sourceControllerId, Ability source, Game game) {
        UUID targetPlayerId = getTargetPlayerId(source);
        if (targetPlayerId == null) {
            // player not chosen yet (example: playable check), so any graveyard can be used
            return super.possibleTargets(sourceControllerId, source, game);
        }
        Set<UUID> possibleTargets = new HashSet<>();
        Player targetPlayer = game.getPlayer(targetPlayerId);
        if (targetPlayer != null) {
            for (Card card : targetPlayer.getGraveyard().getCards(filter, sourceControllerId, source, game)) {
                possibleTargets.add(card.getId());
            }
        }
        return keepValidPossibleTargets(possibleTargets, sourceControllerId, source, game);
    }

    @Override
    public TargetCardInTargetPlayersGraveyard copy() {
        return new TargetCardInTargetPlayersGraveyard(this);
    }
}
