package Clients;
import Models.*;

import java.io.IOException;
import java.net.InterfaceAddress;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static com.company.SyncInformation.*;

public class Client extends BaseClient {
    public Client() throws IOException {
        super();
    }
    public ArrayList<Project> GetProjects() throws IOException {
        return ListRequest(Project.class, "GetProjects");
    }
    public ArrayList<WorkType> GetWorkTypes() throws IOException {
        return ListRequest(WorkType.class, "GetWorkTypes");
    }
    public ArrayList<WorkItem> GetWorkItems() throws IOException {
        return ListRequest(WorkItem.class, "GetWorkItems");
    }
    public ArrayList<BillingPeriod> GetBillingPeriods() throws IOException {
        return ListRequest(BillingPeriod.class, "GetBillingPeriods");
    }
    public Project InsertProject(String projectName) throws IOException {
        Project project = new Project(projectName);
        int id = Request(Integer.class,"InsertProject", project);
        project.setId(id);
        return project;
    }
    public int InsertTask(String taskName, int projectId) throws IOException {
        Task task = new Task(taskName, projectId);
        int id = Request(Integer.class,"InsertTask", task);
        return id;
    }
    public void InsertWorkItem(WorkItem workItem) throws IOException {
        Request("InsertWorkItem", workItem);
    }
    public void InsertBillingPeriod(LocalDate from, LocalDate to) throws IOException {
        Request("InsertBillingPeriod", from, to);

    }
    public int GetSyncData(){
        try {
            return Request(Integer.class, "GetSyncData", ClientId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
