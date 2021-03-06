package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class CavernsBelowTrigger extends BeforeMinionPlayedTrigger {

	public CavernsBelowTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		boolean minionPlayed = super.fire(event, host);
		if (!minionPlayed) {
			return false;
		}

		BeforeSummonEvent summonEvent = (BeforeSummonEvent) event;
		GameContext context = event.getGameContext();
		EnvironmentEntityList list = EnvironmentEntityList.getList(context);
		list.add(host, summonEvent.getSource());
		// We're going to store the most recent max in the conditional attack bonus attribute
		// on our enchantment
		int max = (int) host.getAttributes().getOrDefault(Attribute.RESERVED_INTEGER_1, 0);
		Map<EntityReference, Entity> entities = context.getEntities().collect(toMap(Entity::getReference, Function.identity()));
		Map<String, Long> counts = list.getReferences(context, host)
				.stream()
				.map(entities::get)
				.map(Entity::getSourceCard)
				.collect(groupingBy(Card::getName, Collectors.counting()));
		int newMax = counts.entrySet().stream()
				.max(Comparator.comparingLong(Map.Entry::getValue))
				.orElseThrow(RuntimeException::new).getValue().intValue();
		if (newMax > max) {
			host.getAttributes().put(Attribute.RESERVED_INTEGER_1, newMax);
			return true;
		} else {
			return false;
		}
	}
}
