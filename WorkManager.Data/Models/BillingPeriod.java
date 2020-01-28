package Models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class BillingPeriod {

    public BillingPeriod(LocalDate from, LocalDate to) {
        this.From = from;
        this.To = to;
    }
    public BillingPeriod(ResultSet st) throws SQLException {
        Id = st.getInt("ID");
        From = st.getDate("FROM").toLocalDate();
        To = st.getDate("TO").toLocalDate();
        Hours = st.getInt("HOURS");
        Gross = st.getDouble("GROSS");
        Net = st.getDouble("NET");
    }

    private int Id;
    private LocalDate From;
    private LocalDate To;
    private int Hours;
    private double Gross;
    private double Net;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public LocalDate getFrom() {
        return From;
    }

    public void setFrom(LocalDate from) {
        From = from;
    }

    public LocalDate getTo() {
        return To;
    }

    public void setTo(LocalDate to) {
        To = to;
    }

    public int getHours() {
        return Hours;
    }

    public void setHours(int hours) {
        Hours = hours;
    }

    public double getGross() {
        return Gross;
    }

    public void setGross(double gross) {
        Gross = gross;
    }

    public double getNet() {
        return Net;
    }

    public void setNet(double net) {
        Net = net;
    }
}
