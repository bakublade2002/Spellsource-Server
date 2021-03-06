package net.demilich.metastone.game.heroes.powers;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.actions.HeroPowerAction;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.HasChooseOneActions;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.cards.desc.HeroPowerCardDesc;
import net.demilich.metastone.game.spells.NullSpell;

public class HeroPowerChooseOneCard extends HeroPowerCard implements HasChooseOneActions {

	private String[] cardIds;
	private String cardId;

	public HeroPowerChooseOneCard(HeroPowerCardDesc desc) {
		super(desc);
		setAttribute(Attribute.CHOOSE_ONE);
		this.cardIds = desc.options;
		this.cardId = desc.bothOptions;
		setSpell(NullSpell.create());
	}

	private SpellCard getCard(String cardId) {
		SpellCard card = (SpellCard) CardCatalogue.getCardById(cardId);
		card.setOwner(getOwner());
		card.setId(getId());
		return card;
	}

	public boolean hasBothOptions() {
		return cardId != null;
	}

	@Override
	public PlayCardAction play() {
		throw new UnsupportedOperationException("The method .play() should not be called for HeroPowerChooseOne");
	}

	@Override
	public PlayCardAction[] playOptions() {
		PlayCardAction[] actions = new PlayCardAction[cardIds.length];
		for (int i = 0; i < cardIds.length; i++) {
			String cardId = cardIds[i];
			SpellCard card = getCard(cardId);

			PlayCardAction cardAction = new HeroPowerAction(card.getSpell(), this, getTargetRequirement(), card);
			cardAction.setChooseOneOptionIndex(i);
			actions[i] = cardAction;
		}
		return actions;
	}

	@Override
	public PlayCardAction playBothOptions() {
		SpellCard card = getCard(cardId);

		return new HeroPowerAction(card.getSpell(), this, getTargetRequirement());
	}

}
