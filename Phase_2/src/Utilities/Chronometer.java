package Utilities;

public class Chronometer {

    private long StartTimeInMillis;
    private long ChronometerTime = 0;
    private boolean isStart = false;
    public void start(){
        StartTimeInMillis = System.currentTimeMillis()-ChronometerTime;
        isStart = true;
    }

    public long getUpdatedChronometerTime(){
        if(isStart)
            return ChronometerTime = (System.currentTimeMillis() - StartTimeInMillis);
        return ChronometerTime;
    }

    public void toggle(){
        if(isStart){
            stop();
        } else
            start();
    }

    public void stop(){
        getUpdatedChronometerTime();
        isStart = false;
    }

    public void Reset(){
        ChronometerTime = 0;
        isStart = false;
    }

    public boolean isStart() {
        return isStart;
    }
}
