package net.demilich.metastone.game.cards.costmodifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.AlgebraicOperation;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;

public class CardCostModifier extends CustomCloneable implements Trigger, Serializable {
	private boolean expired;
	private int owner;
	private EntityReference hostReference;
	private EventTrigger expirationTrigger;
	private CardCostModifierDesc desc;

	public CardCostModifier(CardCostModifierDesc desc) {
		this.desc = desc;
		EventTriggerDesc triggerDesc = (EventTriggerDesc) desc.get(CardCostModifierArg.EXPIRATION_TRIGGER);
		if (triggerDesc != null) {
			this.expirationTrigger = triggerDesc.create();
		}
	}

	public boolean appliesTo(GameContext context, Card card, Player player) {
		if (expired) {
			return false;
		}

		if (!getRequiredCardIds().isEmpty() && !getRequiredCardIds().contains(card.getId())) {
			return false;
		}

		if (getRequiredAttribute() != null && !card.hasAttribute(getRequiredAttribute())) {
			return false;
		}

		if (getRequiredRace() != null && card.getAttribute(Attribute.RACE) != getRequiredRace()) {
			return false;
		}

		switch (getTargetPlayer()) {
			case BOTH:
				break;
			case OPPONENT:
				if (card.getOwner() == getOwner()) {
					return false;
				}
				break;
			case SELF:
				if (card.getOwner() != getOwner()) {
					return false;
				}
				break;
			default:
				break;

		}

		if (getCardType() == null && !card.getCardType().isCardType(CardType.HERO_POWER)) {
			return true;
		}

		return card.getCardType().isCardType(getCardType());
	}

	@Override
	public boolean canFire(GameEvent event) {
		return true;
	}

	@Override
	public CardCostModifier clone() {
		CardCostModifier clone = (CardCostModifier) super.clone();
		clone.expirationTrigger = expirationTrigger != null ? expirationTrigger.clone() : null;
		return clone;
	}

	public void expire() {
		expired = true;
	}

	protected Object get(CardCostModifierArg arg) {
		return desc.get(arg);
	}

	protected CardType getCardType() {
		return (CardType) desc.get(CardCostModifierArg.CARD_TYPE);
	}

	@Override
	public EntityReference getHostReference() {
		return hostReference;
	}

	public int getMinValue() {
		return desc.getInt(CardCostModifierArg.MIN_VALUE);
	}

	@Override
	public int getOwner() {
		return owner;
	}

	protected Attribute getRequiredAttribute() {
		return (Attribute) desc.get(CardCostModifierArg.REQUIRED_ATTRIBUTE);
	}

	@SuppressWarnings("unchecked")
	protected List<Integer> getRequiredCardIds() {
		if (!desc.containsKey(CardCostModifierArg.CARD_IDS)) {
			return new ArrayList<Integer>();
		}
		return (List<Integer>) desc.get(CardCostModifierArg.CARD_IDS);
	}

	protected Race getRequiredRace() {
		return (Race) get(CardCostModifierArg.RACE);
	}

	protected TargetPlayer getTargetPlayer() {
		if (!desc.containsKey(CardCostModifierArg.TARGET_PLAYER)) {
			return TargetPlayer.SELF;
		}
		return (TargetPlayer) desc.get(CardCostModifierArg.TARGET_PLAYER);
	}

	@Override
	public boolean interestedIn(GameEventType eventType) {
		if (expirationTrigger == null) {
			return false;
		}
		return eventType == expirationTrigger.interestedIn() || expirationTrigger.interestedIn() == GameEventType.ALL;
	}

	@Override
	public boolean isExpired() {
		return expired;
	}

	@Override
	public void onAdd(GameContext context) {
	}

	@Override
	public void onGameEvent(GameEvent event) {
		Entity host = event.getGameContext().resolveSingleTarget(getHostReference());
		if (expirationTrigger != null && event.getEventType() == expirationTrigger.interestedIn() && expirationTrigger.fires(event, host)) {
			expire();
		}
	}

	@Override
	public void onRemove(GameContext context) {
		expired = true;
	}

	public int process(GameContext context, Entity host, Card card, int currentManaCost, Player player) {
		AlgebraicOperation operation = (AlgebraicOperation) desc.get(CardCostModifierArg.OPERATION);
		int value = desc.getValue(CardCostModifierArg.VALUE, context, player, card, host, 0);
		if (operation != null) {
			return operation.performOperation(currentManaCost, value);
		}
		return currentManaCost + desc.getInt(CardCostModifierArg.VALUE);
	}

	@Override
	public void setHost(Entity host) {
		hostReference = host.getReference();
	}

	@Override
	public void setOwner(int playerIndex) {
		this.owner = playerIndex;
		if (expirationTrigger != null) {
			expirationTrigger.setOwner(playerIndex);
		}
	}

	@Override
	public boolean hasPersistentOwner() {
		return false;
	}

	@Override
	public boolean oneTurnOnly() {
		return false;
	}

	@Override
	public boolean isDelayed() {
		return false;
	}

	@Override
	public void delayTimeDown() {

	}

	@Override
	public boolean canFireCondition(GameEvent event) {
		if (expirationTrigger != null) {
			return expirationTrigger.canFireCondition(event);
		}
		return true;
	}
}
