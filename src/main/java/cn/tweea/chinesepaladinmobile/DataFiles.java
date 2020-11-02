/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

public class DataFiles {
    public static Map<Integer, CardGrade> loadCardGrade(Resource source) {
        Map<Integer, CardGrade> grades = new LinkedHashMap<>();

        try (Workbook workbook = WorkbookFactory.create(source.getInputStream())) {
            for (Sheet sheet : workbook) {
                CardGrade grade = new CardGrade(Integer.parseInt(sheet.getSheetName()));

                Map<String, Integer> upgradeNeedTypeNameIndex = buildTitleIndex(sheet, 1);
                int maxRowNumber = getRowNumber(sheet, 1, 0, "合计") - 1;
                for (Map.Entry<String, Integer> upgradeNeedTypeNameEntry : upgradeNeedTypeNameIndex.entrySet()) {
                    String upgradeNeedTypeName = upgradeNeedTypeNameEntry.getKey();
                    Integer upgradeNeedTypeColumnNumber = upgradeNeedTypeNameEntry.getValue();

                    CardUpgradeNeedType upgradeNeedType = CardUpgradeNeedType.valueOf(upgradeNeedTypeName);
                    if (upgradeNeedType == null) {
                        throw new ConfigurationRuntimeException("CardUpgradeNeedType");
                    }

                    int[] upgradeNeeds = new int[maxRowNumber];
                    for (int rowNumber = 1; rowNumber <= maxRowNumber; rowNumber++) {
                        Row row = sheet.getRow(rowNumber);
                        String upgradeNeedsItemString = getCellStringValue(row, upgradeNeedTypeColumnNumber);
                        if (StringUtils.isBlank(upgradeNeedsItemString)) {
                            upgradeNeeds = Arrays.copyOf(upgradeNeeds, rowNumber - 1);
                            break;
                        }

                        upgradeNeeds[rowNumber - 1] = Integer.parseInt(upgradeNeedsItemString);
                    }

                    grade.addUpgradeNeed(new CardUpgradeNeed(upgradeNeedType, upgradeNeeds));
                }

                grades.put(grade.getGrade(), grade);
            }
        } catch (InvalidFormatException | IOException e) {
            throw new ConfigurationRuntimeException(e);
        }

        return grades;
    }

    public static Map<String, CardDefinition> loadCardDefinition(Resource source, Map<Integer, CardGrade> grades) {
        Map<String, CardDefinition> definitions = new LinkedHashMap<>();

        try (Workbook workbook = WorkbookFactory.create(source.getInputStream())) {
            for (Sheet sheet : workbook) {
                CardGrade grade = grades.get(Integer.valueOf(sheet.getSheetName()));

                int maxRowNumber = getRowNumber(sheet, 1, 0, "合计") - 1;
                for (int rowNumber = 1; rowNumber <= maxRowNumber; rowNumber++) {
                    Row row = sheet.getRow(rowNumber);
                    String name = getCellStringValue(row, 0);
                    if (name == null) {
                        throw new ConfigurationRuntimeException("CardDefinitionName");
                    }

                    definitions.put(name, new CardDefinition(name, grade));
                }
            }
            for (Sheet sheet : workbook) {
                Map<String, Integer> dependencyTypeNameIndex = buildTitleIndex(sheet, 3);
                Integer 云裳ColumnNumber = dependencyTypeNameIndex.remove("云裳");
                Integer 云裳部件ColumnNumber = dependencyTypeNameIndex.remove("云裳部件");
                Map<CardDependencyType, Integer> dependencyTypeIndex = new EnumMap<>(CardDependencyType.class);
                for (Map.Entry<String, Integer> dependencyTypeNameEntry : dependencyTypeNameIndex.entrySet()) {
                    String dependencyTypeName = dependencyTypeNameEntry.getKey();
                    Integer dependencyTypeColumnNumber = dependencyTypeNameEntry.getValue();

                    CardDependencyType dependencyType = CardDependencyType.valueOf(dependencyTypeName);
                    if (dependencyType == null) {
                        throw new ConfigurationRuntimeException("CardDependencyType");
                    }

                    dependencyTypeIndex.put(dependencyType, dependencyTypeColumnNumber);
                }

                int maxRowNumber = getRowNumber(sheet, 1, 0, "合计") - 1;
                for (int rowNumber = 1; rowNumber <= maxRowNumber; rowNumber++) {
                    Row row = sheet.getRow(rowNumber);
                    String name = getCellStringValue(row, 0);
                    CardDefinition definition = definitions.get(name);

                    for (Map.Entry<CardDependencyType, Integer> dependencyTypeEntry : dependencyTypeIndex.entrySet()) {
                        CardDependencyType dependencyType = dependencyTypeEntry.getKey();
                        Integer dependencyTypeColumnNumber = dependencyTypeEntry.getValue();

                        String dependencyName = getCellStringValue(row, dependencyTypeColumnNumber);
                        if (dependencyName == null) {
                            continue;
                        }

                        CardDefinition dependency = definitions.get(dependencyName);
                        if (dependency == null) {
                            throw new ConfigurationRuntimeException("CardDependencyName");
                        }

                        definition.getDependencies().put(dependencyType, dependency);
                    }

                    if (云裳ColumnNumber != null) {
                        String 云裳数量String = getCellStringValue(row, 云裳ColumnNumber);
                        if (云裳数量String != null) {
                            definition.set云裳数量(Integer.parseInt(云裳数量String));
                        }
                    }
                    if (云裳部件ColumnNumber != null) {
                        String 云裳部件数量String = getCellStringValue(row, 云裳部件ColumnNumber);
                        if (云裳部件数量String != null) {
                            definition.set云裳部件数量(Integer.parseInt(云裳部件数量String));
                        }
                    }
                }
            }
        } catch (InvalidFormatException | IOException e) {
            throw new ConfigurationRuntimeException(e);
        }

        return definitions;
    }

