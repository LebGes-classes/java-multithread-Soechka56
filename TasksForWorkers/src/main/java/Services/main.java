package Services;

public class main {
    public static void main(String[] args) {

        for (int i = 0; i < 5; ++i) {
            TasksManager task = new TasksManager(1);
            task.start();

            TasksManager task2 = new TasksManager(2);
            task2.start();

            TasksManager task3 = new TasksManager(3);
            task3.start();

            TasksManager task4 = new TasksManager(4);
            task4.start();

            TasksManager task5 = new TasksManager(5);
            task5.start();

            TasksManager task6 = new TasksManager(6);
            task6.start();
        }
    }
}