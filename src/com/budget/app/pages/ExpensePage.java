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
import java.util.Map;
import java.util.stream.Collectors;

public class ExpensePage {
    private final Stage stage;
    private final AppState state;
    private final Navigator nav;

    public ExpensePage(Stage stage, AppState state, Navigator nav) {
        this.stage = stage; this.state = state; this.nav = nav;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UI.BG + ";");
        root.setLeft(UI.sidebar(state.currentUser, "expense",
            nav::dashboard, nav::income, nav::expense, nav::budget, nav::summary, nav::logout));

        VBox content = new VBox(22);
        content.setPadding(new Insets(32));
        content.getChildren().add(UI.pageHeader("💸  Add Expense", "Log your spending by category"));

        String topCat = state.getExpenses().stream()
            .collect(Collectors.groupingBy(e -> e.category, Collectors.summingDouble(e -> e.amount)))
            .entrySet().stream().max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("N/A");

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
            UI.statCard("Total Expenses", UI.rupees(state.totalExpenses()), "💸", UI.DANGER_L,  "#C62828"),
            UI.statCard("Total Entries",  String.valueOf(state.getExpenses().size()), "📝", UI.PRIMARY_L, UI.PRIMARY_D),
            UI.statCard("Top Category",   topCat,                           "🏆", UI.AMBER_L,  UI.AMBER)
        );

        // Form
        VBox form = UI.card("➕  Add New Expense Entry");

        ComboBox<String> catBox = new ComboBox<>();
        refreshCats(catBox);
        UI.styleCombo(catBox); catBox.setMaxWidth(Double.MAX_VALUE);

        TextField amtF = new TextField(); amtF.setPromptText("Amount (numbers only)");
        UI.styleInput(amtF); amtF.setMaxWidth(Double.MAX_VALUE);
        amtF.textProperty().addListener((o,ov,nv) -> { if (!nv.matches("\\d*\\.?\\d*")) amtF.setText(ov); });

        DatePicker dp = new DatePicker(LocalDate.now());
        UI.styleInput(dp); dp.setMaxWidth(Double.MAX_VALUE);

        TextField noteF = new TextField(); noteF.setPromptText("Note (optional)");
        UI.styleInput(noteF); noteF.setMaxWidth(Double.MAX_VALUE);

        // Custom category
        TextField newCatF = new TextField(); newCatF.setPromptText("New custom category...");
        UI.styleInput(newCatF); HBox.setHgrow(newCatF, Priority.ALWAYS);
        Button addCatBtn = UI.outlineBtn("➕ Add Category");
        HBox catRow = new HBox(12, newCatF, addCatBtn);
        catRow.setAlignment(Pos.CENTER_LEFT);

        addCatBtn.setOnAction(e -> {
            String c = newCatF.getText().trim();
            if (c.isEmpty()) { UI.err("Category name cannot be empty!"); return; }
            if (state.getCategories().contains(c)) { UI.err("Category already exists!"); return; }
            state.addCategory(c);
            refreshCats(catBox); catBox.setValue(c);
            newCatF.clear();
            UI.ok("Category \"" + c + "\" added! ✅");
        });

        Button addBtn = UI.dangerBtn("💾   Save Expense");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(addBtn.getStyle() + "-fx-font-size:14px;-fx-padding:12 0;");

        GridPane grid = new GridPane();
        grid.setHgap(18); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UI.pct(50), UI.pct(50));
        grid.add(UI.field("Category",            catBox),  0, 0);
        grid.add(UI.field("Amount (₹)",          amtF),    1, 0);
        grid.add(UI.field("Date",                dp),      0, 1);
        grid.add(UI.field("Note",                noteF),   1, 1);
        grid.add(UI.field("Add Custom Category", catRow),  0, 2, 2, 1);
        grid.add(addBtn, 0, 3, 2, 1);
        form.getChildren().add(grid);

        // Table
        VBox hist = UI.card("📋  Expense History");
        TableView<AppState.Expense> table = buildTable();
        table.setPrefHeight(230);
        hist.getChildren().add(table);

        addBtn.setOnAction(e -> {
            String cat  = catBox.getValue();
            String amts = amtF.getText().trim();
            if (cat == null || cat.isEmpty()) { UI.err("Please select a category!"); return; }
            if (amts.isEmpty())               { UI.err("Amount cannot be empty!"); return; }
            if (dp.getValue() == null)        { UI.err("Please select a date!"); return; }
            double amt;
            try { amt = Double.parseDouble(amts); if (amt <= 0) { UI.err("Amount must be > 0"); return; } }
            catch (NumberFormatException ex) { UI.err("Numbers only in amount!"); return; }

            state.getExpenses().add(new AppState.Expense(cat, amt, dp.getValue().toString(), noteF.getText().trim()));
            state.saveExpenses();  // ← persist
            UI.ok(UI.rupees(amt) + " expense in \"" + cat + "\" saved! 📝");
            amtF.clear(); noteF.clear(); dp.setValue(LocalDate.now());
            table.setItems(FXCollections.observableArrayList(state.getExpenses()));
        });

        content.getChildren().addAll(stats, form, hist);
        root.setCenter(UI.scroll(content));
        stage.setScene(new Scene(root, 940, 640));
    }

    private void refreshCats(ComboBox<String> b) {
        String cur = b.getValue();
        b.getItems().setAll(state.getCategories());
        if (cur != null && b.getItems().contains(cur)) b.setValue(cur);
        else if (!b.getItems().isEmpty()) b.setValue(b.getItems().get(0));
    }

    private TableView<AppState.Expense> buildTable() {
        TableView<AppState.Expense> t = new TableView<>();
        UI.styleTable(t);
        TableColumn<AppState.Expense,String> c1 = new TableColumn<>("Category");
        c1.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().category));
        TableColumn<AppState.Expense,String> c2 = new TableColumn<>("Amount");
        c2.setCellValueFactory(r -> new SimpleStringProperty(UI.rupees(r.getValue().amount)));
        TableColumn<AppState.Expense,String> c3 = new TableColumn<>("Date");
        c3.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().date));
        TableColumn<AppState.Expense,String> c4 = new TableColumn<>("Note");
        c4.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().note));
        t.getColumns().addAll(c1, c2, c3, c4);
        t.setItems(FXCollections.observableArrayList(state.getExpenses()));
        t.setPlaceholder(new Label("No expenses yet."));
        return t;
    }
}
