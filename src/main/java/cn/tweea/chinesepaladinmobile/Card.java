/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Card {
    private final CardDefinition definition;

    private final Map<CardLevelType, List<Integer>> levels;

    public Card(CardDefinition definition) {
        this.definition = definition;
        this.levels = new EnumMap<>(CardLevelType.class);
    }

    public CardDefinition getDefinition() {
        return definition;
    }

    public Map<CardLevelType, List<Integer>> getLevels() {
        return levels;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("definition", definition.getName()).append("levels", levels).toString();
    }
}
