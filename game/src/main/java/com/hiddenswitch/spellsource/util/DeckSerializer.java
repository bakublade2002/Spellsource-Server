package com.hiddenswitch.spellsource.util;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.demilich.metastone.game.decks.Deck;

public class DeckSerializer extends ObjectSerializer<Deck> implements JsonSerializer<Deck>, JsonDeserializer<Deck> {
}
