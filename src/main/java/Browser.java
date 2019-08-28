import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.Modality;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;

public class Browser extends Application {
    public static void main(String[] args) { Application.launch(args); }

    final private Controller cont = new Controller();
    final private Stage prefStage = new Stage();

    @Override
    public void start(Stage mainStage) {
        cont.setAppIcon(new Image("icon.png"));

        mainStage.setMinWidth(950);
        mainStage.setMinHeight(145);
        mainStage.getIcons().add(cont.getAppIcon());
        mainStage.setTitle("Browser");
        mainStage.centerOnScreen();

        // --customization GridPane on main stage--
        cont.gridMainStage.setLayoutX(0);
        cont.gridMainStage.setLayoutY(0);

        // only one column
        ColumnConstraints column = new ColumnConstraints(940,940, Double.MAX_VALUE);
        column.setHgrow(Priority.ALWAYS); // resize column with window resize (column width in 940...infinity)
        cont.gridMainStage.getColumnConstraints().add(column);

        // first row (menu bar)
        cont.gridMainStage.getRowConstraints().add(new RowConstraints(25));

        // second row (box with buttons and text field)
        cont.gridMainStage.getRowConstraints().add(new RowConstraints(50));

        // third row (tab pane)
        RowConstraints row = new RowConstraints(0,600, Double.MAX_VALUE);
        row.setVgrow(Priority.ALWAYS); // resize row with window resize (row height in 600...infinity)
        cont.gridMainStage.getRowConstraints().add(row);

        // fourth row (progress bar and indicator bar)
        cont.gridMainStage.getRowConstraints().add(new RowConstraints(35));

        GridPane.setRowIndex(cont.menuBar,0);
        GridPane.setRowIndex(cont.box,1);
        GridPane.setRowIndex(cont.tabPane,2);
        GridPane.setRowIndex(cont.progressBar,3);
        GridPane.setRowIndex(cont.progressIndicator,3);
        cont.gridMainStage.getChildren().addAll(cont.menuBar, cont.box, cont.tabPane, cont.progressBar, cont.progressIndicator);

        // --customization HBox (top line with buttons and text field)--
        cont.box.getChildren().addAll(cont.left, cont.right,
                cont.reload, cont.home, cont.textField, cont.search,
                cont.tab, cont.minus, cont.plus);
        cont.box.setSpacing(10);
        cont.box.setPadding(new Insets(10, 10, 10, 10));

        // --customization of top line elements--
        cont.left.setPrefSize(32, 32);
        cont.left.setTooltip(new Tooltip("Back"));
        cont.left.graphicProperty()
                .setValue(new ImageView(new Image("left.png")));
        cont.left.setStyle("-fx-padding:0 0 0 0;");

        cont.right.setPrefSize(32, 32);
        cont.right.setTooltip(new Tooltip("Forward"));
        cont.right.graphicProperty()
                .setValue(new ImageView(new Image("right.png")));
        cont.right.setStyle("-fx-padding:0 0 0 0;");

        cont.reload.setPrefSize(32, 32);
        cont.reload.setTooltip(new Tooltip("Reload"));
        cont.reload.graphicProperty()
                .setValue(new ImageView(new Image("reload.png")));
        cont.reload.setStyle("-fx-padding:0 0 0 0;");

        cont.home.setPrefSize(32, 32);
        cont.home.setTooltip(new Tooltip("Home"));
        cont.home.graphicProperty()
                .setValue(new ImageView(new Image("home.png")));
        cont.home.setStyle("-fx-padding:0 0 0 0;");

        cont.textField.setPrefSize(600, 32);
        cont.textField.setFont(Font.font(null, 15));
        cont.textField.setPromptText("Search or enter address");

        cont.search.setPrefSize(32, 32);
        cont.search.setTooltip(new Tooltip("Search"));
        cont.search.graphicProperty()
                .setValue(new ImageView(new Image("search.png")));
        cont.search.setStyle("-fx-padding:0 0 0 0;");

        cont.tab.setPrefSize(32, 32);
        cont.tab.setTooltip(new Tooltip("New tab"));
        cont.tab.graphicProperty()
                .setValue(new ImageView(new Image("tab.png")));
        cont.tab.setStyle("-fx-padding:0 0 0 0;");

        cont.minus.setPrefSize(32, 32);
        cont.minus.setTooltip(new Tooltip("Zoom out"));
        cont.minus.graphicProperty()
                .setValue(new ImageView(new Image("minus.png")));
        cont.minus.setStyle("-fx-padding:0 0 0 0;");

        cont.plus.setPrefSize(32, 32);
        cont.plus.setTooltip(new Tooltip("Zoom in"));
        cont.plus.graphicProperty()
                .setValue(new ImageView(new Image("plus.png")));
        cont.plus.setStyle("-fx-padding:0 0 0 0;");

        // --customization of bars in bottom of stage--
        cont.progressBar.setVisible(false);
        cont.progressBar.setPrefWidth(150);
        cont.progressBar.setPrefHeight(25);

        cont.progressIndicator.setVisible(false);

        // set focus on textField when application starts
        Platform.runLater(cont.tabPane::requestFocus);

        // --customization of context menus--
        cont.menuBar.getMenus().addAll(cont.menuBrowser, cont.menuFile, cont.menuHistory, cont.menuSource);
        cont.menuBrowser.getItems().addAll(cont.about, cont.preferences, cont.separator1, cont.close);
        cont.menuFile.getItems().addAll(cont.openNewTab, cont.closeCurrentTab, cont.separator2, cont.openFile, cont.download);
        cont.menuHistory.getItems().addAll(cont.show, cont.clear);
        cont.menuSource.getItems().add(cont.gitHub);

        cont.contextMenuTabButton.getItems().add(cont.closeAllTabs);

        mainStage.widthProperty().addListener((arg1, arg2, arg3) ->
            cont.textField.setPrefWidth(mainStage.getWidth() - 8 * cont.left.getWidth() - 10 * cont.box.getSpacing())
        );

        cont.tab.setOnContextMenuRequested((e) ->
            cont.contextMenuTabButton.show(cont.tab, e.getScreenX(), e.getScreenY())
        );

        cont.left.setOnMouseClicked((e) -> cont.goBack());
        cont.right.setOnMouseClicked((e) -> cont.goForward());
        cont.reload.setOnMouseClicked((e) -> cont.reloadPage());
        cont.home.setOnMouseClicked((e) -> cont.loadHomePage());

        cont.textField.setOnKeyPressed((e) -> {
            if (e.getCode() == KeyCode.ENTER) cont.loadURL(cont.textField.getText());
        });

        cont.search.setOnMouseClicked((e) -> cont.loadURL(cont.textField.getText()));

        // create new tab
        cont.tab.setOnMouseClicked((e1) -> {
            if (e1.getButton() == MouseButton.PRIMARY) {
                WebView view = new WebView();
                WebEngine engine = view.getEngine();

                Tab newTab = new Tab();
                cont.tabPane.getTabs().add(newTab);
                cont.tabPane.getSelectionModel().select(newTab);
                newTab.setContent(view);

                newTab.setText("Empty Page");
                cont.textField.clear();

                Worker<Void> worker = engine.getLoadWorker();
                worker.progressProperty().addListener((observable, oldValue, newValue) -> {
                    if (newTab.isSelected()) {
                        cont.progressBar.setVisible(true);
                        cont.progressBar.progressProperty().bind(worker.progressProperty());
                        if (newValue.equals(1.0)) {
                            cont.progressBar.setVisible(false);

                            if (engine.getTitle() != null)
                                newTab.setText(engine.getTitle());

                            cont.updateHistory(engine.getLocation());
                        }
                    } else {
                        cont.progressBar.setVisible(false);
                    }
                });

                // on load new page in engine
                engine.locationProperty().addListener((observable, oldValue, newValue) -> {
                    newTab.setText(engine.getLocation());
                    newTab.setTooltip(new Tooltip(engine.getLocation()));
                    if (newTab.isSelected())
                        cont.textField.setText(engine.getLocation());
                });

                // on activating new tab (on leaving old tab)
                newTab.setOnSelectionChanged((e2) -> {
                    if (newTab.isSelected()) {
                        String url = engine.getLocation();
                        if (url == null)
                            cont.textField.clear();
                        else
                            cont.textField.setText(url);
                    }
                });

                newTab.setOnClosed((e3) -> {
                    WebView tmp = (WebView) newTab.getContent();
                    tmp.getEngine().loadContent("");
                    cont.progressBar.setVisible(false);

                    cont.closeAppIfThereIsNoTabs();
                });
            }
        });

        cont.minus.setOnMouseClicked((e) -> cont.zoomOut());
        cont.plus.setOnMouseClicked((e) -> cont.zoomIn());

        cont.progressBar.setOnMouseClicked((e) -> cont.stopLoading());

        // top line menu events
        cont.about.setOnAction((e) -> cont.showDialog(Alert.AlertType.INFORMATION,
                "About Browser",
                "Developer: Andrew Jeus; version from 28.08.2019. " + "UserAgent: " + cont.getUserAgent()));
        cont.preferences.setOnAction((e) -> {
            cont.setPreferencesToPrefStage();
            prefStage.show();
        });
        cont.preferences.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN));
        cont.close.setOnAction((e) -> mainStage.fireEvent(new WindowEvent(mainStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

        cont.openNewTab.setOnAction((e) -> cont.openNewTab("", true));
        cont.openNewTab.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN));
        cont.closeCurrentTab.setOnAction((e) -> cont.closeCurrentTab());
        cont.closeCurrentTab.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN));
        cont.openFile.setOnAction((e) -> cont.openFile());
        cont.openFile.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        cont.download.setOnAction((e) -> cont.download());
        cont.download.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));

        cont.show.setOnAction((e) -> cont.showHistory());
        cont.clear.setOnAction((e) -> cont.openHistoryFile(false));

        cont.gitHub.setOnAction((e) -> cont.openNewTab("https://github.com/MickeyMouseMouse/Browser", true));

        cont.closeAllTabs.setOnAction((e) -> cont.closeAllTabs());

        mainStage.setOnCloseRequest((e) -> {
            if (!cont.onCloseApp()) e.consume();
        });

        cont.onStartApp();

        final Scene mainScene = new Scene(cont.gridMainStage);
        mainScene.getStylesheets().add("tabs.css");
        mainStage.setScene(mainScene);
        mainStage.show();

        settingsPrefStage();
    }

    // --customization of preferences stage--
    private void settingsPrefStage() {
        prefStage.getIcons().add(cont.getAppIcon());
        prefStage.setTitle("Preferences");
        prefStage.centerOnScreen();
        prefStage.setResizable(false);
        prefStage.initModality(Modality.APPLICATION_MODAL);

        cont.gridPrefStage.setLayoutX(0);
        cont.gridPrefStage.setLayoutY(0);

        cont.labelHomePage.setFont(Font.font(null, 18));
        cont.gridPrefStage.add(cont.labelHomePage, 0, 0);
        GridPane.setMargin(cont.labelHomePage, new Insets(15));

        cont.text.setFont((Font.font(null, 18)));
        cont.text.setPrefWidth(280);
        cont.gridPrefStage.add(cont.text, 1, 0);
        GridPane.setMargin(cont.text, new Insets(15));

        cont.labelSearchEngine.setFont((Font.font(null, 18)));
        cont.gridPrefStage.add(cont.labelSearchEngine, 0, 1);
        GridPane.setMargin(cont.labelSearchEngine, new Insets(15));

        cont.comboBoxSearchEngines.setStyle("-fx-font: 18px \"Times New Roman\";");
        cont.comboBoxSearchEngines.setPrefWidth(170);
        cont.gridPrefStage.add(cont.comboBoxSearchEngines, 1, 1);
        GridPane.setMargin(cont.comboBoxSearchEngines, new Insets(15));

        cont.labelMemorizingHistory.setFont((Font.font(null, 18)));
        cont.gridPrefStage.add(cont.labelMemorizingHistory, 0, 2);
        GridPane.setMargin(cont.labelMemorizingHistory, new Insets(15));

        cont.comboBoxMemorizingHistory.setStyle("-fx-font: 18px \"Times New Roman\";");
        cont.comboBoxMemorizingHistory.setPrefWidth(170);
        cont.gridPrefStage.add(cont.comboBoxMemorizingHistory, 1, 2);
        GridPane.setMargin(cont.comboBoxMemorizingHistory, new Insets(15));

        cont.apply.setFont((Font.font(null, 18)));
        cont.gridPrefStage.add(cont.apply, 1, 3);
        GridPane.setMargin(cont.apply, new Insets(10));
        GridPane.setHalignment(cont.apply, HPos.RIGHT);

        cont.apply.setOnMouseClicked((e) -> cont.updatePreferences());

        final Scene prefScene = new Scene(cont.gridPrefStage);
        prefStage.setScene(prefScene);
    }
}