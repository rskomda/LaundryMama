package laundrymama.laundrymama;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Login login = new Login(stage);
        Scene scene = new Scene(login.getRoot(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/laundrymama/laundrymama/styles.css").toExternalForm());
        
        Image applicationIcon = new Image(getClass().getResourceAsStream("/laundrymama/laundrymama/logo.png"));
        stage.getIcons().add(applicationIcon);
        
        stage.setTitle("Laundry Mama");
        
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }
}