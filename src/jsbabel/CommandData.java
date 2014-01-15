/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel;

import java.io.PrintWriter;
import java.util.logging.Level;

/**
 *
 * @author Marco
 */
public class CommandData {

     public static void sendError(Exception ex, PrintWriter out) {
        CommandData data = new CommandData();
        data.setSuccess(false);
        data.setMessage(ex.getLocalizedMessage() == null ? ex.toString() : ex.getLocalizedMessage());
        data.setLogType(Level.SEVERE);
        out.write(data.toJSON());//error
    }

    public static void sendOK(PrintWriter out) {
       out.write(new CommandData().toJSON());//no error
    }

    private boolean success = true;
    private Level logType;
    private String message = null;

    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":");
        sb.append(success);
       
        if (logType != null) {
            sb.append(", \"logType\":\"");
            sb.append(logType);
            sb.append("\"");
        }
        if (message != null) {
            sb.append(", \"message\":\"");
            sb.append(message);
            sb.append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * @return the result
     */
    public Level getLogType() {
        return logType;
    }

    /**
     * @param result the result to set
     */
    public void setLogType(Level result) {
        this.logType = result;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
