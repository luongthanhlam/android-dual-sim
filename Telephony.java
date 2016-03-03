package alimama.dualsim;

import android.content.Context;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public final class Telephony extends TelephonyBase{
    protected static Telephony telephony;
    protected HashMap<Integer, Sim> simHashMap;
    private int currentSlotId = 0;
    private static Random r = new Random();

    public HashMap<Integer, Sim> getSimHashMap() {
        return simHashMap;
    }

    public void setSimHashMap(HashMap<Integer, Sim> simHashMap) {
        this.simHashMap = simHashMap;
    }

    public Sim getFirstSim() {
        return this.getSIM(0);
    }

    public Sim getSecondSim() {
        return this.getSIM(1);
    }

    public Sim getRandomSim() {
        return r.nextBoolean() ? this.getFirstSim() : this.getSecondSim();
    }

    public Sim getNextSim() {
        return currentSlotId == 0 && this.isDualSIM() ? getSecondSim() : getFirstSim();
    }

    public Sim getNextAvailableSim() {
        Sim _currentSim = this.getNextSim();
        return _currentSim.isBlocked() ? this.getNextSim() : _currentSim;
    }

    public Sim getCurrentSim() {
        return this.getSIM(this.currentSlotId);
    }

    public Collection<Sim> getAllSim() {
        return this.simHashMap.values();
    }

    public Sim getSIM(int slotId) {
        return this.simHashMap.containsKey(slotId) ? simHashMap.get(slotId) : null;
    }

    public int getCurrentSlotId() {
        return currentSlotId;
    }

    public void setCurrentSlotId(int currentSlotId) {
        this.currentSlotId = currentSlotId;
    }

    public void putSIM(int slotId, Sim SIM) {
        simHashMap.put(slotId, SIM);
    }

    public boolean isDualSIM() {
        return simHashMap.size() == 2 && this.getSecondSim().isReady();
    }

    public static Telephony getInstance(Context context) {
        if (telephony == null) {
            telephony = TelephonyBase.getTelephony(context);
        } else if (telephony.isDualSIM() && (!telephony.getFirstSim().isReady() || !telephony.getSecondSim().isReady())) {
            telephony = TelephonyBase.getTelephony(context);
        }
        return telephony;
    }
}