package net.demilich.metastone.game.cards;

import java.util.*;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.actions.PlayMinionCardAction;
import net.demilich.metastone.game.cards.desc.MinionCardDesc;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.minions.Race;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinionCard extends ActorCard {
	private static Logger logger = LoggerFactory.getLogger(MinionCard.class);
	private static final Set<Attribute> ignoredAttributes = new HashSet<Attribute>(
			Arrays.asList(Attribute.PASSIVE_TRIGGERS, Attribute.DECK_TRIGGER, Attribute.MANA_COST_MODIFIER, Attribute.BASE_ATTACK,
					Attribute.BASE_HP, Attribute.SECRET, Attribute.CHOOSE_ONE, Attribute.BATTLECRY, Attribute.COMBO));

	public MinionCard(MinionCardDesc desc) {
		super(desc);
		setAttribute(Attribute.BASE_ATTACK, desc.baseAttack);
		setAttribute(Attribute.ATTACK, desc.baseAttack);
		setAttribute(Attribute.BASE_HP, desc.baseHp);
		setAttribute(Attribute.HP, desc.baseHp);
		setAttribute(Attribute.MAX_HP, desc.baseHp);
		if (desc.race != null) {
			setRace(desc.race);
		}
		this.desc = desc;
	}

	protected Minion createMinion(Attribute... tags) {
		MinionCardDesc desc = getDesc();
		Minion minion = new Minion(this);
		for (Attribute gameTag : getAttributes().keySet()) {
			if (!ignoredAttributes.contains(gameTag)) {
				minion.setAttribute(gameTag, getAttribute(gameTag));
			}
		}

		populate(minion);

		minion.setBaseAttack(getBaseAttack());
		minion.setAttack(getAttack());
		minion.setHp(getHp());
		minion.setMaxHp(getHp());
		minion.setBaseHp(getBaseHp());
		minion.setBattlecry(desc.getBattlecryAction());
		minion.setHp(minion.getMaxHp());

		return minion;
	}

	@Override
	public MinionCardDesc getDesc() {
		return (MinionCardDesc) super.getDesc();
	}

	@Override
	public PlayCardAction play() {
		return new PlayMinionCardAction(getReference());
	}

	public void setRace(Race race) {
		setAttribute(Attribute.RACE, race);
	}

	public Minion summon() {
		return createMinion();
	}

	public boolean hasTrigger() {
		return getDesc().trigger != null || (getDesc().triggers != null && getDesc().triggers.length > 0);
	}

	public boolean hasAura() {
		return getDesc().aura != null;
	}

	public boolean hasCardCostModifier() {
		return getDesc().cardCostModifier != null;
	}

	public boolean hasBattlecry() {
		return getDesc().battlecry != null;
	}

}
