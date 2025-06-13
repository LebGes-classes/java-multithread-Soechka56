package DataBase;

import Workers.Task;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkWithExcel {
    public ArrayList<Task> tasksList = new ArrayList<>();
    private HSSFWorkbook workbook;
    private final String filePath = "src/main/assets/work.xls";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public WorkWithExcel() {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = new HSSFWorkbook(fis);
            tasksList = readTasks(workbook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCellValue(HSSFCell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? dateFormat.format(cell.getDateCellValue())
                    : String.valueOf((cell.getNumericCellValue() == (int)cell.getNumericCellValue())
                    ? (int)cell.getNumericCellValue()
                    : cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    public static ArrayList<Task> readTasks(HSSFWorkbook excelFile) {
        ArrayList<Task> tasks = new ArrayList<>();
        List<List<String>> tableData = readTable(excelFile, "tasks");

        for (int i = 1; i < tableData.size(); i++) {
            List<String> row = tableData.get(i);
            if (row.size() < 3) continue;

            Task task = new Task();
            task.title = row.get(0);
            task.plannedHours = (int) Double.parseDouble(row.get(1));
            task.hoursSpentToday = (int) Double.parseDouble(row.get(2));
            if (row.size() > 3 && !row.get(3).isEmpty()) {
                task.totalHoursSpent = (int) Double.parseDouble(row.get(3));
            } else {
                task.totalHoursSpent = task.hoursSpentToday;
            }
            tasks.add(task);
        }
        return tasks;
    }

    public void saveDayResults() {
        System.out.println("=== Сохранение в Excel началось ===");
        try {
            HSSFSheet sheet = workbook.getSheet("tasks");
            if (sheet == null) {
                sheet = workbook.createSheet("tasks");
                HSSFRow headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Название задачи");
                headerRow.createCell(1).setCellValue("Планируемое время (часов)");
                headerRow.createCell(2).setCellValue("Затрачено сегодня");
            }

            String currentDate = dateFormat.format(new Date());
            HSSFRow headerRow = sheet.getRow(0);

            // Проверяем есть ли столбец с текущей датой
            int dateColIndex = -1;
            for (int i = 3; i < headerRow.getLastCellNum(); i++) {
                HSSFCell cell = headerRow.getCell(i);
                if (cell != null && currentDate.equals(cell.getStringCellValue())) {
                    dateColIndex = i;
                    break;
                }
            }

            // Если нет - создаем новый столбец
            if (dateColIndex == -1) {
                dateColIndex = headerRow.getLastCellNum();
                headerRow.createCell(dateColIndex).setCellValue(currentDate);
            }

            // Обновляем данные задач с поиском и добавлением по названию
            for (Task task : tasksList) {
                boolean rowFound = false;
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    HSSFRow row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    HSSFCell titleCell = row.getCell(0);
                    if (titleCell != null && task.title.equals(titleCell.getStringCellValue())) {
                        rowFound = true;

                        HSSFCell plannedCell = row.getCell(1);
                        if (plannedCell == null) plannedCell = row.createCell(1);
                        plannedCell.setCellValue(task.plannedHours);

                        HSSFCell completedCell = row.getCell(3);
                        if (completedCell == null) completedCell = row.createCell(3);
                        completedCell.setCellValue(task.totalHoursSpent);

                        HSSFCell todayCell = row.getCell(2);
                        if (todayCell == null) todayCell = row.createCell(2);
                        todayCell.setCellValue(task.hoursSpentToday);

                        HSSFCell dateCell = row.getCell(dateColIndex);
                        if (dateCell == null) dateCell = row.createCell(dateColIndex);
                        if (task.hoursSpentToday > 0) {
                            dateCell.setCellValue(task.hoursSpentToday);
                        } else {
                            dateCell.setBlank();
                        }
                        break;
                    }
                }

                if (!rowFound) {
                    int newRowIdx = sheet.getLastRowNum() + 1;
                    HSSFRow newRow = sheet.createRow(newRowIdx);
                    newRow.createCell(0).setCellValue(task.title);
                    newRow.createCell(1).setCellValue(task.plannedHours);
                    newRow.createCell(2).setCellValue(task.hoursSpentToday);
                    newRow.createCell(3).setCellValue(task.totalHoursSpent);
                    newRow.createCell(dateColIndex).setCellValue(task.hoursSpentToday);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                System.out.println("=== Сохранение в Excel завершено ===");
                workbook.write(fos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<List<String>> readTable(HSSFWorkbook excelFile, String sheetName) {
        List<List<String>> tableData = new ArrayList<>();
        try {
            HSSFSheet sheet = excelFile.getSheet(sheetName);
            if (sheet == null) return tableData;

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                HSSFRow row = sheet.getRow(rowIndex);
                List<String> rowData = new ArrayList<>();

                if (row != null) {
                    for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                        rowData.add(getCellValue(row.getCell(colIndex)));
                    }
                }
                tableData.add(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableData;
    }
}