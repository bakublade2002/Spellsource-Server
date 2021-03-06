package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;

import java.io.Serializable;

public class DeckSource extends CardSource implements Serializable {

	public DeckSource(SourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Player player) {
		return player.getDeck();
	}

}
