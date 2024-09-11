package sheep.ui.graphical.javafx;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sheep.core.SheetUpdate;
import sheep.core.SheetView;
import sheep.ui.Prompt;
import sheep.ui.UI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Optional;


/**
 * Graphical interface for the spreadsheet program, built in JavaFX.
 * <p>
 * Requires a {@link SheetView} and {@link SheetUpdate} to determine
 * what to render and how to update the sheet respectively.
 * @provided
 */
public class JFXUI extends UI {
    // The main JavaFX application.
    private SheepApplication application;

    /**
     * Construct a new JavaFX UI for the Sheep program,
     * and initialise it with a sheet.
     *
     * @param view    A view of the sheet to display.
     * @param updater An updater for the sheet to display.
     */
    public JFXUI(SheetView view, SheetUpdate updater) {
        super(view, updater);
//        render();
    }

    /**
     * Render the JavaFX UI by creating a {@link SheepApplication} and starting it.
     * @throws RuntimeException If the JavaFX application fails to start.
     */
    @Override
    public void render() throws RuntimeException {
        // This is not standard practice and should not be used in
        // native JavaFX applications, but in order to make JavaFX operate
        // similarly to Swing's invokeLater() behaviour this is necessary.
        // This is why updating the sheet after calling render() but before
        // returning from main() is still reflected in the UI.
        Platform.setImplicitExit(true);
        Platform.startup(() -> {
            application = new SheepApplication(this.view, this.updater, this.features);
            try {
                application.init();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                try {
                    application.start(new Stage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    /**
     * Create the application's window with a new sheet attached.
     *
     * @param view    A view of the new sheet.
     * @param updater An updater for the new sheet.
     * @throws Exception
     */
    @Override
    public void openWindow(SheetView view, SheetUpdate updater) throws Exception {
        System.out.println("opem window");
        application.createWindow(view, updater);
    }
    protected static class MessagePrompt implements Prompt {
        Stage stage;
        public void setStage(Stage stage) {
            this.stage = stage;
        }
        @Override
        public Optional<String> ask(String prompt) {
            Optional<String[]> answer = askMany(new String[]{prompt});
            return answer.map(strings -> strings[0]);
        }

        @Override
        public Optional<String[]> askMany(String[] prompts) {
           return Optional.ofNullable(null);
        }

        @Override
        public boolean askYesNo(String prompt) {

            return true;
        }

        @Override
        public void message(String prompt) {

        }

        @Override
        public String openFile() {
//            JFileChooser chooser = new JFileChooser();
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Please choose sheet File");
                fileChooser.setInitialDirectory(new File("./resources"));
                FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Sheep Files","*.sheep");
                fileChooser.getExtensionFilters().add(extensionFilter);
                File result = fileChooser.showOpenDialog(stage);
                return result.getAbsolutePath();

            } catch (Exception e){
                throw new RuntimeException("File chooser not implemented in swing");
            }

        }

        @Override
        public String saveFile() {
            try {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Sheep Files","*.sheep");
                fileChooser.getExtensionFilters().add(extensionFilter);
                fileChooser.setTitle("Save File");
                fileChooser.setInitialDirectory(new File("./resources"));
                fileChooser.setInitialFileName("123.sheep");
                return fileChooser.showSaveDialog(stage).getAbsolutePath();
            } catch (Exception e){
                  throw new RuntimeException("File chooser not implemented in swing");
            }
        }
    }
}
