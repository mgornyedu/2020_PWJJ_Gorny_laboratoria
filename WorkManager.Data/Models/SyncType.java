package Models;

public class SyncType {
    public static final int None = 0;
    public static final int SyncWorkItem = 1;
    public static final int SyncBillingPeriod = 2;
    public static final int SyncProject = 4;

    public static boolean HasFlag(int value, int flag){
        return  (value & flag) == flag;
    }
}
