package jsbabel;

public class Messages {

    public static final String SettingsSaved = "Settings have been correctly saved";
    public static final String InvalidActivationCode = "Invalid activation code";
    public static final String InvalidEMail = "Invalid email";
    public static final String InvalidPassword = "Invalid password";
    public static final String ExistingUser = "User already existing";
    public static final String MissingField = "Required field is missing: %s";
    public static final String MailRegistrationBody = "Thank you for registering to JSBABEL.<br/>"
            + "This is an automatic mail, please do not reply to it.<br/>"
            + "Click <a href='%1$s'>this link</a> to activate your account.<br/>"
            + "If you cannot open the link, please copy this address:<br/>%1$s<br/>in your browser's address bar and confirm navigation.";
    public static final String MailResetPwdBody = "JSBABEL reset password procedure.<br/>"
            + "This is an automatic mail, please do not reply to it.<br/>"
            + "Click <a href='%1$s'>this link</a> to introduce a new password.<br/>"
            + "If you cannot open the link, please copy this address:<br/>%1$s<br/>in your browser's address bar and confirm navigation.";
    public static final String MailResetPwdSubject = "DO NOT REPLY - JSBABEL password reset";
    public static final String MailRegistrationSubject = "DO NOT REPLY - JSBABEL user activation";
    public static final String CannotDecrypt = "Cannot decrypt information: no valid decryption code in your working session";
    public static final String SITE_ALREADY_EXISTING = "Site already existing!";
    public static final String INVALID_SITE_URL = "Invalid site url";
    public static final String INVALID_SITE = "Invalid site";
    public static final String INVALID_STATE = "Invalid state";
    public static final String UserNotLogged = "You need to login to perform this operation";
    public static final String INVALID_IMPORT_FILE = "Invalid file: %s; you can only import files with %s extension.";
}
