package gr.ictpro.jsalatas.gestures.db;

import gr.ictpro.jsalatas.gestures.model.Point;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {
    private static Connection c = null;
    private static final String DB_NAME = "data/sensor_data.db";
    private static List<Point> points = null;

    static {
        init();
    }

    public static void init() {

        // Can I do it throught Ebeans????

        Statement stmt = null;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
            c.setAutoCommit(false);

            stmt = c.createStatement();
            String sql = "create table IF NOT EXISTS point (" +
//                    "  id                            integer not null," +
                    "  time                          integer not null," +
                    "  xa                            integer not null," +
                    "  ya                            integer not null," +
                    "  za                            integer not null," +
                    "  classification                integer not null" +
//                    "  constraint pk_point primary key (id)" +
                    ")";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (c != null) {
                    c.commit();
                }
            } catch (SQLException e) {
                // do nothing
            }
        }
    }

    public static void close() {
        try {
            if (c != null) {
                c.commit();
            }
        } catch (SQLException e) {
            // do nothing
        }
    }

    public static void save(Point p) {
        String sql = "INSERT INTO point(time, xa, ya , za, classification) VALUES(?,?,?,?,?)";

        try (PreparedStatement pstmt = c.prepareStatement(sql);){
            pstmt.setLong(1, p.getTime());
            pstmt.setInt(2, p.getXa());
            pstmt.setInt(3, p.getYa());
            pstmt.setInt(4, p.getZa());
            pstmt.setInt(5, p.getClassification());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<Point> getPoints() {
        if(points == null) {
            points = new ArrayList<>();
            String sql = "SELECT * FROM point ORDER BY time asc";

            try (Statement stmt = c.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    points.add(new Point(rs.getLong("time"),
                            rs.getInt("xa"),
                            rs.getInt("ya"),
                            rs.getInt("za"),
                            rs.getInt("classification")));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return points;
    }

    public static void updateClassification(int from, int to, int classification) {
        String sql = "UPDATE point SET classification = ? WHERE time = ?";

        for (int i = from; i <= to; i++) {
            Point p = points.get(i);
            p.setClassification(classification);

            try (PreparedStatement pstmt = c.prepareStatement(sql)){
                pstmt.setInt(1, p.getClassification());
                pstmt.setLong(2, p.getTime());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        }
    }


}
