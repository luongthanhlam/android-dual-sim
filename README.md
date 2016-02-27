# android-dual-sim
Android SMS manager for dual sim phones is a simple library to send the SMS, make a phone call from dual SIM.

## Example
    Telephony telephony = Telephony.getInstance(context);
    for(SIM sim : telephony.getAllSim()){
     sim.sendSMS(context, "0123456789", "Hello World!");
    }
