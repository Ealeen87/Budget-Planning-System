package com.budget.app.pages;

import com.budget.app.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SummaryPage {
    private final Stage stage;
    private final AppState state;
    private final Navigator nav;

    // Chart color palette — bright but elegant
    private static final String[] CHART_COLORS = {
        "#5C6BC0","#43A047","#FB8C00","#E53935","#00897B",
        "#8E24AA","#00ACC1","#F4511E","#6D4C41","#039BE5"
    };

    public SummaryPage(Stage stage, AppState state, Navigator nav) {
        this.stage = stage; this.state = state; this.nav = nav;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UI.BG + ";");
        root.setLeft(UI.sidebar(state.currentUser, "summary",
            nav::dashboard, nav::income, nav::expense, nav::budget, nav::summary, nav::logout));

        VBox content = new VBox(26);
        content.setPadding(new Insets(32));
        content.getChildren().addAll(
            UI.pageHeader("📊  View Summary", "Full financial report — charts, monthly & yearly breakdown"),
            buildTopStats(),
            buildStatusBanner(),
            buildChartsRow(),
            buildMonthly(),
            buildYearly(),
            buildCategoryBars(),
            buildFinalSummary()
        );

        root.setCenter(UI.scroll(content));
        stage.setScene(new Scene(root, 960, 680));
    }

    // ── Top stats ──────────────────────────────────────────────────────────────

    private HBox buildTopStats() {
        HBox row = new HBox(14);
        row.getChildren().addAll(
            UI.statCard("Total Income",   UI.rupees(state.totalIncome()),   "💵", UI.SUCCESS_L, "#2E7D32"),
            UI.statCard("Total Expenses", UI.rupees(state.totalExpenses()), "💸", UI.DANGER_L,  "#C62828"),
            UI.statCard("Balance",        UI.rupees(state.balance()),       "💰", UI.PRIMARY_L, UI.PRIMARY_D),
            UI.statCard("Total Budget",   UI.rupees(state.totalBudget()),   "🎯", UI.TEAL_L,    UI.TEAL)
        );
        return row;
    }

    // ── Status banner ──────────────────────────────────────────────────────────

    private Label buildStatusBanner() {
        boolean over = state.isOverBudget(), noBudg = state.totalBudget() == 0;
        String txt = noBudg ? "ℹ️  No budget set. Go to 'Set Budget' to define your limits."
            : over  ? "⚠️  OVER BUDGET! Total spending exceeds your set budget limits. Take action!"
                    : "✅  WITHIN BUDGET! Great financial discipline. Keep it up!";
        return UI.banner(txt, noBudg ? UI.AMBER_L : over ? UI.DANGER_L : UI.SUCCESS_L,
                             noBudg ? UI.AMBER   : over ? UI.DANGER   : UI.SUCCESS);
    }

    // ── Charts row ────────────────────────────────────────────────────────────

    private HBox buildChartsRow() {
        HBox row = new HBox(18);

        VBox pieCard = UI.card("🥧  Expense by Category");
        pieCard.getChildren().add(buildPieChart());
        HBox.setHgrow(pieCard, Priority.ALWAYS);

        VBox barCard = UI.card("📊  Income vs Expense vs Budget");
        barCard.getChildren().add(buildBarChart());
        HBox.setHgrow(barCard, Priority.ALWAYS);

        row.getChildren().addAll(pieCard, barCard);
        return row;
    }

    // ── Hand-drawn Pie Chart (pure JavaFX shapes) ─────────────────────────────

    private Pane buildPieChart() {
        Pane pane = new Pane();
        pane.setPrefSize(320, 260);

        Map<String, Double> data = state.getExpenses().stream()
            .collect(Collectors.groupingBy(e -> e.category, Collectors.summingDouble(e -> e.amount)));

        if (data.isEmpty()) {
            Label lbl = new Label("No expense data yet.\nAdd some expenses first!");
            lbl.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:13px;-fx-text-alignment:center;");
            lbl.setLayoutX(60); lbl.setLayoutY(100);
            pane.getChildren().add(lbl);
            return pane;
        }

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        double cx = 130, cy = 130, r = 110;
        double startAngle = -90;
        int ci = 0;

        VBox legend = new VBox(5);
        legend.setLayoutX(270); legend.setLayoutY(30);

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double pct    = entry.getValue() / total;
            double extent = pct * 360;
            String color  = CHART_COLORS[ci % CHART_COLORS.length];

            Arc arc = new Arc(cx, cy, r, r, startAngle, -extent);
            arc.setType(ArcType.ROUND);
            arc.setFill(Color.web(color));
            arc.setStroke(Color.WHITE);
            arc.setStrokeWidth(2);
            pane.getChildren().add(arc);

            // Legend item
            HBox item = new HBox(6);
            item.setAlignment(Pos.CENTER_LEFT);
            Rectangle rect = new Rectangle(12, 12);
            rect.setFill(Color.web(color));
            rect.setArcWidth(3); rect.setArcHeight(3);
            Label lbl = new Label(entry.getKey() + "  " + String.format("%.0f%%", pct * 100));
            lbl.setStyle("-fx-font-size:10px;-fx-text-fill:" + UI.TEXT + ";");
            item.getChildren().addAll(rect, lbl);
            legend.getChildren().add(item);

            startAngle -= extent;
            ci++;
        }
        pane.getChildren().add(legend);
        return pane;
    }

    // ── Hand-drawn Bar Chart (pure JavaFX) ────────────────────────────────────

    private Pane buildBarChart() {
        Pane pane = new Pane();
        pane.setPrefSize(320, 260);

        double inc  = state.totalIncome();
        double exp  = state.totalExpenses();
        double budg = state.totalBudget();
        double bal  = Math.max(state.balance(), 0);
        double maxV = Math.max(Math.max(inc, exp), Math.max(budg, bal));
        if (maxV == 0) maxV = 1;

        double chartH = 180, chartY = 20, barW = 46, gap = 14, startX = 30;

        String[] labels = {"Income",   "Expenses", "Budget",    "Balance"};
        double[] values = {inc,         exp,        budg,        bal};
        String[] colors = {UI.SUCCESS,  UI.DANGER,  UI.PRIMARY,  UI.TEAL};

        // Y-axis line
        Line yAxis = new Line(startX - 4, chartY, startX - 4, chartY + chartH);
        yAxis.setStroke(Color.web(UI.BORDER)); yAxis.setStrokeWidth(1.5);
        // X-axis line
        Line xAxis = new Line(startX - 4, chartY + chartH, startX + 4 * (barW + gap) + 10, chartY + chartH);
        xAxis.setStroke(Color.web(UI.BORDER)); xAxis.setStrokeWidth(1.5);
        pane.getChildren().addAll(yAxis, xAxis);

        for (int i = 0; i < values.length; i++) {
            double barH = (values[i] / maxV) * chartH;
            double x    = startX + i * (barW + gap);
            double y    = chartY + chartH - barH;

            Rectangle bar = new Rectangle(x, y, barW, barH);
            bar.setFill(Color.web(colors[i]));
            bar.setArcWidth(6); bar.setArcHeight(6);
            bar.setOpacity(0.88);

            Label valLbl = new Label(shortRupees(values[i]));
            valLbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:" + colors[i] + ";");
            valLbl.setLayoutX(x + 2);
            valLbl.setLayoutY(y - 18);

            Label nameLbl = new Label(labels[i]);
            nameLbl.setStyle("-fx-font-size:10px;-fx-text-fill:" + UI.MUTED + ";-fx-font-weight:bold;");
            nameLbl.setLayoutX(x + 2);
            nameLbl.setLayoutY(chartY + chartH + 6);

            pane.getChildren().addAll(bar, valLbl, nameLbl);
        }
        return pane;
    }

    private String shortRupees(double v) {
        if (v >= 100000) return String.format("₹%.1fL", v / 100000);
        if (v >= 1000)   return String.format("₹%.1fK", v / 1000);
        return String.format("₹%.0f", v);
    }

    // ── Monthly report ────────────────────────────────────────────────────────

    private VBox buildMonthly() {
        String month = LocalDate.now().toString().substring(0, 7);
        Map<String, Double> spent = state.getExpenses().stream()
            .filter(e -> e.date.startsWith(month))
            .collect(Collectors.groupingBy(e -> e.category, Collectors.summingDouble(e -> e.amount)));

        VBox card = UI.card("📅  Monthly Report  —  " + month);
        TableView<String[]> t = summaryTable(spent);
        t.setPrefHeight(200);
        card.getChildren().add(t);
        return card;
    }

    // ── Yearly report ─────────────────────────────────────────────────────────

    private VBox buildYearly() {
        String year = String.valueOf(LocalDate.now().getYear());
        Map<String, Double> spent = state.getExpenses().stream()
            .filter(e -> e.date.startsWith(year))
            .collect(Collectors.groupingBy(e -> e.category, Collectors.summingDouble(e -> e.amount)));

        VBox card = UI.card("📆  Yearly Report  —  " + year);
        TableView<String[]> t = summaryTable(spent);
        t.setPrefHeight(200);
        card.getChildren().add(t);
        return card;
    }

    private TableView<String[]> summaryTable(Map<String, Double> spentMap) {
        TableView<String[]> t = new TableView<>();
        UI.styleTable(t);
        t.getColumns().addAll(col("Category",0), col("Budget Limit",1),
            col("Spent",2), col("Remaining",3), col("Status",4));

        List<String[]> rows = new ArrayList<>();
        for (Map.Entry<String,Double> e : state.getBudgets().entrySet()) {
            double s = spentMap.getOrDefault(e.getKey(), 0.0);
            rows.add(new String[]{e.getKey(), UI.rupees(e.getValue()),
                UI.rupees(s), UI.rupees(e.getValue() - s),
                s > e.getValue() ? "⚠️ Over" : "✅ OK"});
        }
        for (Map.Entry<String,Double> e : spentMap.entrySet())
            if (!state.getBudgets().containsKey(e.getKey()))
                rows.add(new String[]{e.getKey(), "Not Set", UI.rupees(e.getValue()), "N/A", "—"});
        if (rows.isEmpty()) rows.add(new String[]{"No data", "—", "—", "—", "—"});

        t.setItems(FXCollections.observableArrayList(rows));
        t.setPlaceholder(new Label("No expense data for this period."));
        return t;
    }

    private TableColumn<String[],String> col(String title, int idx) {
        TableColumn<String[],String> c = new TableColumn<>(title);
        c.setCellValueFactory(r -> new SimpleStringProperty(r.getValue()[idx]));
        return c;
    }

    // ── Category progress bars ────────────────────────────────────────────────

    private VBox buildCategoryBars() {
        VBox card = UI.card("📊  Category Spending Breakdown");
        Map<String,Double> spent = state.getExpenses().stream()
            .collect(Collectors.groupingBy(e -> e.category, Collectors.summingDouble(e -> e.amount)));

        if (spent.isEmpty()) {
            card.getChildren().add(new Label("No expense data yet. Add expenses to see breakdown!"));
            return card;
        }

        double max = spent.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
        int[] ci = {0};
        VBox bars = new VBox(12);

        spent.entrySet().stream()
            .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
            .forEach(entry -> {
                double s      = entry.getValue();
                double budg   = state.getBudgets().getOrDefault(entry.getKey(), 0.0);
                boolean over  = budg > 0 && s > budg;
                String color  = over ? UI.DANGER : CHART_COLORS[ci[0] % CHART_COLORS.length];
                ci[0]++;
                double fillW  = Math.min(s / max, 1.0) * 300;

                Label catLbl = new Label(entry.getKey());
                catLbl.setMinWidth(110); catLbl.setMaxWidth(110);
                catLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UI.TEXT + ";");

                Pane track = new Pane();
                track.setPrefSize(300, 22);
                track.setStyle("-fx-background-color:#ECEFF1;-fx-background-radius:6;");

                Pane fill = new Pane();
                fill.setPrefSize(fillW, 22);
                fill.setStyle("-fx-background-color:" + color + ";-fx-background-radius:6;");
                fill.setOpacity(0.85);
                track.getChildren().add(fill);

                Label amtLbl = new Label(UI.rupees(s) + (budg > 0 ? "  /  " + UI.rupees(budg) : ""));
                amtLbl.setMinWidth(170);
                amtLbl.setStyle("-fx-font-size:12px;-fx-font-weight:" + (over?"bold":"normal") +
                    ";-fx-text-fill:" + (over ? UI.DANGER : UI.MUTED) + ";");

                Label pctLbl = new Label(String.format("%.0f%%", (s/max)*100));
                pctLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
                pctLbl.setMinWidth(44);

                HBox row = new HBox(12, catLbl, track, amtLbl, pctLbl);
                row.setAlignment(Pos.CENTER_LEFT);
                bars.getChildren().add(row);
            });

        card.getChildren().add(bars);
        return card;
    }

    // ── Final summary ─────────────────────────────────────────────────────────

    private VBox buildFinalSummary() {
        VBox card = UI.card("📋  Final Budget Dashboard Summary");

        double inc  = state.totalIncome(), exp = state.totalExpenses();
        double bal  = state.balance(), budg = state.totalBudget();
        boolean over = state.isOverBudget();

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.getColumnConstraints().addAll(UI.pct(50), UI.pct(50));
        grid.add(sRow("💵  Total Income",      UI.rupees(inc),  UI.SUCCESS),  0, 0);
        grid.add(sRow("💸  Total Expenses",    UI.rupees(exp),  UI.DANGER),   1, 0);
        grid.add(sRow("💰  Remaining Balance", UI.rupees(bal),  bal>=0 ? UI.SUCCESS : UI.DANGER), 0, 1);
        grid.add(sRow("🎯  Total Budget Set",  UI.rupees(budg), UI.PRIMARY),  1, 1);
        if (budg > 0) {
            double pct = (exp / budg) * 100;
            grid.add(sRow("📊  Budget Used",
                String.format("%.1f%%  (%s  of  %s)", pct, UI.rupees(exp), UI.rupees(budg)),
                pct > 100 ? UI.DANGER : UI.SUCCESS), 0, 2, 2, 1);
        }

        String st = budg == 0 ? "ℹ️   NO BUDGET SET"
            : over ? "⚠️   OVER BUDGET" : "✅   WITHIN BUDGET";
        if (budg > 0 && !over)
            st += String.format("   ( %.1f%% of budget used )", (exp/budg)*100);

        String sbg = budg==0 ? UI.AMBER_L : over ? UI.DANGER_L : UI.SUCCESS_L;
        String sfg = budg==0 ? UI.AMBER   : over ? UI.DANGER   : UI.SUCCESS;

        Label big = new Label(st);
        big.setMaxWidth(Double.MAX_VALUE);
        big.setAlignment(Pos.CENTER);
        big.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + sfg +
            ";-fx-background-color:" + sbg + ";-fx-padding:22;" +
            "-fx-background-radius:12;-fx-border-color:" + sfg + "55;" +
            "-fx-border-width:2;-fx-border-radius:12;");

        card.getChildren().addAll(grid, big);
        return card;
    }

    private HBox sRow(String label, String value, String color) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UI.TEXT + ";");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label val = new Label(value);
        val.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        HBox row = new HBox(10, lbl, sp, val);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color:white;-fx-border-color:" + UI.BORDER +
            ";-fx-border-radius:8;-fx-background-radius:8;");
        HBox.setHgrow(row, Priority.ALWAYS);
        return row;
    }
}
