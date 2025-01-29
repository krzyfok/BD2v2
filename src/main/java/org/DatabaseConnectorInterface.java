package org;

import java.sql.SQLException;

public interface DatabaseConnectorInterface {
    void connect() throws SQLException;
    void disconnect() throws SQLException;
}
