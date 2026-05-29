package com.budget.app.pages;

import com.budget.app.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;

public class IncomePage {
    private final Stage stage;
    private final AppState state;
    private final Navigator nav;

    public IncomePage(Stage stage, AppState state, Navigator nav) {
        this.stage = stage; this.state = state; this.nav = nav;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UI.BG + ";");
        root.setLeft(UI.sidebar(state.currentUser, "income",
            nav::dashboard, nav::income, nav::expense, nav::budget, nav::summary, nav::logout));

        VBox content = new VBox(22);
        content.setPadding(new Insets(32));
        content.getChildren().add(UI.pageHeader("💵  Add Income", "Record your main and extra income sources"));

        // Stats
        long mc = state.getIncomes().stream().filter(i -> i.type.equals("Main Income")).count();
        long ec = state.getIncomes().stream().filter(i -> i.type.equals("Extra Income")).count();
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
            UI.statCard("Total Income",         UI.rupees(state.totalIncome()), "💵", UI.SUCCESS_L, "#2E7D32"),
            UI.statCard("Main Income Entries",  String.valueOf(mc),             "🏢", UI.PRIMARY_L, UI.PRIMARY_D),
            UI.statCard("Extra Income Entries", String.valueOf(ec),             "✨", UI.PURPLE_L,  UI.PURPLE)
        );

        // Form
        VBox form = UI.card("➕  Add New Income Entry");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Main Income", "Extra Income");
        typeBox.setValue("Main Income");
        UI.styleCombo(typeBox); typeBox.setMaxWidth(Double.MAX_VALUE);

        TextField srcF = new TextField(); srcF.setPromptText("e.g. Salary, Freelance, Bonus...");
        UI.styleInput(srcF); srcF.setMaxWidth(Double.MAX_VALUE);

        TextField amtF = new TextField(); amtF.setPromptText("Amount (numbers only)");
        UI.styleInput(amtF); amtF.setMaxWidth(Double.MAX_VALUE);
        amtF.textProperty().addListener((o,ov,nv) -> { if (!nv.matches("\\d*\\.?\\d*")) amtF.setText(ov); });

        DatePicker dp = new DatePicker(LocalDate.now());
        UI.styleInput(dp); dp.setMaxWidth(Double.MAX_VALUE);

        Button addBtn = UI.successBtn("💾   Save Income");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(addBtn.getStyle() + "-fx-font-size:14px;-fx-padding:12 0;");

        GridPane grid = new GridPane();
        grid.setHgap(18); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UI.pct(50), UI.pct(50));
        grid.add(UI.field("Income Type",          typeBox), 0, 0);
        grid.add(UI.field("Source / Description", srcF),   1, 0);
        grid.add(UI.field("Amount (₹)",           amtF),   0, 1);
        grid.add(UI.field("Date",                 dp),     1, 1);
        grid.add(addBtn, 0, 2, 2, 1);
        form.getChildren().add(grid);

        // Table
        VBox hist = UI.card("📋  Income History");
        TableView<AppState.Income> table = buildTable();
        table.setPrefHeight(230);
        hist.getChildren().add(table);

        addBtn.setOnAction(e -> {
            String type = typeBox.getValue();
            String src  = srcF.getText().trim();
            String amts = amtF.getText().trim();
            if (src.isEmpty())         { UI.err("Source cannot be empty!"); return; }
            if (amts.isEmpty())        { UI.err("Amount cannot be empty!"); return; }
            if (dp.getValue() == null) { UI.err("Please select a date!"); return; }
            double amt;
            try { amt = Double.parseDouble(amts); if (amt <= 0) { UI.err("Amount must be > 0"); return; } }
            catch (NumberFormatException ex) { UI.err("Numbers only in amount!"); return; }

            state.getIncomes().add(new AppState.Income(type, src, amt, dp.getValue().toString()));
            state.saveIncomes();   // ← persist
            UI.ok(UI.rupees(amt) + " income added! 🎉");
            srcF.clear(); amtF.clear(); dp.setValue(LocalDate.now());
            table.setItems(FXCollections.observableArrayList(state.getIncomes()));
        });

        content.getChildren().addAll(stats, form, hist);
        root.setCenter(UI.scroll(content));
        stage.setScene(new Scene(root, 940, 640));
    }

    private TableView<AppState.Income> buildTable() {
        TableView<AppState.Income> t = new TableView<>();
        UI.styleTable(t);
        TableColumn<AppState.Income,String> c1 = new TableColumn<>("Type");
        c1.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().type));
        TableColumn<AppState.Income,String> c2 = new TableColumn<>("Source");
        c2.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().source));
        TableColumn<AppState.Income,String> c3 = new TableColumn<>("Amount");
        c3.setCellValueFactory(r -> new SimpleStringProperty(UI.rupees(r.getValue().amount)));
        TableColumn<AppState.Income,String> c4 = new TableColumn<>("Date");
        c4.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().date));
        t.getColumns().addAll(c1, c2, c3, c4);
        t.setItems(FXCollections.observableArrayList(state.getIncomes()));
        t.setPlaceholder(new Label("No income entries yet."));
        return t;
    }
}
