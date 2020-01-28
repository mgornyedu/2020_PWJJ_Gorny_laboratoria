package WorkManager;

import Clients.Client;
import Models.*;
import WorkManager.Managers.ValidManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.io.*;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

/**
 * Klasa głównego kontrolera projektu
 */
public class Controller implements Initializable {
    /**
     * Tabela danych czasów pracy
     */
    @FXML private TableView<WorkItem> tableView;
    /**
     * Tabela danych okresów rozliczeniowych
     */
    @FXML private TableView<BillingPeriod> summaryView;

    /**
     * Kontrolka wyboru typu wykonanej pracy
     */
    @FXML private ComboBox<WorkType> type;
    /**
     * Kontrolka wyboru daty wykonanej pracy
     */
    @FXML private DatePicker date;
    /**
     * Polę wprowadzania czasu rozpoczęcia pracy
     */
    @FXML private TextField from;
    /**
     * Polę wprowadzania czasu zakończenia pracy
     */
    @FXML private TextField to;
    /**
     * Kontrolka wyboru projektu
     */
    @FXML private ComboBox<Project> project;
    /**
     * Pole wprowadzenia zadania
     */
    @FXML private TextField task;
    /**
     * Pole wprowadzenia komentarza
     */
    @FXML private TextArea desc;

    @FXML private TextField newProjectName;
    /**
     * Kontrolka wyboru daty rozpoczęcia okresu rozliczeniowego
     */
    @FXML private DatePicker billingPeriodFrom;
    /**
     * Kontrolka wyboru daty zakończenia okresu rozliczeniowego
     */
    @FXML private DatePicker billingPeriodTo;

    /**
     * Przycisk dodający czas pracy
     */
    @FXML private Button addWorkItemButton;
    /**
     * Przycisk dodający nowy projekt
     */
    @FXML private Button addNewProjectButton;
    /**
     * Przycik dodający okres rozliczeniowy
     */
    @FXML private Button addBillingPeriodButton;

