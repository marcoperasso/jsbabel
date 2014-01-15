package jsbabel.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.CommandData;
import jsbabel.Const;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Messages;
import jsbabel.entities.Gender;
import jsbabel.entities.User;
import jsbabel.entities.UserType;
import jsbabel.entities.ValidationKey;

/**
 * Servlet implementation class RegisterUserServlet
 */
public class RegisterUser extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterUser() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        User user;
        DataHelper dh = new DataHelper();

        try {
            response.setContentType("text/json");
            if (!Helper.isNullOrEmpty((String) request.getParameter("verify"))) {
                throw new ServletException("Invalid request!");
            }
            boolean newUser = "true".equals(request.getParameter("newuser"));
            user = dh.getUserByMail(request.getParameter("mail"));
            if (newUser) {
                if (user != null) {
                    throw new Exception(Messages.ExistingUser);
                }
                user = new User();
                user.setType(UserType.SiteOwner);
                user.setActive(false);
                Helper.testCaptcha(request, true);
            } else {
                if (user == null || !user.equals(Helper.getUser(request.getSession()))) {
                    throw new Exception(Messages.INVALID_STATE);
                }
            }
            

            for (Field field : User.class.getDeclaredFields()) {
                Object o = request.getParameter(field.getName());
                if (o != null) {
                    if (field.getType() == Gender.class) {
                        o = Gender.valueOf((String) o);
                    } else if (field.getType() == Date.class) {
                        o = Helper.parseDate((String) o);
                    }
                    field.setAccessible(true);
                    field.set(user, o);
                }
            }
            Helper.checkRequiredFields(user);
            if (newUser)
            {
                ValidationKey key = dh.saveUserToValidate(user);
                user.sendActivationMail(key, request);
                request.getSession().setAttribute(Const.UserKey, user);
             }
            else
            {
                dh.saveUser(user);
            }
            
            response.getWriter().write(new CommandData().toJSON());//no error

        } catch (Exception e) {
            Helper.log(e, request.getSession());
            CommandData.sendError(e, response.getWriter());
        } finally {
            dh.dispose();
        }
    }
}
