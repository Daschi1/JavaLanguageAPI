package de.daschi.javalanguageapi.mysql;

import java.sql.*;

public class MySQL {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;
    private Connection connection;

    public MySQL(final String hostname, final String port, final String username, final String password) {
        this(hostname, port, null, username, password);
    }

    public MySQL(final String hostname, final String port, final String database, final String username, final String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (this.checkConnection()) {
            return this.connection;
        }

        String connectionURL = "jdbc:mysql://" + this.hostname + ":" + this.port;
        if (this.database != null) {
            connectionURL = connectionURL + "/" + this.database;
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection(connectionURL, this.user, this.password);
        return this.connection;
    }

    public boolean checkConnection() throws SQLException {
        return this.connection != null && !this.connection.isClosed();
    }

    public boolean closeConnection() throws SQLException {
        if (this.connection == null) {
            return false;
        }
        this.connection.close();
        return true;
    }

    public void executeUpdate(final String sql) throws SQLException {
        if (!this.checkConnection()) {
            throw new SQLException("The connection to '" + this.toString() + "' is not open or initialised.");
        }
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public ResultSet executeQuery(final String sql) throws SQLException {
        if (!this.checkConnection()) {
            throw new SQLException("The connection to '" + this.toString() + "' is not open or initialised.");
        }
        final Statement statement = this.connection.createStatement();
        return statement.executeQuery(sql);
    }

    @Override
    public String toString() {
        return "MySQL{" +
                "user='" + this.user + '\'' +
                ", database='" + this.database + '\'' +
                ", password='" + this.password + '\'' +
                ", port='" + this.port + '\'' +
                ", hostname='" + this.hostname + '\'' +
                ", connection=" + this.connection +
                '}';
    }
}
