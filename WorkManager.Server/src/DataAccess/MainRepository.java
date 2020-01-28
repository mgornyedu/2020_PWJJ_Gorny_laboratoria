package DataAccess;

import Models.*;
import com.company.SyncInformation;
import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import com.sun.deploy.net.proxy.ProxyUnavailableException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.company.SyncInformation.SyncData;

public class MainRepository {
    public MainRepository() throws SQLException {
        String connectionUrl = "jdbc:sqlserver://PCV2\\SQLEXPRESS17:1433;" +
                "databaseName=PK_J_WM;user=sa;password=sa;";
        Connection connection = DriverManager.getConnection(connectionUrl);
        String schema = connection.getSchema();
        statement = connection.createStatement();
    }
    Statement statement;
    UUID clientId;
    public void SetClient(UUID clientId){
        this.clientId = clientId;
    }
    public ArrayList<Project> GetProjects() throws SQLException{
        ArrayList<Project> result = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT * FROM PROJECTS ORDER BY NAME");
        while (rs.next()) {
            result.add(new Project(rs));
        }
        return result;
    }
    public ArrayList<WorkType> GetWorkTypes() throws SQLException
    {
        ArrayList<WorkType> result = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT * FROM WORKTYPES ORDER BY NAME");
        while (rs.next()) {
            result.add(new WorkType(rs));
        }
        return result;
    }
    public ArrayList<WorkItem> GetWorkItems() throws SQLException
    {
        ArrayList<WorkItem> result = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT WI.*, T.NAME TASK, P.NAME PROJECT, WT.NAME TYPE FROM WORKITEMS WI\n" +
                "JOIN TASKS T ON T.ID = TASKID\n" +
                "JOIN PROJECTS P ON P.ID = PROJECTID\n" +
                "JOIN WORKTYPES WT ON WT.ID = TYPEID\n" +
                "ORDER BY [DATE]");
        while (rs.next()) {
            result.add(new WorkItem(rs));
        }
        return result;
    }
    public ArrayList<BillingPeriod> GetBillingPeriods() throws SQLException
    {
        ArrayList<BillingPeriod> result = new ArrayList<>();
        String query = "WITH WI_CTE AS( \n" +
                "\tSELECT WI.ID, WI.DATE, WT.PERHOUR, WT.TAX, DATEDIFF(HOUR,STARTTIME, ENDTIME) HOURS FROM WORKITEMS WI\n" +
                "\tJOIN WORKTYPES WT ON TYPEID = WT.ID\n" +
                ")\n" +
                "SELECT BP.*, SUM(WI.HOURS) HOURS, SUM(WI.HOURS * WI.PERHOUR) GROSS, SUM(WI.HOURS * WI.PERHOUR * (1-TAX)) NET FROM BILLINGPERIODS BP\n" +
                "JOIN WI_CTE WI ON WI.DATE BETWEEN BP.[FROM] AND BP.[TO]\n" +
                "GROUP BY BP.ID, BP.[FROM], BP.[TO]";
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            result.add(new BillingPeriod(rs));
        }
        return result;
    }
    public int InsertProject(Project project) {
        try {
            statement.execute(String.format("INSERT INTO PROJECTS (NAME) VALUES (N'%s')", project.getName()));
            ResultSet rs = statement.executeQuery("SELECT IDENT_CURRENT('PROJECTS')");
            SetSync(SyncType.SyncProject);
            if(rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int InsertTask(Task task) throws SQLException{
        ResultSet rs = statement.executeQuery(String.format("SELECT ID FROM TASKS WHERE NAME = N'%s' AND PROJECTID = %d",task.getName(), task.getProjektId()));
        if(rs.next())
            return rs.getInt(1);
        else
        {
            statement.execute(String.format("INSERT INTO TASKS (NAME, PROJECTID) VALUES (N'%s', %d)", task.getName(), task.getProjektId()));
            rs = statement.executeQuery("SELECT IDENT_CURRENT('TASKS')");
            if(rs.next())
                return rs.getInt(1);
        }
        throw new SQLException();
    }
    public void InsertWorkItem(WorkItem workItem) throws SQLException {
        try {
            String query = String.format("INSERT INTO WORKITEMS (TASKID, TYPEID, DATE, STARTTIME, ENDTIME, COMMENT) " +
                            "VALUES (%d, %d, N'%s', N'%s', N'%s', N'%s')",
                    workItem.getTaskId(),
                    workItem.getTypeId(),
                    workItem.getDate().toString(),
                    workItem.getStartTime().toString(),
                    workItem.getEndTime().toString(),
                    IfNull(workItem.getComment(), ""));
            System.out.println(query);
            statement.execute(query);
            SetSync(SyncType.SyncWorkItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void InsertBillingPeriod(LocalDate from, LocalDate to){
        String query = String.format("INSERT INTO BILLINGPERIODS ([FROM], [TO]) " +
                        "VALUES (N'%s', N'%s')",
                from.toString(),
                to.toString());
        System.out.println(query);
        try {
            statement.execute(query);
            SetSync(SyncType.SyncBillingPeriod);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public int GetSyncData(UUID id){
        synchronized (SyncInformation.class){
            int data = SyncData.get(id);
            SyncData.replace(clientId, SyncType.None);
            return data;
        }
    }

    private void SetSync(int flag){
        new Thread(()->{
            synchronized (SyncType.class) {
                for (UUID id : SyncData.keySet()) {
                    if (id != clientId) {
                        int value = SyncData.get(id);
                        SyncData.replace(id, value | flag);
                    }
                }
            }
        }).run();
    }
    private String IfNull(String str1, String str2)
    {
        if(str1 == null)
            return str2;
        return str1;
    }
}
