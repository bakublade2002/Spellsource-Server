package com.hiddenswitch.cluster.applications;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckCatalogue;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CommonTest {
	@Test(enabled = false)
	public void testGetDefaultDecks() throws Exception {
		assertTrue(Common.getDefaultDecks().size() > 0);
	}

	@Test(enabled = false)
	public void testAllDecksValid() throws Exception {
		CardCatalogue.loadCardsFromPackage();
		DeckCatalogue.loadDecksFromPackage();
		Common.getDefaultDecks().forEach(d -> {
			assertNotNull(DeckCatalogue.getDeckByName(d));
		});
	}
}