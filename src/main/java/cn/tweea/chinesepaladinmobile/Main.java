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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

public final class Main {
    public static void main(String[] args) {
        String cardInputResourcePath;
        if (args.length >= 1) {
            cardInputResourcePath = args[0];
        } else {
            cardInputResourcePath = "人物列表.xlsx";
        }

        Resource gradeResource = new ClassPathResource("CardPiece.xlsx");
        Resource definitionResource = new ClassPathResource("CardList.xlsx");
        Resource cardInputResource = new FileSystemResource(cardInputResourcePath);
        if (!cardInputResource.exists()) {
            cardInputResource = new ClassPathResource("CardStatus.xlsx");
        }
        WritableResource cardOutputResource = new FileSystemResource(cardInputResourcePath);

        Map<CardLevelType, List<Pair<CardDependencyType, CardUpgradeNeedType>>> levelNeedMap = buildLevelNeedMap();
        Map<Integer, CardGrade> grades = DataFiles.loadCardGrade(gradeResource);
        Map<String, CardDefinition> definitions = DataFiles.loadCardDefinition(definitionResource, grades);
        Map<String, Card> cards = DataFiles.loadCard(cardInputResource, definitions);
        Map<String, Map<String, Integer>> needsMap = computeNeed(cards, levelNeedMap);
        DataFiles.writeNeedsMap(cardInputResource, needsMap, cardOutputResource);
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
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技2, CardDependencyType.绝技21, CardUpgradeNeedType.绝技二副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技2, CardDependencyType.绝技22, CardUpgradeNeedType.绝技二副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, null, CardUpgradeNeedType.绝技三);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, CardDependencyType.绝技31, CardUpgradeNeedType.绝技三副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, CardDependencyType.绝技32, CardUpgradeNeedType.绝技三副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技3, CardDependencyType.绝技33, CardUpgradeNeedType.绝技三副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技4, null, CardUpgradeNeedType.绝技四);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技5, null, CardUpgradeNeedType.绝技五);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, null, CardUpgradeNeedType.绝技六);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, CardDependencyType.绝技61, CardUpgradeNeedType.绝技六副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, CardDependencyType.绝技62, CardUpgradeNeedType.绝技六副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.绝技6, CardDependencyType.绝技63, CardUpgradeNeedType.绝技六副);
        buildLevelNeedMap(levelNeedMap, CardLevelType.云裳, null, CardUpgradeNeedType.云裳);
        buildLevelNeedMap(levelNeedMap, CardLevelType.云裳部件, null, CardUpgradeNeedType.云裳部件);
        buildLevelNeedMap(levelNeedMap, CardLevelType.云裳点化, null, CardUpgradeNeedType.云裳点化);
        buildLevelNeedMap(levelNeedMap, CardLevelType.云裳点化副, null, CardUpgradeNeedType.云裳点化);
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

    private static Map<String, Map<String, Integer>> computeNeed(Map<String, Card> cards,
        Map<CardLevelType, List<Pair<CardDependencyType, CardUpgradeNeedType>>> levelNeedMap) {
        Map<String, Map<String, Integer>> needsMap = new LinkedHashMap<>();

        for (String name : cards.keySet()) {
            Map<String, Integer> needs = new LinkedHashMap<>();
            for (String column : DataFiles.STATIC_RESULT_COLUMNS) {
                needs.put(column, 0);
            }
            needsMap.put(name, needs);
        }
        for (Card card : cards.values()) {
            CardDefinition definition = card.getDefinition();
            String name = definition.getName();
            CardGrade grade = definition.getGrade();
            Map<CardDependencyType, CardDefinition> dependencies = definition.getDependencies();
            int 云裳数量 = definition.get云裳数量();
            int 云裳部件数量 = definition.get云裳部件数量();

            // 初始化累计值
            Map<String, Integer> needs = needsMap.get(name);
            for (CardDefinition dependency : dependencies.values()) {
                // 未出，跳过
                if (dependency.getGrade() == null) {
                    continue;
                }

                needs.put(dependency.getName(), 0);
            }

            for (Map.Entry<CardLevelType, List<Integer>> levelEntry : card.getLevels().entrySet()) {
                CardLevelType levelType = levelEntry.getKey();
                List<Integer> levels = levelEntry.getValue();

                int maxLevelNumber;
                if (levelType == CardLevelType.云裳) {
                    maxLevelNumber = 云裳数量;
                } else if (levelType == CardLevelType.云裳部件) {
                    maxLevelNumber = 云裳部件数量;
                } else if (levelType == CardLevelType.云裳点化 || levelType == CardLevelType.云裳点化副) {
                    maxLevelNumber = 云裳数量 * 2;
                } else {
                    maxLevelNumber = 1;
                }

                for (Pair<CardDependencyType, CardUpgradeNeedType> levelNeed : levelNeedMap.get(levelType)) {
                    CardDependencyType dependencyType = levelNeed.getLeft();
                    CardUpgradeNeedType upgradeNeedType = levelNeed.getRight();

                    int need = 0;
                    for (int levelIndex = 0; levelIndex < maxLevelNumber; levelIndex++) {
                        Integer level;
                        if (levelIndex >= levels.size()) {
                            level = 0;
                        } else {
                            level = levels.get(levelIndex);
                        }
                        need += grade.computeNeed(upgradeNeedType, level);
                    }
                    if (dependencyType == null) {
                        accumulateNeed(needs, "总计", need);
                        accumulateNeed(needs, "自用", need);
                        if (levelType == CardLevelType.云裳 || levelType == CardLevelType.云裳部件) {
                            accumulateNeed(needs, "公用", need);
                        }
                        // 预留云裳数量
                        if (levelType == CardLevelType.云裳 && 云裳数量 == 0) {
                            accumulateNeed(needs, "公用", grade.computeNeed(upgradeNeedType, 0));
                        }
                    } else {
                        CardDefinition dependency = dependencies.get(dependencyType);
                        if (dependency == null) {
                            continue;
                        }
                        // 未出，跳过
                        if (dependency.getGrade() == null) {
                            continue;
                        }

                        String dependencyName = dependency.getName();
                        accumulateNeed(needs, dependencyName, need);
                        Map<String, Integer> dependencyNeeds = needsMap.get(dependencyName);
                        accumulateNeed(dependencyNeeds, "总计", need);
                        accumulateNeed(dependencyNeeds, "他用", need);
                    }
                }
            }
        }

        return needsMap;
    }

    private static void accumulateNeed(Map<String, Integer> needs, String name, int need) {
        needs.put(name, needs.get(name) + need);
    }
}
