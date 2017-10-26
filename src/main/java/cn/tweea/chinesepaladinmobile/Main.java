/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;

public final class Main {
	public static void main(String[] args) {
		Map<CardLevelType, List<Pair<CardDependencyType, CardUpgradeNeedType>>> levelNeedMap = buildLevelNeedMap();
		Map<Integer, CardGrade> grades = DataFiles.loadCardGrade(new ClassPathResource("CardPiece.xlsx"));
		Map<String, CardDefinition> definitions = DataFiles.loadCardDefinition(new ClassPathResource("CardList.xlsx"), grades);
		Map<String, Card> cards = DataFiles.loadCard(new ClassPathResource("CardStatus.xlsx"), definitions);

		Map<String, Map<String, Integer>> needsMap = new LinkedHashMap<>();
		for (String name : cards.keySet()) {
			Map<String, Integer> needs = new LinkedHashMap<>();
			needs.put("总计", 0);
			needs.put("自用", 0);
			needs.put("他用", 0);
			needsMap.put(name, needs);
		}
		for (Card card : cards.values()) {
			CardDefinition definition = card.getDefinition();
			String name = definition.getName();
			CardGrade grade = definition.getGrade();
			Map<CardDependencyType, CardDefinition> dependencies = definition.getDependencies();
			int 云裳数量 = definition.get云裳数量();

			// 初始化累计值
			Map<String, Integer> needs = needsMap.get(name);
			for (CardDefinition dependency : dependencies.values()) {
				needs.put(dependency.getName(), 0);
			}

			for (Map.Entry<CardLevelType, Integer> levelEntry : card.getLevels().entrySet()) {
				CardLevelType levelType = levelEntry.getKey();
				Integer level = levelEntry.getValue();

				if (云裳数量 == 0 && (levelType == CardLevelType.云裳1 || levelType == CardLevelType.云裳2)) {
					continue;
				} else if (云裳数量 == 1 && levelType == CardLevelType.云裳2) {
					continue;
				}

				for (Pair<CardDependencyType, CardUpgradeNeedType> levelNeed : levelNeedMap.get(levelType)) {
					CardDependencyType dependencyType = levelNeed.getLeft();
					CardUpgradeNeedType upgradeNeedType = levelNeed.getRight();

					int need = grade.computeNeed(upgradeNeedType, level);
					if (dependencyType == null) {
						accumulateNeed(needs, "自用", need);
					} else {
						CardDefinition dependency = dependencies.get(dependencyType);
						if (dependency == null) {
							continue;
						}

						String dependencyName = dependency.getName();
						accumulateNeed(needs, dependencyName, need);
					}
				}
			}
		}
		for (Map.Entry<String, Map<String, Integer>> needsEntry : needsMap.entrySet()) {
			String name = needsEntry.getKey();
			Map<String, Integer> needs = needsEntry.getValue();
			System.out.println(name + ':' + needs);
		}
	}

	private static Map<CardLevelType, List<Pair<CardDependencyType, CardUpgradeNeedType>>> buildLevelNeedMap() {
		Map<CardLevelType, List<Pair<CardDependencyType, CardUpgradeNeedType>>> levelNeedMap = new EnumMap<>(CardLevelType.class);
		buildLevelNeedMap(levelNeedMap, CardLevelType.聚魂, null, CardUpgradeNeedType.聚魂);
		buildLevelNeedMap(levelNeedMap, CardLevelType.修炼, null, CardUpgradeNeedType.修炼);
		buildLevelNeedMap(levelNeedMap, CardLevelType.飞升, null, CardUpgradeNeedType.飞升);
		buildLevelNeedMap(levelNeedMap, CardLevelType.仙缘1, CardDependencyType.仙缘1, CardUpgradeNeedType.仙缘);
		buildLevelNeedMap(levelNeedMap, CardLevelType.仙缘2, CardDependencyType.仙缘2, CardUpgradeNeedType.仙缘);
		buildLevelNeedMap(levelNeedMap, CardLevelType.仙缘31, CardDependencyType.仙缘31, CardUpgradeNeedType.仙缘);
		buildLevelNeedMap(levelNeedMap, CardLevelType.仙缘32, CardDependencyType.仙缘32, CardUpgradeNeedType.仙缘);
		buildLevelNeedMap(levelNeedMap, CardLevelType.仙缘33, CardDependencyType.仙缘33, CardUpgradeNeedType.仙缘);
		buildLevelNeedMap(levelNeedMap, CardLevelType.仙缘34, CardDependencyType.仙缘34, CardUpgradeNeedType.仙缘);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技1, null, CardUpgradeNeedType.绝技一);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技1, CardDependencyType.绝技1, CardUpgradeNeedType.绝技一副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技2, null, CardUpgradeNeedType.绝技二);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技2, CardDependencyType.绝技2, CardUpgradeNeedType.绝技二副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技2, CardDependencyType.绝技3, CardUpgradeNeedType.绝技二副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, null, CardUpgradeNeedType.绝技三);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, CardDependencyType.绝技1, CardUpgradeNeedType.绝技三副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, CardDependencyType.绝技2, CardUpgradeNeedType.绝技三副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, CardDependencyType.绝技3, CardUpgradeNeedType.绝技三副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技4, null, CardUpgradeNeedType.绝技四);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技5, null, CardUpgradeNeedType.绝技五);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, null, CardUpgradeNeedType.绝技六);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, CardDependencyType.绝技1, CardUpgradeNeedType.绝技六副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, CardDependencyType.绝技2, CardUpgradeNeedType.绝技六副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, CardDependencyType.绝技3, CardUpgradeNeedType.绝技六副);
		buildLevelNeedMap(levelNeedMap, CardLevelType.云裳1, null, CardUpgradeNeedType.云裳);
		buildLevelNeedMap(levelNeedMap, CardLevelType.云裳2, null, CardUpgradeNeedType.云裳);
		return levelNeedMap;
	}

	private static void buildLevelNeedMap(Map<CardLevelType, List<Pair<CardDependencyType, CardUpgradeNeedType>>> levelNeedMap, CardLevelType levelType,
		CardDependencyType dependencyType, CardUpgradeNeedType upgradeNeedType) {
		List<Pair<CardDependencyType, CardUpgradeNeedType>> levelNeedList = levelNeedMap.get(levelType);
		if (levelNeedList == null) {
			levelNeedList = new ArrayList<>();
			levelNeedMap.put(levelType, levelNeedList);
		}

		levelNeedList.add(Pair.of(dependencyType, upgradeNeedType));
	}

	private static void accumulateNeed(Map<String, Integer> needs, String name, int need) {
		needs.put(name, needs.get(name) + need);
	}
}
