package laundrymama.laundrymama;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

public class Register {
    private StackPane root;
    
    public Register(Stage stage){
        
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
        loginButton.getStyleClass().add("inactive-button");
        
        loginButton.setOnMouseEntered(e -> loginButton.setCursor(Cursor.HAND));
        loginButton.setOnMouseExited(e -> loginButton.setCursor(Cursor.DEFAULT));
        
        loginButton.setOnAction(e -> {
            Login login = new Login(stage);
            Scene loginScene = new Scene(login.getRoot(), 900, 600);
            loginScene.getStylesheets().add(getClass().getResource("/laundrymama/laundrymama/styles.css").toExternalForm());
            stage.setScene(loginScene);
        });

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(182);
        registerButton.setPrefHeight(34);
        registerButton.getStyleClass().add("custom-button");

        HBox button = new HBox();
        button.setAlignment(Pos.CENTER);
        button.getChildren().addAll(loginButton, registerButton);
        
        Rectangle bodyRectangle = new Rectangle(364, 270);
        bodyRectangle.setFill(Color.WHITE);
        
        Label nameLabel = new Label("Name");
        TextField nameInput = new TextField();
        
        VBox name = new VBox(5);
        name.getChildren().addAll(nameLabel, nameInput);
        
        Label phoneNumberLabel = new Label("Phone Number");
        TextField phoneNumberInput = new TextField();
        
        VBox phoneNumber = new VBox(5);
        phoneNumber.getChildren().addAll(phoneNumberLabel, phoneNumberInput);
        
        Label passwordLabel = new Label("Password");
        PasswordField  passwordInput = new PasswordField ();
        
        VBox password = new VBox(5);
        password.getChildren().addAll(passwordLabel, passwordInput);
        
        Button submitButton = new Button("Submit");
        submitButton.setPrefWidth(120);
        submitButton.setPrefHeight(34);
        submitButton.getStyleClass().add("custom-button");
        
        submitButton.setOnMouseEntered(e -> submitButton.setCursor(Cursor.HAND));
        submitButton.setOnMouseExited(e -> submitButton.setCursor(Cursor.DEFAULT));
        submitButton.setOnAction(e -> handleRegistration(nameInput, phoneNumberInput, passwordInput));
        
        nameInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                phoneNumberInput.requestFocus();
            }
        });
        
        phoneNumberInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordInput.requestFocus();
            }
        });
        
        passwordInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleRegistration(nameInput, phoneNumberInput, passwordInput);
            }
        });
        
        VBox itemToFill = new VBox(15);
        itemToFill.setAlignment(Pos.CENTER);
        itemToFill.setPadding(new Insets(10, 40, 10, 40));
        itemToFill.getChildren().addAll(name, phoneNumber, password, submitButton);
        
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
    
    private void handleRegistration(TextField nameInput, TextField phoneNumberInput, PasswordField passwordInput) {
        String employeeName = nameInput.getText();
        String employeePhone = phoneNumberInput.getText();
        String pass = passwordInput.getText();

        if (!employeeName.isEmpty() && !employeePhone.isEmpty() && !pass.isEmpty()) {
            int employeeID = saveEmployeeData(employeeName, employeePhone, pass);
            if (employeeID != -1) {
                showEmployeeIDDialog(employeeID);
                nameInput.clear();
                phoneNumberInput.clear();
                passwordInput.clear();
            }
        } else {
            showValidationAlert();
        }
    }
    
    private int saveEmployeeData(String name, String phoneNumber, String password) {
        String query = "INSERT INTO employee (employeeName, employeePhone, password) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, phoneNumber);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
    
    private void showEmployeeIDDialog(int employeeID) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Welcome to the team!\nYour Employee ID: " + employeeID);
        
        Image customIcon = new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/logo.png"));
        ImageView customIconView = new ImageView(customIcon);
        customIconView.setFitWidth(48);
        customIconView.setFitHeight(48);
        alert.setGraphic(customIconView);
        
        alert.showAndWait();
    }
    
    private void showValidationAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Validation");
        alert.setHeaderText(null);
        alert.setContentText("Please fill out all fields before submitting.");

        alert.showAndWait();
    }
    
    public StackPane getRoot() {
        return root;
    }
}
