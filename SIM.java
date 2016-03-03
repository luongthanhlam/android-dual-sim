import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SIM {
    public int slotId;
    public String deviceId;
    public String network;
    public String serial;
    public boolean ready;
    public int totalReceived;
    public int totalDone;
    public long timestamp;
    public int totalFailed;
    public int totalRetry;

    public SIM(int slotId) {
        this.slotId = slotId;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void clear() {
        this.totalFailed = 0;
        this.totalReceived = 0;
        this.totalRetry = 0;
        this.totalDone = 0;
    }

    public int getTotalRetry() {
        return totalRetry;
    }

    public boolean isBlocked() {
        return this.totalRetry > 0;
    }

    public void setTotalRetry(int totalRetry) {
        this.totalRetry = totalRetry;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }

    public int getTotalDone() {
        return totalDone;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTotalDone(int totalDone) {
        this.totalDone = totalDone;
    }

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
        try{
            mContext.startActivity(intent);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void sendSMS(Context mContext, SMS sms, Intent intent) {
        ArrayList<String> parts = this.divideMessage(sms.getMessage());
        int size = parts.size();
        if (size == 1) {
            PendingIntent piSend = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            this.sendTextMessage(mContext, this.slotId, sms.getPhoneNumber(), sms.getMessage(), piSend, null);
        } else {
            ArrayList<PendingIntent> piSends = new ArrayList<>();
            for (int i = 1; i <= size; i++) {
                PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                piSends.add(pi);
            }
            Log.e("sms_over_length", parts.toString());
            this.sendMultipartTextMessage(mContext, slotId, sms.getPhoneNumber(), parts, piSends, null);
        }
    }

    public void sendSMS(Context mContext, String phoneNumber, String smsText) {
        ArrayList<String> parts = this.divideMessage(smsText);
        int size = parts.size();
        if (size == 1) {
            this.sendTextMessage(mContext, slotId, phoneNumber, smsText, null, null);
        } else {
            Log.e("sms_over_length", parts.toString());
            this.sendMultipartTextMessage(mContext, slotId, phoneNumber, parts, null, null);
        }
    }

    private ArrayList<String> divideMessage(String message) {
        ArrayList<String> result = new ArrayList<>();
        do {
            String pattern = "(.{0,160})((?!\\w).*)";

            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(message);
            if (m.find() && m.groupCount() >= 2) {
                result.add(m.group(1));
                message = m.group(2);
            } else {
                result.add(message);
                break;
            }
        } while (message.length() > 0);

        return result;
    }

    private boolean sendTextMessage(Context mContext, int simID, String toNum, String smsText, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        String name;

        try {
            if (!Telephony.getInstance(mContext).isDualSIM()) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(toNum, null, smsText, sentIntent, deliveryIntent);
                return true;
            }
            if (simID == 0) {
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                name = "isms2";
            } else {
                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            if (Build.VERSION.SDK_INT < 18) {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, toNum, null, smsText, sentIntent, deliveryIntent);
            } else {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, mContext.getPackageName(), toNum, null, smsText, sentIntent, deliveryIntent);
            }

            return true;
        } catch (ClassNotFoundException e) {
            Log.e("apipas", "ClassNotFoundException:" + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e("apipas", "NoSuchMethodException:" + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e("apipas", "InvocationTargetException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("apipas", "IllegalAccessException:" + e.getMessage());
        } catch (Exception e) {
            Log.e("apipas", "Exception:" + e.getMessage());
        }
        return false;
    }

    private boolean sendMultipartTextMessage(Context mContext, int simID, String toNum, ArrayList<String> smsTextlist, ArrayList<PendingIntent> sentIntentList, ArrayList<PendingIntent> deliveryIntentList) {
        String name;
        try {
            if (!Telephony.getInstance(mContext).isDualSIM()) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendMultipartTextMessage(toNum, null, smsTextlist, sentIntentList, deliveryIntentList);
                return true;
            }
            if (simID == 0) {
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                name = "isms2";
            } else {
                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            if (Build.VERSION.SDK_INT < 18) {
                method = stubObj.getClass().getMethod("sendMultipartText", String.class, String.class, List.class, List.class, List.class);
                method.invoke(stubObj, toNum, null, smsTextlist, sentIntentList, deliveryIntentList);
            } else {
                method = stubObj.getClass().getMethod("sendMultipartText", String.class, String.class, String.class, List.class, List.class, List.class);
                method.invoke(stubObj, mContext.getPackageName(), toNum, null, smsTextlist, sentIntentList, deliveryIntentList);
            }
            return true;
        } catch (ClassNotFoundException e) {
            Log.e("apipas", "ClassNotFoundException:" + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e("apipas", "NoSuchMethodException:" + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e("apipas", "InvocationTargetException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("apipas", "IllegalAccessException:" + e.getMessage());
        } catch (Exception e) {
            Log.e("apipas", "Exception:" + e.getMessage());
        }
        return false;
    }
}
