/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

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
}
