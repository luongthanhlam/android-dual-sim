package alimama.dualsim;

public class Sms {
    protected String ID;
    protected String PhoneNumber;
    protected String Message;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getMessage() {
        return "" + Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}