# android-dual-sim
a simple library for sending sms, making calls from dual SIM android phone

## Example
```java
    Telephony telephony = Telephony.getInstance(context);
    for(Sim sim : telephony.getAllSim()){
        sim.sendSMS(context, "0123456789", "Hello World!");
    }
```
