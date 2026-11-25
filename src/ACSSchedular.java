import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class ACSSchedular {

  private int quantumTime;
  private int contextSwitchTime;
  private int totalProcesses;

  private int quantumUsed = 0;
  private int currentTime = 0;
  private int csRemaining = 0;
  private int completedProcessCount = 0;
  private int pendingTier = 0;
  private int currentTier = 0;

  private Process pendingProcess = null;
  private Process currentProcess = null;
  private Process previousProcess = null;

  private Tier2Manager tier2Manager = new Tier2Manager();

  private Queue<Process> tier1Queue = new LinkedList<>();
  private List<Process> incomingProcesses = new ArrayList<>();
  private List<Process> historyOfProcesses = new ArrayList<>();

  public ACSSchedular(int quantumTime, int contextSwitchTime, List<Process>inputProcess) {
    this.quantumTime = quantumTime;
    this.contextSwitchTime = contextSwitchTime;
    this.incomingProcesses = new ArrayList<>(inputProcess);
    this.historyOfProcesses = new ArrayList<>(inputProcess);
    this.totalProcesses = inputProcess.size();
  }


  public void run(){

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
      if (csRemaining > 0 ){
       logState("Context Switching to P" + pendingProcess.id + " Target Tier : " + pendingTier );
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

      if (currentProcess == null){
        Process nextProcess = null;
        int nextTier = 0;

        if (!tier1Queue.isEmpty()){
          nextProcess = tier1Queue.poll();
          nextTier = 1;
        }
        else if (!tier2Manager.isEmpty()){
          nextProcess = tier2Manager.getNextP(currentTime);
          nextTier = 2;
        }
        if (nextProcess == null){
          logState("IDLE");
          previousProcess = null;
          currentTime = currentTime + 1;
          continue;
        }
        if (contextSwitchTime > 0 && previousProcess != null && nextProcess != previousProcess){
        pendingProcess = nextProcess;
        pendingTier = nextTier;
        csRemaining = contextSwitchTime;
        continue;
        }
        currentProcess = nextProcess;
        currentTier = nextTier;
        quantumUsed = 0;
      }
      if (currentProcess.firstStartTime == -1) {
        currentProcess.firstStartTime = currentTime;
      }
        logState("Running P" + currentProcess.id+ " (Tier " + currentTier + ")");
        currentProcess.remainingTime = currentProcess.remainingTime - 1;

      if(currentTier == 1){
        quantumUsed = quantumUsed + 1;
      }
      previousProcess = currentProcess;
      currentTime = currentTime + 1;

      if (currentProcess.remainingTime == 0){
        currentProcess.calculateMetrics(currentTime);
        completedProcessCount = completedProcessCount + 1;
        currentProcess = null;
        currentTier = 0;
        quantumUsed = 0;
      }
      else if ( currentTier == 1 && quantumUsed == quantumTime){
        tier2Manager.addProcess(currentProcess, currentTime);
        currentProcess = null;
        currentTier = 0;
        quantumUsed = 0;
      }
    }
    printFinalMetrics();
  }
  private void logState(String statusMessage ){
    tier2Manager.updateAging(currentTime);
    String firstTierState = tier1Queue.isEmpty() ? "[Empty]" : tier1Queue.toString();
    List<Process> tier2List = tier2Manager.getQueue();
    String secondTierState = tier2Manager.isEmpty() ? "[Empty]" : tier2List.toString();

    System.out.println("Time " + currentTime + " | " + statusMessage);
    System.out.println(" Tier 1: " + firstTierState);
    System.out.println(" Tier 2: " + secondTierState);
  }
  private void printFinalMetrics(){
    historyOfProcesses.sort(Comparator.comparingInt((Process p) -> p.id ));
    double totalWT = 0;
    double totalTAT = 0;
    double totalRT = 0;
    System.out.println("\n==========  Test Case ========== ");
    System.out.println("Process | Arrival Time | Burst Time | Initial Priority");
    System.out.println("--------------------------------------------------");

    for (Process p : historyOfProcesses) {
      System.out.printf(
              "P%-5d | %-12d | %-10d | %-16d%n",
              p.id,
              p.arrivalTime,
              p.burstTime,
              p.initialPriority
      );
    }

    System.out.println("\n========== PROCESS METRICS ==========\n");

    System.out.printf(
            "%-10s | %-12s | %-12s | %-15s | %-12s | %-12s%n",
            "Process",
            "Arrival Time",
            "Finish Time",
            "Turnaround Time",
            "Waiting Time",
            "Response Time"
    );

    System.out.println(
            "--------------------------------------------------------------------------"
    );

    for (Process p : historyOfProcesses) {

      totalWT += p.waitingTime;
      totalTAT += p.turnaroundTime;
      totalRT += p.responseTime;

      System.out.printf(
              "P%-9d | %-12d | %-12d | %-15d | %-12d | %-12d%n",
              p.id,
              p.arrivalTime,
              p.completionTime,
              p.turnaroundTime,
              p.waitingTime,
              p.responseTime
      );
    }

    System.out.println(
            "--------------------------------------------------------------------------"
    );

    System.out.printf(
            "%nAverage Turnaround Time: %.2f time units%n",
            totalTAT / totalProcesses
    );

    System.out.printf(
            "Average Waiting Time: %.2f time units%n",
            totalWT / totalProcesses
    );

    System.out.printf(
            "Average Response Time: %.2f time units%n",
            totalRT / totalProcesses
    );
  }
}