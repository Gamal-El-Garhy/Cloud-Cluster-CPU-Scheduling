import java.util.List;

public interface Scheduler
{
    /**
     * @param input             the original processes (do not modify them, use copy())
     * @param quantum           Round Robin quantum (SJF ignores it)
     * @param contextSwitchTime length of one context switch in time units
     */

    SimulationResult run(List<Process> input, int quantum, int contextSwitchTime);
}
