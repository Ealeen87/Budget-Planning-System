package com.budget.app.pages;

import com.budget.app.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DashboardPage {
    private final Stage stage;
    private final AppState state;
    private final Navigator nav;

    public DashboardPage(Stage stage, AppState state, Navigator nav) {
        this.stage = stage; this.state = state; this.nav = nav;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UI.BG + ";");
        root.setLeft(UI.sidebar(state.currentUser, "dash",
            nav::dashboard, nav::income, nav::expense, nav::budget, nav::summary, nav::logout));
        root.setCenter(UI.scroll(buildContent()));
        stage.setScene(new Scene(root, 940, 640));
    }

    private VBox buildContent() {
        VBox v = new VBox(24);
        v.setPadding(new Insets(32));

        v.getChildren().add(UI.pageHeader(
            "Dashboard Overview",
            "Hello, " + state.currentUser + "! 👋  Here's your financial snapshot."
        ));

        // Stats
        double inc = state.totalIncome(), exp = state.totalExpenses(), bal = state.balance();
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
            UI.statCard("Total Income",   UI.rupees(inc), "💵", UI.SUCCESS_L, "#2E7D32"),
            UI.statCard("Total Expenses", UI.rupees(exp), "💸", UI.DANGER_L,  "#C62828"),
            UI.statCard("Balance",        UI.rupees(bal), "💰", UI.PRIMARY_L, UI.PRIMARY_D),
            UI.statCard("Budget Set",     UI.rupees(state.totalBudget()), "🎯", UI.TEAL_L, UI.TEAL)
        );

        // Status
        boolean over = state.isOverBudget(), noBudg = state.totalBudget() == 0;
        String btxt = noBudg ? "ℹ️  No budget set yet — click 'Set Budget' to define your spending limits."
            : over  ? "⚠️  OVER BUDGET!  Your expenses have exceeded your total budget limits."
                    : "✅  WITHIN BUDGET!  Great job keeping your spending under control!";
        String bbg  = noBudg ? UI.AMBER_L   : over ? UI.DANGER_L  : UI.SUCCESS_L;
        String bfg  = noBudg ? UI.AMBER     : over ? UI.DANGER    : UI.SUCCESS;

        // Quick Actions
        VBox actCard = UI.card("🚀  Quick Actions");
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.getColumnConstraints().addAll(UI.pct(50), UI.pct(50));

        Button bInc  = UI.successBtn("💵   Add Income");
        Button bExp  = UI.dangerBtn("💸   Add Expense");
        Button bBudg = UI.primaryBtn("🎯   Set Budget");
        Button bSum  = UI.tealBtn("📊   View Summary");
        for (Button b : new Button[]{bInc, bExp, bBudg, bSum}) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle(b.getStyle() + "-fx-font-size:14px;-fx-padding:14 0;");
        }
        bInc.setOnAction(e -> nav.income());
        bExp.setOnAction(e -> nav.expense());
        bBudg.setOnAction(e -> nav.budget());
        bSum.setOnAction(e -> nav.summary());

        grid.add(bInc, 0, 0); grid.add(bExp, 1, 0);
        grid.add(bBudg, 0, 1); grid.add(bSum, 1, 1);
        actCard.getChildren().add(grid);

        // At a glance
        VBox glance = UI.card("📋  At a Glance");
        GridPane mini = new GridPane();
        mini.setHgap(20); mini.setVgap(10);
        mini.getColumnConstraints().addAll(UI.pct(25), UI.pct(25), UI.pct(25), UI.pct(25));
        mini.add(miniStat("Income Entries",  String.valueOf(state.getIncomes().size()),   UI.SUCCESS),  0, 0);
        mini.add(miniStat("Expense Entries", String.valueOf(state.getExpenses().size()),  UI.DANGER),   1, 0);
        mini.add(miniStat("Categories",      String.valueOf(state.getCategories().size()),UI.PRIMARY),  2, 0);
        mini.add(miniStat("Budgets Set",     String.valueOf(state.getBudgets().size()),   UI.TEAL),     3, 0);
        glance.getChildren().add(mini);

        v.getChildren().addAll(stats, UI.banner(btxt, bbg, bfg), actCard, glance);
        return v;
    }

    private VBox miniStat(String label, String val, String color) {
        VBox b = new VBox(4);
        Label l = new Label(label);
        l.setStyle("-fx-font-size:11px;-fx-text-fill:" + UI.MUTED + ";-fx-font-weight:bold;");
        Label v = new Label(val);
        v.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        b.getChildren().addAll(l, v);
        return b;
    }
}
