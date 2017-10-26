/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CardUpgradeNeed {
	private final CardUpgradeNeedType type;

	private final int[] needs;

	public CardUpgradeNeed(CardUpgradeNeedType type, int[] needs) {
		this.type = type;
		this.needs = needs;
	}

	public CardUpgradeNeedType getType() {
		return type;
	}

	public int[] getNeeds() {
		return needs;
	}

	public int computeNeed(int currentLevel) {
		int need = 0;
		for (int level = currentLevel; level < needs.length; level++) {
			need += needs[level];
		}
		return need;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("type", type).append("needs", needs).toString();
	}
}
