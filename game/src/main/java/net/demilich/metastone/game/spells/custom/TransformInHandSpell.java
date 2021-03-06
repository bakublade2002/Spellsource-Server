package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.TriggerDesc;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TransformInHandSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;
		boolean startedInDeck = card.hasAttribute(Attribute.STARTED_IN_DECK);
		EntityFilter cardFilter = desc.getCardFilter();
		EntityReference secondaryTarget = (EntityReference) desc.get(SpellArg.SECONDARY_TARGET);
		List<Card> replacementCard = Arrays.asList(SpellUtils.getCards(context, desc));

		Card newCard = null;
		if (cardFilter != null) {
			newCard = context.getLogic().getRandom(desc.getFilteredCards(context, player, source));
		} else if (secondaryTarget != null) {
			newCard = ((Card) context.resolveSingleTarget(secondaryTarget)).getCopy();
		} else if (replacementCard.size() > 0) {
			if (replacementCard.size() == 1) {
				newCard = replacementCard.get(0);
			} else {
				newCard = context.getLogic().getRandom(replacementCard);
			}
		}

		Card replaced = context.getLogic().replaceCardInHand(player.getId(), card, newCard);

		// Cards that are transformed in the hand started in the deck if the originating card started in the deck
		// See https://www.reddit.com/r/hearthstone/comments/7ia60v/shifting_scroll_does_not_work_with_leyline/
		if (startedInDeck) {
			replaced.setAttribute(Attribute.STARTED_IN_DECK);
		}
	}

}
