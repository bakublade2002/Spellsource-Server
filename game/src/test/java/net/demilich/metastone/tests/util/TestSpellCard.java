package net.demilich.metastone.tests.util;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.desc.SpellCardDesc;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.utils.AttributeMap;

public class TestSpellCard extends SpellCard {

	private static SpellCardDesc toDesc() {
		SpellCardDesc desc = new SpellCardDesc();
		desc.name = "Unit Test Spell";
		desc.rarity = Rarity.FREE;
		desc.type = CardType.SPELL;
		desc.heroClass = HeroClass.ANY;
		desc.attributes = new AttributeMap();
		return desc;
	}

	public TestSpellCard(SpellDesc spell) {
		super(toDesc());
		setDescription("This spell can have various effects and should only be used in the context of unit net.demilich.metastone.tests.");
		setCollectible(false);

		setSpell(spell);
		setTargetRequirement(TargetSelection.NONE);
	}

}
