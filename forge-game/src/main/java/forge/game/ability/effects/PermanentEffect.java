package forge.game.ability.effects;

import com.google.common.collect.Lists;

import forge.game.Game;
import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CardZoneTable;
import forge.game.card.CounterType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

public class PermanentEffect extends SpellAbilityEffect {

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.abilityfactory.SpellEffect#resolve(forge.card.spellability.
     * SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        final Card host = sa.getHostCard();
        final Player player = sa.getActivatingPlayer();
        final Game game = host.getGame();
        CardZoneTable table = new CardZoneTable();
        ZoneType previousZone = host.getZone().getZoneType();

        host.setController(sa.getActivatingPlayer(), 0);

        // Alternate Costs entering With Counters
        if (sa.hasParam("WithCountersType")) {
            CounterType cType = CounterType.getType(sa.getParam("WithCountersType"));
            int cAmount = AbilityUtils.calculateAmount(host, sa.getParamOrDefault("WithCountersAmount", "1"), sa);
            host.addEtbCounter(cType, cAmount, player);
        }

        final Card c = game.getAction().moveToPlay(host, sa);
        sa.setHostCard(c);

        // some extra for Dashing
        if (sa.isDash() && c.isInPlay()) {
            c.setSVar("EndOfTurnLeavePlay", "Dash");
            registerDelayedTrigger(sa, "Hand", Lists.newArrayList(c));
        }

        ZoneType newZone = c.getZone().getZoneType();
        if (newZone != previousZone) {
            table.put(previousZone, newZone, c);
        }
        table.triggerChangesZoneAll(game);
    }
}
