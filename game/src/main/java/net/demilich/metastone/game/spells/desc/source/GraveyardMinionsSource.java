package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;

import java.io.Serializable;
import java.util.Objects;

public class GraveyardMinionsSource extends CardSource implements Serializable {

	public GraveyardMinionsSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		CardList deadMinions = new CardArrayList();
		player.getGraveyard()
				.stream()
				.filter(c -> c.getEntityType() == EntityType.MINION)
				.map(Entity::getSourceCard)
				.filter(Objects::nonNull)
				.forEach(deadMinions::addCard);
		return deadMinions.getCopy();
	}

}

