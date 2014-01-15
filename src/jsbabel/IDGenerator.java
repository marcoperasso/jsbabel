/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.*;
import jsbabel.entities.BaseLongString;
import jsbabel.entities.BaseString;
import jsbabel.entities.Page;
import jsbabel.entities.Site;
import jsbabel.entities.User;

/**
 *
 * @author Marco
 */
public class IDGenerator implements IdentifierGenerator  {
   
    public IDGenerator() {
    }
    public static final int Page = 0;
    public static final int BaseString = 1;
    public static final int BaseLongString = 2;
    public static final int User = 3;
    public static final int Site = 4;

    public Serializable castId(int ownerId, Long id) {
        switch(ownerId)
        {
            case User:
                return new Integer(id.intValue());
            default:
                return id;
        }
    }

    public static int fromClass(Class c) {
        if (c == Page.class) {
            return Page;
        }
        if (c == BaseString.class) {
            return BaseString;
        }
        if (c == BaseLongString.class) {
            return BaseLongString;
        }
        if (c == User.class) {
            return User;
        }
        if (c == Site.class) {
            return Site;
        }
        throw new UnsupportedOperationException(String.format("Class %s does not support ID generation", c.getName()));
    }

    @Override
    public Serializable generate(SessionImplementor session, Object object)
            throws HibernateException {
        Connection connection = session.connection();
        PreparedStatement selectStmt = null;
        ResultSet rs = null;
        try {
            IIDConsumer consumer = (IIDConsumer) object;
            long contextId = consumer.getContextId();
            selectStmt = connection.prepareStatement("select next_id(?,?);");
            selectStmt.setLong(1, contextId);
            int ownerId = fromClass(object.getClass());
            selectStmt.setInt(2, ownerId);
            rs = selectStmt.executeQuery();
            rs.next();
            Long id = contextId << 32;
            id += rs.getLong(1); 
            return castId(ownerId, id);
        } catch (Exception e) {
            throw new HibernateException("Unable to generate ID: " + e.getLocalizedMessage());
        }
        finally
        {
        if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Helper.log(ex);
                }
            }
        if (selectStmt != null) {
                try {
                    selectStmt.close();
                } catch (SQLException ex) {
                    Helper.log(ex);
                }
            }
        }


    }

}
