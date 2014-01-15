package jsbabel;

import jsbabel.entities.BaseString;
import jsbabel.entities.Site;
import jsbabel.entities.Parsedsite;
import jsbabel.entities.BaseLongString;
import jsbabel.entities.DemoTrial;
import jsbabel.entities.Page;
import jsbabel.entities.TargetLongString;
import jsbabel.entities.User;
import jsbabel.entities.TargetString;
import jsbabel.entities.ValidationKey;
import jsbabel.entities.Stringstatistics;
import jsbabel.entities.TargetLocale;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class MySessionFactory {

    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;

    public static void dispose() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
        serviceRegistry = null;
    }

    public static Session configureSession() throws HibernateException, ClassNotFoundException {

        return getSessionFactory().openSession();
    }

    public static SessionFactory getSessionFactory() throws ClassNotFoundException {
        if (sessionFactory == null) {
            Class.forName("com.mysql.jdbc.Driver");
            Configuration configuration = new Configuration()
                    .addAnnotatedClass(Site.class)
                    .addAnnotatedClass(TargetLocale.class)
                    .addAnnotatedClass(Page.class)
                    .addAnnotatedClass(BaseString.class)
                    .addAnnotatedClass(TargetString.class)
                    .addAnnotatedClass(BaseLongString.class)
                    .addAnnotatedClass(TargetLongString.class)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(ValidationKey.class)
                    .addAnnotatedClass(Parsedsite.class)
                    .addAnnotatedClass(Stringstatistics.class)
                    .addAnnotatedClass(DemoTrial.class)
                    .configure();
            serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        }
        return sessionFactory;
    }
}
