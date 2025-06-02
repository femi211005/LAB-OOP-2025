import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;

public class TP9 extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Photo Register App");
        Scene registerScene = RegisterScene.createRegisterScene(stage, user -> {
            Scene home = HomeScene.createHomeScene(stage, user);
            stage.setScene(home);
        });
        stage.setScene(registerScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Model: User
class User {
    private String nickName;
    private String fullName;
    private String profileImagePath;

    public User(String nickName, String fullName, String profileImagePath) {
        this.nickName = nickName;
        this.fullName = fullName;
        this.profileImagePath = profileImagePath;
    }

    public String getNickName() {
        return nickName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }
}

// Model: Post
class Post {
    private String caption;
    private String postImagePath;

    public Post(String caption, String postImagePath) {
        this.caption = caption;
        this.postImagePath = postImagePath;
    }

    public String getCaption() {
        return caption;
    }

    public String getPostImagePath() {
        return postImagePath;
    }
}

// Scene: Register
class RegisterScene {

    public static Scene createRegisterScene(Stage stage, Callback callback) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField nickNameField = new TextField();
        TextField fullNameField = new TextField();
        Button uploadBtn = new Button("Upload Profile Image");
        Button registerBtn = new Button("Register");
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        final String[] profileImagePath = new String[1];

        uploadBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                profileImagePath[0] = file.toURI().toString();
                Image image = new Image(profileImagePath[0]);
                imageView.setImage(image);
                imageView.setPreserveRatio(false);
                Circle clip = new Circle(50, 50, 50);
                imageView.setClip(clip);
            }
        });

        registerBtn.setOnAction(e -> {
            if (nickNameField.getText().isEmpty() || fullNameField.getText().isEmpty() || profileImagePath[0] == null) {
                new Alert(Alert.AlertType.ERROR, "Isi semua field").show();
            } else {
                User user = new User(nickNameField.getText(), fullNameField.getText(), profileImagePath[0]);
                callback.onRegister(user);
            }
        });

        layout.getChildren().addAll(
                new Label("Nickname:"), nickNameField,
                new Label("Fullname:"), fullNameField,
                uploadBtn, imageView, registerBtn
        );

        return new Scene(layout, 300, 400);
    }

    public interface Callback {
        void onRegister(User user);
    }
}

// Scene: Home
class HomeScene {

    private static final ArrayList<Post> posts = new ArrayList<>();

    public static Scene createHomeScene(Stage stage, User user) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label nickLabel = new Label(user.getNickName());
        nickLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox nickBox = new HBox(nickLabel);
        nickBox.setAlignment(Pos.CENTER);

        GridPane postGrid = new GridPane();
        postGrid.setHgap(10);
        postGrid.setVgap(10);
        postGrid.setPadding(new Insets(10));

        // Profile Image with circular clip
        Image profileImage = new Image(user.getProfileImagePath());
        ImageView profileImg = new ImageView(profileImage);
        profileImg.setFitWidth(80);
        profileImg.setFitHeight(80);
        profileImg.setPreserveRatio(false);
        
        Circle clip = new Circle(40, 40, 40);
        profileImg.setClip(clip);

        Label fullLabel = new Label(user.getFullName());
        fullLabel.setStyle("-fx-font-size: 14px;");

        Button addPostBtn = new Button("Add Post");
        addPostBtn.setOnAction(e -> openAddPostWindow(stage, postGrid));

        HBox infoBox = new HBox(20, profileImg, fullLabel, addPostBtn);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(postGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        layout.getChildren().addAll(nickBox, infoBox, scrollPane);

        Scene scene = new Scene(layout, 500, 500);
        updatePostsDisplay(postGrid);

        return scene;
    }

    private static void openAddPostWindow(Stage parent, GridPane postGrid) {
        Stage dialog = new Stage();
        dialog.initOwner(parent);
        dialog.setTitle("Add New Post");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TextField captionField = new TextField();
        Button uploadBtn = new Button("Upload Image");
        Button saveBtn = new Button("Save");

        final String[] postImagePath = {null};

        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File selectedFile = fc.showOpenDialog(dialog);
            if (selectedFile != null) {
                postImagePath[0] = selectedFile.toURI().toString();
            }
        });

        saveBtn.setOnAction(e -> {
            if (captionField.getText().trim().isEmpty() || postImagePath[0] == null) {
                new Alert(Alert.AlertType.ERROR, "Caption atau Foto belum diisi!").show();
            } else {
                Post newPost = new Post(captionField.getText(), postImagePath[0]);
                posts.add(newPost);
                updatePostsDisplay(postGrid);
                dialog.close();
            }
        });

        layout.getChildren().addAll(new Label("Caption:"), captionField, uploadBtn, saveBtn);
        dialog.setScene(new Scene(layout, 300, 200));
        dialog.show();
    }

    private static void updatePostsDisplay(GridPane postGrid) {
        postGrid.getChildren().clear();

        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            VBox postPane = new VBox(5);
            postPane.setAlignment(Pos.CENTER);
            
            ImageView imgView = new ImageView(new Image(p.getPostImagePath(), 150, 150, false, true));
            Label captionLabel = new Label(p.getCaption());
            captionLabel.setVisible(false);
            captionLabel.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: white; " +
                               "-fx-padding: 5px; -fx-background-radius: 5px; -fx-font-size: 12px;");

            // Create drop shadow effect
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(10.0);
            dropShadow.setOffsetX(3.0);
            dropShadow.setOffsetY(3.0);
            dropShadow.setColor(Color.color(0.4, 0.5, 0.5));

            // Create scale animations
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), postPane);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), postPane);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);

            // Enhanced hover effects
            postPane.setOnMouseEntered((MouseEvent e) -> {
                // Show caption
                captionLabel.setVisible(true);
                
                // Add drop shadow
                postPane.setEffect(dropShadow);
                
                // Scale up animation
                scaleUp.play();
                
                // Change cursor to hand
                postPane.setStyle("-fx-cursor: hand;");
            });

            postPane.setOnMouseExited((MouseEvent e) -> {
                // Hide caption
                captionLabel.setVisible(false);
                
                // Remove drop shadow
                postPane.setEffect(null);
                
                // Scale down animation
                scaleDown.play();
                
                // Reset cursor
                postPane.setStyle("-fx-cursor: default;");
            });

            // Add click effect (optional - makes it feel more interactive)
            postPane.setOnMousePressed((MouseEvent e) -> {
                postPane.setScaleX(0.95);
                postPane.setScaleY(0.95);
            });

            postPane.setOnMouseReleased((MouseEvent e) -> {
                if (postPane.isHover()) {
                    postPane.setScaleX(1.05);
                    postPane.setScaleY(1.05);
                } else {
                    postPane.setScaleX(1.0);
                    postPane.setScaleY(1.0);
                }
            });

            postPane.getChildren().addAll(imgView, captionLabel);

            int col = i % 3;
            int row = i / 3;
            postGrid.add(postPane, col, row);
        }
    }
}