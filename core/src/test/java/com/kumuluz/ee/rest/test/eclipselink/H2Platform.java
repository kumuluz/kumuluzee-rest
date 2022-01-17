package com.kumuluz.ee.rest.test.eclipselink;

import org.eclipse.persistence.internal.sessions.AbstractSession;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

/**
 * Temporary fix for H2 2.x database eclipselink integration.
 *
 * The default implementation uses preparedStatement.setTimestamp instead of preparedStatement.setTime when the type
 * of parameter is LocalTime. This doesn't work on H2 versions 2.x.
 *
 * This should be removed along with the property eclipselink.target-database when the issue is fixed upstream.
 *
 * @author Urban Malc
 * @since 1.6.0
 */
public class H2Platform extends org.eclipse.persistence.platform.database.H2Platform {

    @Override
    public void setParameterValueInDatabaseCall(Object parameter, PreparedStatement statement, int index, AbstractSession session) throws SQLException {

        if (parameter instanceof LocalTime) {
            LocalTime lt = (LocalTime) parameter;
            statement.setTime(index, new Time(lt.getHour(), lt.getMinute(), lt.getSecond()));
        } else {
            super.setParameterValueInDatabaseCall(parameter, statement, index, session);
        }
    }

    @Override
    public void setParameterValueInDatabaseCall(Object parameter, CallableStatement statement, String name, AbstractSession session) throws SQLException {

        if (parameter instanceof LocalTime) {
            LocalTime lt = (LocalTime) parameter;
            statement.setTime(name, new Time(lt.getHour(), lt.getMinute(), lt.getSecond()));
        } else {
            super.setParameterValueInDatabaseCall(parameter, statement, name, session);
        }
    }
}
