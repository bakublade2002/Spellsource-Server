package net.demilich.metastone.game.cards.desc;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.*;

import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterArg;
import net.demilich.metastone.game.spells.desc.filter.FilterDesc;

public class FilterDescSerializer implements JsonDeserializer<FilterDesc>, JsonSerializer<FilterDesc> {
	@SuppressWarnings("unchecked")
	@Override
	public FilterDesc deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("ValueProvider parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		String filterClassName = EntityFilter.class.getPackage().getName() + "." + jsonData.get("class").getAsString();
		Class<? extends EntityFilter> filterClass;
		try {
			filterClass = (Class<? extends EntityFilter>) Class.forName(filterClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JsonParseException("ValueProvider parser encountered an invalid class: " + filterClassName);
		}
		Map<FilterArg, Object> arguments = FilterDesc.build(filterClass);

		parseArgument(FilterArg.VALUE, jsonData, arguments, ParseValueType.VALUE);
		parseArgument(FilterArg.TARGET_PLAYER, jsonData, arguments, ParseValueType.TARGET_PLAYER);
		parseArgument(FilterArg.ATTRIBUTE, jsonData, arguments, ParseValueType.ATTRIBUTE);
		parseArgument(FilterArg.RACE, jsonData, arguments, ParseValueType.RACE);
		parseArgument(FilterArg.OPERATION, jsonData, arguments, ParseValueType.OPERATION);
		parseArgument(FilterArg.INVERT, jsonData, arguments, ParseValueType.BOOLEAN);
		parseArgument(FilterArg.CARD_TYPE, jsonData, arguments, ParseValueType.CARD_TYPE);
		parseArgument(FilterArg.CARD_SET, jsonData, arguments, ParseValueType.CARD_SET);
		parseArgument(FilterArg.HERO_CLASS, jsonData, arguments, ParseValueType.HERO_CLASS);
		parseArgument(FilterArg.HERO_CLASSES, jsonData, arguments, ParseValueType.HERO_CLASS_ARRAY);
		parseArgument(FilterArg.RARITY, jsonData, arguments, ParseValueType.RARITY);
		parseArgument(FilterArg.MANA_COST, jsonData, arguments, ParseValueType.VALUE);
		parseArgument(FilterArg.CARD_ID, jsonData, arguments, ParseValueType.STRING);
		parseArgument(FilterArg.FILTERS, jsonData, arguments, ParseValueType.ENTITY_FILTER_ARRAY);
		parseArgument(FilterArg.TARGET, jsonData, arguments, ParseValueType.TARGET_REFERENCE);

		return new FilterDesc(arguments);
	}

	private void parseArgument(FilterArg arg, JsonObject jsonData, Map<FilterArg, Object> arguments, ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(arg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		arguments.put(arg, value);
	}

	@Override
	public JsonElement serialize(FilterDesc src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.add("class", new JsonPrimitive(src.getFilterClass().getSimpleName()));
		for (FilterArg attribute : FilterArg.values()) {
			if (attribute == FilterArg.CLASS) {
				continue;
			}
			if (!src.containsKey(attribute)) {
				continue;
			}
			String argName = ParseUtils.toCamelCase(attribute.toString());
			result.add(argName, context.serialize(src.get(attribute)));
		}
		return result;
	}
}
