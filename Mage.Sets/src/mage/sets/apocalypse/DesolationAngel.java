/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */

package mage.sets.apocalypse;

import java.util.UUID;
import mage.Constants.CardType;
import mage.Constants.Outcome;
import mage.Constants.Rarity;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.costs.mana.KickerManaCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.KickerAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.game.Game;
import mage.game.permanent.Permanent;

/**
 * @author Loki
 */
public class DesolationAngel extends CardImpl<DesolationAngel> {

    public DesolationAngel(UUID ownerId) {
        super(ownerId, 38, "Desolation Angel", Rarity.RARE, new CardType[]{CardType.CREATURE}, "{3}{B}{B}");
        this.expansionSetCode = "APC";
        this.subtype.add("Angel");
        this.color.setBlack(true);
        this.power = new MageInt(5);
        this.toughness = new MageInt(4);

        // Kicker (You may pay an additional as you cast this spell.)
        this.addAbility(new KickerAbility(new KickerManaCost("{W}{W}")));

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // When Desolation Angel enters the battlefield, destroy all lands you control. If it was kicked, destroy all lands instead.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new DesolationAngelEntersBattlefieldEffect()));
    }

    public DesolationAngel(final DesolationAngel card) {
        super(card);
    }

    @Override
    public DesolationAngel copy() {
        return new DesolationAngel(this);
    }
}

class DesolationAngelEntersBattlefieldEffect extends OneShotEffect<DesolationAngelEntersBattlefieldEffect> {
    DesolationAngelEntersBattlefieldEffect() {
        super(Outcome.DestroyPermanent);
        staticText = "destroy all lands you control. If it was kicked, destroy all lands instead";
    }

    DesolationAngelEntersBattlefieldEffect(final DesolationAngelEntersBattlefieldEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card p = game.getCard(source.getSourceId());

        boolean kicked = false;
        if (p != null) {
            for (Object cost : p.getSpellAbility().getOptionalCosts()) {
                if (cost instanceof KickerManaCost) {
                    if (((KickerManaCost) cost).isPaid()) {
                        kicked = true;
                    }
                }
            }
        }

        for (Permanent permanent : game.getBattlefield().getAllActivePermanents()) {
            if (permanent.getCardType().contains(CardType.LAND)) {
                if ((!kicked && permanent.getControllerId() == source.getControllerId()) || kicked) {
                    permanent.destroy(source.getSourceId(), game, false);
                }
            }
        }

        return true;
    }

    @Override
    public DesolationAngelEntersBattlefieldEffect copy() {
        return new DesolationAngelEntersBattlefieldEffect(this);
    }

}