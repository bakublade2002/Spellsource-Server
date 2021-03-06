package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class AndFilter extends EntityFilter {

	public static AndFilter create() {
		return new AndFilter(new FilterDesc(FilterDesc.build(AndFilter.class)));
	}

	public AndFilter(FilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityFilter[] filters = (EntityFilter[]) desc.get(FilterArg.FILTERS);
		if (filters == null) {
			return true;
		}
		for (EntityFilter filter : filters) {
			if (!filter.matches(context, player, entity, host)) {
				return false;
			}
		}
		return true;
	}

}
