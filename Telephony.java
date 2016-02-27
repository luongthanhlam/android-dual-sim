import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class Telephony {
    private static Telephony telephony;
    private HashMap<Integer, SIM> simHashMap;
    private int currentSlotId = 0;
    private static Random r = new Random();

    public HashMap<Integer, SIM> getSimHashMap() {
        return simHashMap;
    }

    public void setSimHashMap(HashMap<Integer, SIM> simHashMap) {
        this.simHashMap = simHashMap;
    }

    public SIM getFirstSim() {
        return this.getSIM(0);
    }

    public SIM getSecondSim() {
        return this.getSIM(1);
    }

    public SIM getRandomSim() {
        return r.nextBoolean() ? this.getFirstSim() : this.getSecondSim();
    }

    public SIM getNextSim() {
        return currentSlotId == 0 && this.isDualSIM() ? getSecondSim() : getFirstSim();
    }

    public SIM getNextAvaiableSim() {
        SIM _currentSim = this.getNextSim();
        return _currentSim.isBlocked() ? this.getNextSim() : _currentSim;
    }

    public SIM getCurrentSim() {
        return this.getSIM(this.currentSlotId);
    }

    public Collection<SIM> getAllSim() {
        return this.simHashMap.values();
    }

    public SIM getSIM(int slotId) {
        return this.simHashMap.containsKey(slotId) ? simHashMap.get(slotId) : null;
    }

    public int getCurrentSlotId() {
        return currentSlotId;
    }

    public void setCurrentSlotId(int currentSlotId) {
        this.currentSlotId = currentSlotId;
    }

    public void putSIM(int slotId, SIM SIM) {
        simHashMap.put(slotId, SIM);
    }

    public boolean isDualSIM() {
        return simHashMap.size() == 2 && this.getSecondSim().isReady();
    }

    private Telephony() {
    }

    public static Telephony getInstance(Context context) {

        if (telephony == null) {
            telephony = new Telephony();
            telephony.simHashMap = new HashMap<>();

            TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

            Map<String, String> _networkOperators = new HashMap<>();
            _networkOperators.put("45204", "viettel");
            _networkOperators.put("45201", "mobifone");
            _networkOperators.put("45202", "vinaphone");

            printTelephonyManagerMethodNamesForThisDevice(context);

            for (int i = 0; i <= 1; i++) {
                TelephonyManager telephonyManager2 = telephony.getTelephonyManager(context, i);
                if (telephonyManager2 != null) {
                    SIM SIM = new SIM(i);
                    SIM.setSlotId(i);
                    SIM.serial = telephonyManager2.getSimSerialNumber();
                    SIM.deviceId = telephonyManager.getDeviceId();
                    SIM.network = _networkOperators.get(telephonyManager2.getNetworkOperator());
                    SIM.ready = telephonyManager2.getSimState() == android.telephony.TelephonyManager.SIM_STATE_READY;
                    telephony.simHashMap.put(i, SIM);
                } else {
                    try {
                        SIM SIM = new SIM(i);
                        SIM.setSlotId(i);
                        SIM.serial = getDetailBySlot(context, "getSimSerialNumberGemini", i);
                        SIM.deviceId = getDetailBySlot(context, "getDeviceIdGemini", i);
                        SIM.network = _networkOperators.get(getDetailBySlot(context, "getNetworkOperatorGemini", i));
                        SIM.ready = getSIMStateBySlot(context, "getSimStateGemini", i);
                        telephony.simHashMap.put(i, SIM);
                    } catch (GeminiMethodNotFoundException e) {
                        Log.e("GeminiMethodNotFound", e.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (telephony.simHashMap.size() == 0) {
                SIM SIM = new SIM(0);
                SIM.serial = telephonyManager.getSimSerialNumber();
                SIM.network = _networkOperators.get(telephonyManager.getNetworkOperator());
                SIM.deviceId = telephonyManager.getDeviceId();
                SIM.setReady(telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY);
                telephony.simHashMap.put(0, SIM);
            }
        }

        return telephony;
    }

    public static void printTelephonyManagerMethodNamesForThisDevice(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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

    public static Class RILConstants() {
        try {
            return Class.forName("com.android.internal.telephony.RILConstants$SimCardID");
        } catch (Throwable throwable) {
            // ignore
        }
        return null;
    }

    private static TelephonyManager getTelephonyManager(Context context, int slotID) {
        android.telephony.TelephonyManager telephony = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

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

    private static String getDetailBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        String imsi = null;
        android.telephony.TelephonyManager telephony = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);

            if (ob_phone != null) {
                imsi = ob_phone.toString();

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return imsi;
    }

    private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        boolean isReady = false;

        android.telephony.TelephonyManager telephony = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {

            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == android.telephony.TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }


    private static class GeminiMethodNotFoundException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }
}