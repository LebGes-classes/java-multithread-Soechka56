package Services;

import Workers.Task;
import DataBase.WorkWithExcel;

class Init {
    public static WorkWithExcel excel = new WorkWithExcel();
}

public class TasksManager extends Thread {
    final int dayHours = 8;
    public int id;
    private static volatile boolean daySaved = false;
    private static final Object dayLock = new Object();

    public TasksManager(int id) {
        this.id = id;
    }

    public void run() {
        int spentHoursToday = 0;

        // Рабочий день
        while (spentHoursToday < dayHours) {
            for (Task task : Init.excel.tasksList) {
                synchronized (task) {
                    if (task.active && (task.workerID == id || task.workerID == -1)) {
                        int remainingHours = task.plannedHours - getTotalSpentHours(task);
                        if (remainingHours <= 0) {
                            task.active = false;
                            continue;
                        }

                        task.workerID = id;
                        int availableHours = dayHours - spentHoursToday;
                        int workHours = Math.min(remainingHours, availableHours);

                        if (workHours > 0) {
                            try {
                                Thread.sleep(workHours * 10); // Имитация работы
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            task.hoursSpentToday += workHours;
                            task.totalHoursSpent += workHours;
                            spentHoursToday += workHours;

                            System.out.printf("Работник %d: %s - %d часов (всего: %d/%d)%n",
                                    id, task.title, workHours,
                                    getTotalSpentHours(task),
                                    task.plannedHours);

                            if (spentHoursToday >= dayHours) break;
                        }
                    }
                }
            }
        }

        // Сохранение результатов дня
        synchronized (dayLock) {
            if (!daySaved) {
                Init.excel.saveDayResults();
                resetDailyHours();
                daySaved = true;
                System.out.println("=== День завершен, результаты сохранены ===");
            }
        }
    }

    private int getTotalSpentHours(Task task) {
        // Временное решение для подсчета общего времени без поля completedHours
        // В реальной системе нужно либо добавить это поле, либо хранить историю
        return task.totalHoursSpent;
    }

    private void resetDailyHours() {
        for (Task task : Init.excel.tasksList) {
            task.hoursSpentToday = 0;
            if (task.active) {
                task.workerID = -1;
            }
        }
    }
}