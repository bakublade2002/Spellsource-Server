package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.reflect.TypeToken;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.util.SerializationTestContext;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.*;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import com.hiddenswitch.spellsource.util.TestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.hiddenswitch.spellsource.Assert.assertReflectionEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SerializationTest extends TestBase {

	private static HeroClass getRandomClass() {
		HeroClass randomClass = HeroClass.ANY;
		HeroClass[] values = HeroClass.values();
		while (!randomClass.isBaseClass()) {
			randomClass = values[ThreadLocalRandom.current().nextInt(values.length)];
		}
		return randomClass;
	}

	@Before
	public void loggerSetup() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);
	}

	@Test
	public void testDeckSerialization() {
		DeckFormat deckFormat = new DeckFormat();
		for (CardSet set : CardSet.values()) {
			deckFormat.addSet(set);
		}
		Deck deck = DeckFactory.getRandomDeck(HeroClass.BLUE, deckFormat);
		String s = Serialization.serialize(deck);
		Deck deck2 = Serialization.deserialize(s, Deck.class);
		assertReflectionEquals(deck, deck2);
	}

	@Test
	public void testTypedSerialization() {
		PhysicalAttackAction action = new PhysicalAttackAction(EntityReference.ALL_MINIONS);
		String s = Serialization.serialize(action);
		PhysicalAttackAction deserializedAction = Serialization.deserialize(s, PhysicalAttackAction.class);
		assertEquals(deserializedAction.getAttackerReference(), action.getAttackerReference());

		GameAction deserializedGameAction = Serialization.deserialize(s, GameAction.class);
		PhysicalAttackAction castedAction = (PhysicalAttackAction) deserializedGameAction;
		assertEquals(castedAction.getAttackerReference(), action.getAttackerReference());
	}

	@Test
	@Ignore
	public void testAllGameActions() {
		SpellCard fireball = (SpellCard) CardCatalogue.getCardById("spell_fireball");
		MinionCard elven_archer = (MinionCard) CardCatalogue.getCardById("minion_elven_archer");
		HeroPowerCard heroPowerFireblast = (HeroPowerCard) CardCatalogue.getCardById("hero_power_fireblast");
		WeaponCard assassinsBlade = (WeaponCard) CardCatalogue.getCardById("weapon_assassins_blade");
		SpellCard journeyBelow = (SpellCard) CardCatalogue.getCardById("spell_journey_below");
		CardList discoverCards = new CardArrayList();
		discoverCards.addCard(fireball.getCopy());
		DiscoverAction discoverAction = DiscoverAction.createDiscover(journeyBelow.getSpell());

		ArrayList<GameAction> gameActions = new ArrayList<>();

		EndTurnAction endTurnAction = new EndTurnAction();
		PhysicalAttackAction physicalAttackAction = new PhysicalAttackAction(new EntityReference(1));
		physicalAttackAction.setTargetReference(new EntityReference(2));
		PlaySpellCardAction playSpellCardAction = new PlaySpellCardAction(fireball.getSpell(), fireball, TargetSelection.ENEMY_CHARACTERS);
		playSpellCardAction.setTargetReference(new EntityReference(3));
		BattlecryAction elvenArcherAction = elven_archer.summon().getBattlecry();
		PlayMinionCardAction playMinionCardAction = new PlayMinionCardAction(elven_archer.getReference(), elvenArcherAction);
		HeroPowerAction heroPowerAction = new HeroPowerAction(heroPowerFireblast.getSpell(), heroPowerFireblast.getCopy(), TargetSelection.ENEMY_CHARACTERS);
		PlayWeaponCardAction playWeaponCardAction = new PlayWeaponCardAction(assassinsBlade.getReference());

		// 0
		gameActions.add(endTurnAction);
		// 1
		gameActions.add(physicalAttackAction);
		// 2
		gameActions.add(playSpellCardAction);
		// 3
		gameActions.add(playMinionCardAction);
		// 4
		gameActions.add(elvenArcherAction);
		// 5
		gameActions.add(heroPowerAction);
		// 6
		gameActions.add(playWeaponCardAction);
		// 7
		gameActions.add(discoverAction);

		String s = Serialization.serialize(gameActions);
		TypeToken<ArrayList<GameAction>> gameActionListTT = new TypeToken<ArrayList<GameAction>>() {
		};
		ArrayList<GameAction> deserializedGameActions = Serialization.deserialize(s, gameActionListTT.getType());

		EndTurnAction endTurnAction1 = (EndTurnAction) deserializedGameActions.get(0);
		assertNotNull(endTurnAction1);
		assertEquals(endTurnAction1.getActionType(), ActionType.END_TURN);

		PhysicalAttackAction physicalAttackAction1 = (PhysicalAttackAction) deserializedGameActions.get(1);
		assertNotNull(physicalAttackAction1);
		assertEquals(physicalAttackAction1.getActionType(), ActionType.PHYSICAL_ATTACK);
		assertEquals(physicalAttackAction1.getAttackerReference(), physicalAttackAction.getAttackerReference());
		assertEquals(physicalAttackAction1.getTargetReference(), physicalAttackAction.getTargetReference());

		PlaySpellCardAction playSpellCardAction1 = (PlaySpellCardAction) deserializedGameActions.get(2);
		assertNotNull(playSpellCardAction1);
		assertEquals(playSpellCardAction1.getActionType(), ActionType.SPELL);
		assertSpellsEqual(playSpellCardAction1.getSpell(), playSpellCardAction.getSpell());
		assertEquals(playSpellCardAction1.getTargetReference(), playSpellCardAction.getTargetReference());

		PlayMinionCardAction playMinionCardAction1 = (PlayMinionCardAction) deserializedGameActions.get(3);
		assertNotNull(playMinionCardAction1);
		assertEquals(playMinionCardAction1.getActionType(), ActionType.SUMMON);

		BattlecryAction battlecryAction1 = (BattlecryAction) deserializedGameActions.get(4);
		assertNotNull(battlecryAction1);
		assertSpellsEqual(battlecryAction1.getSpell(), elvenArcherAction.getSpell());

		HeroPowerAction heroPowerAction1 = (HeroPowerAction) deserializedGameActions.get(5);
		assertNotNull(heroPowerAction1);
		assertEquals(heroPowerAction1.getActionType(), ActionType.HERO_POWER);
		assertSpellsEqual(heroPowerAction1.getSpell(), heroPowerAction.getSpell());

		PlayWeaponCardAction playWeaponCardAction1 = (PlayWeaponCardAction) deserializedGameActions.get(6);
		assertNotNull(playWeaponCardAction1);
		assertEquals(playWeaponCardAction1.getActionType(), ActionType.EQUIP_WEAPON);

		DiscoverAction discoverAction1 = (DiscoverAction) deserializedGameActions.get(7);
		assertNotNull(discoverAction1);
		assertEquals(discoverAction1.getActionType(), ActionType.DISCOVER);
		assertEquals(discoverAction1.getSpell().getSpellClass(), discoverAction.getSpell().getSpellClass());
		assertSpellsEqual(discoverAction1.getSpell(), discoverAction.getSpell());
	}

	@Test
	@Ignore
	public void testGameStateSerialization() {
		DeckFormat deckFormat = new DeckFormat();
		for (CardSet set : CardSet.values()) {
			deckFormat.addSet(set);
		}
		HeroClass heroClass1 = getRandomClass();
		PlayerConfig player1Config = new PlayerConfig(DeckFactory.getRandomDeck(heroClass1, deckFormat), new PlayRandomBehaviour());
		player1Config.setName("Player 1");
		player1Config.setHeroCard(getHeroCardForClass(heroClass1));
		Player player1 = new Player(player1Config);

		HeroClass heroClass2 = getRandomClass();
		PlayerConfig player2Config = new PlayerConfig(DeckFactory.getRandomDeck(heroClass2, deckFormat), new PlayRandomBehaviour());
		player2Config.setName("Player 2");
		player2Config.setHeroCard(getHeroCardForClass(heroClass2));
		Player player2 = new Player(player2Config);
		GameContext context = new SerializationTestContext(player1, player2, new GameLogic(), deckFormat);
		try {
			context.play();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}

		// Get the game state now
		GameState state = context.getGameStateCopy();
		String json = Serialization.serialize(state);
		GameState state1 = Serialization.deserialize(json, GameState.class);
		assertReflectionEquals(state, state1);
	}

	private void assertSpellsEqual(SpellDesc actual, SpellDesc expected) {
		for (SpellArg arg : SpellArg.values()) {
			if (expected.containsKey(arg)) {
				assertEquals(actual.get(arg), expected.get(arg));
			}
		}
	}
}