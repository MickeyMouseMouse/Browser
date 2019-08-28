import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Downloader extends Task<Void> {
    final private String url; // download file from  url
    final private String fullPath; // full path to downloadable file
    private Image appIcon; // app icon for success/error dialog

    Downloader(String url, String fullPath, Image appIcon) {
        this.url = url;
        this.fullPath = fullPath;
        this.appIcon = appIcon;
    }

    @Override
    protected Void call() throws Exception {
        URLConnection connection = new URL(url).openConnection();

        try (InputStream is = connection.getInputStream();
             OutputStream os = Files.newOutputStream(Paths.get(fullPath))) {
            long read = 0L;
            byte[] buf = new byte[256];
            int n;
            while ((n = is.read(buf)) > 0) {
                os.write(buf, 0, n);
                read += n;
                updateProgress(read, connection.getContentLengthLong());
            }
        }

        return null;
    }

    @Override
    protected void succeeded() {
        showDialog(Alert.AlertType.INFORMATION, "Download success: " + fullPath);
    }

    @Override
    protected void failed() {
        showDialog(Alert.AlertType.ERROR, "Download failed.");
    }

    // Show INFORMATION/ERROR dialog
    private void showDialog(Alert.AlertType dialogType, String text) {
        Alert alert = new Alert(dialogType);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(appIcon);
        alert.setTitle("Downloader");
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }
}