    /**
     * Domyślny konstruktor kontrolera
     */
    public  Controller(){
        _Timer = new Timer();
        _Timer.scheduleAtFixedRate(new SyncTask(), 1000, 100);
    }
    private Timer _Timer;
    class SyncTask extends TimerTask
    {
        public void run() {
            try {
                Client client = new Client();
                int data = client.GetSyncData();
                if(SyncType.HasFlag(data, SyncType.SyncWorkItem)){
                    RefreshItems();
                    RefreshBillingPeriod();
                }
                else if(SyncType.HasFlag(data, SyncType.SyncBillingPeriod)){
                    RefreshBillingPeriod();
                }
                else if(SyncType.HasFlag(data, SyncType.SyncProject)) {
                    RefreshProjects();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Kolekcja czasów pracy
     */
    private ObservableList<WorkItem> workItems;
    private ObservableList<WorkType> workTypes;
    /**
     * Kolekcja okresów rozliczeniowych
     */
    private ObservableList<BillingPeriod> billingPeriods;
    /**
     * Kolekcja projektów
     */
    private ObservableList<Project> projects;
    /**
     * Pole przechowujące wartość rozpoczęcia pracy
     */
    private LocalTime workItemFromTime;
    /**
     * Pole przechoujące wartość zakończenia pracy
     */
    private LocalTime workItemToTime;

    /**
     * Funkcja inicjalizująca kontroller
     * @param location Lokalizacja
     * @param resources Zasoby
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        workItems = tableView.getItems();
        billingPeriods = summaryView.getItems();
        projects = project.getItems();
        workTypes = type.getItems();
        InitValidation();
        try {
            LoadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funkcja służąca do inicjalizacji klas walidujących
     */
    private void InitValidation(){
        /**
         * Initializacja walidacji dla wykonanej pracy
         */
        ValidManager workItemValidManager = new ValidManager(addWorkItemButton.disableProperty()){
            @Override
            protected boolean Valid() {
                boolean castResult = true;
                try{
                    workItemFromTime = LocalTime.parse(from.getCharacters(), DateTimeFormatter.ofPattern("H:mm"));
                    workItemToTime = LocalTime.parse(to.getCharacters(), DateTimeFormatter.ofPattern("H:mm"));
                }
                catch (Exception ex){
                    castResult = false;
                }
                return castResult
                        && workItemFromTime.compareTo(workItemToTime) < 0
                        && type.getSelectionModel().getSelectedIndex() >= 0
                        && project.getSelectionModel().getSelectedIndex() >= 0
                        && date.getValue() != null
                        && !task.getText().isEmpty();
            }
        };
        /**
         * Initializacja walidacji dla definicji projektu
         */
        ValidManager newProjectNameValidManager = new ValidManager(addNewProjectButton.disableProperty()){
            @Override
            protected boolean Valid() {
                return !newProjectName.getText().isEmpty() && !projects.contains(newProjectName.getText());
            }
        };
        /**
         * Initializacja walidacji dla okresu rozliczeniowego
         */
        ValidManager bilingPeriodValidManager = new ValidManager(addBillingPeriodButton.disableProperty()){
            @Override
            protected boolean Valid() {
                LocalDate fromDate = billingPeriodFrom.getValue(), toDate = billingPeriodTo.getValue();
                return fromDate != null && toDate != null && fromDate.compareTo(toDate) < 0
                        && !billingPeriods.stream().anyMatch(x-> x.getFrom().compareTo(fromDate) == 0 && x.getTo().compareTo(toDate) == 0);
            }
        };
        workItemValidManager.AppendMonitoringProperty(type.getSelectionModel().selectedIndexProperty());
        workItemValidManager.AppendMonitoringProperty(project.getSelectionModel().selectedIndexProperty());
        workItemValidManager.AppendMonitoringProperty(date.valueProperty());
        workItemValidManager.AppendMonitoringProperty(from.textProperty());
        workItemValidManager.AppendMonitoringProperty(to.textProperty());
        workItemValidManager.AppendMonitoringProperty(task.textProperty());

        newProjectNameValidManager.AppendMonitoringProperty(newProjectName.textProperty());

        bilingPeriodValidManager.AppendMonitoringProperty(billingPeriodFrom.valueProperty());
        bilingPeriodValidManager.AppendMonitoringProperty(billingPeriodTo.valueProperty());
    }

    /**
     * Funkcja dodająca wykonaną pracę
     * @param event
     */
    @FXML
    private void AddWorkItem(ActionEvent event) throws IOException {
        WorkItem item = null;
        int officeHour = 0, remoteHour = 0, businessTripHour = 0;
        Client client = new Client();
        int taskId = client.InsertTask(task.getText(), project.getSelectionModel().getSelectedItem().getId());
        Models.WorkItem workItem = new Models.WorkItem();
        workItem.setDate(date.getValue());
        workItem.setStartTime(workItemFromTime);
        workItem.setEndTime(workItemToTime);
        workItem.setComment(desc.getText());
        workItem.setTaskId(taskId);
        workItem.setTypeId(type.getSelectionModel().getSelectedItem().getId());
        client.InsertWorkItem(workItem);

        project.getSelectionModel().clearSelection();
        task.setText(null);
        date.setValue(null);
        from.setText(null);
        to.setText(null);
        desc.setText(null);
        client.Close();
        RefreshItems();
        RefreshBillingPeriod();
    }

    /**
     * Funkcja dodająca projekt
     * @param event argument zdarzenia
     */
    @FXML
    private void AddProject(ActionEvent event) throws IOException {
        String projectName = newProjectName.getText();
        newProjectName.setText("");
        Client client = new Client();
        client.Close();
        RefreshProjects();
    }

    /**
     * Funkcja dodająca okres rozliczeniowy
     * @param event argument zdarzenia
     */
    @FXML
    private void AddBilingPeriod(ActionEvent event) throws IOException {
        Client client = new Client();
        client.InsertBillingPeriod(billingPeriodFrom.getValue(), billingPeriodTo.getValue());
        client.Close();
        billingPeriodFrom.setValue(null);
        billingPeriodTo.setValue(null);
        RefreshBillingPeriod();
    }

    /**
     * Fukcja pobierająca okresy rozliczeniowy dla wskazanej daty
     * @param date Data wykonania pracy
     * @return Okres rozliczeniowy
     */
    private BillingPeriod GetPeriod(LocalDate date)
    {
        for (BillingPeriod period: billingPeriods) {
            if(period.getFrom().compareTo(date)< 0 && period.getTo().compareTo(date) >0)
               return  period;
        }
        return null;
    }

    /**
     * Funkcja zapisywania danych do pliku workManager.dat
     * @throws IOException Wyjątek związany z czytaniem plików
     */
    public void Save() {
    }

    /**
     * Funkcja odczytujaca zapisane dane z pliku workManager.dat i zapisująca je do wykorzystanych kolekcji
     * @throws IOException Wyjątek związany z czytaniem plików
     */
    private void LoadData()throws IOException{
        Client client = new Client();
        projects.addAll(client.GetProjects());
        workTypes.addAll(client.GetWorkTypes());
        workItems.addAll(client.GetWorkItems());
        billingPeriods.addAll(client.GetBillingPeriods());
        client.Close();
    }
    private void RefreshItems() throws IOException {
        synchronized (SyncType.class) {
            Platform.runLater(()->{
                try {
                    Client client = new Client();
                    workItems.clear();
                    workItems.addAll(client.GetWorkItems());
                    client.Close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    private void RefreshBillingPeriod() throws IOException {
        synchronized (SyncType.class) {
            Platform.runLater(()->{
                try {
                    Client client = new Client();
                    billingPeriods.clear();
                    billingPeriods.addAll(client.GetBillingPeriods());
                    client.Close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    private void RefreshProjects() throws IOException {
        synchronized (SyncType.class) {
            Platform.runLater(()->{
                try {
                    Client client = new Client();
                    projects.clear();
                    projects.addAll(client.GetProjects());
                    client.Close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

