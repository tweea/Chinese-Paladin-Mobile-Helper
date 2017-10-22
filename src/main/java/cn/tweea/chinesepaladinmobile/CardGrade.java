/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.util.EnumMap;
import java.util.Map;

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
}
