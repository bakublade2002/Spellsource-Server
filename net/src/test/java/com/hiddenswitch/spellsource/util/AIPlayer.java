package com.hiddenswitch.spellsource.util;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.gameconfig.PlayerConfig;

public class AIPlayer extends Player {
	private transient Deck configuredDeck;
	private String userId;

	public AIPlayer() {
		this(DeckFactory.getRandomDeck());
	}

	public AIPlayer(Deck deck) {
		super();
		setConfiguredDeck(deck);
		buildFromConfig(new PlayerConfig(deck, new AI()));
	}

	public Deck getConfiguredDeck() {
		return configuredDeck;
	}

	public void setConfiguredDeck(Deck configuredDeck) {
		this.configuredDeck = configuredDeck;
	}

	public AIPlayer withConfiguredDeck(Deck configuredDeck) {
		setConfiguredDeck(configuredDeck);
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public AIPlayer withUserId(final String userId) {
		this.userId = userId;
		return this;
	}
}
