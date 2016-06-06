package ch.bbcag.bespin.smsscheduler;

/**
 * Row items for the MainActivity SMS ListView.
 */
public class RowItem {

    /**
     * The Title.
     */
    public String title;
    /**
     * The Phone nr.
     */
    public String phoneNr;
    /**
     * The Sms text.
     */
    public String smsText;
    /**
     * The Timestamp.
     */
    public long timestamp;
    /**
     * The Uuid.
     */
    public String UUID;

    /**
     * Instantiates a new Row item.
     *
     * @param title     - the title
     * @param phoneNr   - the phone nr
     * @param smsText   - the sms text
     * @param timestamp - the timestamp
     * @param UUID      - the uuid
     */
    public RowItem(String title, String phoneNr, String smsText, long timestamp, String UUID) {
        this.title = title;
        this.phoneNr = phoneNr;
        this.smsText = smsText;
        this.timestamp = timestamp;
        this.UUID = UUID;
    }
}