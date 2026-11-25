import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ACSScheduler implements Scheduler
{

    private int quantumTime;
    private int contextSwitchTime;
    private int totalProcesses;

    private int quantumUsed;
    private int currentTime;
    private int csRemaining;
    private int completedProcessCount;
    private int pendingTier;
    private int currentTier;

    private Process pendingProcess;
    private Process currentProcess;
    private Process previousProcess;

    private Tier2Manager tier2Manager;

    private Queue<Process> tier1Queue;
    private List<Process> incomingProcesses;
    private SimulationResult result;


    private Map<String, List<ProcessSnapshot>> selectionQueues;
    private List<AgingNote> selectionNotes = new ArrayList<>();

    @Override
    public SimulationResult run(List<Process> inputProcess, int quantum, int cs)
    {
        if (inputProcess.isEmpty() || quantum <= 0 || cs < 0)
        {
            throw new IllegalArgumentException("Need at least one process, quantum > 0 and CS >= 0");
        }
        quantumTime = quantum;
        contextSwitchTime = cs;
        totalProcesses = inputProcess.size();
        quantumUsed = currentTime = csRemaining = completedProcessCount = pendingTier = currentTier = 0;
        pendingProcess = currentProcess = previousProcess = null;
        tier2Manager = new Tier2Manager();
        tier1Queue = new LinkedList<>();
        selectionQueues = null;
        selectionNotes.clear();

        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcess) {
            processes.add(p.copy());
        }
        incomingProcesses = new ArrayList<>(processes);
        result = new SimulationResult("ACS", quantum, cs, processes);

        incomingProcesses.sort(
                Comparator.comparingInt((Process p) -> p.arrivalTime).thenComparingInt(p -> p.id)
        );

        while (completedProcessCount < totalProcesses){
            Iterator<Process> processIterator = incomingProcesses.iterator();
            while(processIterator.hasNext()){
                Process p = processIterator.next();
                if (p.arrivalTime == currentTime){
                    tier1Queue.offer(p);
                    processIterator.remove();
                }
            }
            if (csRemaining > 0 )
            {
                String from = previousProcess.remainingTime == 0 ? null : "P" + previousProcess.id;
                log(TimeStepLog.contextSwitch(currentTime, from, "P" + pendingProcess.id, queuesForLog()));
                csRemaining = csRemaining - 1;
                currentTime = currentTime + 1;

                if (csRemaining == 0){
                    currentProcess = pendingProcess;
                    currentTier = pendingTier;
                    pendingTier = 0;
                    pendingProcess = null;
                    quantumUsed = 0;
                }
                continue;
            }

            if (currentProcess == null)
            {
                Process nextProcess = null;
                int nextTier = 0;

                if (!tier1Queue.isEmpty()){
                    selectionQueues = currentQueues();
                    nextProcess = tier1Queue.poll();
                    nextTier = 1;
                }
                else if (!tier2Manager.isEmpty()){
                    tier2Manager.updateAging(currentTime);
                    selectionQueues = currentQueues();
                    addAgingNotes();
                    nextProcess = tier2Manager.getNextP(currentTime);
                    nextTier = 2;
                }
                if (nextProcess == null)
                {
                    log(TimeStepLog.idle(currentTime, currentQueues()));
                    previousProcess = null;
                    currentTime = currentTime + 1;
                    continue;
                }
                if (contextSwitchTime > 0 && previousProcess != null && nextProcess != previousProcess)
                {
                    pendingProcess = nextProcess;
                    pendingTier = nextTier;
                    csRemaining = contextSwitchTime;
                    continue;
                }
                currentProcess = nextProcess;
                currentTier = nextTier;
                quantumUsed = 0;
            }
            if (currentProcess.firstStartTime == -1)
            {
                currentProcess.firstStartTime = currentTime;
            }
            log(TimeStepLog.running(currentTime, "P" + currentProcess.id, queuesForLog()));
            currentProcess.remainingTime = currentProcess.remainingTime - 1;

            if(currentTier == 1){
                quantumUsed = quantumUsed + 1;
            }
            previousProcess = currentProcess;
            currentTime = currentTime + 1;

            if (currentProcess.remainingTime == 0)
            {
                currentProcess.calculateMetrics(currentTime);
                completedProcessCount = completedProcessCount + 1;
                currentProcess = null;
                currentTier = 0;
                quantumUsed = 0;
            }
            else if ( currentTier == 1 && quantumUsed == quantumTime)
            {
                tier2Manager.addProcess(currentProcess, currentTime);
                currentProcess = null;
                currentTier = 0;
                quantumUsed = 0;
            }
        }
        log(TimeStepLog.end(currentTime, currentQueues()));
        return result;
    }

    private void log(TimeStepLog entry)
    {
        selectionNotes.forEach(entry::addAgingNote);
        selectionNotes.clear();
        result.addLog(entry);
    }

    private Map<String, List<ProcessSnapshot>> currentQueues()
    {
        Map<String, List<ProcessSnapshot>> queues = new LinkedHashMap<>();
        queues.put("Tier 1", tier1Queue.stream().map(ProcessSnapshot::of).toList());
        queues.put("Tier 2", tier2Manager.getQueue().stream().map(ProcessSnapshot::of).toList());
        return queues;
    }


    private Map<String, List<ProcessSnapshot>> queuesForLog()
    {
        Map<String, List<ProcessSnapshot>> queues =
                selectionQueues != null ? selectionQueues : currentQueues();
        selectionQueues = null;
        return queues;
    }


    private void addAgingNotes() {
        if (tier2Manager.getQueue().size() < 2)
        {
            return;
        }
        for (Process p : tier2Manager.getQueue()) {
            selectionNotes.add(new AgingNote("P" + p.id, p.tier2EntryTime, currentTime,
                    p.initialPriority, p.effectivePriority));
        }
    }
}