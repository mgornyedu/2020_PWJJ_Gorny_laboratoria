package Models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Project {
    public  Project() { }
    public Project(String name){
        Name = name;
    }
    public Project(ResultSet st) throws SQLException {
        Id = st.getInt("ID");
        Name = st.getString("NAME");
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

    int Id;
    String Name;

    @Override
    public String toString() {
        return Name;
    }
}
