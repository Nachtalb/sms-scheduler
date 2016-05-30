package ch.bbcag.bespin.smsscheduler;

import java.io.Serializable;

public class ScheduledSms implements Serializable {
    public String title;
    public String phoneNr;
    public String smsText;
    public long timestamp;
    public String uniqueId;

    public ScheduledSms(String title, String phoneNr, String smsText, long timestamp, String uniqueId) {
        this.title = title;
        this.phoneNr = phoneNr;
        this.smsText = smsText;
        this.timestamp = timestamp;
        this.uniqueId = uniqueId;
    }
}
