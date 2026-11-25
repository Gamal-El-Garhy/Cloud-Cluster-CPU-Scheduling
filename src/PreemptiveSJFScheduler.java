import java.util.*;

public class PreemptiveSJFScheduler implements Scheduler
{

    @Override
    public SimulationResult run(List<Process> input, int quantum, int cs)
    {
        if (input.isEmpty() || cs < 0) {
            throw new IllegalArgumentException("Need at least one process and CS >= 0");
        }
        List<Process> processes = new ArrayList<>();
        for (Process p : input) {
            processes.add(p.copy());
        }
        SimulationResult result = new SimulationResult("Preemptive SJF", quantum, cs, processes);

        List<Process> order = new ArrayList<>(processes);
        order.sort(Comparator.comparingInt((Process p) -> p.arrivalTime).thenComparingInt(p -> p.id));

        int time = 0, completed = 0;
        Process previous = null;

        while (completed < order.size())
        {
            Process next = shortest(order, time);

            if (next == null) {
                result.addLog(TimeStepLog.idle(time, readyQueue(order, time, null)));
                time++;
                previous = null;
                continue;
            }


            if (previous != null && previous != next && cs > 0)
            {
                String from = previous.remainingTime == 0 ? null : "P" + previous.id;
                for (int c = 0; c < cs; c++) {
                    result.addLog(TimeStepLog.contextSwitch(time, from, "P" + next.id,
                            readyQueue(order, time, null)));
                    time++;
                }
                previous = next;
                continue;
            }

            result.addLog(TimeStepLog.running(time, "P" + next.id,
                    readyQueue(order, time, previous == next ? next : null)));
            if (next.firstStartTime == -1) {
                next.firstStartTime = time;
            }
            next.remainingTime--;
            time++;
            previous = next;
            if (next.remainingTime == 0) {
                next.calculateMetrics(time);
                completed++;
            }
        }
        result.addLog(TimeStepLog.end(time, readyQueue(order, time, null)));
        return result;
    }


    private Process shortest(List<Process> order, int time)
    {
        Process best = null;
        for (Process p : order)
        {
            if (p.arrivalTime <= time && p.remainingTime > 0
                    && (best == null || p.remainingTime < best.remainingTime))
            {
                best = p;
            }
        }
        return best;
    }

    private Map<String, List<ProcessSnapshot>> readyQueue(List<Process> order, int time, Process exclude)
    {
        List<ProcessSnapshot> ready = new ArrayList<>();
        for (Process p : order)
        {
            if (p != exclude && p.arrivalTime <= time && p.remainingTime > 0)
            {
                ready.add(ProcessSnapshot.of(p));
            }
        }
        return Map.of("Ready Queue", ready);
    }
}