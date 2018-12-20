package candor.fulki.chat;


public class Messages {

    private String message , type ;
    private Boolean seen;
    private long  time;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    private String from ;

    public Messages(String message, Boolean seen, long time, String type) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
    }

    public Messages(){

    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Boolean getSeen() {
        return seen;
    }
    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }



}

