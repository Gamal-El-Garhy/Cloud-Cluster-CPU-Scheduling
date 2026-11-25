public class Process {
    int id, arrivalTime, burstTime, remainingTime;
    int waitingTime, turnaroundTime, completionTime, responseTime;
    int firstStartTime = -1;

    int initialPriority;
    int effectivePriority;
    int tier2EntryTime = -1; 
   

    // constructor 1 for SRTF
    public Process(int id, int arrivalTime, int burstTime) {
        this(id, arrivalTime, burstTime, 0);
    }

    // constructor 2 for ACS
    public Process(int id, int arrivalTime, int burstTime, int initialPriority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.initialPriority = initialPriority;
        this.effectivePriority = initialPriority;
    }


    public Process copy()
    {
        return new Process(id, arrivalTime, burstTime, initialPriority);
    }

    public void calculateMetrics(int finishTime) {
        this.completionTime = finishTime;
        this.turnaroundTime = this.completionTime - this.arrivalTime;
        this.waitingTime = this.turnaroundTime - this.burstTime;
        this.responseTime = this.firstStartTime - this.arrivalTime;
    }

    @Override
    public String toString() {
        return "P" + id + " (remaining: " + remainingTime + ", Priority: " + effectivePriority + ")";
    }
}