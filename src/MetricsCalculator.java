import java.util.ArrayList;
import java.util.List;

public final class MetricsCalculator
{

    private MetricsCalculator() { }

    public static Metrics calculate(SimulationResult result)
    {
        if (result.getProcesses().isEmpty()) {
            throw new IllegalArgumentException("No processes to measure");
        }
        List<Metrics.Row> rows = new ArrayList<>();
        for (Process p : result.getProcesses()) {
            if (p.remainingTime != 0) {
                throw new IllegalStateException("P" + p.id + " did not finish");
            }

            rows.add(new Metrics.Row("P" + p.id, p.arrivalTime, p.burstTime, p.completionTime,
                    p.turnaroundTime, p.waitingTime, p.responseTime));
        }

        int busy = 0, switching = 0, idle = 0;
        for (TimeStepLog log : result.getLogs()) {
            switch (log.getActivity()) {
                case RUNNING -> busy++;
                case CONTEXT_SWITCH -> switching++;
                case IDLE -> idle++;
                case END -> { }
            }
        }
        int total = busy + switching + idle;


        int totalBurst = rows.stream().mapToInt(Metrics.Row::burst).sum();
        if (busy != totalBurst) {
            throw new IllegalStateException(
                    "Logs show " + busy + " running units but total burst time is " + totalBurst);
        }

        return new Metrics(List.copyOf(rows),
                rows.stream().mapToInt(Metrics.Row::waiting).average().orElse(0),
                rows.stream().mapToInt(Metrics.Row::turnaround).average().orElse(0),
                rows.stream().mapToInt(Metrics.Row::response).average().orElse(0),
                total, busy, switching, idle,
                100.0 * busy / total);
    }
}

