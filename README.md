# android-dual-sim
Android SMS manager for dualsim phone is a simple library to send sms, make calls from both SIM.

## Example
```java
    Telephony telephony = Telephony.getInstance(context);
    for(Sim sim : telephony.getAllSim()){
        sim.sendSMS(context, "0123456789", "Hello World!");
    }
```
