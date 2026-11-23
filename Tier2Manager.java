import java.util.ArrayList;
import java.util.List;

public class Tier2Manager {
    private List<Process> tier2Queue = new ArrayList<>();

    public void addProcess(Process p, int currentTime) 
    {
        tier2Queue.add(p);
        p.tier2EntryTime = currentTime;
    }

    public void updateAging(int currentTime) 
    {
        for (Process p : tier2Queue) 
        {
            int t2Wait = currentTime - p.tier2EntryTime;
            int aging = t2Wait / 4;
            p.effectivePriority = Math.max(1, p.initialPriority - aging);

        }
    }

    public Process getNextP(int currentTime) 
    {
        if (tier2Queue.isEmpty()) 
        {
            return null;
        }
        updateAging(currentTime);
        Process best = tier2Queue.get(0);
        for (Process p : tier2Queue) 
            {
            if (p.effectivePriority < best.effectivePriority) 
            {
                best = p;
            } 
            else if (p.effectivePriority == best.effectivePriority) 
            {
                // first arrived
                if (p.arrivalTime < best.arrivalTime) 
                {
                    best = p;
                }
                // less id
                else if (p.arrivalTime == best.arrivalTime && p.id < best.id)
                {
                    best = p;
                }
            }
        }
        // remove because its non preemptive
        tier2Queue.remove(best);
        return best;

    }

    public boolean isEmpty() 
    {
        return tier2Queue.isEmpty();
    }

    public List<Process> getQueue() 
    {
        return tier2Queue;
    }



}