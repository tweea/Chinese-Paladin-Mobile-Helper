/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CardGrade {
	private final int grade;

	private final Map<CardUpgradeNeedType, CardUpgradeNeed> upgradeNeeds;

	public CardGrade(int grade) {
		this.grade = grade;
		this.upgradeNeeds = new EnumMap<>(CardUpgradeNeedType.class);
	}

	public int getGrade() {
		return grade;
	}

	public Map<CardUpgradeNeedType, CardUpgradeNeed> getUpgradeNeeds() {
		return upgradeNeeds;
	}

	public void addUpgradeNeed(CardUpgradeNeed upgradeNeed) {
		upgradeNeeds.put(upgradeNeed.getType(), upgradeNeed);
	}

	public int computeNeed(CardUpgradeNeedType upgradeNeedType, int currentLevel) {
		CardUpgradeNeed upgradeNeed = upgradeNeeds.get(upgradeNeedType);
		if (upgradeNeed == null) {
			return 0;
		}

		return upgradeNeed.computeNeed(currentLevel);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("grade", grade).append("upgradeNeeds", upgradeNeeds).toString();
	}
}
