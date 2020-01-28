package Models;

import java.math.BigDecimal;
import java.net.InterfaceAddress;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Task{

    public Task() { }
    public Task(String name, int projektId) {
        this.Name = name;
        this.ProjektId = projektId;
    }
    public Task(ResultSet st) throws SQLException {
        Id = st.getInt("ID");
        Name = st.getString("NAME");
        ProjektId = st.getInt("PROJECTID");
    }
    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getProjektId() {
        return ProjektId;
    }

    public void setProjektId(int projektId) {
        ProjektId = projektId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    private int Id;
    private int ProjektId;
    private String Name;
}

