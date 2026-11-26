import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MatrixPrinter
{
    private static final String LINE = "+------+---------+" + "-".repeat(90);

    private final PrintStream out;
    private final boolean compact;

    public MatrixPrinter(PrintStream out, boolean compact)
    {
        this.out = out;
        this.compact = compact;
    }

    public void print(SimulationResult result)
    {
        out.println();
        out.println("=== " + result.getSchedulerName() + "  (Q = " + result.getQuantum()
                + ", CS = " + result.getContextSwitchTime() + ") ===");
        printMatrix(result.getLogs());
        printMetrics(MetricsCalculator.calculate(result));
    }

    private void printMatrix(List<TimeStepLog> logs)
    {
        out.println(LINE);
        out.println("| Time | Process | Queues");
        out.println(LINE);
        TimeStepLog previous = null;
        for (TimeStepLog log : logs) {
            for (AgingNote note : log.getAgingNotes()) {
                out.println("| " + note);
            }
            if (!log.getAgingNotes().isEmpty()) {
                out.println(LINE);
            }

            List<String> details = new ArrayList<>();
            if (!compact || isImportant(previous, log)) {
                if (log.getActivity() == TimeStepLog.Activity.CONTEXT_SWITCH)
                {
                    details.add(switchText(log));
                }
                log.getQueues().forEach((name, list) -> details.add(name + ": " + queueText(list)));
            }
            if (details.isEmpty())
            {
                details.add("");
            }

            String process = log.getRunningId() == null ? "" : log.getRunningId();
            if (log.getActivity() == TimeStepLog.Activity.IDLE) {
                process = "Idle";
            }
            out.printf("| %4d | %-7s | %s%n", log.getTime(), process, details.get(0));
            for (int i = 1; i < details.size(); i++) {
                out.printf("| %4s | %-7s | %s%n", "", "", details.get(i));
            }
            out.println(LINE);
            previous = log;
        }
    }

    private boolean isImportant(TimeStepLog previous, TimeStepLog log)
    {
        if (previous == null || log.getActivity() == TimeStepLog.Activity.END)
        {
            return true;
        }
        boolean wasSwitching = previous.getActivity() == TimeStepLog.Activity.CONTEXT_SWITCH;
        if (log.getActivity() == TimeStepLog.Activity.CONTEXT_SWITCH && !wasSwitching)
        {
            return true;
        }
        Set<String> before = entries(previous);
        if (!before.containsAll(entries(log)))
        {
            return true;
        }
        return !wasSwitching && (log.getActivity() != previous.getActivity()
                || (log.getRunningId() != null && !log.getRunningId().equals(previous.getRunningId())));
    }


    private Set<String> entries(TimeStepLog log)
    {
        Set<String> set = new HashSet<>();
        log.getQueues().forEach((name, list) -> list.forEach(p -> set.add(name + "/" + p.id())));
        return set;
    }

    private String switchText(TimeStepLog log)
    {
        if (log.getSwitchFrom() == null)
        {
            return "Context switch loads " + log.getSwitchTo();
        }
        return "Context switch saves " + log.getSwitchFrom() + " and loads " + log.getSwitchTo();
    }

    private String queueText(List<ProcessSnapshot> queue)
    {
        if (queue.isEmpty()) {
            return "[Empty]";
        }
        return "[" + String.join(", ", queue.stream().map(ProcessSnapshot::toString).toList()) + "]";
    }

    private void printMetrics(Metrics m)
    {
        String line = "+---------+---------+-------+--------+------------+---------+----------+";
        out.println();
        out.println(line);
        out.println("| Process | Arrival | Burst | Finish | Turnaround | Waiting | Response |");
        out.println(line);
        for (Metrics.Row r : m.rows()) {
            out.printf("| %-7s | %7d | %5d | %6d | %10d | %7d | %8d |%n", r.id(), r.arrival(),
                    r.burst(), r.finish(), r.turnaround(), r.waiting(), r.response());
        }
        out.println(line);
        out.printf(Locale.US, "Average Turnaround Time: %.2f time units%n", m.averageTurnaround());
        out.printf(Locale.US, "Average Waiting Time:    %.2f time units%n", m.averageWaiting());
        out.printf(Locale.US, "Average Response Time:   %.2f time units%n", m.averageResponse());
        out.printf(Locale.US, "CPU Utilization:         %.2f%% (busy %d, context switch %d, idle %d, total %d)%n",
                m.cpuUtilization(), m.busyTime(), m.contextSwitchTime(), m.idleTime(), m.totalTime());
    }
}