    public static Map<String, Card> loadCard(Resource source, Map<String, CardDefinition> definitions) {
        Map<String, Card> cards = new LinkedHashMap<>();

        try (Workbook workbook = WorkbookFactory.create(source.getInputStream())) {
            for (Sheet sheet : workbook) {
                Map<String, Integer> levelTypeNameIndex = buildTitleIndex(sheet, 3);
                Map<CardLevelType, Integer> levelTypeIndex = new EnumMap<>(CardLevelType.class);
                for (Map.Entry<String, Integer> levelTypeNameEntry : levelTypeNameIndex.entrySet()) {
                    String levelTypeName = levelTypeNameEntry.getKey();
                    Integer levelTypeColumnNumber = levelTypeNameEntry.getValue();

                    CardLevelType levelType = CardLevelType.valueOf(levelTypeName);
                    if (levelType == null) {
                        throw new ConfigurationRuntimeException("CardLevelType");
                    }

                    levelTypeIndex.put(levelType, levelTypeColumnNumber);
                }

                int maxRowNumber = getRowNumber(sheet, 1, 0, "合计") - 1;
                for (int rowNumber = 1; rowNumber <= maxRowNumber; rowNumber++) {
                    Row row = sheet.getRow(rowNumber);
                    String name = getCellStringValue(row, 0);
                    if (name == null) {
                        throw new ConfigurationRuntimeException("CardName");
                    }

                    CardDefinition definition = definitions.get(name);
                    if (definition == null) {
                        throw new ConfigurationRuntimeException("CardName");
                    }

                    Card card = new Card(definition);
                    for (Map.Entry<CardLevelType, Integer> levelTypeEntry : levelTypeIndex.entrySet()) {
                        CardLevelType levelType = levelTypeEntry.getKey();
                        Integer levelTypeColumnNumber = levelTypeEntry.getValue();

                        String levelString = getCellStringValue(row, levelTypeColumnNumber);
                        if (StringUtils.isBlank(levelString)) {
                            levelString = "0";
                        }

                        String[] levelStringParts = StringUtils.split(levelString, ',');
                        List<Integer> levels = new ArrayList<>();
                        for (String levelStringPart : levelStringParts) {
                            levels.add(Integer.valueOf(levelStringPart));
                        }
                        card.getLevels().put(levelType, levels);
                    }
                    cards.put(name, card);
                }
            }
        } catch (InvalidFormatException | IOException e) {
            throw new ConfigurationRuntimeException(e);
        }

        return cards;
    }

    public static final String[] STATIC_RESULT_COLUMNS = {
        "总计", "自用", "他用", "公用", "现有", "差额", "差额排名"
    };

