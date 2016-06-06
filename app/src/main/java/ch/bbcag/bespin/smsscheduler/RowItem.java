package ch.bbcag.bespin.smsscheduler;

public class RowItem {
    public String title;
    public String phoneNr;
    public String smsText;
    public long timestamp;
    public String UUID;

    public RowItem(String title, String phoneNr, String smsText, long timestamp, String UUID) {
        this.title = title;
        this.phoneNr = phoneNr;
        this.smsText = smsText;
        this.timestamp = timestamp;
        this.UUID = UUID;
    }
}