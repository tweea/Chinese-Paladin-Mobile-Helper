/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

						card.getLevels().put(levelType, Integer.valueOf(levelString));
					}
					cards.put(name, card);
				}
			}
		} catch (InvalidFormatException | IOException e) {
			throw new ConfigurationRuntimeException(e);
		}

		return cards;
	}

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
				int maxNeedColumnNumber = minNeedColumnNumber + 20;

				Row titleRow = sheet.getRow(0);
				titleRow.createCell(minNeedColumnNumber).setCellValue("总计");
				titleRow.createCell(minNeedColumnNumber + 1).setCellValue("自用");
				titleRow.createCell(minNeedColumnNumber + 2).setCellValue("他用");

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
						if ("总计".equals(needName) || "自用".equals(needName) || "他用".equals(needName)) {
							row.createCell(needColumnNumber).setCellValue(need);
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
