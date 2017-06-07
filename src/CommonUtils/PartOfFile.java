package CommonUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 14Malgavka on 05.05.2017.
 */
public class PartOfFile extends TimerTask {
    final byte[] data;
    int packageNumber;
    boolean isReceived;
    ClientCallBack callBack;
    Timer timer;

    public byte[] getData() {
        return data;
    }

    public int getPackageNumber() {
        return packageNumber;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived() {
        isReceived = true;
    }

    public PartOfFile(byte[] data, int packageNumber, ClientCallBack callBack) {
        this.data = data;
        this.packageNumber = packageNumber;
        isReceived = false;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        if (!isReceived)
            callBack.resend(this);
        else
            timer.cancel();
    }

    @Override
    public String toString() {
        return "PartOfFile{" +
                "packageNumber=" + packageNumber +
                ", isReceived=" + isReceived +
                '}';
    }

    public void startTimer() {
        TimerTask timerTask = this;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 100, 1000);
    }
}
