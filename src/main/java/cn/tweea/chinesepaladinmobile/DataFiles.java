/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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

public class DataFiles {
	public static Map<Integer, CardGrade> loadCardGrade(Resource source) {
		Map<Integer, CardGrade> grades = new HashMap<>();

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
