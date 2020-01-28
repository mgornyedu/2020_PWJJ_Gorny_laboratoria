package Models;

import DataAccess.MainRepository;
import com.sun.scenario.effect.impl.prism.PrImage;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class WorkItem {
    public WorkItem() {
    }

    public WorkItem(ResultSet st) throws SQLException {
        Id = st.getInt("ID");
        TaskId = st.getInt("TASKID");
        TypeId = st.getInt("TYPEID");
        Date = st.getDate("DATE").toLocalDate();
        StartTime = st.getTime("STARTTIME").toLocalTime();
        EndTime = st.getTime("ENDTIME").toLocalTime();
        Comment = st.getString("COMMENT");
        Project = st.getString("PROJECT");
        Task = st.getString("TASK");
        Type = st.getString("TYPE");
    }

    private int Id;
    private int TaskId;
    private int TypeId;
    private LocalDate Date;
    private LocalTime StartTime;
    private LocalTime EndTime;
    private String Comment;
    private String Project;
    private String Task;
    private String Type;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getTaskId() {
        return TaskId;
    }

    public void setTaskId(int taskId) {
        TaskId = taskId;
    }

    public int getTypeId() {
        return TypeId;
    }

    public void setTypeId(int typeId) {
        TypeId = typeId;
    }

    public LocalTime getStartTime() {
        return StartTime;
    }

    public void setStartTime(LocalTime startTime) {
        StartTime = startTime;
    }

    public LocalTime getEndTime() {
        return EndTime;
    }

    public void setEndTime(LocalTime endTime) {
        EndTime = endTime;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public LocalDate getDate() {
        return Date;
    }

    public void setDate(LocalDate date) {
        Date = date;
    }

    public String getProject() {
        return Project;
    }

    public void setProject(String project) {
        Project = project;
    }

    public String getTask() {
        return Task;
    }

    public void setTask(String task) {
        Task = task;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}

