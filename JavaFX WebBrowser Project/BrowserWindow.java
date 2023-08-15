import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import java.util.ArrayList;

class BrowserWindow extends Stage {
    private WebBrowser owner;
    private WebEngine webEngine;
    private Menu windowMenu;

    BrowserWindow(WebBrowser browser, String initialURL) {
        owner = browser;

        WebView webview = new WebView();
        webEngine = webview.getEngine();

        Label status = new Label("Status:Idle");

        status.setMaxWidth(Double.POSITIVE_INFINITY);
        Label location = new Label("Location: (empty)");

        location.setMaxWidth(Double.POSITIVE_INFINITY);
        TextField urlInput = new TextField();

        urlInput.setMaxWidth(600);
        Button loadButton = new Button("Load");

        loadButton.setOnAction(e -> doLoad(urlInput.getText()));
        loadButton.defaultButtonProperty().bind(urlInput.focusedProperty());
        Button cancelButton = new Button("Cancel");

        cancelButton.setDisable(true);

        HBox loader = new HBox(8, new Label("URL:"), urlInput, loadButton, cancelButton);
        HBox.setHgrow(urlInput, Priority.ALWAYS);

        VBox bottom = new VBox(10, location, status, loader);
        bottom.setStyle("-fx-padding: 10px; -fx-border-color:black; -fx-border-width:3px 0 0 0");

        BorderPane root = new BorderPane(webview);
        root.setBottom(bottom);
        root.setTop(makeMenuBar());

        setScene(new Scene(root));

        webEngine.locationProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null || newVal.equals("about:blank"))
                location.setText("Location: (empty)");
            else
                location.setText("Location: " + newVal);
        });

        webEngine.titleProperty().addListener((o, oldVal, newVal) -> {
            if (newVal == null)
                setTitle("Untitled " + owner.getNextUntitledCount());
            else
                setTitle(newVal);
        });

        webEngine.getLoadWorker().stateProperty().addListener((o, oldVal, newVal) -> {
            status.setText("Status: " + newVal);
            switch (newVal) {
                case READY:
                    status.setText("Status:  Idle.");
                    break;
                case SCHEDULED:
                case RUNNING:
                    status.setText("Status:  Loading a web page.");
                    break;
                case SUCCEEDED:
                    status.setText("Status:  Web page has been successfully loaded.");
                    break;
                case FAILED:
                    status.setText("Status:  Loading of the web page has failed.");
                    break;
                case CANCELLED:
                    status.setText("Status:  Loading of the web page has been cancelled.");
                    break;
            }
            cancelButton.setDisable(newVal != Worker.State.RUNNING);
        });

        cancelButton.setOnAction(e -> {
            if (webEngine.getLoadWorker().getState() == Worker.State.RUNNING)
                webEngine.getLoadWorker().cancel();

        });

        webEngine.setOnAlert(evt -> SimpleDialogs.message(evt.getData(), "Alert from web page"));
        webEngine.setPromptHandler(promptData -> SimpleDialogs.prompt(promptData.getMessage(),
                "Query from web page", promptData.getDefaultValue()));
        webEngine.setConfirmHandler(str -> SimpleDialogs.confirm(str, "Confirmation Needed").equals("yes"));

        if (initialURL != null) {
            doLoad(initialURL);
        }

    }

    private void doLoad(String url) {
        if (url == null || url.trim().length() == 0)
            return;
        url = url.trim();
        if (!url.matches("^[a-zA-Z]+:.*")) {
            url = "http://" + url;
        }
        System.out.println("Loading URL " + url);
        webEngine.load(url);
    }

    private MenuBar makeMenuBar() {
        MenuItem newWin = new MenuItem("New Window");
        newWin.setOnAction(e -> owner.newBrowserWindow(null));
        MenuItem close = new MenuItem("Close Window");
        close.setOnAction(e -> hide());
        MenuItem open = new MenuItem("Open URL in New Window...");
        open.setOnAction(e -> {
            String url = SimpleDialogs.prompt(
                    "Enter the URL of the page that you want to open.", "Get URL");
            if (url != null && url.trim().length() > 0)
                owner.newBrowserWindow(url);
        });
        windowMenu = new Menu("Window");
        windowMenu.getItems().addAll(newWin, close, open, new SeparatorMenuItem());
        windowMenu.setOnShowing(e -> populateWindowMenu());
        MenuBar menubar = new MenuBar(windowMenu);
        return menubar;
    }

    private void populateWindowMenu() {
        ArrayList<BrowserWindow> windows = owner.getOpenWindowList();
        while (windowMenu.getItems().size() > 4) {
            windowMenu.getItems().remove(windowMenu.getItems().size() - 1);
        }
        if (windows.size() > 1) {
            MenuItem item = new MenuItem("Close All and Exit");
            item.setOnAction(e -> Platform.exit());
            windowMenu.getItems().add(item);
            windowMenu.getItems().add(new SeparatorMenuItem());
        }
        for (BrowserWindow window : windows) {
            String title = window.getTitle();
            if (title.length() > 60) {
                title = title.substring(0, 57) + ". . .";
            }
            MenuItem item = new MenuItem(title);
            final BrowserWindow win = window;
            item.setOnAction(e -> win.requestFocus());
            windowMenu.getItems().add(item);
            if (window == this) {
                item.setDisable(true);
            }
        }
    }
}