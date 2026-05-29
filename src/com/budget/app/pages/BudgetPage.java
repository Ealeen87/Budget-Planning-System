package com.budget.app.pages;

import com.budget.app.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Map;

public class BudgetPage {
    private final Stage stage;
    private final AppState state;
    private final Navigator nav;

    public BudgetPage(Stage stage, AppState state, Navigator nav) {
        this.stage = stage; this.state = state; this.nav = nav;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UI.BG + ";");
        root.setLeft(UI.sidebar(state.currentUser, "budget",
            nav::dashboard, nav::income, nav::expense, nav::budget, nav::summary, nav::logout));

        VBox content = new VBox(22);
        content.setPadding(new Insets(32));
        content.getChildren().add(UI.pageHeader("🎯  Set Budget", "Define spending limits per category"));

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
            UI.statCard("Total Budget Set", UI.rupees(state.totalBudget()),           "🎯", UI.PRIMARY_L, UI.PRIMARY_D),
            UI.statCard("Total Spent",      UI.rupees(state.totalExpenses()),          "💸", UI.DANGER_L,  "#C62828"),
            UI.statCard("Categories Set",   String.valueOf(state.getBudgets().size()), "📂", UI.SUCCESS_L, UI.SUCCESS)
        );

        Label info = UI.banner(
            "ℹ️  Categories added here or in 'Add Expense' are shared across both pages.",
            UI.PRIMARY_L, UI.PRIMARY_D
        );

        // Form
        VBox form = UI.card("💾  Set Budget Limit");

        ComboBox<String> catBox = new ComboBox<>();
        refreshCats(catBox);
        UI.styleCombo(catBox); catBox.setMaxWidth(Double.MAX_VALUE);

        TextField limitF = new TextField(); limitF.setPromptText("Budget limit (numbers only)");
        UI.styleInput(limitF); limitF.setMaxWidth(Double.MAX_VALUE);
        limitF.textProperty().addListener((o,ov,nv) -> { if (!nv.matches("\\d*\\.?\\d*")) limitF.setText(ov); });

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

        Button saveBtn = UI.primaryBtn("💾   Save Budget Limit");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle(saveBtn.getStyle() + "-fx-font-size:14px;-fx-padding:12 0;");

        GridPane grid = new GridPane();
        grid.setHgap(18); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UI.pct(50), UI.pct(50));
        grid.add(UI.field("Select Category",     catBox),  0, 0);
        grid.add(UI.field("Budget Limit (₹)",    limitF),  1, 0);
        grid.add(UI.field("Add Custom Category", catRow),  0, 1, 2, 1);
        grid.add(saveBtn, 0, 2, 2, 1);
        form.getChildren().add(grid);

        VBox tableCard = UI.card("📋  Current Budget Limits & Status");
        TableView<Map.Entry<String,Double>> table = buildTable();
        table.setPrefHeight(260);
        tableCard.getChildren().add(table);

        saveBtn.setOnAction(e -> {
            String cat  = catBox.getValue();
            String lims = limitF.getText().trim();
            if (cat == null || cat.isEmpty()) { UI.err("Please select a category!"); return; }
            if (lims.isEmpty())               { UI.err("Budget limit cannot be empty!"); return; }
            double lim;
            try { lim = Double.parseDouble(lims); if (lim <= 0) { UI.err("Limit must be > 0"); return; } }
            catch (NumberFormatException ex) { UI.err("Numbers only in limit!"); return; }

            state.getBudgets().put(cat, lim);
            state.saveBudgets();  // ← persist
            UI.ok("Budget limit of " + UI.rupees(lim) + " set for \"" + cat + "\" 🎯");
            limitF.clear();
            table.setItems(FXCollections.observableArrayList(state.getBudgets().entrySet()));
        });

        content.getChildren().addAll(stats, info, form, tableCard);
        root.setCenter(UI.scroll(content));
        stage.setScene(new Scene(root, 940, 640));
    }

    private void refreshCats(ComboBox<String> b) {
        String cur = b.getValue();
        b.getItems().setAll(state.getCategories());
        if (cur != null && b.getItems().contains(cur)) b.setValue(cur);
        else if (!b.getItems().isEmpty()) b.setValue(b.getItems().get(0));
    }

    private TableView<Map.Entry<String,Double>> buildTable() {
        TableView<Map.Entry<String,Double>> t = new TableView<>();
        UI.styleTable(t);

        TableColumn<Map.Entry<String,Double>,String> c1 = new TableColumn<>("Category");
        c1.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getKey()));

        TableColumn<Map.Entry<String,Double>,String> c2 = new TableColumn<>("Budget Limit");
        c2.setCellValueFactory(r -> new SimpleStringProperty(UI.rupees(r.getValue().getValue())));

        TableColumn<Map.Entry<String,Double>,String> c3 = new TableColumn<>("Spent");
        c3.setCellValueFactory(r -> {
            double s = state.getExpenses().stream()
                .filter(e -> e.category.equals(r.getValue().getKey()))
                .mapToDouble(e -> e.amount).sum();
            return new SimpleStringProperty(UI.rupees(s));
        });

        TableColumn<Map.Entry<String,Double>,String> c4 = new TableColumn<>("Remaining");
        c4.setCellValueFactory(r -> {
            double s = state.getExpenses().stream()
                .filter(e -> e.category.equals(r.getValue().getKey()))
                .mapToDouble(e -> e.amount).sum();
            return new SimpleStringProperty(UI.rupees(r.getValue().getValue() - s));
        });

        TableColumn<Map.Entry<String,Double>,String> c5 = new TableColumn<>("Status");
        c5.setCellValueFactory(r -> {
            double s = state.getExpenses().stream()
                .filter(e -> e.category.equals(r.getValue().getKey()))
                .mapToDouble(e -> e.amount).sum();
            return new SimpleStringProperty(s > r.getValue().getValue() ? "⚠️ Over" : "✅ OK");
        });

        t.getColumns().addAll(c1, c2, c3, c4, c5);
        t.setItems(FXCollections.observableArrayList(state.getBudgets().entrySet()));
        t.setPlaceholder(new Label("No budget limits set yet."));
        return t;
    }
}
