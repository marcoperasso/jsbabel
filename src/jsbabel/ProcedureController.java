package jsbabel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ProcedureController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1646973121936553517L;
    List<String> messages = new ArrayList<String>();
    List<Level> types = new ArrayList<Level>();
    private boolean aborted = false;
    private boolean finished;

    public void setAborted() {
        aborted = true;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void log(String msg, Level type) {
        messages.add(msg);
        types.add(type);
    }

    public void log(String msg) {
        log(msg, Level.SEVERE);
    }

    public String getLastMessage() {

        return messages.size() > 0 ? messages.get(messages.size() - 1) : "";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String msg : messages) {
            sb.append(msg);
        }

        return sb.toString();
    }

    public void setFinished() {
        this.finished = true;

    }

    public boolean isFinished() {
        return finished;
    }

}
