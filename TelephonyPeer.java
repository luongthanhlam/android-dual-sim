package alimama.dualsim;

import android.telephony.TelephonyManager;
import android.util.Log;

import com.mediatek.telephony.TelephonyManagerEx;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
public class TelephonyPeer {
    static Map<String, String> _networkOperators;

    /**
     * Mobile Network Codes (MNC) for the international identification plan
     * for public networks and subscriptions
     * See more in MNC.pdf
     */
    static {
        _networkOperators = new HashMap<>();
        _networkOperators.put("45204", "viettel");
        _networkOperators.put("45201", "mobifone");
        _networkOperators.put("45202", "vinaphone");
    }


    public static Sim getSimByTelephonyManager(TelephonyManager telephonyManager, int i) {
        Sim sim = new Sim(i);
        sim.setSlotId(i);
        sim.serial = telephonyManager.getSimSerialNumber();
        sim.deviceId = telephonyManager.getDeviceId();
        sim.network = _networkOperators.get(telephonyManager.getNetworkOperator());
        sim.ready = telephonyManager.getSimState() == android.telephony.TelephonyManager.SIM_STATE_READY;
        return sim;
    }

    public static Sim getSimByTelephonyManagerGemini(TelephonyManager telephonyManager, int i) throws GeminiMethodNotFoundException {
        Sim sim = new Sim(i);
        sim.setSlotId(i);
        sim.serial = getDetailBySlot(telephonyManager, "getSimSerialNumberGemini", i);
        sim.deviceId = getDetailBySlot(telephonyManager, "getDeviceIdGemini", i);
        sim.network = _networkOperators.get(getDetailBySlot(telephonyManager, "getNetworkOperatorGemini", i));
        sim.ready = getSIMStateBySlot(telephonyManager, "getSimStateGemini", i);
        return sim;
    }

    public static Sim getSimByTelephonyManager(TelephonyManagerEx telephonyManagerEx, int i) {
        Sim sim = new Sim(i);
        sim.setSlotId(i);
        sim.serial = telephonyManagerEx.getSimSerialNumber(i);
        sim.deviceId = telephonyManagerEx.getDeviceId(i);
        sim.network = _networkOperators.get(telephonyManagerEx.getNetworkOperator(i));
        sim.ready = telephonyManagerEx.getSimState(i) == android.telephony.TelephonyManager.SIM_STATE_READY;
        return sim;
    }

    public static void printTelephonyManagerMethodNamesForThisDevice(TelephonyManager telephony) {
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++) {
                Log.d("METHOD", "\n" + methods[idx] + " declared by " + methods[idx].getDeclaringClass() + " return " + methods[idx].getReturnType());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //PRIVATE
    private static String getDetailBySlot(TelephonyManager telephonyManager, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {
        String imsi = null;
        try {
            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephonyManager, obParameter);

            if (ob_phone != null) {
                imsi = ob_phone.toString();

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return imsi;
    }

    private static boolean getSIMStateBySlot(TelephonyManager telephonyManager, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {
        boolean isReady = false;
        try {
            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephonyManager, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }
}
