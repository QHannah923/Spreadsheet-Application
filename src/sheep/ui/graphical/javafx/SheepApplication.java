package sheep.ui.graphical.javafx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import sheep.core.SheetUpdate;
import sheep.core.SheetView;
import sheep.core.UpdateResponse;
import sheep.sheets.CellLocation;
import sheep.ui.Prompt;
import sheep.ui.UI;
import sheep.ui.graphical.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.paint.Color.*;

/**
 * The SheeP JavaFX application.
 * @stage0
 */
public class SheepApplication extends Application {
    //sheetView
    SheetView view;
    //updater
    SheetUpdate updater;
    //menu features
    Map<String,Map<String,UI.Feature>> features;
    //tableview
    TableView table;
    //stage of window
    Stage stage;


    /**
     * Construct a new SheeP Application with a sheet preloaded.
     * The application has a menu bar with each feature available.
     *
     * @param view A view of the primary sheet.
     * @param updater An updater for the primary sheet.
     * @param features A mapping of all the menu bar features.
     */
    public SheepApplication(SheetView view, SheetUpdate updater, Map<String, Map<String, UI.Feature>> features) {
        this.view = view;
        this.updater = updater;
        this.features = features;
    }

    /**
     * Start the SheeP Application.
     * Creates a new window to display and modify the sheet.
     * The scene has a menu bar with all the features.
     *
     * @param stage the primary stage.
     * @throws Exception if the application fails to run.
     */
    @Override
    public void start(Stage stage) throws Exception {
        // This is the main entry point for your javafx code.
        this.stage = stage;
        innitialization();
    }

