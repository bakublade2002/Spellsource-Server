package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.*;
import com.hiddenswitch.spellsource.impl.util.InventoryRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.AfterPhysicalAttackEvent;
import net.demilich.metastone.game.events.BeforeSummonEvent;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Created by bberman on 1/31/17.
 */
public class LogicTest extends ServiceTest<LogicImpl> {
	private AccountsImpl accounts;
	private CardsImpl cards;
	private InventoryImpl inventory;
	private DecksImpl decks;
	private ClusteredGamesImpl games;
	private GatewayImpl gateway;

	@Test
	@Suspendable
	public void testStartsGame(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::startsGameSync);
	}

	public static InitializeUserResponse initializeUserId(Logic service, String userId) throws SuspendExecution,
			InterruptedException {
		return service.initializeUser(new InitializeUserRequest(userId).withUserId(userId));
	}

	private void startsGameSync() throws SuspendExecution, InterruptedException {
		// Create two players

		CreateAccountResponse player1 = accounts.createAccount("a@b.com", "a", "1");
		CreateAccountResponse player2 = accounts.createAccount("b@b.com", "b", "2");

		final String userId1 = player1.getUserId();
		final String userId2 = player2.getUserId();

		List<String> deckIds = new ArrayList<>();

		// Build two random decks
		for (String userId : new String[]{userId1, userId2}) {
			initializeUserId(service, userId);
			DeckCreateResponse deckCreateResponse = DeckTest.createDeckForUserId(inventory, decks, userId);
			deckIds.add(deckCreateResponse.getDeckId());
		}

		StartGameResponse response = service.startGame(new StartGameRequest().withPlayers(new StartGameRequest.Player
				().withUserId(userId1).withId(0).withDeckId(deckIds.get(0)), new StartGameRequest.Player().withUserId
				(userId2).withId(1).withDeckId(deckIds.get(1))));

		for (StartGameResponse.Player player : response.getPlayers()) {
			player.getDeck().getCards().toList().forEach(c -> {
				final String cardInstanceId = (String) c.getAttribute(Attribute.CARD_INVENTORY_ID);
				getContext().assertNotNull(cardInstanceId);
			});
		}
	}

	@Test
	@Suspendable
	public void testCreatesInventory(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::createInventorySync);
	}

	@Test
	@Suspendable
	public void testHandleGameEvent(TestContext context) {
		context.async().complete();
	}

	@Suspendable
	private void createInventorySync() throws SuspendExecution, InterruptedException {
		final Method awaitFiber;
		try {
			awaitFiber = Sync.class.getMethod("awaitFiber", Consumer.class);
		} catch (NoSuchMethodException e) {
			getContext().fail(e);
			return;
		}
		getContext().assertTrue(Arrays.stream(awaitFiber.getAnnotations()).anyMatch(a -> a.annotationType().equals
				(Suspendable.class)));
		getContext().assertTrue(Fiber.isCurrentFiber());

		CreateAccountResponse createAccountResponse = accounts.createAccount("benjamin.s.berman@gmail.com",
				"testpass", "doctorpangloss");
		final String userId = createAccountResponse.getUserId();

		service.initializeUser(new InitializeUserRequest(userId).withUserId(userId));

		GetCollectionResponse response = inventory.getCollection(GetCollectionRequest.user(userId));

		Set<String> cardIds = response.getInventoryRecords().stream().map(r -> r.getCardDesc().id).collect(
				toSet());

		// Get the starting decks distinct card IDs
		Set<String> actualCardIds = Spellsource.spellsource().getStandardDecks().stream().flatMap(d -> d.getCardIds().stream()).collect(toSet());

		getContext().assertTrue(cardIds.equals(actualCardIds), "The user's initial collection should contain all the cards they need in the starter decks.");
	}

	@Test
	public void testAllianceCardExtensionsDontBreak(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::allianceCardExtensionsDontBreak);
	}

	private void allianceCardExtensionsDontBreak() throws SuspendExecution, InterruptedException {
		// Create the users
		final CreateAccountResponse car1 = accounts.createAccount("a@b.com", "123567", "abfdsc");
		String userId1 = car1.getUserId();
		final CreateAccountResponse car2 = accounts.createAccount("a@c.com", "1235688", "abde");
		String userId2 = car2.getUserId();

		// Initialize them
		InitializeUserResponse userResponse1 = service.initializeUser(new InitializeUserRequest(userId1));
		InitializeUserResponse userResponse2 = service.initializeUser(new InitializeUserRequest(userId2));

		// Give a player the forever post doc card
		AddToCollectionResponse minionResponse = inventory.addToCollection(new AddToCollectionRequest().withUserId
				(userId1).withCardIds(Collections.singletonList("minion_the_forever_postdoc")));

		String foreverPostdocInventoryId = minionResponse.getInventoryIds().get(0);

		String gameId = "g";

		// Start a new alliance
		// TODO: This should be its own method

		final String allianceId = "a1";
		// Create the alliance
		inventory.createCollection(CreateCollectionRequest.alliance(allianceId, userId1, Collections.emptyList(),
				Collections.emptyList()));

		// Donate the card to the alliance
		inventory.donateToCollection(new DonateToCollectionRequest(allianceId, Collections.singletonList
				(foreverPostdocInventoryId)));

		// Borrow it from the collection
		inventory.borrowFromCollection(new BorrowFromCollectionRequest().withUserId(userId1).withCollectionId
				(allianceId).withInventoryIds(Collections.singletonList(foreverPostdocInventoryId)));

		// Create a 29 card deck then add Forever Post Doc
		List<String> inventoryIds = Stream.concat(userResponse1.getCreateCollectionResponse().getCreatedInventoryIds()
				.stream().limit(29), Stream.of(foreverPostdocInventoryId)).collect(Collectors.toList());

		DeckCreateResponse dcr1 = decks.createDeck(new DeckCreateRequest().withName("d1").withHeroClass(HeroClass
				.RED).withUserId(userId1).withInventoryIds(inventoryIds));

		// Create a 30 card deck
		DeckCreateResponse dcr2 = decks.createDeck(new DeckCreateRequest().withName("d2").withHeroClass(HeroClass
				.VIOLET).withUserId(userId2).withCardIds(Collections.nCopies(30, "minion_novice_engineer")));

		StartGameResponse sgr = service.startGame(new StartGameRequest().withGameId(gameId).withPlayers(new
				StartGameRequest.Player().withId(0).withUserId(userId1).withDeckId(dcr1.getDeckId()), new
				StartGameRequest.Player().withId(1).withUserId(userId2).withDeckId(dcr2.getDeckId())));

		CreateGameSessionResponse cgsr = games.createGameSession(new CreateGameSessionRequest().withGameId(gameId)
				.withPregame1(sgr.getPregamePlayerConfiguration1()).withPregame2(sgr.getPregamePlayerConfiguration2
						()));

		getContext().assertNotNull(cgsr.gameId);

		UnityClient client1 = new UnityClient(getContext(), car1.getLoginToken().getToken());
		UnityClient client2 = new UnityClient(getContext(), car2.getLoginToken().getToken());

		client1.play();
		client2.play();
		client2.waitUntilDone();

		// Assert that there are minions who recorded some stats
		getContext().assertTrue(inventory.getCollection(GetCollectionRequest.user(userId1)).getInventoryRecords()
				.stream().anyMatch(ir -> ir.getPersistentAttribute(Attribute.UNIQUE_CHAMPION_IDS_SIZE, 0) > 0));
		final boolean inventory1 = inventory.getCollection(GetCollectionRequest.user(userId1)).getInventoryRecords()
				.stream().anyMatch(ir -> ir.getPersistentAttribute(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID) !=
						null);
		final boolean inventory2 = inventory.getCollection(GetCollectionRequest.user(userId2)).getInventoryRecords()
				.stream().anyMatch(ir -> ir.getPersistentAttribute(Attribute.LAST_MINION_DESTROYED_CARD_ID) != null);
		getContext().assertTrue(inventory1 || inventory2);
	}

	@Test
	public void testFirstTimePlaysStatistic(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::firstTimePlaysStatistic);
	}

	private void firstTimePlaysStatistic() throws SuspendExecution, InterruptedException {
		CreateCollectionResponse car = inventory.createCollection(CreateCollectionRequest.startingCollection("1"));
		AddToCollectionResponse atcr = inventory.addToCollection(new AddToCollectionRequest().withUserId("1")
				.withCardIds(Collections.singletonList("minion_the_forever_postdoc")));
		String foreverPostdocInventoryId = atcr.getInventoryIds().get(0);
		EventLogicRequest<BeforeSummonEvent> elr = new EventLogicRequest<>();
		elr.setCardInventoryId(foreverPostdocInventoryId);
		elr.setUserId("1");
		elr.setGameId("g1");
		elr.setEntityId(1);
		GetCollectionResponse gcr = inventory.getCollection(new GetCollectionRequest().withUserId("1"));
		InventoryRecord record = gcr.getInventoryRecords().stream().filter(p -> Objects.equals(p.getId(),
				foreverPostdocInventoryId)).findFirst().get();
		MinionCard foreverPostdocCard = (MinionCard) Logic.getDescriptionFromRecord(record, "1", "d1")
				.createInstance();
		Minion foreverPostdoc = foreverPostdocCard.summon();
		foreverPostdoc.setId(1);
		elr.setEvent(new BeforeSummonEvent(null, foreverPostdoc.clone(), null));
		LogicResponse lr1 = service.beforeSummon(elr);

		getContext().assertTrue(lr1.getEntityIdsAffected().contains(1));
		getContext().assertTrue(lr1.getGameIdsAffected().contains("g1"));
		getContext().assertTrue(lr1.getModifiedAttributes().containsKey(new EntityReference(1)));
		getContext().assertTrue(lr1.getModifiedAttributes().get(new EntityReference(1)).containsKey(Attribute
				.UNIQUE_CHAMPION_IDS_SIZE));
		getContext().assertEquals(1, lr1.getModifiedAttributes().get(new EntityReference(1)).get(Attribute
				.UNIQUE_CHAMPION_IDS_SIZE));

		// Modify the minion
		foreverPostdoc.setAttribute(Attribute.UNIQUE_CHAMPION_IDS_SIZE, lr1.getModifiedAttributes().get(new
				EntityReference(foreverPostdoc.getId())).get(Attribute.UNIQUE_CHAMPION_IDS_SIZE));

		// Try the call again
		// I happen to know that we only need the minion for this event
		elr.setEvent(new BeforeSummonEvent(null, foreverPostdoc.clone(), null));
		LogicResponse lr2 = service.beforeSummon(elr);

		// Should not have changed
		getContext().assertTrue(lr2.getModifiedAttributes().isEmpty());

		// Check that the database updated
		gcr = inventory.getCollection(new GetCollectionRequest().withUserId("1"));
		record = gcr.getInventoryRecords().stream().filter(p -> Objects.equals(p.getId(), foreverPostdocInventoryId))
				.findFirst().get();
		// Different player
		foreverPostdocCard = (MinionCard) Logic.getDescriptionFromRecord(record, "2", "d1").createInstance();
		foreverPostdoc = foreverPostdocCard.summon();
		foreverPostdoc.setId(42);
		getContext().assertEquals(1, foreverPostdoc.getAttribute(Attribute.UNIQUE_CHAMPION_IDS_SIZE));

		// Try the call with a different player
		elr = new EventLogicRequest<>();
		elr.setEntityId(42);
		elr.setCardInventoryId(foreverPostdocInventoryId);
		elr.setUserId("2");
		elr.setGameId("g1");
		elr.setEvent(new BeforeSummonEvent(null, foreverPostdoc.clone(), null));
		LogicResponse lr3 = service.beforeSummon(elr);

		// Should have changed because we used a different user ID
		getContext().assertEquals(2, lr3.getModifiedAttributes().get(new EntityReference(42)).get(Attribute
				.UNIQUE_CHAMPION_IDS_SIZE));
	}

	@Test
	public void testLastMinionDestroyed(TestContext context) {
		wrapSync(context, this::lastMinionDestroyed);
	}

	private void lastMinionDestroyed() throws SuspendExecution, InterruptedException {
		String sourcingSpecialistInventoryId = createCardAndUser("minion_sourcing_specialist", "userId1");
		Minion sourcingSpecialist = createMinionFromId(sourcingSpecialistInventoryId, 1, "userId1", "deckId1");

		getContext().assertNull(sourcingSpecialist.getAttribute(Attribute.LAST_MINION_DESTROYED_CARD_ID));
		getContext().assertNull(sourcingSpecialist.getAttribute(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID));

		String opponentCardInventoryId = createCardAndUser("minion_wisp", "opponentUserId");
		Minion opponentMinion = createMinionFromId(opponentCardInventoryId, 2, "opponentUserId", "deckId2");
		opponentMinion.setAttribute(Attribute.DESTROYED);
		EventLogicRequest<AfterPhysicalAttackEvent> logicRequest = new EventLogicRequest<>();

		AfterPhysicalAttackEvent event = new AfterPhysicalAttackEvent(null, sourcingSpecialist, opponentMinion, 6);
		logicRequest.setEvent(event);
		logicRequest.setCardInventoryId(sourcingSpecialistInventoryId);
		logicRequest.setEntityId(1);
		logicRequest.setGameId("g1");
		logicRequest.setUserId("userId1");

		LogicResponse logicResponse = service.afterPhysicalAttack(logicRequest);

		getContext().assertEquals("minion_wisp", logicResponse.getModifiedAttributes().get(new EntityReference(1)).get
				(Attribute.LAST_MINION_DESTROYED_CARD_ID));
		getContext().assertEquals(opponentCardInventoryId, logicResponse.getModifiedAttributes().get(new
				EntityReference(1)).get(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID));

		// Create the minion again and assert that it recorded the right card destroyed attributes
		Minion newSourcingSpecialist = createMinionFromId(sourcingSpecialistInventoryId, 42, "userId1", "deckId1");
		getContext().assertEquals("minion_wisp", newSourcingSpecialist.getAttribute(Attribute
				.LAST_MINION_DESTROYED_CARD_ID));
		getContext().assertEquals(opponentCardInventoryId, newSourcingSpecialist.getAttribute(Attribute
				.LAST_MINION_DESTROYED_INVENTORY_ID));

		String stonetuskBoarInventoryId = addCardForUser("minion_stonetusk_boar", "opponentUserId");
		Minion stonetuskBoar = createMinionFromId(stonetuskBoarInventoryId, 43, "opponentUserId", "deckId2");
		stonetuskBoar.setAttribute(Attribute.DESTROYED);

		logicRequest = new EventLogicRequest<>();

		event = new AfterPhysicalAttackEvent(null, newSourcingSpecialist, stonetuskBoar, 6);
		logicRequest.setEvent(event);
		logicRequest.setCardInventoryId(sourcingSpecialistInventoryId);
		logicRequest.setEntityId(newSourcingSpecialist.getId());
		logicRequest.setGameId("g2");
		logicRequest.setUserId("userId1");

		logicResponse = service.afterPhysicalAttack(logicRequest);
		getContext().assertEquals("minion_stonetusk_boar", logicResponse.getModifiedAttributes().get(new
				EntityReference(newSourcingSpecialist.getId())).get(Attribute.LAST_MINION_DESTROYED_CARD_ID));
		getContext().assertEquals(stonetuskBoarInventoryId, logicResponse.getModifiedAttributes().get(new
				EntityReference(newSourcingSpecialist.getId())).get(Attribute.LAST_MINION_DESTROYED_INVENTORY_ID));

		newSourcingSpecialist = createMinionFromId(sourcingSpecialistInventoryId, 11, "userId1", "deckId1");
		String boulderfistOgreInventoryId = addCardForUser("minion_boulderfist_ogre", "opponentUserId");
		Minion boulderfistOgre = createMinionFromId(boulderfistOgreInventoryId, 10, "opponentUserId", "deckId2");

		event = new AfterPhysicalAttackEvent(null, newSourcingSpecialist, boulderfistOgre, 6);
		logicRequest.setEvent(event);
		logicRequest.setCardInventoryId(sourcingSpecialistInventoryId);
		logicRequest.setEntityId(newSourcingSpecialist.getId());
		logicRequest.setGameId("g2");
		logicRequest.setUserId("userId1");

		logicResponse = service.afterPhysicalAttack(logicRequest);
		getContext().assertTrue(logicResponse.getModifiedAttributes().isEmpty());

		newSourcingSpecialist = createMinionFromId(sourcingSpecialistInventoryId, 11, "userId1", "deckId1");
		getContext().assertEquals("minion_stonetusk_boar", newSourcingSpecialist.getAttribute(Attribute
				.LAST_MINION_DESTROYED_CARD_ID));
		getContext().assertEquals(stonetuskBoarInventoryId, newSourcingSpecialist.getAttribute(Attribute
				.LAST_MINION_DESTROYED_INVENTORY_ID));
	}

	private Minion createMinionFromId(String inventoryId, int entityId, String userId, String deckId) throws
			InterruptedException, SuspendExecution {
		GetCollectionResponse gcr = inventory.getCollection(new GetCollectionRequest().withUserId(userId));
		InventoryRecord record = gcr.getInventoryRecords().stream().filter(p -> Objects.equals(p.getId(), inventoryId)
		).findFirst().get();
		MinionCard minionCard = (MinionCard) Logic.getDescriptionFromRecord(record, userId, deckId).createInstance();
		Minion minion = minionCard.summon();
		minion.setId(entityId);
		return minion;
	}

	private String addCardForUser(String cardId, String userId) throws InterruptedException, SuspendExecution {
		AddToCollectionResponse atcr = inventory.addToCollection(new AddToCollectionRequest().withUserId(userId)
				.withCardIds(Collections.singletonList(cardId)));
		return atcr.getInventoryIds().get(0);
	}

	private String createCardAndUser(String cardId, String userId) throws InterruptedException, SuspendExecution {
		CreateCollectionResponse car = inventory.createCollection(CreateCollectionRequest.startingCollection(userId));
		AddToCollectionResponse atcr = inventory.addToCollection(new AddToCollectionRequest().withUserId(userId)
				.withCardIds(Collections.singletonList(cardId)));
		return atcr.getInventoryIds().get(0);
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<LogicImpl>> done) {
		accounts = new AccountsImpl();
		cards = new CardsImpl();
		inventory = new InventoryImpl();
		decks = new DecksImpl();
		games = new ClusteredGamesImpl();
		gateway = new GatewayImpl();
		LogicImpl instance = new LogicImpl();

		vertx.deployVerticle(games, then -> {
			vertx.deployVerticle(accounts, then2 -> {
				vertx.deployVerticle(cards, then3 -> {
					vertx.deployVerticle(inventory, then4 -> {
						vertx.deployVerticle(decks, then5 -> {
							vertx.deployVerticle(gateway, then6 -> {
								vertx.deployVerticle(instance, then7 -> done.handle(Future.succeededFuture(instance)));
							});
						});
					});
				});
			});
		});


	}
}
