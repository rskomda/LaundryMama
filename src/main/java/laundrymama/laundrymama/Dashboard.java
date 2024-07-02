package laundrymama.laundrymama;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.animation.KeyFrame;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Dashboard {
    private Pane root;
    private Label dateTimeLabel;
    private String employeeName;
    private Stage stage;
    private Label infoAmount;
    private VBox pendingBox;
    private VBox processingBox;
    private VBox doneBox;
    private Dashboard dashboard;
    private Runnable onNewOrderPaid;

    public Dashboard(Stage stage, String employeeName) {
        this.stage = stage;
        this.employeeName = employeeName;
        root = new Pane();

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

        Label title = new Label("Dashboard");
        title.setLayoutX(230);
        title.setLayoutY(25);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System Bold", 20));

        Rectangle infoBox = new Rectangle(219, 58, Color.WHITE);
        infoBox.setLayoutX(230);
        infoBox.setLayoutY(67);
        infoBox.setArcWidth(10);
        infoBox.setArcHeight(10);

        Label infoLabel = new Label("Total Pendapatan Hari Ini");
        infoLabel.setLayoutX(273);
        infoLabel.setLayoutY(74);

        infoAmount = new Label("Rp000.000");
        infoAmount.setLayoutX(288);
        infoAmount.setLayoutY(90);
        infoAmount.setFont(Font.font(22));
        updateTotalPendapatan();

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

        HBox statusBox = new HBox(10);
        statusBox.setLayoutX(227);
        statusBox.setLayoutY(155);
        statusBox.setPrefSize(660, 432);

        Label pendingLabel = new Label("Pending");
        pendingLabel.setFont(Font.font("System", 16));
        pendingLabel.setPadding(new Insets(0, 0, 0, 10));

        Label processingLabel = new Label("Processing");
        processingLabel.setFont(Font.font("System", 16));
        processingLabel.setPadding(new Insets(0, 0, 0, 10));

        Label doneLabel = new Label("Done");
        doneLabel.setFont(Font.font("System", 16));
        doneLabel.setPadding(new Insets(0, 0, 0, 10));

        pendingBox = new VBox();
        processingBox = new VBox();
        doneBox = new VBox();

        ScrollPane pendingScrollPane = createScrollPane(pendingBox);
        ScrollPane processingScrollPane = createScrollPane(processingBox);
        ScrollPane doneScrollPane = createScrollPane(doneBox);

        VBox pendingContainer = new VBox(pendingLabel, pendingScrollPane);
        VBox processingContainer = new VBox(processingLabel, processingScrollPane);
        VBox doneContainer = new VBox(doneLabel, doneScrollPane);

        statusBox.getChildren().addAll(pendingContainer, processingContainer, doneContainer);

        root.getChildren().addAll(header, sideMenu, logo, title, infoBox, infoLabel, infoAmount, userInfoBox, sidebar, statusBox);

        loadOrders();
        
        stage.setResizable(false);
    }

    private StackPane createMenuItem(String iconPath, String text) {
        StackPane stackPane = new StackPane();

        Color backgroundColor = text.equals("Dashboard") ? Color.WHITE : Color.web("#eaf8ff");  // Conditional background color
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
    
    private void updateTotalPendapatan() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String currentDate = now.format(formatter);

        String query = "SELECT SUM(total) FROM `order` WHERE DATE(date) = ?";
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, currentDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int totalPendapatan = resultSet.getInt(1);
                infoAmount.setText("Rp" + totalPendapatan);
                infoAmount.setAlignment(Pos.CENTER);
            } else {
                infoAmount.setText("Rp0");
                infoAmount.setAlignment(Pos.CENTER);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            infoAmount.setText("Rp0");
        }
    }
    
    private ScrollPane createScrollPane(VBox box) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(box);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefSize(220, 432);
        return scrollPane;
    }

    private StackPane createCard(String customerName, String serviceDescription, int orderId, String status) {
        StackPane card = new StackPane();
        Rectangle background = new Rectangle(210, 59, Color.WHITE);
        background.setArcWidth(10);
        background.setArcHeight(10);

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setPrefSize(210, 59);
        vbox.setPadding(new Insets(0, 0, 0, 10));

        HBox hboxTop = new HBox();
        hboxTop.setSpacing(5);
        Label nameLabel = new Label(customerName);
        nameLabel.setPrefWidth(128);
        nameLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        HBox iconHbox = new HBox(3);
        iconHbox.setAlignment(Pos.CENTER);
        
        ImageView processIcon = new ImageView();
        processIcon.setFitHeight(17);
        processIcon.setFitWidth(17);
        
        ImageView doneIcon = new ImageView();
        doneIcon.setFitHeight(17);
        doneIcon.setFitWidth(17);
        
        ImageView pickupIcon = new ImageView();
        pickupIcon.setFitHeight(17);
        pickupIcon.setFitWidth(17);
        
        if ("Pending".equalsIgnoreCase(status)) {
            processIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/processgreylogo.png")));
            doneIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/outgreylogo.png")));
            pickupIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/pickupgreylogo.png")));
            processIcon.setOnMouseClicked(event -> moveToProcessing(card, orderId));
            processIcon.setOnMouseEntered(e -> processIcon.setCursor(Cursor.HAND));
            processIcon.setOnMouseExited(e -> processIcon.setCursor(Cursor.DEFAULT));
            doneIcon.setOnMouseClicked(null);
            doneIcon.setCursor(Cursor.DEFAULT);
            pickupIcon.setOnMouseClicked(null);
            pickupIcon.setCursor(Cursor.DEFAULT);
        } else if ("Processing".equalsIgnoreCase(status)) {
            processIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/processbluelogo.png")));
            doneIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/outgreylogo.png")));
            pickupIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/pickupgreylogo.png")));
            doneIcon.setOnMouseClicked(event -> moveToDone(card, orderId));
            doneIcon.setOnMouseEntered(e -> doneIcon.setCursor(Cursor.HAND));
            doneIcon.setOnMouseExited(e -> doneIcon.setCursor(Cursor.DEFAULT));
            processIcon.setOnMouseClicked(null);
            processIcon.setCursor(Cursor.DEFAULT);
            pickupIcon.setOnMouseClicked(null);
            pickupIcon.setCursor(Cursor.DEFAULT);
        } else if ("Done".equalsIgnoreCase(status)) {
            processIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/processbluelogo.png")));
            doneIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/outbluelogo.png")));
            pickupIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/pickupgreylogo.png")));
            pickupIcon.setOnMouseClicked(event -> moveToPickedUp(card, orderId));
            pickupIcon.setOnMouseEntered(e -> pickupIcon.setCursor(Cursor.HAND));
            pickupIcon.setOnMouseExited(e -> pickupIcon.setCursor(Cursor.DEFAULT));
            processIcon.setOnMouseClicked(null);
            processIcon.setCursor(Cursor.DEFAULT);
            doneIcon.setOnMouseClicked(null);
            doneIcon.setCursor(Cursor.DEFAULT);
        }
        
        iconHbox.getChildren().addAll(processIcon, doneIcon, pickupIcon);

        hboxTop.getChildren().addAll(nameLabel, iconHbox);

        Label serviceLabel = new Label(serviceDescription);
        serviceLabel.setPrefWidth(184);
        serviceLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        vbox.getChildren().addAll(hboxTop, serviceLabel);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setBlurType(BlurType.GAUSSIAN);
        dropShadow.setHeight(3.33);
        dropShadow.setOffsetX(1.0);
        dropShadow.setOffsetY(1.0);
        dropShadow.setRadius(0.75);
        dropShadow.setWidth(1.67);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.35));
        card.setEffect(dropShadow);

        card.getChildren().addAll(background, vbox);

        return card;
    }
    
    private void moveToProcessing(StackPane card, int orderId) {
        Platform.runLater(() -> {
            pendingBox.getChildren().remove(card);
            if (!processingBox.getChildren().contains(card)) {
                processingBox.getChildren().add(card);
            }
            updateOrderStatus(orderId, "Processing");

            VBox vbox = (VBox) card.getChildren().get(1);
            HBox hboxTop = (HBox) vbox.getChildren().get(0);
            HBox iconHbox = (HBox) hboxTop.getChildren().get(1);

            ImageView processIcon = (ImageView) iconHbox.getChildren().get(0);
            processIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/processbluelogo.png")));
            processIcon.setOnMouseClicked(null);
            processIcon.setOnMouseEntered(null);
            processIcon.setOnMouseExited(null);
            processIcon.setCursor(Cursor.DEFAULT);

            ImageView doneIcon = (ImageView) iconHbox.getChildren().get(1);
            doneIcon.setOnMouseClicked(event -> moveToDone(card, orderId));
            doneIcon.setOnMouseEntered(e -> doneIcon.setCursor(Cursor.HAND));
            doneIcon.setOnMouseExited(e -> doneIcon.setCursor(Cursor.DEFAULT));

            ImageView pickupIcon = (ImageView) iconHbox.getChildren().get(2);
            pickupIcon.setOnMouseClicked(null);
            pickupIcon.setOnMouseEntered(null);
            pickupIcon.setOnMouseExited(null);
            pickupIcon.setCursor(Cursor.DEFAULT);
        });
    }

    private void moveToDone(StackPane card, int orderId) {
        Platform.runLater(() -> {
            processingBox.getChildren().remove(card);
            doneBox.getChildren().add(card);
            updateOrderStatus(orderId, "Done");

            VBox vbox = (VBox) card.getChildren().get(1);
            HBox hboxTop = (HBox) vbox.getChildren().get(0);
            HBox iconHbox = (HBox) hboxTop.getChildren().get(1);
            
            ImageView doneIcon = (ImageView) iconHbox.getChildren().get(1);
            doneIcon.setImage(new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/outbluelogo.png")));
            doneIcon.setOnMouseClicked(null);
            doneIcon.setOnMouseEntered(null);
            doneIcon.setOnMouseExited(null);
            doneIcon.setCursor(Cursor.DEFAULT);

            ImageView pickupIcon = (ImageView) iconHbox.getChildren().get(2);
            pickupIcon.setOnMouseClicked(event -> moveToPickedUp(card, orderId));
            pickupIcon.setOnMouseEntered(e -> pickupIcon.setCursor(Cursor.HAND));
            pickupIcon.setOnMouseExited(e -> pickupIcon.setCursor(Cursor.DEFAULT));

            ImageView processIcon = (ImageView) iconHbox.getChildren().get(0);
            processIcon.setOnMouseClicked(null);
            processIcon.setOnMouseEntered(null);
            processIcon.setOnMouseExited(null);
            processIcon.setCursor(Cursor.DEFAULT);
        });
    }

    private void moveToPickedUp(StackPane card, int orderId) {
        Platform.runLater(() -> {
            doneBox.getChildren().remove(card);
            updateOrderStatus(orderId, "Picked Up");
        });
    }
    
    private void updateOrderStatus(int orderId, String status) {
        String query = "UPDATE `order` SET status = ? WHERE orderID = ?";
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, orderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void loadOrders() {
        loadOrdersByStatus("Pending", pendingBox);
        loadOrdersByStatus("processing", processingBox);
        loadOrdersByStatus("done", doneBox);
    }

    private void loadOrdersByStatus(String status, VBox box) {
        String query = "SELECT o.orderID, c.customerName, GROUP_CONCAT(s.serviceName SEPARATOR ', ') AS serviceDescription " +
                       "FROM `order` o " +
                       "JOIN orderdetail od ON o.orderID = od.orderID " +
                       "JOIN service s ON od.serviceID = s.serviceID " +
                       "JOIN customer c ON o.customerID = c.customerID " +
                       "WHERE o.status = ? " +
                       "GROUP BY o.orderID " +
                       "ORDER BY o.orderID ASC";
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                box.getChildren().clear();
                while (resultSet.next()) {
                    int orderId = resultSet.getInt("orderID");
                    String customerName = resultSet.getString("customerName");
                    String serviceDescription = resultSet.getString("serviceDescription");
                    StackPane card = createCard(customerName, serviceDescription, orderId, status);
                    box.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void handleMenuClick(String menuItem, String employeeName) {
        Scene newScene;

        switch (menuItem) {
            case "New Order":
                newScene = new Scene(new Order(stage, employeeName, this).getRoot(), 900, 600);
                newScene.getStylesheets().add(getClass().getResource("/laundrymama/laundrymama/styles.css").toExternalForm());
                stage.setScene(newScene);
                break;
            case "Customers":
                newScene = new Scene(new Customer(stage, employeeName, this).getRoot(), 900, 600);
                stage.setScene(newScene);
                break;
            case "History":
                newScene = new Scene(new History(stage, employeeName, this).getRoot(), 900, 600);
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
    
    public void setOnNewOrderPaid(Runnable onNewOrderPaid) {
        this.onNewOrderPaid = onNewOrderPaid;
    }

    public Pane getRoot() {
        return root;
    }
}
