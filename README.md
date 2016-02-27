# android-dual-sim
Android SMS manager for dual sim phones is a simple SMSManager to send the SMS from dual SIM.

## Example
    Telephony telephony = Telephony.getInstance(mContext);
    for(SIM sim : telephony.getAllSim()){
     SMS sms = new SMS("0123456789", "Hello World!");
     sim.sendSMS(mContext, sms, intent);
    }
