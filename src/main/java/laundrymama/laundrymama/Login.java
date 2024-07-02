package laundrymama.laundrymama;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Login {
    private StackPane root;
    private Stage stage;
    
    public Login(Stage stage){
        this.stage = stage;
        Image backgroundImage = new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/background.png"));
        ImageView backgroundImageView = new ImageView(backgroundImage);
        
        Rectangle backgroundRectangle = new Rectangle(415, 515);
        backgroundRectangle.setFill(Color.web("#EAF8FF"));
        backgroundRectangle.setArcWidth(25);
        backgroundRectangle.setArcHeight(25);
        
        Image logoImage = new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/logo.png"));
        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(142);
        logoImageView.setFitHeight(142);
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        backgroundRectangle.setEffect(dropShadow);
        
        Button loginButton = new Button("Log in");        
        loginButton.setPrefWidth(182);
        loginButton.setPrefHeight(34);
        loginButton.getStyleClass().add("custom-button");

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(182);
        registerButton.setPrefHeight(34);
        registerButton.getStyleClass().add("inactive-button");
        
        registerButton.setOnMouseEntered(e -> registerButton.setCursor(Cursor.HAND));
        registerButton.setOnMouseExited(e -> registerButton.setCursor(Cursor.DEFAULT));
        
        registerButton.setOnAction(e -> {
            Register register = new Register(stage);
            Scene registerScene = new Scene(register.getRoot(), 900, 600);
            registerScene.getStylesheets().add(getClass().getResource("/laundrymama/laundrymama/styles.css").toExternalForm());
            stage.setScene(registerScene);
        });

        HBox button = new HBox();
        button.setAlignment(Pos.CENTER);
        button.getChildren().addAll(loginButton, registerButton);
        
        Rectangle bodyRectangle = new Rectangle(364, 270);
        bodyRectangle.setFill(Color.WHITE);
        
        Label employeeIDLabel = new Label("Employee ID");
        TextField employeeIDInput = new TextField();
        
        VBox employeeID = new VBox(10);
        employeeID.getChildren().addAll(employeeIDLabel, employeeIDInput);
        
        Label passwordLabel = new Label("Password");
        PasswordField passwordInput = new PasswordField();
        
        VBox password = new VBox(10);
        password.getChildren().addAll(passwordLabel, passwordInput);
        
        Button submitButton = new Button("Submit");
        submitButton.setPrefWidth(120);
        submitButton.setPrefHeight(34);
        submitButton.getStyleClass().add("custom-button");
        
        submitButton.setOnMouseEntered(e -> submitButton.setCursor(Cursor.HAND));
        submitButton.setOnMouseExited(e -> submitButton.setCursor(Cursor.DEFAULT));
        
        submitButton.setOnAction(e -> handleLogin(employeeIDInput.getText(), passwordInput.getText()));
        
        employeeIDInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordInput.requestFocus();
            }
        });
        
        passwordInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin(employeeIDInput.getText(), passwordInput.getText());
            }
        });
        
        VBox itemToFill = new VBox(20);
        itemToFill.setAlignment(Pos.CENTER);
        itemToFill.setPadding(new Insets(40));
        itemToFill.getChildren().addAll(employeeID, password, submitButton);
        
        StackPane itemWithBodyRectangle = new StackPane();
        itemWithBodyRectangle.getChildren().addAll(bodyRectangle, itemToFill);
        
        VBox allPane = new VBox();
        allPane.setAlignment(Pos.CENTER);
        allPane.getChildren().addAll(logoImageView, button, itemWithBodyRectangle);
        allPane.setMaxSize(380, 465);
        allPane.setMinSize(380, 465);
        
        StackPane logoTabPaneStack = new StackPane();
        logoTabPaneStack.getChildren().addAll(backgroundRectangle, allPane);

        StackPane.setAlignment(logoTabPaneStack, Pos.CENTER);
        
        root = new StackPane(backgroundImageView, logoTabPaneStack);
        stage.setResizable(false);
    }
    
    private void handleLogin(String employeeID, String password) {
        if (employeeID.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please enter both Employee ID and Password.");
            alert.showAndWait();
            return;
        }
        
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT * FROM employee WHERE employeeID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, employeeID);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                String queryPassword = "SELECT * FROM employee WHERE employeeID = ? AND password = ?";
                statement = connection.prepareStatement(queryPassword);
                statement.setString(1, employeeID);
                statement.setString(2, password);
                resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    String employeeName = resultSet.getString("employeeName");
                    Dashboard dashboard = new Dashboard(stage, employeeName);

                    dashboard.setOnNewOrderPaid(() -> {
                        Platform.runLater(() -> {
                            dashboard.loadOrders();
                        });
                    });

                    dashboard.loadOrders();

                    Scene dashboardScene = new Scene(dashboard.getRoot(), 900, 600);
                    dashboardScene.getStylesheets().add(getClass().getResource("/laundrymama/laundrymama/styles.css").toExternalForm());
                    stage.setScene(dashboardScene);
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid password.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No Employee ID found.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public StackPane getRoot() {
        return root;
    }
}
