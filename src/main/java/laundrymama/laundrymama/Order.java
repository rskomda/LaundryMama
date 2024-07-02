package laundrymama.laundrymama;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Order {
    private Pane root;
    private Stage stage;
    private String employeeName;
    private Label dateTimeLabel;
    private ImageView searchIcon;
    private ScheduledExecutorService executorService;
    private Runnable checkTask;
    private TextField customerPhoneTextField;
    private Label customerName;
    private Label phoneNumber;
    private Label orderID;
    private Label itemQuantityLabel;
    private Label itemName;
    private Label itemDesc;
    private Label itemPrice;
    private Label subtotalLabel;
    private Label loyaltyDiscountLabel;
    private Label totalValueLabel;
    private VBox orderItemsBox;
    private Button downloadReceiptButton;
    private Button paidButton;
    
    private Map<Integer, HBox> orderItemsMap = new HashMap<>();
    private boolean phoneVerified = false;
    private Runnable onNewOrderPaid;
    private Dashboard dashboard;
    
    public Order(Stage stage, String employeeName, Dashboard dashboard) {
        this.stage = stage;
        this.employeeName = employeeName;
        this.dashboard = dashboard;
        root = new Pane();
        executorService = Executors.newSingleThreadScheduledExecutor(); 
        
        Rectangle header = new Rectangle(900, 141);
        header.setFill(Color.web("#0a57a2"));
        
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
        
        Label title = new Label("New Order");
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
        
        VBox customerPhoneBox = new VBox(5);
        customerPhoneBox.setLayoutX(230);
        customerPhoneBox.setLayoutY(68);
        customerPhoneBox.setPrefSize(245, 52);

        Label customerPhoneLabel = new Label("Phone Number");
        customerPhoneLabel.setTextFill(Color.WHITE);
        customerPhoneLabel.setFont(Font.font(16));

        HBox customerPhoneInputBox = new HBox(10);
        customerPhoneInputBox.setPrefSize(280, 25);

        customerPhoneTextField = new TextField();
        customerPhoneTextField.setPrefSize(245, 25);

        searchIcon = new ImageView(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/finding.png")));
        searchIcon.setFitHeight(25);
        searchIcon.setFitWidth(25);

        customerPhoneInputBox.getChildren().addAll(customerPhoneTextField, searchIcon);
        customerPhoneBox.getChildren().addAll(customerPhoneLabel, customerPhoneInputBox);
        
        setupPhoneNumberListener();

        HBox mainContentBox = new HBox(20);
        mainContentBox.setLayoutX(232);
        mainContentBox.setLayoutY(155);
        mainContentBox.setPrefSize(647, 432);

        VBox servicesBox = new VBox(10);
        servicesBox.setPrefSize(336, 432);

        Label servicesLabel = new Label("Services");
        servicesLabel.setFont(Font.font(16));

        GridPane servicesGrid = new GridPane();
        servicesGrid.setHgap(10);
        servicesGrid.setVgap(10);
        servicesGrid.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(100), new ColumnConstraints(100));
        servicesGrid.getRowConstraints().addAll(new RowConstraints(110), new RowConstraints(110), new RowConstraints(110));

        servicesGrid.add(createServiceItem("/laundrymama/laundrymama/service1.png", "Wash, dry (regular)", 1), 0, 0);
        servicesGrid.add(createServiceItem("/laundrymama/laundrymama/service2.png", "Wash, dry, fold (regular)", 2), 1, 0);
        servicesGrid.add(createServiceItem("/laundrymama/laundrymama/service3.png", "Wash, dry, iron (regular)", 3), 2, 0);
        servicesGrid.add(createServiceItem("/laundrymama/laundrymama/service4.png", "Wash, dry, iron (express)", 4), 0, 1);
        servicesGrid.add(createServiceItem("/laundrymama/laundrymama/service5.png", "Detergent", 5), 1, 1);

        servicesBox.getChildren().addAll(servicesLabel, servicesGrid);

        VBox orderDetailsBox = new VBox(10);
        orderDetailsBox.setPrefSize(293, 343);

        Label orderDetailsLabel = new Label("Order Details");
        orderDetailsLabel.setFont(Font.font(16));

        HBox orderIdBox = new HBox();
        orderIdBox.setPrefSize(293, 79);

        VBox labelsBox = new VBox(5);
        labelsBox.setPrefWidth(100);
        labelsBox.getChildren().addAll(new Label("Order ID"), new Label("Customer Name"), new Label("Phone Number"));

        VBox valuesBox = new VBox(5);
        valuesBox.setAlignment(Pos.TOP_RIGHT);
        
        orderID = new Label("-");
        orderID.setPrefWidth(194);
        orderID.setAlignment(Pos.BASELINE_RIGHT);
        
        customerName = new Label("-");
        customerName.setPrefWidth(192);
        customerName.setAlignment(Pos.BASELINE_RIGHT);
        
        phoneNumber = new Label("-");
        phoneNumber.setPrefWidth(194);
        phoneNumber.setAlignment(Pos.BASELINE_RIGHT);
        
        valuesBox.getChildren().addAll(orderID, customerName, phoneNumber);

        orderIdBox.getChildren().addAll(labelsBox, valuesBox);
        
        orderItemsBox = new VBox(5);
        orderItemsBox.setPrefSize(293, 138);
        orderItemsBox.setVisible(false);

        Line subtotalLine = new Line(-100, 0, 190, 0);

        HBox subtotalBox = new HBox();
        subtotalBox.setPrefSize(293, 16);

        VBox subtotalLabelBox = new VBox();
        subtotalLabelBox.setPrefWidth(100);
        subtotalLabelBox.getChildren().add(new Label("Subtotal"));

        VBox subtotalValueBox = new VBox();
        subtotalValueBox.setPrefWidth(192);
        subtotalValueBox.setAlignment(Pos.TOP_RIGHT);
        subtotalLabel = new Label("Rp0");
        subtotalValueBox.getChildren().add(subtotalLabel);

        subtotalBox.getChildren().addAll(subtotalLabelBox, subtotalValueBox);

        Line loyaltyDiscountLine = new Line(-100, 0, 190, 0);

        HBox loyaltyDiscountBox = new HBox();
        loyaltyDiscountBox.setPrefSize(293, 16);

        VBox loyaltyDiscountLabelBox = new VBox();
        loyaltyDiscountLabelBox.setPrefWidth(100);
        loyaltyDiscountLabelBox.getChildren().add(new Label("Loyalty Discount"));

        VBox loyaltyDiscountValueBox = new VBox();
        loyaltyDiscountValueBox.setPrefWidth(192);
        loyaltyDiscountValueBox.setAlignment(Pos.TOP_RIGHT);
        loyaltyDiscountLabel = new Label("Rp0");
        loyaltyDiscountValueBox.getChildren().add(loyaltyDiscountLabel);

        loyaltyDiscountBox.getChildren().addAll(loyaltyDiscountLabelBox, loyaltyDiscountValueBox);

        Line totalLine = new Line(-100, 0, 190, 0);

        HBox totalBox = new HBox();
        totalBox.setPrefSize(293, 16);

        VBox totalLabelBox = new VBox();
        totalLabelBox.setPrefWidth(100);
        Label totalLabel = new Label("Total");
        totalLabel.setFont(Font.font(14));
        totalLabelBox.getChildren().add(totalLabel);

        VBox totalValueBox = new VBox();
        totalValueBox.setPrefWidth(192);
        totalValueBox.setAlignment(Pos.TOP_RIGHT);
        totalValueLabel = new Label("Rp0");
        totalValueLabel.setFont(Font.font(14));
        totalValueBox.getChildren().add(totalValueLabel);

        totalBox.getChildren().addAll(totalLabelBox, totalValueBox);

        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPrefSize(293, 37);

        downloadReceiptButton = new Button("Download Receipt");
        downloadReceiptButton.setPrefSize(150, 37);
        downloadReceiptButton.setOnAction(e -> handleDownloadReceiptButtonClick());
        downloadReceiptButton.setOnMouseEntered(e -> {
            downloadReceiptButton.setCursor(Cursor.HAND);
            downloadReceiptButton.getStyleClass().add("custom-button");
        });
        downloadReceiptButton.setOnMouseExited(e -> {
            downloadReceiptButton.setCursor(Cursor.DEFAULT);
            downloadReceiptButton.getStyleClass().remove("custom-button");
        });

        paidButton = new Button("Paid");
        paidButton.setPrefSize(138, 37);
        paidButton.setOnAction(e -> handlePaidButtonClick());
        paidButton.setOnMouseEntered(e -> {
            paidButton.setCursor(Cursor.HAND);
            paidButton.getStyleClass().add("custom-button");
        });
        paidButton.setOnMouseExited(e -> {
            paidButton.setCursor(Cursor.DEFAULT);
            paidButton.getStyleClass().remove("custom-button");
        });

        buttonsBox.getChildren().addAll(downloadReceiptButton, paidButton);
        
        updateButtonStates();

        orderDetailsBox.getChildren().addAll(orderDetailsLabel, orderIdBox, orderItemsBox, subtotalLine, subtotalBox, loyaltyDiscountLine, loyaltyDiscountBox, totalLine, totalBox, buttonsBox);

        mainContentBox.getChildren().addAll(servicesBox, orderDetailsBox);

        root.getChildren().addAll(header, sideMenu, logo, title, userInfoBox, sidebar, customerPhoneBox, mainContentBox);

        stage.setResizable(false);
    }
    
    private StackPane createMenuItem(String iconPath, String text) {
        StackPane stackPane = new StackPane();

        Color backgroundColor = text.equals("New Order") ? Color.WHITE : Color.web("#eaf8ff");
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmyyy:ss");
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
    
    private StackPane createServiceItem(String imagePath, String text, int serviceID) {
        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(200, 150);

        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        imageView.setFitHeight(110);
        imageView.setFitWidth(105);
        imageView.setPreserveRatio(true);

        Rectangle background = new Rectangle(105, 110, Color.web("#ffffffa6"));
        background.setArcWidth(10);
        background.setArcHeight(10);

        Label label = new Label(text);
        label.setPrefSize(105, 110);
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.setFont(Font.font(14));

        StackPane.setAlignment(label, Pos.CENTER);

        stackPane.getChildren().addAll(imageView, background, label);

        stackPane.setOnMouseClicked(e -> {
            if (phoneVerified) {
                handleServiceItemClick(serviceID);
            }
        });
        stackPane.setOnMouseEntered(e -> stackPane.setCursor(phoneVerified ? Cursor.HAND : Cursor.DEFAULT));
        stackPane.setOnMouseExited(e -> stackPane.setCursor(Cursor.DEFAULT));

        return stackPane;
    }
    
    private void handleServiceItemClick(int serviceID) {
        ServiceModel service = getServiceDetails(serviceID);

        if (service != null) {
            if (orderItemsMap.containsKey(serviceID)) {
                
                HBox orderItemBox = orderItemsMap.get(serviceID);
                Label itemQuantityLabel = (Label) orderItemBox.lookup("#itemQuantityLabel");
                Label itemPriceLabel = (Label) orderItemBox.lookup("#itemPriceLabel");

                int currentQuantity = Integer.parseInt(itemQuantityLabel.getText());
                currentQuantity++;
                itemQuantityLabel.setText(String.valueOf(currentQuantity));

                int totalPrice = currentQuantity * service.getPrice();
                itemPriceLabel.setText("Rp" + totalPrice);
            } else {
                HBox orderItemBox = new HBox(10);
                orderItemBox.setPrefSize(293, 41);

                StackPane itemQuantityBox = new StackPane();
                Rectangle quantityBackground = new Rectangle(50, 50, Color.web("#0a57a2"));
                quantityBackground.setArcWidth(10);
                quantityBackground.setArcHeight(10);

                Label itemQuantityLabel = new Label("1");
                itemQuantityLabel.setId("itemQuantityLabel");
                itemQuantityLabel.setTextFill(Color.WHITE);
                itemQuantityLabel.setFont(Font.font(16));

                itemQuantityBox.getChildren().addAll(quantityBackground, itemQuantityLabel);

                VBox itemDetailsBox = new VBox(5);
                itemDetailsBox.setPrefWidth(150);
                itemDetailsBox.setAlignment(Pos.CENTER_LEFT);
                Label itemName = new Label(service.getName());
                Label itemDesc = new Label(service.getDescription());

                itemDetailsBox.getChildren().addAll(itemName, itemDesc);

                VBox itemPriceBox = new VBox(5);
                itemPriceBox.setPrefWidth(100);
                itemPriceBox.setAlignment(Pos.CENTER_RIGHT);
                Label itemPriceLabel = new Label("Rp" + service.getPrice());
                itemPriceLabel.setId("itemPriceLabel");

                ImageView delete = new ImageView(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/trashlogo.png")));
                delete.setFitWidth(16);
                delete.setFitHeight(16);

                delete.setOnMouseClicked(e -> {
                    Optional<HBox> optionalOrderItemBox = Optional.ofNullable(orderItemsMap.get(serviceID));
                    optionalOrderItemBox.ifPresent(box -> {
                        orderItemsBox.getChildren().remove(box);
                        orderItemsMap.remove(serviceID);
                        updateTotalPrice();
                    });
                });
                delete.setOnMouseEntered(e -> delete.setCursor(Cursor.HAND));
                delete.setOnMouseExited(e -> delete.setCursor(Cursor.DEFAULT));

                itemPriceBox.getChildren().addAll(itemPriceLabel, delete);

                orderItemBox.getChildren().addAll(itemQuantityBox, itemDetailsBox, itemPriceBox);
                orderItemsBox.getChildren().add(orderItemBox);

                orderItemsMap.put(serviceID, orderItemBox);
            }

            updateTotalPrice();

            if (!orderItemsBox.isVisible()) {
                orderItemsBox.setVisible(true);
            }
        }
    }
    
    private ServiceModel getServiceDetails(int serviceID) {
        ServiceModel service = null;
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM service WHERE serviceID = ?")) {
            preparedStatement.setInt(1, serviceID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String serviceName = resultSet.getString("serviceName");
                int servicePrice = resultSet.getInt("servicePrice");
                String description = resultSet.getString("description");
                service = new ServiceModel(serviceID, serviceName, servicePrice, description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return service;
    }

    private void updateTotalPrice() {
        int subtotal = 0;
        for (HBox orderItemBox : orderItemsMap.values()) {
            Label itemQuantityLabel = (Label) orderItemBox.lookup("#itemQuantityLabel");
            Label itemPriceLabel = (Label) orderItemBox.lookup("#itemPriceLabel");

            int quantity = Integer.parseInt(itemQuantityLabel.getText());
            int price = Integer.parseInt(itemPriceLabel.getText().replace("Rp", ""));

            subtotal += price;
        }

        int loyaltyDiscount = calculateLoyaltyDiscount();
        int total = subtotal - loyaltyDiscount;

        subtotalLabel.setText("Rp" + subtotal);
        loyaltyDiscountLabel.setText("Rp" + loyaltyDiscount);
        totalValueLabel.setText("Rp" + total);
        
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        int total = Integer.parseInt(totalValueLabel.getText().replace("Rp", ""));
        boolean disableButtons = total == 0;
        downloadReceiptButton.setDisable(disableButtons);
        paidButton.setDisable(disableButtons);
    }
    
    private int calculateLoyaltyDiscount() {
        int discount = 0;
        String query = "SELECT COUNT(*) " +
                       "FROM `order` o " +
                       "JOIN customer c ON o.customerID = c.customerID " +
                       "WHERE c.customerPhone = ?";

        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, phoneNumber.getText());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int orderCount = resultSet.getInt(1);
                if (orderCount > 0 && orderCount % 5 == 0) {
                    discount = 10000;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return discount;
    }

    private void handleMenuClick(String menuItem, String employeeName) {
        Scene newScene;

        switch (menuItem) {
            case "Dashboard":
                newScene = new Scene(new Dashboard(stage, employeeName).getRoot(), 900, 600);
                stage.setScene(newScene);
                break;
            case "Customers":
                newScene = new Scene(new Customer(stage, employeeName, dashboard).getRoot(), 900, 600);
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
                    executorService.shutdown(); // Shutdown the executor service
                    Platform.exit(); // Terminate the JavaFX Application Thread
                    System.exit(0); // Exit the JVM
                }
                break;
        }
    }
    
    private void setupPhoneNumberListener() {
        customerPhoneTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/finding.png")));
            if (checkTask != null) {
                executorService.shutdownNow();
                executorService = Executors.newSingleThreadScheduledExecutor();
            }

            checkTask = () -> {
                boolean exists = checkPhoneNumberInDatabase(newValue);
                Platform.runLater(() -> {
                    if (exists) {
                        searchIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/done.png")));
                        String customerNameText = getCustomerNameFromDatabase(newValue);
                        if (customerNameText != null) {
                            customerName.setText(customerNameText);
                        } else {
                            customerName.setText("-");
                        }

                        String phoneNumberText = getPhoneNumberFromDatabase(newValue);
                        if (phoneNumberText != null) {
                            phoneNumber.setText(phoneNumberText);
                        } else {
                            phoneNumber.setText("-");
                        }
                        generateNewOrderID();
                        phoneVerified = true;
                    } else {
                        searchIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/no.png")));
                        customerName.setText("-");
                        phoneNumber.setText("-");
                        orderID.setText("-");
                        phoneVerified = false;
                    }
                });
            };

            executorService.schedule(checkTask, 1, TimeUnit.SECONDS);
        });
    }

    private boolean checkPhoneNumberInDatabase(String phoneNumber) {
        boolean exists = false;
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM customer WHERE customerPhone = ?")) {
            preparedStatement.setString(1, phoneNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            exists = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    private String getCustomerNameFromDatabase(String phoneNumber) {
        String customerName = null;
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT customerName FROM customer WHERE customerPhone = ?")) {
            preparedStatement.setString(1, phoneNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                customerName = resultSet.getString("customerName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerName;
    }
    
    private String getPhoneNumberFromDatabase(String phoneNumber) {
        String customerPhoneNumber = null;
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT customerPhone FROM customer WHERE customerPhone = ?")) {
            preparedStatement.setString(1, phoneNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                customerPhoneNumber = resultSet.getString("customerPhone");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerPhoneNumber;
    }
    
    private void generateNewOrderID() {
        orderID.setText("-");
        try {
            String enteredPhoneNumber = customerPhoneTextField.getText();
            if (enteredPhoneNumber != null && !enteredPhoneNumber.isEmpty() && checkPhoneNumberInDatabase(enteredPhoneNumber)) {
                try (Connection connection = Database.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement("SELECT MAX(orderID) FROM `order`");
                     ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int maxOrderID = resultSet.getInt(1);
                        orderID.setText(String.valueOf(maxOrderID + 1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handlePaidButtonClick() {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            String insertOrderQuery = "INSERT INTO `order` (employeeID, customerID, date, loyaltyDiscount, total, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertOrderQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, getEmployeeID(employeeName));
                preparedStatement.setInt(2, getCustomerID(phoneNumber.getText()));
                preparedStatement.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.setBoolean(4, calculateLoyaltyDiscount() > 0);
                preparedStatement.setInt(5, Integer.parseInt(totalValueLabel.getText().replace("Rp", "")));

                preparedStatement.executeUpdate();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedOrderID = generatedKeys.getInt(1);

                    String insertOrderDetailQuery = "INSERT INTO orderdetail (orderID, serviceID, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement detailPreparedStatement = connection.prepareStatement(insertOrderDetailQuery)) {
                        for (Map.Entry<Integer, HBox> entry : orderItemsMap.entrySet()) {
                            int serviceID = entry.getKey();
                            HBox orderItemBox = entry.getValue();
                            Label itemQuantityLabel = (Label) orderItemBox.lookup("#itemQuantityLabel");

                            detailPreparedStatement.setInt(1, generatedOrderID);
                            detailPreparedStatement.setInt(2, serviceID);
                            detailPreparedStatement.setInt(3, Integer.parseInt(itemQuantityLabel.getText()));

                            detailPreparedStatement.addBatch();
                        }
                        detailPreparedStatement.executeBatch();
                    }
                    Platform.runLater(() -> {
                        dashboard.loadOrders();
                        if (onNewOrderPaid != null) {
                            onNewOrderPaid.run();
                        }
                    });
                }
            }
            connection.commit();
            Platform.runLater(() -> {
                showAlert("Order Recorded", "Order successfully paid and recorded.");
                resetOrderForm();
            });
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Error", "Failed to record the order."));
        }
    }
    
    private void handleDownloadReceiptButtonClick() {
        Canvas canvas = new Canvas(600, 900);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Image logo = new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/logo.png"));
        int width = (int) logo.getWidth();
        int height = (int) logo.getHeight();
        WritableImage bwLogo = new WritableImage(width, height);

        PixelReader pixelReader = logo.getPixelReader();
        PixelWriter pixelWriter = bwLogo.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                double grayValue = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
                Color grayColor = new Color(grayValue, grayValue, grayValue, color.getOpacity());
                pixelWriter.setColor(x, y, grayColor);
            }
        }
        
        gc.drawImage(bwLogo, 150, 20, 300, 300);

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("LAUNDRY MAMA RECEIPT", canvas.getWidth() / 2, 340);

        gc.setLineWidth(2);
        gc.strokeLine(50, 360, canvas.getWidth() - 50, 360);

        gc.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        drawWrappedText(gc, "Terima kasih " + customerName.getText() + " telah mempercayakan pakaian-pakaianmu kepada Laundry Mama! <3", canvas.getWidth() / 2, 380, canvas.getWidth() - 100);
        
        gc.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Kasir: " + employeeName, 50, 460);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(dateTimeLabel.getText(), canvas.getWidth() - 50, 460);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        gc.fillText("Detail Pesanan:", 50, 550);

        gc.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
        double yPosition = 580;
        for (HBox orderItemBox : orderItemsMap.values()) {
            Label itemQuantityLabel = (Label) orderItemBox.lookup("#itemQuantityLabel");
            Label itemNameLabel = (Label) ((VBox) orderItemBox.getChildren().get(1)).getChildren().get(0);
            Label itemPriceLabel = (Label) orderItemBox.lookup("#itemPriceLabel");

            gc.fillText(itemNameLabel.getText(), 50, yPosition);
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(itemQuantityLabel.getText() + " * " + itemPriceLabel.getText(), canvas.getWidth() - 50, yPosition);
            yPosition += 20;
            gc.setTextAlign(TextAlignment.LEFT);
        }

        gc.setLineWidth(1);
        gc.strokeLine(50, yPosition + 30, canvas.getWidth() - 50, yPosition + 30);
        yPosition += 70;

        gc.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
        gc.fillText("Subtotal", 50, yPosition);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(subtotalLabel.getText(), canvas.getWidth() - 50, yPosition);
        yPosition += 20;

        gc.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Loyalty Discount", 50, yPosition);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(loyaltyDiscountLabel.getText(), canvas.getWidth() - 50, yPosition);
        yPosition += 30;

        gc.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Total", 50, yPosition);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(totalValueLabel.getText(), canvas.getWidth() - 50, yPosition);

        gc.setLineWidth(1);
        gc.strokeLine(50, yPosition + 30, canvas.getWidth() - 50, yPosition + 30);
        yPosition += 60;

        gc.setFont(Font.font("Verdana", FontWeight.NORMAL, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        drawWrappedText(gc, "Mohon untuk segera melakukan pembayaran agar pencucian dapat diproses secepatnya! Terima kasih.", canvas.getWidth() / 2, yPosition, canvas.getWidth() - 100);

        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formattedDateTime = now.format(formatter);
        String filePath = "D:\\Download\\receipt" + formattedDateTime + ".png";

        saveImage(writableImage, filePath);
    }

    private void drawWrappedText(GraphicsContext gc, String text, double x, double y, double maxWidth) {
        Text tempText = new Text();
        tempText.setFont(gc.getFont());
        double lineHeight = tempText.getBoundsInLocal().getHeight();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = (line.length() == 0) ? word : line + " " + word;
            tempText.setText(testLine);
            double lineWidth = tempText.getBoundsInLocal().getWidth();
            if (lineWidth > maxWidth) {
                gc.fillText(line.toString(), x, y);
                y += lineHeight;
                line = new StringBuilder(word);
            } else {
                line.append((line.length() == 0) ? word : " " + word);
            }
        }
        if (line.length() > 0) {
            gc.fillText(line.toString(), x, y);
        }
    }

    private void saveImage(WritableImage image, String filename) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            writePngHeader(fos, width, height);
            writeImageData(fos, buffer, width, height);
            writePngEnd(fos);
            showAlert("Receipt Downloaded", "Receipt image successfully generated.");
        } catch (IOException e) {
            showAlert("Error", "Failed to generate receipt image.");
            e.printStackTrace();
        }
    }

    private void writePngHeader(FileOutputStream fos, int width, int height) throws IOException {
        fos.write(new byte[]{(byte) 137, 'P', 'N', 'G', (byte) 13, (byte) 10, (byte) 26, (byte) 10});

        fos.write(intToBytes(13));
        fos.write(new byte[]{'I', 'H', 'D', 'R'});

        ByteArrayOutputStream ihdrData = new ByteArrayOutputStream();
        ihdrData.write(intToBytes(width));
        ihdrData.write(intToBytes(height));
        ihdrData.write(new byte[]{8, 6, 0, 0, 0});

        byte[] ihdrBytes = ihdrData.toByteArray();
        fos.write(ihdrBytes);

        fos.write(intToBytes(crc(new byte[]{'I', 'H', 'D', 'R'}, ihdrBytes)));
    }

    private void writeImageData(FileOutputStream fos, byte[] buffer, int width, int height) throws IOException {
        ByteBuffer compressed = ByteBuffer.allocate(buffer.length + height);
        for (int y = 0; y < height; y++) {
            compressed.put((byte) 0);
            compressed.put(buffer, y * width * 4, width * 4);
        }

        byte[] data = new byte[compressed.position()];
        compressed.flip();
        compressed.get(data);

        byte[] compressedData = compress(data);
        fos.write(intToBytes(compressedData.length));
        fos.write(new byte[]{'I', 'D', 'A', 'T'});
        fos.write(compressedData);
        fos.write(intToBytes(crc(new byte[]{'I', 'D', 'A', 'T'}, compressedData)));
    }

    private void writePngEnd(FileOutputStream fos) throws IOException {
        fos.write(new byte[]{0, 0, 0, 0, 'I', 'E', 'N', 'D', 0, 0, 0, 0});
    }

    private byte[] intToBytes(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    private int crc(byte[] type, byte[] data) {
        int crc = 0xFFFFFFFF;
        for (byte b : type) {
            crc = updateCrc(crc, b);
        }
        for (byte b : data) {
            crc = updateCrc(crc, b);
        }
        return ~crc;
    }

    private int updateCrc(int crc, byte b) {
        crc ^= b;
        for (int k = 0; k < 8; k++) {
            if ((crc & 1) == 1) crc = (crc >>> 1) ^ 0xEDB88320;
            else crc = crc >>> 1;
        }
        return crc;
    }

    private byte[] compress(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    private int getEmployeeID(String employeeName) throws SQLException {
        int employeeID = -1;
        String query = "SELECT employeeID FROM employee WHERE employeeName = ?";
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, employeeName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                employeeID = resultSet.getInt("employeeID");
            }
        }
        return employeeID;
    }

    private int getCustomerID(String customerPhone) throws SQLException {
        int customerID = -1;
        String query = "SELECT customerID FROM customer WHERE customerPhone = ?";
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, customerPhone);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                customerID = resultSet.getInt("customerID");
            }
        }
        return customerID;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        if ("Order Recorded".equals(title)) {
            ImageView checkIcon = new ImageView(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/check.png")));
            checkIcon.setFitHeight(48);
            checkIcon.setFitWidth(48);
            alert.setGraphic(checkIcon);
        }
        else if ("Receipt Downloaded".equals(title)) {
            ImageView checkIcon = new ImageView(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/download.png")));
            checkIcon.setFitHeight(48);
            checkIcon.setFitWidth(48);
            alert.setGraphic(checkIcon);
        }

        alert.showAndWait();
    }
    
    private void resetOrderForm() {
        customerPhoneTextField.clear();
        customerName.setText("-");
        phoneNumber.setText("-");
        orderID.setText("-");

        orderItemsBox.getChildren().clear();
        orderItemsBox.setVisible(false);
        orderItemsMap.clear();

        subtotalLabel.setText("Rp0");
        loyaltyDiscountLabel.setText("Rp0");
        totalValueLabel.setText("Rp0");

        phoneVerified = false;

        searchIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/finding.png")));
        
        updateButtonStates();
    }
    
    public Pane getRoot() {
        return root;
    }
}
    