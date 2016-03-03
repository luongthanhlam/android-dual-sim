import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tester-Ali on 12-10-2015.
 */
public class SMS {
    private String ID;
    private String PhoneNumber;
    private String Message;
    private SmsStatus Status;
    private String SendingStatus;

    public static final String EXTRA_SMS_ID = "EXTRA_SMS_ID";
    public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";

    public SMS(Intent intent) {
        this.ID = intent.getStringExtra(EXTRA_SMS_ID);
        PhoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER);
    }

    public SMS(JSONObject json) throws JSONException {
        this.ID = json.getString("sms_id");
        this.PhoneNumber = json.getString("phone_number");
        this.Message = json.getString("message_content");
    }

    public SMS(Map<String, Object> params) {
        this.ID = "" + params.get("sms_id");
        PhoneNumber = "" + params.get("phone_number");
        SendingStatus = "" + params.get("sending_status");
    }

    @Override
    public String toString() {
        return String.format("%s / %s / %s", this.getID(), this.getPhoneNumber(), this.getStatusName());
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public SmsStatus getStatus() {
        return Status;
    }

    public String getStatusName() {
        return Status.name();
    }

    public void setStatus(SmsStatus status) {
        Status = status;
    }

    public String getMessage() {
        return "" + Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getSendingStatus() {
        return SendingStatus;
    }

    public void setSendingStatus(String sendingStatus) {
        SendingStatus = sendingStatus;
    }

    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("sms_id", this.getID());
        data.put("phone_number", this.getPhoneNumber());
        data.put("sending_status", this.getSendingStatus());
        return data;
    }
}