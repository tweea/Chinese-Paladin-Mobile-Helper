/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.util.EnumMap;
import java.util.Map;

public class Card {
	private final CardDefinition definition;

	private final Map<CardLevelType, Integer> levels;

	public Card(CardDefinition definition) {
		this.definition = definition;
		this.levels = new EnumMap<>(CardLevelType.class);
	}

	public CardDefinition getDefinition() {
		return definition;
	}

	public Map<CardLevelType, Integer> getLevels() {
		return levels;
	}
}
