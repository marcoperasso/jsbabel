/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel;

import java.lang.reflect.Field;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 *
 * @author Marco
 */
public class MyContextListener implements ServletContextListener {

   @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            MySessionFactory.getSessionFactory();
        } catch (ClassNotFoundException ex) {
            Helper.log(ex);  
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            // This manually deregisters JDBC driver, which prevents Tomcat from complaining about memory leaks wrto this class
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                try {
                    DriverManager.deregisterDriver(driver);
                    Helper.log(Level.INFO, "Deregistering driver " + driver.toString());
                } catch (SQLException e) {
                    Helper.log(e);
                }
            }
            MySessionFactory.dispose();
            Helper.cleanupThreadLocals(null, Thread.currentThread().getContextClassLoader());
            Helper.dispose();
            //elimino un reference che causa memory leak
            
                Field f = Level.class.getDeclaredField("known");
                f.setAccessible(true);       
                ArrayList list = (ArrayList) f.get(Level.class);
                list.clear();
        } catch (Exception ex) {
            Helper.log(ex);
        } 
       
    }
}
