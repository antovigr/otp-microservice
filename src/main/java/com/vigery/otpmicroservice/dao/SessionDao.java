package com.vigery.otpmicroservice.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.management.ConstructorParameters;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class SessionDao {

    @Value("${jdbc.url}")
    private String url;
    @Value("${jdbc.table}")
    private String table;
    @Value("${jdbc.user}")
    private String user;
    @Value("${jdbc.pass}")
    private String pass;
    private static final int TAILLE_FILE = 100;
    private final String PUT;
    private final String DELETE;
    private final String EXPIRE;

    private final ArrayBlockingQueue<Connection> file;

    private final Logger logger;

    @Autowired
    public SessionDao(@Value("${jdbc.url}") String url, @Value("${jdbc.table}") String table) throws SQLException {

        this.url=url;
        this.table=table;

        logger = LoggerFactory.getLogger(this.getClass());
        file = new ArrayBlockingQueue<>(TAILLE_FILE);
        PUT = "INSERT INTO " + table + " (session, token, expiretime)"
                + " VALUES (?, ?, ?)";
        DELETE = "DELETE FROM " + table
                + " WHERE session = ? AND token = ? AND expiretime > ?";
        EXPIRE = "DELETE FROM " + table
                + " WHERE expiretime <= ?";

        try{
            for (int i = 0; i < TAILLE_FILE; i++) {
                file.add(DriverManager.getConnection("jdbc:mysql://" + url, user, pass));
            }
        }catch(Exception e){
            e.printStackTrace();
        }



        ScheduledThreadPoolExecutor expiredRemover = new ScheduledThreadPoolExecutor(1);
        expiredRemover.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Erasing expired sessions.");
                    PreparedStatement stmt = file.take().prepareStatement(EXPIRE);
                    stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    stmt.executeUpdate();
                    logger.info("Expired sessions erased from database.");
                } catch (SQLException | InterruptedException e) {
                    logger.error("Error", e);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);

    }

    public void session(String token, String id, LocalDateTime expiration) throws Exception {
        try{

            PreparedStatement ps = file.take().prepareStatement(PUT);
            ps.setString(1, id);
            ps.setString(2, token);
            ps.setTimestamp(3, new Timestamp(expiration.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));

            ps.executeUpdate();

            ps.close();

        }catch(Exception e){
            throw new Exception("error (createSession())");
        }
    }

    public boolean notExpired(String id, String token) throws SQLException, InterruptedException {
        PreparedStatement ps = file.take().prepareStatement(DELETE);
        ps.setString(1, id);
        ps.setString(2, token);
        ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

        return ps.executeUpdate()>0;
    }

    public void close(){

    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


}
