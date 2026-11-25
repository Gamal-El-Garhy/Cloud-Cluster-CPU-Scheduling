import java.util.List;

public record Metrics(List<Row> rows,
                      double averageWaiting,
                      double averageTurnaround,
                      double averageResponse,
                      int totalTime,
                      int busyTime,
                      int contextSwitchTime,
                      int idleTime,
                      double cpuUtilization) {

    public record Row(String id, int arrival, int burst, int finish,
                      int turnaround, int waiting, int response) { }
}
