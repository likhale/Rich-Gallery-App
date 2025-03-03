package com.example.galleryapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
    private static final String IMAGE_DIR = "src/main/resources/images";
    private static final List<String> IMAGE_PATHS = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        loadImagesFromFolder();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        ThumbnailGridView thumbnailGridView = new ThumbnailGridView(root);
        root.setCenter(thumbnailGridView);

        Scene scene = new Scene(root, 800, 450);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("LIKHALE's Rich Internet Gallery App!");
//        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void loadImagesFromFolder() {
        File folder = new File(IMAGE_DIR);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    IMAGE_PATHS.add("/images/" + file.getName());
                }
            }
        } else {
            System.err.println("Image folder not found: " + IMAGE_DIR);
        }
    }

    static class ThumbnailGridView extends ScrollPane {
        private final BorderPane root;

        public ThumbnailGridView(BorderPane root) {
            this.root = root;
            FlowPane thumbnailFlowPane = new FlowPane();
            thumbnailFlowPane.setHgap(20);
            thumbnailFlowPane.setVgap(20);
            thumbnailFlowPane.setPadding(new Insets(20));

            for (String imagePath : IMAGE_PATHS) {
                ImageView thumbnail = createThumbnail(imagePath);
                if (thumbnail != null) {
                    thumbnailFlowPane.getChildren().add(thumbnail);
                }
            }

            this.setContent(thumbnailFlowPane);
            this.setFitToWidth(true);
            this.setFitToHeight(true);
            this.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }

        private ImageView createThumbnail(String imagePath) {
            if (getClass().getResource(imagePath) == null) {
                System.err.println("Image not found: " + imagePath);
                return null;
            }

            Image image = new Image(getClass().getResourceAsStream(imagePath));
            ImageView thumbnail = new ImageView(image);
            thumbnail.setFitWidth(150);
            thumbnail.setFitHeight(150);
            thumbnail.getStyleClass().add("thumbnail");

            thumbnail.setOnMouseClicked(event -> {
                // Pass imagePath
                FullImageView fullImageView = new FullImageView(image, root, imagePath);
                root.setCenter(fullImageView);
            });

            return thumbnail;
        }
    }

    static class FullImageView extends BorderPane {
        private final List<String> imagePaths = IMAGE_PATHS;
        private int currentImageIndex;
        private ImageView fullImage;
        private ImageView prevImagePreview;
        private ImageView nextImagePreview;

        public FullImageView(Image image, BorderPane root, String imagePath) {
            // Use imagePath directly
            this.currentImageIndex = imagePaths.indexOf(imagePath);

            fullImage = new ImageView(image);
            fullImage.setFitWidth(600);
            fullImage.setFitHeight(400);
            fullImage.getStyleClass().add("full-image");

            // Previous and next image previews
            prevImagePreview = new ImageView();
            prevImagePreview.setFitWidth(100);
            prevImagePreview.setFitHeight(100);
            prevImagePreview.getStyleClass().add("preview-image");

            nextImagePreview = new ImageView();
            nextImagePreview.setFitWidth(100);
            nextImagePreview.setFitHeight(100);
            nextImagePreview.getStyleClass().add("preview-image");

            updatePreviews();

            // Previous button with image icon
            ImageView prevIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/prev.jpg")));
            prevIcon.setFitWidth(30);
            prevIcon.setFitHeight(30);
            Button prevButton = new Button("", prevIcon);
            prevButton.getStyleClass().add("nav-button");

            // Next button with image icon
            ImageView nextIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/next.png")));
            nextIcon.setFitWidth(30);
            nextIcon.setFitHeight(30);
            Button nextButton = new Button("", nextIcon);
            nextButton.getStyleClass().add("nav-button");

            // Close button with image icon
            ImageView closeIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/close.jpg")));
            closeIcon.setFitWidth(30);
            closeIcon.setFitHeight(30);
            Button closeButton = new Button("", closeIcon);
            closeButton.getStyleClass().add("nav-button");

            // Button actions
            prevButton.setOnAction(event -> showPreviousImage());
            nextButton.setOnAction(event -> showNextImage());
            closeButton.setOnAction(event -> {
                ThumbnailGridView thumbnailGridView = new ThumbnailGridView(root);
                root.setCenter(thumbnailGridView);
            });

            // Button container
            HBox buttonBox = new HBox(prevButton, nextButton);
            buttonBox.getStyleClass().add("button-box");
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setSpacing(20);
            buttonBox.setPadding(new Insets(20));

            // Close button container
            StackPane closeButtonContainer = new StackPane(closeButton);
            closeButtonContainer.setAlignment(Pos.TOP_RIGHT);
            closeButtonContainer.setPadding(new Insets(10));

            // Main image container
            HBox imageContainer = new HBox(prevImagePreview, fullImage, nextImagePreview);
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setSpacing(20);

            this.setCenter(imageContainer);
            this.setBottom(buttonBox);
            this.setTop(closeButtonContainer);
        }

        private void updatePreviews() {
            int prevIndex = (currentImageIndex - 1 + imagePaths.size()) % imagePaths.size();
            int nextIndex = (currentImageIndex + 1) % imagePaths.size();

            Image prevImage = new Image(getClass().getResourceAsStream(imagePaths.get(prevIndex)));
            Image nextImage = new Image(getClass().getResourceAsStream(imagePaths.get(nextIndex)));

            prevImagePreview.setImage(prevImage);
            nextImagePreview.setImage(nextImage);
        }

        private void showNextImage() {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), fullImage);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                currentImageIndex = (currentImageIndex + 1) % imagePaths.size();
                Image nextImage = new Image(getClass().getResourceAsStream(imagePaths.get(currentImageIndex)));
                fullImage.setImage(nextImage);
                updatePreviews();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), fullImage);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }

        private void showPreviousImage() {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), fullImage);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                currentImageIndex = (currentImageIndex - 1 + imagePaths.size()) % imagePaths.size();
                Image prevImage = new Image(getClass().getResourceAsStream(imagePaths.get(currentImageIndex)));
                fullImage.setImage(prevImage);
                updatePreviews();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), fullImage);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }
}