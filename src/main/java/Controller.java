import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Scanner;

class Controller {
    final GridPane gridMainStage = new GridPane();

    final HBox box = new HBox();

        final Button left = new Button();
        final Button right = new Button();
        final Button reload = new Button();
        final Button home = new Button();

        final TextField textField = new TextField();

        final Button search = new Button();
        final Button tab = new Button();
        final Button minus = new Button();
        final Button plus = new Button();

    final TabPane tabPane = new TabPane();

    final ProgressBar progressBar = new ProgressBar();
    final ProgressIndicator progressIndicator = new ProgressIndicator();

    final MenuBar menuBar = new MenuBar();
    final Menu menuBrowser = new Menu("Browser");
        final MenuItem about = new MenuItem("About Browser");
    final MenuItem preferences  = new MenuItem("Preferences");
        final SeparatorMenuItem separator1 = new SeparatorMenuItem();
        final MenuItem close = new MenuItem("Quit");
    final Menu menuFile = new Menu("File");
        final MenuItem openNewTab = new MenuItem("New Tab");
        final MenuItem closeCurrentTab = new MenuItem("Close Tab");
        final SeparatorMenuItem separator2 = new SeparatorMenuItem();
        final MenuItem openFile = new MenuItem("Open File");
        final MenuItem download = new MenuItem("Download");
    final Menu menuHistory = new Menu("History");
        final MenuItem show = new MenuItem("Show");
        final MenuItem clear = new MenuItem("Clear");
    final Menu menuSource = new Menu("Source");
        final MenuItem gitHub = new MenuItem("View code in GitHub");

     final ContextMenu contextMenuTabButton = new ContextMenu();
        final MenuItem closeAllTabs = new MenuItem("Close all tabs");

    final GridPane gridPrefStage = new GridPane();

        final Label labelHomePage = new Label("Home page:");
        final TextField text = new TextField();

        final Label labelSearchEngine = new Label("Search engine:");
        final private ObservableList<String> engines = FXCollections.observableArrayList("Google", "DuckDuckGo", "Yandex");
        final ComboBox<String> comboBoxSearchEngines = new ComboBox<>(engines);

        final Label labelMemorizingHistory = new Label("Memorizing history:");
        final private ObservableList<String> items = FXCollections.observableArrayList("Yes", "No");
        final ComboBox<String> comboBoxMemorizingHistory = new ComboBox<>(items);

        final Button apply = new Button("Restart to apply new preferences");

    private Image appIcon = null;

    private String homePage = "https://www.google.com/";
    private String searchEngine = "Google";
    private boolean memorizeHistory = true;

    private boolean canDownload = true; // in this version the maximum number of simultaneous downloads is 1 (artificial limitation)

    final private File prefFile = new File("preferences.txt");
    final private File historyFile = new File("history.txt");
    private FileWriter historyWriter = null;

    void setAppIcon(Image icon) { appIcon = icon; }

    Image getAppIcon() { return appIcon; }

    void onStartApp() {
        preferences();
        openNewTab("", true);
        if (memorizeHistory) {
            openHistoryFile(true);
        } else {
            show.setDisable(true);
            clear.setDisable(true);
        }
    }

    boolean onCloseApp() {
        boolean close = true;
        int numberOfTabs = tabPane.getTabs().size();

        if (numberOfTabs > 1)
            close = showDialog(Alert.AlertType.CONFIRMATION, "Close tabs?",
                    "You are about to close " + numberOfTabs + " tabs. Are you sure you want to continue?");

        if (close)
            if (memorizeHistory) closeHistoryFile();

        return close;
    }

