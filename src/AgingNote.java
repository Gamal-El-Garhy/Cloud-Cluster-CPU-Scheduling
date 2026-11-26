public record AgingNote(String id, int waitStart, int now, int basePriority, int effectivePriority)
{
    public static final int INTERVAL = 4;

    @Override
    public String toString()
    {
        int waited = now - waitStart;
        String text = id + ": Waited from T=" + waitStart + " to T=" + now + " (" + waited + " units).";
        int boost = waited / INTERVAL;
        if (boost == 0)
        {
            return text + " Effective Priority = " + effectivePriority;
        }
        String limit = basePriority - boost == effectivePriority ? "" : " (limited by the minimum of 1)";
        return text + " Aging: floor(" + waited + "/" + INTERVAL + ") = " + boost
                + ". Effective Priority = " + basePriority + " - " + boost + " -> " + effectivePriority + limit;
    }
}
