package alimama.dualsim;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mediatek.telephony.TelephonyManagerEx;

import java.lang.reflect.Method;
import java.util.HashMap;

public class TelephonyBase {
    protected static Telephony telephony;
    protected HashMap<Integer, Sim> simHashMap;

    public static Telephony getTelephony(Context context) {
        telephony = new Telephony();
        telephony.simHashMap = new HashMap<>();

        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

        TelephonyPeer.printTelephonyManagerMethodNamesForThisDevice(telephonyManager);

        for (int i = 0; i <= 1; i++) {
            TelephonyManager telephonyManagerRefactor = TelephonyBase.getTelephonyManagerByRefactor(i);
            if (telephonyManagerRefactor != null) {
                telephony.simHashMap.put(i, TelephonyPeer.getSimByTelephonyManager(telephonyManagerRefactor, i));
            } else {
                try {
                    telephony.simHashMap.put(i, TelephonyPeer.getSimByTelephonyManagerGemini(telephonyManager, i));
                } catch (GeminiMethodNotFoundException e) {
                    Log.e("GeminiMethodNotFound", e.getMessage());
                    try {
                        TelephonyManagerEx telephonyManagerEx = new TelephonyManagerEx(context);
                        telephony.simHashMap.put(i, TelephonyPeer.getSimByTelephonyManager(telephonyManagerEx, i));
                    } catch (Exception ec) {
                        ec.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (telephony.simHashMap.size() == 0) {
            telephony.simHashMap.put(0, TelephonyPeer.getSimByTelephonyManager(telephonyManager, 0));
        }
        return telephony;
    }

    public static Class RILConstants() {
        try {
            return Class.forName("com.android.internal.telephony.RILConstants$SimCardID");
        } catch (Throwable throwable) {
            // ignore
        }
        return null;
    }

    private static TelephonyManager getTelephonyManagerByRefactor(int slotID) {
        String simID;
        if (slotID == 0) {
            simID = "ID_ZERO";
        } else {
            simID = "ID_ONE";
        }
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            //new
            Class<?>[] parameter = new Class[1];
            parameter[0] = RILConstants();
            Method getFirstMethod = telephonyClass.getMethod("getDefault", parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = Enum.valueOf(RILConstants(), simID);
            TelephonyManager telephonyManager = (TelephonyManager) getFirstMethod.invoke(null, obParameter);

            if (telephonyManager == null) {
                //new
                Class<?>[] paras = new Class[1];
                paras[0] = int.class;
                Method getDefault = telephonyClass.getMethod("getDefault", paras);

                Object[] obs = new Object[1];
                obParameter[0] = slotID;
                telephonyManager = (TelephonyManager) getDefault.invoke(null, obs);
            }
            return telephonyManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
