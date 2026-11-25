import java.util.List;
//ابقي اعدل الكلاس ده يا تمير لما تيجي تعمل run لكلو في الاخر
public interface Scheduler
{
    /**
     * @param input             the original processes (do not modify them, use copy())
     * @param quantum           Round Robin quantum (SJF ignores it)
     * @param contextSwitchTime length of one context switch in time units
     */

    SimulationResult run(List<Process> input, int quantum, int contextSwitchTime);
}
