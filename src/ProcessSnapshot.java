public record ProcessSnapshot(String id, int remainingTime, int priority)
{

        public static ProcessSnapshot of(Process p)
        {
            return new ProcessSnapshot("P" + p.id, p.remainingTime, p.effectivePriority);
        }

        @Override
        public String toString()
        {
            return id + " (remaining: " + remainingTime + ", Priority: " + priority + ")";
        }
    }


