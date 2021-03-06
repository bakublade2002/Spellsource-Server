package net.demilich.metastone.game.spells.desc;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.CustomCloneable;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.*;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * A definition for a spell.
 * <p>
 * A spell description has a variety of arguments of type {@link SpellArg}.
 */
public class SpellDesc extends Desc<SpellArg> {
	public SpellDesc(Map<SpellArg, Object> arguments) {
		super(arguments);
	}

	public static Map<SpellArg, Object> build(Class<? extends Spell> spellClass) {
		final Map<SpellArg, Object> arguments = new EnumMap<>(SpellArg.class);
		arguments.put(SpellArg.CLASS, spellClass);
		return arguments;
	}

	public SpellDesc addArg(SpellArg spellArg, Object value) {
		SpellDesc clone = clone();
		clone.put(spellArg, value);
		return clone;
	}

	public SpellDesc removeArg(SpellArg spellArg) {
		SpellDesc clone = clone();
		clone.remove(spellArg);
		return clone;
	}

	@Override
	public SpellDesc clone() {
		SpellDesc clone = new SpellDesc(build(getSpellClass()));
		for (SpellArg spellArg : keySet()) {
			Object value = get(spellArg);
			if (value instanceof CustomCloneable) {
				CustomCloneable cloneable = (CustomCloneable) value;
				clone.put(spellArg, cloneable.clone());
			} else {
				clone.put(spellArg, value);
			}
		}
		return clone;
	}

	public EntityFilter getEntityFilter() {
		return (EntityFilter) get(SpellArg.FILTER);
	}

	public int getInt(SpellArg spellArg, int defaultValue) {
		return containsKey(spellArg) ? (int) get(spellArg) : defaultValue;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Spell> getSpellClass() {
		return (Class<? extends Spell>) get(SpellArg.CLASS);
	}

	public EntityReference getTarget() {
		return (EntityReference) get(SpellArg.TARGET);
	}

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) get(SpellArg.TARGET_PLAYER);
	}

	public boolean hasPredefinedTarget() {
		return get(SpellArg.TARGET) != null;
	}

	@Override
	public String toString() {
		String result = "[SpellDesc arguments= {\n";
		for (SpellArg spellArg : keySet()) {
			result += "\t" + spellArg + ": " + get(spellArg) + "\n";
		}
		result += "}";
		return result;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	public void setTarget(EntityReference target) {
		put(SpellArg.TARGET, target);
	}

	public Stream<SpellDesc> subSpells(final int depth) {
		Stream<SpellDesc> spells;
		SpellDesc[] spellsArray = (SpellDesc[]) get(SpellArg.SPELLS);
		if (spellsArray != null && spellsArray.length > 0) {
			spells = Stream.concat(Stream.of(spellsArray),
					Stream.of(spellsArray).flatMap(SpellDesc::subSpells)
			);
		} else {
			spells = Stream.empty();
		}
		List<SpellDesc> units = Stream.of(SpellArg.SPELL, SpellArg.SPELL_1, SpellArg.SPELL_2)
				.map(this::get)
				.filter(Objects::nonNull)
				.map(o -> (SpellDesc) o).collect(toList());

		Stream<SpellDesc> unitSpells;
		if (depth == 0) {
			unitSpells = units.stream();
		} else {
			unitSpells = Stream.concat(units.stream(), units.stream().flatMap(u -> u.subSpells(depth - 1)));
		}

		return Stream.concat(spells, unitSpells);
	}

	public Stream<SpellDesc> subSpells() {
		return subSpells(20);
	}

	/**
	 * Joins a spell description with another spell using a {@link MetaSpell}.
	 *
	 * @param masterSpell The spell from which to inherit the {@link EntityFilter}, {@link TargetPlayer}, {@link
	 *                    SpellArg#TARGET} and {@link SpellArg#RANDOM_TARGET} attributes to put into the {@link
	 *                    MetaSpell}.
	 * @param childSpells The spells that will occur after the {@code masterSpell} is casted.
	 * @return A new {@link SpellDesc}.
	 */
	public static SpellDesc join(SpellDesc masterSpell, SpellDesc... childSpells) {
		// Remove nulls
		childSpells = Arrays.stream(childSpells).filter(Objects::nonNull).toArray(SpellDesc[]::new);

		if (masterSpell == null) {
			if (childSpells == null || childSpells.length == 0) {
				return null;
			} else if (childSpells.length == 1) {
				return childSpells[0].clone();
			} else {
				return SpellDesc.join(childSpells[0], Arrays.copyOfRange(childSpells, 1, childSpells.length));
			}
		} else if (childSpells == null || childSpells.length == 0) {
			return masterSpell.clone();
		}

		SpellDesc[] descs = new SpellDesc[childSpells.length + 1];
		descs[0] = masterSpell;
		System.arraycopy(childSpells, 0, descs, 1, childSpells.length);
		SpellDesc newDesc = join(masterSpell.getTarget(), masterSpell.getBool(SpellArg.RANDOM_TARGET), descs);
		if (masterSpell.getEntityFilter() != null) {
			newDesc.put(SpellArg.FILTER, masterSpell.getEntityFilter());
		}
		if (masterSpell.getTargetPlayer() != null) {
			newDesc.put(SpellArg.TARGET_PLAYER, masterSpell.getTargetPlayer());
		}
		return newDesc;
	}

	private static SpellDesc join(EntityReference target, boolean randomTarget, SpellDesc... descs) {
		return MetaSpell.create(target, randomTarget, descs);
	}

	public EntityFilter getCardFilter() {
		return (EntityFilter) get(SpellArg.CARD_FILTER);
	}

	public CardSource getCardSource() {
		return (CardSource) get(SpellArg.CARD_SOURCE);
	}

	public CardList getFilteredCards(GameContext context, Player player, Entity host) {
		CardSource source = getCardSource();
		final EntityFilter filter;
		if (source == null) {
			source = CatalogueSource.create();
		}
		if (containsKey(SpellArg.CARD_FILTER)) {
			filter = getCardFilter();
		} else {
			filter = AndFilter.create();
		}
		return source.getCards(context, player).filtered(c -> filter.matches(context, player, c, host));
	}
}
