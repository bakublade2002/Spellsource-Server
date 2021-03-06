package net.demilich.metastone.game.spells.custom;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TransformMinionSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Trigger;

public class CopyMinionSpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(CopyMinionSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Minion template = (Minion) target;
		Minion clone = template.getCopy();
		clone.getAttributes().remove(Attribute.AURA_ATTACK_BONUS);
		clone.getAttributes().remove(Attribute.AURA_HP_BONUS);
		clone.getAttributes().remove(Attribute.AURA_UNTARGETABLE_BY_SPELLS);
		clone.clearEnchantments();
		clone.setCardCostModifier(null);

		Minion sourceActor = (Minion) context.resolveSingleTarget(context.getSummonReferenceStack().peek());
		SpellDesc transformSpell = TransformMinionSpell.create(clone);
		if (context.getEnvironment().get(Environment.TRANSFORM_REFERENCE) != null) {
			SpellUtils.castChildSpell(context, player, transformSpell, source, sourceActor);
			return;
		}
		SpellUtils.castChildSpell(context, player, transformSpell, source, sourceActor);

		for (Trigger trigger : context.getTriggersAssociatedWith(template.getReference())) {
			Trigger triggerClone = trigger.clone();
			if (triggerClone instanceof CardCostModifier) {
				context.getLogic().addManaModifier(player, (CardCostModifier) triggerClone, clone);
			} else {
				context.getLogic().addGameEventListener(player, triggerClone, clone);
			}

		}
	}
}