package net.demilich.metastone.game.cards.desc;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.*;

import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.SourceArg;
import net.demilich.metastone.game.spells.desc.source.SourceDesc;

public class SourceDescSerializer implements JsonDeserializer<SourceDesc>, JsonSerializer<SourceDesc> {
	@SuppressWarnings("unchecked")
	@Override
	public SourceDesc deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("ValueProvider parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		String cardSourceClassName = CardSource.class.getPackage().getName() + "." + jsonData.get("class").getAsString();
		Class<? extends CardSource> cardSourceClass;
		try {
			cardSourceClass = (Class<? extends CardSource>) Class.forName(cardSourceClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JsonParseException("ValueProvider parser encountered an invalid class: " + cardSourceClassName);
		}
		Map<SourceArg, Object> arguments = SourceDesc.build(cardSourceClass);

//		parseArgument(SourceArg.VALUE, jsonData, arguments, ParseValueType.INTEGER);
		parseArgument(SourceArg.TARGET_PLAYER, jsonData, arguments, ParseValueType.TARGET_PLAYER);
//		parseArgument(SourceArg.ATTRIBUTE, jsonData, arguments, ParseValueType.ATTRIBUTE);
//		parseArgument(SourceArg.RACE, jsonData, arguments, ParseValueType.RACE);
//		parseArgument(SourceArg.OPERATION, jsonData, arguments, ParseValueType.OPERATION);
		parseArgument(SourceArg.INVERT, jsonData, arguments, ParseValueType.BOOLEAN);
//		parseArgument(SourceArg.CARD_TYPE, jsonData, arguments, ParseValueType.CARD_TYPE);
//		parseArgument(SourceArg.BACKUP_HERO_CLASS, jsonData, arguments, ParseValueType.HERO_CLASS);
//		parseArgument(SourceArg.VALID_HERO_CLASSES, jsonData, arguments, ParseValueType.HERO_CLASS_ARRAY);
//		parseArgument(SourceArg.RARITY, jsonData, arguments, ParseValueType.RARITY);
//		parseArgument(SourceArg.MANA_COST, jsonData, arguments, ParseValueType.VALUE);
//		parseArgument(SourceArg.CARD_ID, jsonData, arguments, ParseValueType.STRING);
//		parseArgument(SourceArg.FILTERS, jsonData, arguments, ParseValueType.ENTITY_FILTER_ARRAY);
//		parseArgument(SourceArg.TARGET, jsonData, arguments, ParseValueType.TARGET_REFERENCE);

		return new SourceDesc(arguments);
	}

	private void parseArgument(SourceArg arg, JsonObject jsonData, Map<SourceArg, Object> arguments, ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(arg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		arguments.put(arg, value);
	}

	@Override
	public JsonElement serialize(SourceDesc src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.add("class", new JsonPrimitive(src.getSourceClass().getSimpleName()));
		for (SourceArg attribute : SourceArg.values()) {
			if (attribute == SourceArg.CLASS) {
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
