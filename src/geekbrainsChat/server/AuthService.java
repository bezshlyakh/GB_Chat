package geekbrainsChat.server;
import java.sql.*;

public class AuthService {
    private static Connection connection = null;
    private static Statement stmt;

    public static void dbConnect () {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:/Users/yolkina/Java_course/Chat_JC/src/ChatUsers.db";
            connection = DriverManager.getConnection(url);
            stmt = connection.createStatement();
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    public static void dbDisconnect () {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUser (String login, String password, String username){ //for empty DB
        String query = "INSERT INTO main (login, password, username) VALUES (?, ?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(query)){
            ps.setString(1, login);
            ps.setInt(2, password.hashCode());
            ps.setString(3, username);
            ps.executeUpdate();
            ps.clearParameters();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static String getNameByLoginAndPass (String login, String password){
        try {
            ResultSet rs = stmt.executeQuery("SELECT username, password FROM main WHERE login = '" + login + "'");
           if (rs.next()) {
               String user_name = rs.getString("username");
               Integer passHC = password.hashCode();
               if(passHC == rs.getInt("password") ){
                   return user_name;
               }
           }
            } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
