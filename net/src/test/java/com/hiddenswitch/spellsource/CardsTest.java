package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.impl.CardsImpl;
import com.hiddenswitch.spellsource.models.QueryCardsRequest;
import com.hiddenswitch.spellsource.impl.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.Test;

import java.util.List;

/**
 * Created by bberman on 1/22/17.
 */
public class CardsTest extends ServiceTest<CardsImpl> {
	@Test
	public void testQuery(TestContext context) {
		wrapSync(context, this::queryCardsSync);
	}

	private void queryCardsSync() throws SuspendExecution, InterruptedException {
		// Query for a bunch of different things and compare the values
		List<CardCatalogueRecord> commons = service.queryCards(new QueryCardsRequest()
				.withSets(CardSet.BASIC, CardSet.CLASSIC)
				.withRarity(Rarity.COMMON)).getRecords();

		int expectedCount = CardCatalogue.query(new DeckFormat()
				.withCardSets(CardSet.BASIC, CardSet.CLASSIC), c -> c.isCollectible() && c.getRarity().isRarity(Rarity.COMMON))
				.getCount();

		getContext().assertEquals(expectedCount, commons.size());
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<CardsImpl>> done) {
		CardsImpl cards = new CardsImpl();
		vertx.deployVerticle(cards, then -> done.handle(Future.succeededFuture(cards)));
	}
}