    void closeAppIfThereIsNoTabs() {
        Stage stage = (Stage)(left.getScene().getWindow());

        if (tabPane.getTabs().size() == 0)
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void restartApp() {
        // it works only with jar file (no in debug mode); incorrectly on Windows
        try {
            Runtime.getRuntime().exec("java -jar " + this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (IOException | URISyntaxException e) {
            showDialog(Alert.AlertType.ERROR, "Error",
                    "Restart failed. Please restart the app manually.");
        }

        System.exit(0);
    }

    private void preferences() {
        boolean needRewrite = false;

        String tmpHomePage = "";
        String tmpSearchEngine = "";
        String tmpMemorizeHistory = "";

        if (!prefFile.exists())
            needRewrite = true;
        else {
            try {
                Scanner prefs = new Scanner(prefFile);

                tmpHomePage = prefs.nextLine();
                if (prefs.hasNext()) {
                    tmpSearchEngine = prefs.nextLine();
                    if (prefs.hasNext())
                        tmpMemorizeHistory = prefs.nextLine();
                    else
                        needRewrite = true;
                } else
                    needRewrite = true;

                if (!needRewrite) {
                    if (!tmpSearchEngine.equals("Google") && !tmpSearchEngine.equals("DuckDuckGo") &&
                            !tmpSearchEngine.equals("Yandex"))
                        needRewrite = true;
                    else
                        if (!tmpMemorizeHistory.equals("true") && !tmpMemorizeHistory.equals("false"))
                            needRewrite = true;
                }
            } catch (IOException e) {
                showDialog(Alert.AlertType.ERROR, "Error", "File work failed.");
            }
        }

        if (needRewrite) {
            rewritePreferencesFile(homePage, searchEngine, memorizeHistory);
        } else {
            homePage = tmpHomePage;
            searchEngine = tmpSearchEngine;
            memorizeHistory = tmpMemorizeHistory.equals("true");
        }
    }

    private void rewritePreferencesFile(String homePage, String searchEngine, boolean memorizeHistory) {
        try {
            FileWriter prefWriter = new FileWriter(prefFile, false);

            prefWriter.write(homePage + "\n");
            prefWriter.write(searchEngine + "\n");
            prefWriter.write(memorizeHistory + "\n");
            prefWriter.close();
        } catch (IOException e) {
            showDialog(Alert.AlertType.ERROR, "Error", "File work failed.");
        }
    }

    void updatePreferences() {
        rewritePreferencesFile(text.getText(), comboBoxSearchEngines.getValue(),
                comboBoxMemorizingHistory.getValue().equals("Yes"));

        restartApp();
    }

    void download() {
        if (!canDownload) return;

        String url = textField.getText();

        if (isValidURL(url)) {
            String fullPath = showSaveFileDialog();

            if (!fullPath.equals("")) {
                Task<Void> downloadTask = new Downloader(url, fullPath, getAppIcon());
                progressIndicator.setVisible(true);
                progressIndicator.progressProperty().bind(downloadTask.progressProperty());

                downloadTask.stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        progressIndicator.setVisible(false);
                        canDownload = true;
                    }
                });

                Thread newThread = new Thread(downloadTask);
                newThread.setDaemon(true);
                newThread.start();
                canDownload = false;
            }
        } else
            showDialog(Alert.AlertType.ERROR, "Downloader", "Current URL is not valid.");
    }

