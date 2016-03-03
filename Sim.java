package alimama.dualsim;

public class Sim extends SimBase{
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

    public Sim(int slotId) {
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
}
