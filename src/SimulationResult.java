import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SimulationResult
{
    private final String schedulerName;
    private final int quantum;
    private final int contextSwitchTime;
    private final List<Process> processes;
    private final List<TimeStepLog> logs = new ArrayList<>();

    public SimulationResult(String schedulerName, int quantum, int contextSwitchTime,
                            List<Process> processes)
    {
        this.schedulerName = schedulerName;
        this.quantum = quantum;
        this.contextSwitchTime = contextSwitchTime;
        this.processes = processes;
    }


    public void addLog(TimeStepLog log) {
        if (log.getTime() != logs.size()) {
            throw new IllegalStateException(
                    "Expected log for time " + logs.size() + " but got time " + log.getTime());
        }
        logs.add(log);
    }

    public String getSchedulerName()   { return schedulerName; }
    public int getQuantum()            { return quantum; }
    public int getContextSwitchTime()  { return contextSwitchTime; }
    public List<Process> getProcesses() { return Collections.unmodifiableList(processes); }
    public List<TimeStepLog> getLogs()  { return Collections.unmodifiableList(logs); }
}
