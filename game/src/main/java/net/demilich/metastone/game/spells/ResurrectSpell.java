package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.Zones;

public class ResurrectSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Minion> deadMinions = new ArrayList<>();
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		List<Entity> graveyard = new ArrayList<Entity>();
		graveyard.addAll(player.getGraveyard());
		for (Entity deadEntity : graveyard) {
			if (deadEntity.getEntityType() == EntityType.MINION) {
				if (cardFilter == null || cardFilter.matches(context, player, deadEntity, source)) {
					deadMinions.add((Minion) deadEntity);
				}
			}
		}
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < count; i++) {
			if (deadMinions.isEmpty()) {
				return;
			}
			Minion resurrectedMinion = context.getLogic().getRandom(deadMinions);
			MinionCard minionCard = (MinionCard) resurrectedMinion.getSourceCard();
			final Minion summonedMinion = minionCard.summon();
			final boolean summoned = context.getLogic().summon(player.getId(), summonedMinion, null, -1, false);
			if (summoned
					&& desc.containsKey(SpellArg.SPELL)
					&& summonedMinion.getZone() == Zones.BATTLEFIELD) {
				SpellUtils.castChildSpell(context, player, (SpellDesc) desc.get(SpellArg.SPELL), source, summonedMinion);
			}
			deadMinions.remove(resurrectedMinion);
		}
	}

}
