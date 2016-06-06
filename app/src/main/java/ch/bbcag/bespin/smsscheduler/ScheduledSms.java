package ch.bbcag.bespin.smsscheduler;

import java.io.Serializable;

public class ScheduledSms implements Serializable {
    public String title;
    public String phoneNr;
    public String smsText;
    public long timestamp;
    public String UUID;
    public int pendingIntentId;

    public ScheduledSms(String title, String phoneNr, String smsText, long timestamp, String uniqueId, int pendingIntentId) {
        this.title = title;
        this.phoneNr = phoneNr;
        this.smsText = smsText;
        this.timestamp = timestamp;
        this.UUID = uniqueId;
        this.pendingIntentId = pendingIntentId;
    }
}
