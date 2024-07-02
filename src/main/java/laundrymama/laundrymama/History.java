package laundrymama.laundrymama;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class History {
    private Pane root;
    private Stage stage;
    private String employeeName;
    private Label dateTimeLabel;
    private Dashboard dashboard;
    private TableView<HistoryModel> tableView;
    
    public History(Stage stage, String employeeName, Dashboard dashboard){
        this.stage = stage;
        this.employeeName = employeeName;
        this.dashboard = dashboard;
        root = new Pane();
        
        Rectangle header = new Rectangle(900, 141);
        header.setFill(Color.web("#0a57a2"));
        header.setArcWidth(5.0);
        header.setArcHeight(5.0);
        
        Rectangle sideMenu = new Rectangle(200, 575);
        sideMenu.setFill(Color.web("#eaf8ff"));
        sideMenu.setArcWidth(15.0);
        sideMenu.setArcHeight(15.0);
        sideMenu.setLayoutX(14);
        sideMenu.setLayoutY(14);
        
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/logo.png")));
        logo.setFitHeight(112);
        logo.setFitWidth(112);
        logo.setLayoutX(58);
        logo.setLayoutY(40);
        
        Label title = new Label("History");
        title.setLayoutX(230);
        title.setLayoutY(25);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System Bold", 20));

        HBox userInfoBox = new HBox(5);
        userInfoBox.setAlignment(Pos.CENTER_RIGHT);
        userInfoBox.setLayoutX(624);
        userInfoBox.setLayoutY(31);
        userInfoBox.setPrefSize(262, 17);

        Label userLabel = new Label(employeeName);
        userLabel.setTextFill(Color.WHITE);
        
        Label separatorLabel = new Label("|");
        separatorLabel.setTextFill(Color.WHITE);
        
        dateTimeLabel = new Label();
        dateTimeLabel.setTextFill(Color.WHITE);
        initializeDateTimeLabel();
        startDateTimeUpdater();

        userInfoBox.getChildren().addAll(userLabel, separatorLabel, dateTimeLabel);
        
        VBox sidebar = new VBox();
        sidebar.setLayoutX(22);
        sidebar.setLayoutY(155);
        sidebar.setPrefSize(183, 228);
        
        sidebar.getChildren().add(createMenuItem("/laundrymama/laundrymama/dashboardlogo.png", "Dashboard"));
        sidebar.getChildren().add(createMenuItem("/laundrymama/laundrymama/neworderlogo.png", "New Order"));
        sidebar.getChildren().add(createMenuItem("/laundrymama/laundrymama/customerslogo.png", "Customers"));
        sidebar.getChildren().add(createMenuItem("/laundrymama/laundrymama/historylogo.png", "History"));
        sidebar.getChildren().add(createMenuItem("/laundrymama/laundrymama/logoutlogo.png", "Log out"));
        
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(25); // Example fixed cell size, adjust as needed
        tableView.setPrefWidth(657);
        tableView.setMaxWidth(657);
        tableView.setMinWidth(657);
        tableView.setLayoutX(229);
        tableView.setLayoutY(162);
        tableView.setPrefSize(657, 425);

        TableColumn<HistoryModel, Integer> column1 = new TableColumn<>("Order ID");
        column1.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<HistoryModel, String> column2 = new TableColumn<>("Date");
        column2.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<HistoryModel, String> column3 = new TableColumn<>("Employee Name");
        column3.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        TableColumn<HistoryModel, String> column4 = new TableColumn<>("Customer Name");
        column4.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        TableColumn<HistoryModel, Integer> column5 = new TableColumn<>("Total Price");
        column5.setCellValueFactory(new PropertyValueFactory<>("total"));

        tableView.getColumns().addAll(column1, column2, column3, column4, column5);

        root.getChildren().addAll(header, sideMenu, logo, title, userInfoBox, sidebar, tableView);
        
        loadOrderHistory();
        
        stage.setResizable(false);
    }
    
    private StackPane createMenuItem(String iconPath, String text) {
        StackPane stackPane = new StackPane();

        Color backgroundColor = text.equals("History") ? Color.WHITE : Color.web("#eaf8ff");  // Conditional background color
        Rectangle background = new Rectangle(183, 38, backgroundColor);
        background.setArcWidth(15);
        background.setArcHeight(15);

        HBox hbox = new HBox(15);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(0, 0, 0, 17));

        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        icon.setFitWidth(25);
        icon.setFitHeight(25);

        Label label = new Label(text);

        hbox.getChildren().addAll(icon, label);
        stackPane.getChildren().addAll(background, hbox);
        
        stackPane.setOnMouseClicked(event -> handleMenuClick(text, employeeName));
        stackPane.setOnMouseEntered(e -> stackPane.setCursor(Cursor.HAND));
        stackPane.setOnMouseExited(e -> stackPane.setCursor(Cursor.DEFAULT));

        return stackPane;
    }
    
    private void initializeDateTimeLabel() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText(now.format(formatter));
    }

    private void startDateTimeUpdater() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                dateTimeLabel.setText(now.format(formatter));
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void loadOrderHistory() {
        ObservableList<HistoryModel> data = FXCollections.observableArrayList();
        
        String query = "SELECT o.orderID, o.date, e.employeeName, c.customerName, o.total " +
                       "FROM `order` o " +
                       "JOIN employee e ON o.employeeID = e.employeeID " +
                       "JOIN customer c ON o.customerID = c.customerID " +
                       "WHERE o.status = 'Picked Up' " +
                       "ORDER BY o.orderID DESC";
        
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int orderId = resultSet.getInt("orderID");
                String date = resultSet.getString("date");
                String employeeName = resultSet.getString("employeeName");
                String customerName = resultSet.getString("customerName");
                int total = resultSet.getInt("total");

                data.add(new HistoryModel(orderId, date, employeeName, customerName, total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        tableView.setItems(data);
    }
    
        
    private void handleMenuClick(String menuItem, String employeeName) {
        Scene newScene;

        switch (menuItem) {
            case "Dashboard":
                newScene = new Scene(new Dashboard(stage, employeeName).getRoot(), 900, 600);
                stage.setScene(newScene);
                break;
            case "New Order":
                newScene = new Scene(new Order(stage, employeeName, dashboard).getRoot(), 900, 600);
                newScene.getStylesheets().add(getClass().getResource("/laundrymama/laundrymama/styles.css").toExternalForm());
                stage.setScene(newScene);
                break;
            case "Customers":
                newScene = new Scene(new Customer(stage, employeeName, dashboard).getRoot(), 900, 600);
                stage.setScene(newScene);
                break;
            case "Log out":
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit App Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to log out?");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");

                alert.getButtonTypes().setAll(yesButton, noButton);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == yesButton) {
                    stage.close();
                } 
                break;
        }
    }
     
    public Pane getRoot() {
        return root;
    }
}
