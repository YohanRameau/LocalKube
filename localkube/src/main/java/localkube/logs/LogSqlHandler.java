package localkube.logs;

import localkube.lib.LocalKubeApi;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlite3.SQLitePlugin;

import java.sql.*;
import java.util.ArrayList;

public class LogSqlHandler {
    static final String url = "jdbc:sqlite:logs/logs.db";
    private final Jdbi jdbi = Jdbi.create(url).installPlugin(new SQLitePlugin());


    static class LogMapper implements RowMapper<Log> {
        @Override
        public Log map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Log(rs.getInt("appId"), rs.getString("name"), rs.getInt("dockerPort"), rs.getInt("servicePort"), rs.getString("dockerInstance"), rs.getString("message"), rs.getString("time"));
        }
    }

    public static void createDatabase(){

        try (var conn = DriverManager.getConnection(url)){
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println(LocalKubeApi.formatTime() + "Connection on the database. If the database is not present, a new database will be created.");
                createTable();
            }
        } catch (SQLException throwables) {
            System.out.println(LocalKubeApi.formatTime() + "The database could not be created.");
            throwables.printStackTrace();
        }
    }

    private static void createTable(){
        try {
            Jdbi.create(url).useHandle(handle -> handle.execute("create table logs (id INTEGER PRIMARY KEY AUTOINCREMENT, appId int, name varchar(100), dockerPort int, servicePort int, dockerInstance varchar(100), message text, time text)"));
        } catch (Exception e){
            System.out.println(LocalKubeApi.formatTime() + "The database already exists. You can see your old logs");
            return;
        }
    }

    public void insert(Log log){
        jdbi.useHandle(handle -> handle.createUpdate("insert into logs (appId, name, dockerPort, servicePort, dockerInstance, message, time) values (:appId, :name, :dockerPort, :servicePort, :dockerInstance, :message, datetime('now', 'localtime'))" )
                                       .bind("appId", log.appId())
                                       .bind("name", log.dockerName())
                                       .bind("dockerPort", log.dockerPort())
                                       .bind("servicePort", log.servicePort())
                                       .bind("dockerInstance", log.dockerInstance())
                                       .bind("message", log.message())
                                       .execute());
    }

    public ArrayList<Log> allLogs(){
        var l =  jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM logs ").map(new LogMapper()).list());
        return new ArrayList<>(l);
    }

    public ArrayList<Log> logsByDockerName(String time, String name){
        var l =  jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM logs where time >= " + time + " and name = " + name).map(new LogMapper()).list());
        return new ArrayList<>(l);
    }

    public ArrayList<Log> logsByTime(String time){
        var l =  jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM logs where time >= " + time).map(new LogMapper()).list());
        return new ArrayList<>(l);
    }

    public ArrayList<Log> logsById(String time, int id){
        var l =  jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM logs where time >= " + time + " and appId = " + id).map(new LogMapper()).list());
        return new ArrayList<>(l);
    }

    public ArrayList<Log> logsByInstance(String time, String instance){
        var l =  jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM logs where time >= " + time + " and dockerInstance = " + instance).map(new LogMapper()).list());
        return new ArrayList<>(l);
    }



}
