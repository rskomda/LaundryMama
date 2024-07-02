package laundrymama.laundrymama;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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

public class Customer {
    private Pane root;
    private Stage stage;
    private String employeeName;
    private Label dateTimeLabel;
    private TableView<CustomerModel> tableView;
    private TextField customerNameTextField;
    private TextField phoneNumberTextField;
    private Dashboard dashboard;
    
    public Customer(Stage stage, String employeeName, Dashboard dashboard){
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
        
        Label title = new Label("Customer");
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
        
        HBox customerDetailsBox = new HBox(10);
        customerDetailsBox.setAlignment(Pos.BOTTOM_LEFT);
        customerDetailsBox.setLayoutX(230);
        customerDetailsBox.setLayoutY(68);
        customerDetailsBox.setPrefSize(579, 55);

        VBox customerNameBox = new VBox(5);
        customerNameBox.setPrefSize(245, 52);

        Label customerNameLabel = new Label("Customer Name");
        customerNameLabel.setTextFill(Color.WHITE);
        customerNameLabel.setFont(Font.font(16));

        HBox customerNameInputBox = new HBox(10);
        customerNameInputBox.setPrefSize(280, 25);

        customerNameTextField = new TextField();
        customerNameTextField.setPrefSize(269, 25);

        customerNameInputBox.getChildren().add(customerNameTextField);
        customerNameBox.getChildren().addAll(customerNameLabel, customerNameInputBox);

        VBox phoneNumberBox = new VBox(5);
        phoneNumberBox.setPrefSize(245, 52);

        Label phoneNumberLabel = new Label("Phone Number");
        phoneNumberLabel.setTextFill(Color.WHITE);
        phoneNumberLabel.setFont(Font.font(16));

        HBox phoneNumberInputBox = new HBox(10);
        phoneNumberInputBox.setPrefSize(280, 25);

        phoneNumberTextField = new TextField();
        phoneNumberTextField.setPrefSize(269, 25);

        phoneNumberInputBox.getChildren().add(phoneNumberTextField);
        phoneNumberBox.getChildren().addAll(phoneNumberLabel, phoneNumberInputBox);

        Button addButton = new Button("Add");
        addButton.setPrefSize(70, 25);
        
        addButton.setOnMouseEntered(e -> addButton.setCursor(Cursor.HAND));
        addButton.setOnMouseExited(e -> addButton.setCursor(Cursor.DEFAULT));
        addButton.setOnAction(event -> {
            addCustomer();
        });

        customerDetailsBox.getChildren().addAll(customerNameBox, phoneNumberBox, addButton);

        tableView = new TableView<>();
        tableView.setEditable(true);
        tableView.setLayoutX(229);
        tableView.setLayoutY(162);
        tableView.setPrefSize(657, 425);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CustomerModel, Integer> column1 = new TableColumn<>("Customer ID");
        column1.setCellValueFactory(new PropertyValueFactory<>("customerID"));

        TableColumn<CustomerModel, String> column2 = new TableColumn<>("Customer Name");
        column2.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        column2.setCellFactory(TextFieldTableCell.forTableColumn());
        column2.setOnEditCommit(event -> {
            CustomerModel customer = event.getRowValue();
            String oldValue = customer.getCustomerName();
            customer.setCustomerName(event.getNewValue());
            showAlert(oldValue, event.getNewValue());
            updateCustomerNameInDatabase(customer);
        });

        TableColumn<CustomerModel, String> column3 = new TableColumn<>("Phone Number");
        column3.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        column3.setCellFactory(TextFieldTableCell.forTableColumn());
        column3.setOnEditCommit(event -> {
            CustomerModel customer = event.getRowValue();
            String oldValue = customer.getCustomerPhone();
            customer.setCustomerPhone(event.getNewValue());
            showAlert(oldValue, event.getNewValue());
            updatePhoneNumberInDatabase(customer);
        });

        TableColumn<CustomerModel, Integer> column4 = new TableColumn<>("Order Amount");
        column4.setCellValueFactory(new PropertyValueFactory<>("orderAmount"));

        tableView.getColumns().addAll(column1, column2, column3, column4);

        root.getChildren().addAll(header, sideMenu, logo, title, userInfoBox, sidebar, customerDetailsBox, tableView);
        
        loadCustomerData();
        
        stage.setResizable(false);
    }
    
    private StackPane createMenuItem(String iconPath, String text) {
        StackPane stackPane = new StackPane();

        Color backgroundColor = text.equals("Customers") ? Color.WHITE : Color.web("#eaf8ff");
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
    
    private void loadCustomerData() {
        ObservableList<CustomerModel> customerData = FXCollections.observableArrayList();
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT customer.customerID, customer.customerName, customer.customerPhone, " +
                           "COUNT(`order`.customerID) AS orderAmount " +
                           "FROM customer LEFT JOIN `order` ON customer.customerID = `order`.customerID " +
                           "GROUP BY customer.customerID, customer.customerName, customer.customerPhone";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int customerID = resultSet.getInt("customer.customerID");
                String customerName = resultSet.getString("customer.customerName");
                String customerPhone = resultSet.getString("customer.customerPhone");
                int orderAmount = resultSet.getInt("orderAmount");

                customerData.add(new CustomerModel(customerID, customerName, customerPhone, orderAmount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableView.setItems(customerData);
    }
    
    private void addCustomer() {
        String customerName = customerNameTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();

        if (!customerName.isEmpty() && !phoneNumber.isEmpty()) {
            if (isPhoneNumberUnique(phoneNumber)) {
                try (Connection connection = Database.getConnection()) {
                    String query = "INSERT INTO customer (customerName, customerPhone) VALUES (?, ?)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, customerName);
                    statement.setString(2, phoneNumber);
                    statement.executeUpdate();

                    customerNameTextField.setText("");
                    phoneNumberTextField.setText("");

                    loadCustomerData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Duplicate Phone Number");
                alert.setHeaderText(null);
                alert.setContentText("The phone number entered already exists in the database.");
                phoneNumberTextField.setText("");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Validation");
            alert.setHeaderText(null);
            alert.setContentText("Please fill out all fields before submitting.");
            alert.showAndWait();
        }
    }
    
    private boolean isPhoneNumberUnique(String phoneNumber) {
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) AS count FROM customer WHERE customerPhone = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, phoneNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void updateCustomerNameInDatabase(CustomerModel customer) {
        try (Connection connection = Database.getConnection()) {
            String query = "UPDATE customer SET customerName = ? WHERE customerID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, customer.getCustomerName());
            statement.setInt(2, customer.getCustomerID());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePhoneNumberInDatabase(CustomerModel customer) {
        try (Connection connection = Database.getConnection()) {
            String query = "UPDATE customer SET customerPhone = ? WHERE customerID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, customer.getCustomerPhone());
            statement.setInt(2, customer.getCustomerID());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String oldValue, String newValue) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Data Change Alert");
        alert.setHeaderText(null);
        alert.setContentText("Data changed from \"" + oldValue + "\" to \"" + newValue + "\".");
        Platform.runLater(alert::showAndWait);
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
            case "History":
                newScene = new Scene(new History(stage, employeeName, dashboard).getRoot(), 900, 600);
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
