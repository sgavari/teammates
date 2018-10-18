package teammates.ui.newcontroller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.common.datatransfer.UserInfo;
import teammates.common.exception.NullHttpParameterException;
import teammates.common.util.Config;
import teammates.common.util.Const;
import teammates.logic.api.EmailGenerator;
import teammates.logic.api.EmailSender;
import teammates.logic.api.GateKeeper;
import teammates.logic.api.Logic;
import teammates.logic.api.TaskQueuer;

/**
 * An "action" to be performed by the system.
 * If the requesting user is allowed to perform the requested action,
 * this object can talk to the back end to perform that action.
 */
public abstract class Action {

    protected Logic logic = new Logic();
    protected GateKeeper gateKeeper = new GateKeeper();
    protected EmailGenerator emailGenerator = new EmailGenerator();
    protected TaskQueuer taskQueuer = new TaskQueuer();
    protected EmailSender emailSender = new EmailSender();

    protected HttpServletRequest req;
    protected HttpServletResponse resp;
    protected UserInfo userInfo;
    private AuthType authType;

    /**
     * Initializes the action object based on the HTTP request.
     */
    protected void init(HttpServletRequest req, HttpServletResponse resp) {
        this.req = req;
        this.resp = resp;
        initAuthInfo();
    }

    public TaskQueuer getTaskQueuer() {
        return taskQueuer;
    }

    public void setTaskQueuer(TaskQueuer taskQueuer) {
        this.taskQueuer = taskQueuer;
    }

    public EmailSender getEmailSender() {
        return emailSender;
    }

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * Returns true if the requesting user has sufficient authority to access the resource.
     */
    protected boolean checkAccessControl() {
        if (authType.getLevel() < getMinAuthLevel().getLevel()) {
            // Access control level lower than required
            return false;
        }

        if (getMinAuthLevel() == AuthType.UNAUTHENTICATED) {
            // No authentication necessary for this resource
            return true;
        }

        if (authType == AuthType.ALL_ACCESS) {
            // All-access pass granted
            return true;
        }

        // All other cases: to be dealt in case-by-case basis
        return checkSpecificAccessControl();
    }

    private void initAuthInfo() {
        if (Config.BACKDOOR_KEY.equals(req.getParameter("backdoorkey"))) {
            authType = AuthType.ALL_ACCESS;
            return;
        }

        userInfo = gateKeeper.getCurrentUser();
        if (userInfo != null) {
            authType = AuthType.REGISTERED;
            return;
        }

        String regkey = req.getParameter("regkey");
        authType = regkey == null ? AuthType.UNAUTHENTICATED : AuthType.UNREGISTERED;
    }

    /**
     * Returns the first value for the specified parameter in the HTTP request, or null if such parameter is not found.
     */
    protected String getRequestParamValue(String paramName) {
        return req.getParameter(paramName);
    }

    /**
     * Returns the first value for the specified parameter expected to be present in the HTTP request.
     */
    protected String getNonNullRequestParamValue(String paramName) {
        return getNonNullRequestParamValues(paramName)[0];
    }

    /**
     * Returns the values for the specified parameter in the HTTP request, or null if such parameter is not found.
     */
    protected String[] getRequestParamValues(String paramName) {
        return req.getParameterValues(paramName);
    }

    /**
     * Returns the values for the specified parameter expected to be present in the HTTP request.
     */
    protected String[] getNonNullRequestParamValues(String paramName) {
        String[] values = getRequestParamValues(paramName);
        if (values == null) {
            throw new NullHttpParameterException(String.format(Const.StatusCodes.NULL_HTTP_PARAMETER, paramName));
        }
        return values;
    }

    /**
     * Returns the log message in the special format used for generating the 'activity log' for the Admin.
     */
    public String getLogMessage() {
        // TODO
        return "Test log message";
    }

    /**
     * Gets the minimum access control level required to access the resource.
     */
    protected abstract AuthType getMinAuthLevel();

    /**
     * Checks the specific access control needs for the resource.
     */
    protected abstract boolean checkSpecificAccessControl();

    /**
     * Executes the action.
     */
    protected abstract ActionResult execute();

}