package alimama.dualsim;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import com.mediatek.telephony.SmsManagerEx;

import java.util.ArrayList;

public class SimBase {
    protected int slotId;

    public void callForward(Context mContext, String phoneNumber, boolean autoForward) {
        String callForwardString = autoForward ? "**21*" + phoneNumber + "#" : "##21#";
        this.call(mContext, callForwardString);
    }

    public void call(Context mContext, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.putExtra("com.android.phone.extra.slot", slotId);
        intent.putExtra("simSlot", slotId);
        Uri uri = Uri.fromParts("tel", phoneNumber, "#");
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void sendSMS(Context mContext, Sms sms, Intent intent) {
        ArrayList<String> parts = SimPeer.divideMessage(sms.getMessage());
        int size = parts.size();
        if (size == 1) {
            PendingIntent piSend = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            sendTextMessage(mContext, sms.getPhoneNumber(), sms.getMessage(), piSend, null);
        } else {
            ArrayList<PendingIntent> piSends = new ArrayList<>();
            for (int i = 1; i <= size; i++) {
                PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                piSends.add(pi);
            }
            Log.e("sms_over_length", parts.toString());
            sendMultipartTextMessage(mContext, sms.getPhoneNumber(), parts, piSends, null);
        }
    }

    public void sendSMS(Context mContext, String phoneNumber, String smsText) {
        ArrayList<String> parts = SimPeer.divideMessage(smsText);
        int size = parts.size();
        if (size == 1) {
            sendTextMessage(mContext, phoneNumber, smsText, null, null);
        } else {
            Log.e("sms_over_length", parts.toString());
            sendMultipartTextMessage(mContext, phoneNumber, parts, null, null);
        }
    }


    private boolean sendTextMessage(Context mContext, String toNum, String smsText, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        try {
            if (!Telephony.getInstance(mContext).isDualSIM()) {
                SmsManager.getDefault().sendTextMessage(toNum, null, smsText, sentIntent, deliveryIntent);
            } else if (!SimPeer.sendTextMessage(mContext, slotId, toNum, smsText, sentIntent, deliveryIntent)) {
                SmsManagerEx.getDefault().sendTextMessage(toNum, null, smsText, sentIntent, deliveryIntent, slotId);
            }
            return true;
        } catch (Exception e) {
            Log.e("TelephoneEx", "Exception:" + e.getMessage());
        }
        return false;
    }

    private boolean sendMultipartTextMessage(Context mContext, String toNum, ArrayList<String> smsTextlist, ArrayList<PendingIntent> sentIntentList, ArrayList<PendingIntent> deliveryIntentList) {
        try {
            if (!Telephony.getInstance(mContext).isDualSIM()) {
                SmsManager.getDefault().sendMultipartTextMessage(toNum, null, smsTextlist, sentIntentList, deliveryIntentList);
            } else if (!SimPeer.sendMultipartTextMessage(mContext, slotId, toNum, smsTextlist, sentIntentList, deliveryIntentList)) {
                SmsManagerEx.getDefault().sendMultipartTextMessage(toNum, null, smsTextlist, sentIntentList, deliveryIntentList, slotId);
            }
            return true;
        } catch (Exception e) {
            Log.e("TelephoneEx", "Exception:" + e.getMessage());
        }
        return false;
    }
}
