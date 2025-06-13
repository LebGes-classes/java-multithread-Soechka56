package Workers;

public class Task {
    public volatile String title;
    public volatile int plannedHours;
    public volatile int hoursSpentToday;
    public volatile Boolean active = true;

    public volatile int workerID = -1;
    //в целом должно хватить атрибутов

    public volatile int totalHoursSpent = 0;

    @Override
    public String toString() {
        return "WorkerID" + workerID + " Task" +
                "title='" + title + '\'' +
                ", plannedHours=" + plannedHours +
                ", hoursSpentToday=" + hoursSpentToday +
                '}';
    }
}