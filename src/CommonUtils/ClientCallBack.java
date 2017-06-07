package CommonUtils;

/**
 * Created by Alex on 27.05.2017.
 */
public interface ClientCallBack {
    void onRead(byte[] data);
    void resend(PartOfFile partOfFile);
}
