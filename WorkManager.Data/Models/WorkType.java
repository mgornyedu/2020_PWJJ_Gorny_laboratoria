package Models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

public class WorkType {
    public  WorkType() { }
    public WorkType(ResultSet st) throws SQLException {
        Id = st.getInt("ID");
        Name = st.getString("NAME");
        PerHour = st.getFloat("PERHOUR");
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getPerHour() {
        return PerHour;
    }

    public void setPerHour(double perHour) {
        PerHour = perHour;
    }

    private int Id;
    private String Name;
    private double PerHour;

    @Override
    public String toString() {
        return Name;
    }
}