    private String showSaveFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null)
            return file.getAbsolutePath();
        else
            return "";
    }

    void openHistoryFile(boolean append) {
        if (!append)
            if (!showDialog(Alert.AlertType.CONFIRMATION, "Clear history?", "Are you sure?"))
                return;

        try {
            historyWriter = new FileWriter(historyFile, append);
        } catch (IOException e) {
            showDialog(Alert.AlertType.ERROR, "Error", "File open failed.");
        }
    }

    private void closeHistoryFile() {
        try {
            historyWriter.close();
        } catch (IOException e) {
            showDialog(Alert.AlertType.ERROR, "Error", "File close failed.");
        }
    }

    void showHistory() {
        closeHistoryFile();

        try {
            openNewTab(historyFile.toURI().toURL().toExternalForm(), false);
        } catch (MalformedURLException e) {
            showDialog(Alert.AlertType.ERROR, "Error", "File open failed.");
        }

        openHistoryFile(true);
    }

    void updateHistory(String url) {
        if (memorizeHistory)
            try {
                historyWriter.write(url + "\n");
            } catch (IOException e) {
                showDialog(Alert.AlertType.ERROR, "Error", "File write failed.");
            }
    }

    String getUserAgent() {
        WebView webView = new WebView();
        return webView.getEngine().getUserAgent();
    }

    private WebView getCurrentWebView() {
        return (WebView) tabPane.getSelectionModel().getSelectedItem().getContent();
    }

    void goBack() {
        Platform.runLater(() -> getCurrentWebView().getEngine().executeScript("history.back()"));
    }

    void goForward() {
        Platform.runLater(() -> getCurrentWebView().getEngine().executeScript("history.forward()"));
    }

    void reloadPage() {
        WebView webView = getCurrentWebView();
        webView.getEngine().reload();

        String url = webView.getEngine().getLocation();
        if (url == null)
            textField.clear();
        else
            textField.setText(url);
    }

    void loadHomePage() { loadURL(homePage); }

    void loadURL(String url) {
        if (!url.equals("")) {
            if (isValidURL(url))
                getCurrentWebView().getEngine().load(url);
            else {
                if (searchEngine.equals("Google"))
                    url = "https://google.com/search?q=" + url;
                else
                    if (searchEngine.equals("DuckDuckGo"))
                        url = "https://duckduckgo.com/?q=" + url;
                    else
                        url = "https://yandex.ru/search/?text=" + url;

                getCurrentWebView().getEngine().load(url);
            }
        }
    }

    // internet = true => search in the Internet / false => open from disk
    void openNewTab(String url, boolean internet) {
        // generation of button click (creation of tab)
        Event.fireEvent(tab, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                true, true, true, true, true, true, null));

        if (internet) {
            if (!url.equals("")) loadURL(url);
        } else
            loadFileFromDisk(url);
    }

    private void loadFileFromDisk(String path) {
        getCurrentWebView().getEngine().load(path);
    }

    private boolean isValidURL(String url) {
        return url.matches("^(ht|f)tp(s?)://[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(/?)([a-zA-Z0-9\\-.?,:'/\\\\+=&amp;%$#_]*)?$");
    }

    void closeCurrentTab() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        currentTab.getOnClosed().handle(null);
        tabPane.getTabs().remove(currentTab);
        closeAppIfThereIsNoTabs();
    }

    void closeAllTabs() {
        int numberOfTabs = tabPane.getTabs().size();
        for (int i = 0; i < numberOfTabs; i++) {
            Tab currentTab = tabPane.getTabs().get(0);
            currentTab.getOnClosed().handle(null);
            tabPane.getTabs().remove(currentTab);
        }

        openNewTab("", true);
    }

    void zoomOut() {
        WebView webView = getCurrentWebView();
        webView.setZoom(webView.getZoom() - 0.2);
    }

    void zoomIn() {
        WebView webView = getCurrentWebView();
        webView.setZoom(webView.getZoom() + 0.2);
    }

    void stopLoading() {
        getCurrentWebView().getEngine().executeScript("window.stop();");
        progressBar.setVisible(false);
    }

    void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("File", "*.html", "*.txt", ".js", ".css", "*.png", "*.jpg", "*.gif", "*.svg", "*.mp3"));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file == null) return;

        try {
            openNewTab(file.toURI().toURL().toExternalForm(), false);
        } catch (MalformedURLException e) {
            showDialog(Alert.AlertType.ERROR, "Error", "File open failed.");
        }
    }

    void setPreferencesToPrefStage() {
        text.setText(homePage);
        comboBoxSearchEngines.setValue(searchEngine);
        if (memorizeHistory)
            comboBoxMemorizingHistory.setValue("Yes");
        else
            comboBoxMemorizingHistory.setValue("No");
    }

    // Show INFORMATION/ERROR/CONFORMATION dialog
    boolean showDialog(Alert.AlertType dialogType, String title, String text) {
        Alert alert = new Alert(dialogType);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(appIcon);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        if (dialogType == Alert.AlertType.INFORMATION ||
                dialogType == Alert.AlertType.ERROR) {
            alert.showAndWait();
            return true;
        } else
            return (alert.showAndWait().get() == ButtonType.OK);
    }
}