    /**
     * Initializes the JavaFX application by setting up the main UI elements.
     *
     * [1] based on idea from "Example 13-1 Adding a Table"
     */
    protected void innitialization() {
        // This is the main entry point for your JavaFX code.
        Scene scene = new Scene(new Group());
        stage.setTitle(Configuration.TITLE);

        // --- Formula Bar
        TextField formulaTextField = new TextField();
        formulaTextField.setEditable(false);
        BackgroundFill bf = new BackgroundFill(LIGHTGREY, CornerRadii.EMPTY, Insets.EMPTY);
        formulaTextField.setBackground(new Background(bf));

        // --- Table
        table = new TableView(generateDataInMap());

        // Update the formula bar when a cell is selected
        updateFormulaBar(table, formulaTextField);
        // --- Table: columns
        List<TableColumn> columns = generateColumns(table);
        table.setEditable(true);
        table.getColumns().addAll(columns);

        final VBox vbox = new VBox();
        // --- Menu Bar
        MenuBar menuBar = generateMenuBar();

        // Add elements to VBox
        vbox.getChildren().addAll(menuBar, formulaTextField, table);
        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Create a new window, with a new sheet attached.
     *
     * @param view a view of the new sheet.
     * @param updater an updater for the new sheet.
     */
    public void createWindow(SheetView view, SheetUpdate updater) {
        this.view = view;
        this.updater = updater;
        innitialization();
    }

    /**
     * Generates a list of maps containing the data from the view.
     * Each map represents a row in the view, with the keys being the column names and the values being the cell contents.
     * <p>
     * [1] based on idea from "Example 13-12 Adding Map Data to the Table"
     *
     * @return An observable list of maps containing the data from the view.
     */
    private ObservableList<Map> generateDataInMap() {
        ObservableList<Map> allData = FXCollections.observableArrayList();
        for (int row = 0; row < view.getRows(); row++) {
            Map<String, String> dataRow = new HashMap<>();
            String rowNumber = String.valueOf(row);
            dataRow.put(" ", rowNumber);
            for (int col = 0; col < view.getColumns(); col++) {
                String colName = new CellLocation(0, col).toString().charAt(0) + "";
                String cellValue = view.valueAt(row, col).getContent();
                dataRow.put(colName, cellValue);
            }
            allData.add(dataRow);
        }
        return allData;
    }

    /**
     * Updates the formula bar based on the selected cell in the table.
     * If a formula exists for the selected cell, it will be displayed in the formula bar; otherwise, the cell value will be displayed.
     *
     * @param table The table view containing the data.
     * @param formulaTextField The text field to display the formula or cell value.
     */
    private void updateFormulaBar(TableView table, TextField formulaTextField) {
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().getSelectedCells().addListener((ListChangeListener<TablePosition>) change -> {
            if (change != null && !table.getSelectionModel().getSelectedCells().isEmpty()) {
                int col, row;
                col = ((TablePosition<?, ?>) table.getSelectionModel().getSelectedCells().getFirst()).getColumn() - 1; // minus index column
                row = ((TablePosition<?, ?>) table.getSelectionModel().getSelectedCells().getFirst()).getRow();

                String formula = view.formulaAt(row, col).getContent();
                String value = view.valueAt(row, col).getContent();
                formulaTextField.setText(formula.isEmpty() ? value : formula);
            }
        });
    }

    /**
     * Generates a list of table columns for the given table view.
     * Each column is represented by a TableColumn object, with the header text being the column name and the cell value factory being a MapValueFactory.
     * The cell factory is set to a {@link  CustomizedTextFieldTableCell} with a StringConverter that converts between strings and objects.
     *
     * <p>
     * [1] based on idea from "Example 13-12 Adding Map Data to the Table"
     *
     * @param table The table view to generate columns for.
     * @return A list of table columns for the given table view.
     */
    private List<TableColumn> generateColumns(TableView table) {
        Callback<TableColumn<Map, String>, TableCell<Map, String>> cellFactoryForMap = (TableColumn<Map, String> p) ->
                new CustomizedTextFieldTableCell(new StringConverter() {
                    @Override
                    public String toString(Object t) {
                        return t.toString();
                    }

                    @Override
                    public Object fromString(String string) {
                        return string;
                    }
                });

        // Columns
        List<TableColumn> columns = new ArrayList<>();
        TableColumn tableIndexColumn = new TableColumn<>(" ");
        tableIndexColumn.setCellValueFactory(new MapValueFactory<>(" "));
        tableIndexColumn.setCellFactory(cellFactoryForMap);
        tableIndexColumn.setResizable(false);
        tableIndexColumn.setPrefWidth(Configuration.HEADER_COLUMN_WIDTH);
        tableIndexColumn.setStyle("-fx-background-color:#f0f0f0;-fx-text-fill:grey;-fx-alignment:center;");
        columns.add(tableIndexColumn);
        for (int col = 0; col < view.getColumns(); col++) {
            String colName = new CellLocation(0, col).toString().charAt(0) + "";
            TableColumn tableColumn = new TableColumn(colName);
            tableColumn.setCellValueFactory(new MapValueFactory<>(colName));
            tableColumn.setCellFactory(cellFactoryForMap);
            tableColumn.setPrefWidth(Configuration.COLUMN_WIDTH);
            tableColumn.setResizable(false);
            columns.add(tableColumn);
        } // Index Column
        return columns;
    }

    /**
     * Generates a menu bar for the application.
     * The menu bar contains menus and menu items based on the features map.
     * Each menu item has an action associated with it, which is performed when the menu item is clicked.
     * Menu Prompt: {@link JFXUI.MessagePrompt}
     *
     * @return A menu bar containing the generated menus and menu items.
     */
    private MenuBar generateMenuBar() {
        MenuBar menuBar = new MenuBar();
        Prompt prompt = new JFXUI.MessagePrompt();
        ((JFXUI.MessagePrompt) prompt).setStage(stage);
        for (String menuName : features.keySet()) {
            Menu m = new Menu(menuName);
            for (String menuItem : features.get(menuName).keySet()) {
                MenuItem item = new MenuItem(features.get(menuName).get(menuItem).name());

                item.setOnAction(actionEvent -> {
                    System.out.println(menuItem + " clicked");
                    features.get(menuName).get(menuItem).action().perform(0, 0, prompt, view, updater);
                });
                m.getItems().add(item);
            }
            menuBar.getMenus().add(m);
        }
        return menuBar;
    }

    /**
     * Customized TextFieldTableCell class that extends TextFieldTableCell.
     * This class is used to customize the behavior of the table cell when editing.
     *
     * [1] based on idea from "Example 13-11 Alternative Solution Of Cell Editing"
     *
     * @param <S> The type of the item in the table view.
     * @param <T> The type of the value in the table cell.
     */
    protected class CustomizedTextFieldTableCell<S,T> extends TextFieldTableCell{

        /**
         * Constructor for CustomizedTextFieldTableCell.
         *
         * @param var1 The StringConverter used for converting between the item and the text field's text.
         */
        public CustomizedTextFieldTableCell(StringConverter<T> var1){
            super(var1);
        }

        /**
         * Updates the item in the table cell.
         *
         * @param var1 The new item to be displayed in the table cell.
         * @param var2 Whether the update should be empty.
         */
        @Override
        public void updateItem(Object var1, boolean var2) {
            super.updateItem(var1, var2);
        }

        /**
         * Commits the edit of the table cell.
         *
         * @param var1 The new value to be committed.
         */
        @Override
        public void commitEdit(Object var1) {
            System.out.println("commit:"+var1);
            int col,row;
            col = ((TablePosition<?, ?>) table.getSelectionModel().getSelectedCells().getFirst()).getColumn()-1;//minus index column
            row = ((TablePosition<?, ?>) table.getSelectionModel().getSelectedCells().getFirst()).getRow();
            UpdateResponse response = updater.update(row,col,var1.toString());

            if(var1 instanceof Integer){
                super.commitEdit(var1);
            }else {
                super.commitEdit(view.valueAt(row,col).getContent() !=null ? view.valueAt(row,col).getContent():"");
            }
            table.setItems(generateDataInMap());
        }


        /**
         * Starts the edit of the table cell.
         */
        @Override
        public void startEdit() {
            super.startEdit();

        }

    }


}