    public static void writeNeedsMap(Resource source, Map<String, Map<String, Integer>> needsMap, WritableResource target) {
        try (Workbook workbook = WorkbookFactory.create(source.getInputStream())) {
            for (Sheet sheet : workbook) {
                Map<String, Integer> levelTypeNameIndex = buildTitleIndex(sheet, 3);
                int maxLevelTypeColumnNumber = 2;
                for (Integer levelTypeColumnNumber : levelTypeNameIndex.values()) {
                    if (levelTypeColumnNumber > maxLevelTypeColumnNumber) {
                        maxLevelTypeColumnNumber = levelTypeColumnNumber;
                    }
                }
                int minNeedColumnNumber = maxLevelTypeColumnNumber + 2;
                int maxNeedColumnNumber = minNeedColumnNumber + 25;

                Map<String, String> staticNeedColumnReferenceIndex = new LinkedHashMap<>();
                Row titleRow = sheet.getRow(0);
                int staticNeedColumnNumber = minNeedColumnNumber;
                for (String column : STATIC_RESULT_COLUMNS) {
                    staticNeedColumnReferenceIndex.put(column, CellReference.convertNumToColString(staticNeedColumnNumber));
                    titleRow.createCell(staticNeedColumnNumber).setCellValue(column);
                    staticNeedColumnNumber++;
                }

                int maxRowNumber = getRowNumber(sheet, 1, 0, "合计") - 1;
                for (int rowNumber = 1; rowNumber <= maxRowNumber; rowNumber++) {
                    Row row = sheet.getRow(rowNumber);
                    String name = getCellStringValue(row, 0);
                    if (name == null) {
                        throw new ConfigurationRuntimeException("CardName");
                    }

                    Map<String, Integer> needs = needsMap.get(name);
                    if (needs == null) {
                        throw new ConfigurationRuntimeException("CardName");
                    }

                    int needColumnNumber = minNeedColumnNumber;
                    for (Map.Entry<String, Integer> needEntry : needs.entrySet()) {
                        String needName = needEntry.getKey();
                        Integer need = needEntry.getValue();
                        if ("现有".equals(needName)) {
                            needColumnNumber++;
                        } else if ("差额".equals(needName)) {
                            StringBuilder formula = new StringBuilder();
                            formula.append("MAX(");
                            formula.append(staticNeedColumnReferenceIndex.get("总计"));
                            formula.append(rowNumber + 1);
                            formula.append(" - ");
                            formula.append(staticNeedColumnReferenceIndex.get("现有"));
                            formula.append(rowNumber + 1);
                            formula.append(", 0)");
                            row.createCell(needColumnNumber).setCellFormula(formula.toString());
                            needColumnNumber++;
                        } else if ("差额排名".equals(needName)) {
                            StringBuilder formula = new StringBuilder();
                            formula.append("RANK(");
                            formula.append(staticNeedColumnReferenceIndex.get("差额"));
                            formula.append(rowNumber + 1);
                            formula.append(", $");
                            formula.append(staticNeedColumnReferenceIndex.get("差额"));
                            formula.append("$2:$");
                            formula.append(staticNeedColumnReferenceIndex.get("差额"));
                            formula.append("$");
                            formula.append(maxRowNumber + 1);
                            formula.append(")");
                            row.createCell(needColumnNumber).setCellFormula(formula.toString());
                            needColumnNumber++;
                        } else if (ArrayUtils.contains(STATIC_RESULT_COLUMNS, needName)) {
                            if (need == 0) {
                                row.createCell(needColumnNumber);
                            } else {
                                row.createCell(needColumnNumber).setCellValue(need);
                            }
                            needColumnNumber++;
                        } else {
                            if (need == 0) {
                                continue;
                            }
                            row.createCell(needColumnNumber).setCellValue(needName);
                            needColumnNumber++;
                            row.createCell(needColumnNumber).setCellValue(need);
                            needColumnNumber++;
                        }
                    }
                    for (; needColumnNumber <= maxNeedColumnNumber; needColumnNumber++) {
                        Cell cell = row.getCell(needColumnNumber);
                        if (cell == null) {
                            break;
                        }

                        row.removeCell(cell);
                    }
                }
            }
            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            workbook.write(target.getOutputStream());
        } catch (InvalidFormatException | IOException e) {
            throw new ConfigurationRuntimeException(e);
        }
    }

    private static Map<String, Integer> buildTitleIndex(Sheet sheet, int startColumnNumber) {
        Map<String, Integer> titleIndex = new LinkedHashMap<>();

        Row titleRow = sheet.getRow(0);
        for (int columnNumber = startColumnNumber;; columnNumber++) {
            String title = getCellStringValue(titleRow, columnNumber);
            if (StringUtils.isBlank(title)) {
                break;
            }

            titleIndex.put(title, columnNumber);
        }

        return titleIndex;
    }

    private static int getRowNumber(Sheet sheet, int startRowNumber, int columnNumber, String cellValue) {
        for (int rowNumber = startRowNumber;; rowNumber++) {
            Row row = sheet.getRow(rowNumber);
            if (row == null) {
                return -1;
            }

            if (StringUtils.equals(getCellStringValue(row, columnNumber), cellValue)) {
                return rowNumber;
            }
        }
    }

    private static String getCellStringValue(Row row, int columnNumber) {
        Cell cell = row.getCell(columnNumber);
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellTypeEnum();
        if (cellType == CellType.NUMERIC) {
            cell.setCellType(CellType.STRING);
        }

        String cellValue = cell.getStringCellValue();
        if (cellValue == null) {
            return null;
        }

        return cellValue.trim();
    }
}
