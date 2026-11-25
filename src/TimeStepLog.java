import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class TimeStepLog
{
    public enum Activity
    {
        RUNNING, CONTEXT_SWITCH, IDLE, END
    }

    private final int time;
    private final Activity activity;
    private final String runningId;
    private final String switchFrom;
    private final String switchTo;
    private final Map<String, List<ProcessSnapshot>> queues;
    private final List<AgingNote> agingNotes = new ArrayList<>();

    private TimeStepLog(int time, Activity activity, String runningId, String switchFrom,
                        String switchTo, Map<String, List<ProcessSnapshot>> queues) {
        this.time = time;
        this.activity = activity;
        this.runningId = runningId;
        this.switchFrom = switchFrom;
        this.switchTo = switchTo;

        Map<String, List<ProcessSnapshot>> copy = new LinkedHashMap<>();
        queues.forEach((name, list) -> copy.put(name, List.copyOf(list)));
        this.queues = Collections.unmodifiableMap(copy);
    }

    public static TimeStepLog running(int time, String id, Map<String, List<ProcessSnapshot>> queues)
    {
        return new TimeStepLog(time, Activity.RUNNING, id, null, null, queues);
    }


    public static TimeStepLog contextSwitch(int time, String from, String to,
                                            Map<String, List<ProcessSnapshot>> queues)
    {
        return new TimeStepLog(time, Activity.CONTEXT_SWITCH, null, from, to, queues);
    }

    public static TimeStepLog idle(int time, Map<String, List<ProcessSnapshot>> queues)
    {
        return new TimeStepLog(time, Activity.IDLE, null, null, null, queues);
    }


    public static TimeStepLog end(int time, Map<String, List<ProcessSnapshot>> queues)
    {
        return new TimeStepLog(time, Activity.END, null, null, null, queues);
    }

    public void addAgingNote(AgingNote note) { agingNotes.add(note); }

    public int getTime()           { return time; }
    public Activity getActivity()  { return activity; }
    public String getRunningId()   { return runningId; }
    public String getSwitchFrom()  { return switchFrom; }
    public String getSwitchTo()    { return switchTo; }
    public Map<String, List<ProcessSnapshot>> getQueues() { return queues; }
    public List<AgingNote> getAgingNotes() { return Collections.unmodifiableList(agingNotes); }
}
