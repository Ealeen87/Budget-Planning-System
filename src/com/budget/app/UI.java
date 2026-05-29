package com.budget.app;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

public class UI {

    // ── Elegant Light & Bright Palette ────────────────────────────────────────
    public static final String PRIMARY    = "#5C6BC0";   // soft indigo
    public static final String PRIMARY_D  = "#3949AB";
    public static final String PRIMARY_L  = "#E8EAF6";
    public static final String SUCCESS    = "#43A047";   // fresh green
    public static final String SUCCESS_L  = "#E8F5E9";
    public static final String DANGER     = "#E53935";   // warm red
    public static final String DANGER_L   = "#FFEBEE";
    public static final String AMBER      = "#FB8C00";   // bright amber
    public static final String AMBER_L    = "#FFF3E0";
    public static final String TEAL       = "#00897B";
    public static final String TEAL_L     = "#E0F2F1";
    public static final String PURPLE     = "#8E24AA";
    public static final String PURPLE_L   = "#F3E5F5";
    public static final String CYAN       = "#00ACC1";
    public static final String CYAN_L     = "#E0F7FA";

    public static final String BG         = "#F5F6FA";   // very light page bg
    public static final String WHITE      = "#FFFFFF";
    public static final String BORDER     = "#E0E3EB";
    public static final String TEXT       = "#263238";
    public static final String MUTED      = "#78909C";
    public static final String SIDEBAR_BG = "#3D4C6F";   // soft navy (not too dark)
    public static final String SIDEBAR_ACT= "#5C6BC0";

    // ── Buttons ───────────────────────────────────────────────────────────────

    public static Button primaryBtn(String text) { return btn(text, PRIMARY,  WHITE); }
    public static Button successBtn(String text) { return btn(text, SUCCESS,  WHITE); }
    public static Button dangerBtn(String text)  { return btn(text, DANGER,   WHITE); }
    public static Button tealBtn(String text)    { return btn(text, TEAL,     WHITE); }
    public static Button purpleBtn(String text)  { return btn(text, PURPLE,   WHITE); }
    public static Button amberBtn(String text)   { return btn(text, AMBER,    WHITE); }

    public static Button outlineBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:white;-fx-text-fill:" + PRIMARY +
            ";-fx-border-color:" + PRIMARY + ";-fx-border-width:1.5;" +
            "-fx-border-radius:8;-fx-background-radius:8;" +
            "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:8 18 8 18;-fx-cursor:hand;");
        hover(b);
        return b;
    }

    private static Button btn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg +
            ";-fx-font-size:13px;-fx-font-weight:bold;" +
            "-fx-padding:10 22 10 22;-fx-background-radius:9;-fx-cursor:hand;");
        hover(b);
        return b;
    }

    private static void hover(Button b) {
        b.setOnMouseEntered(e -> b.setOpacity(0.82));
        b.setOnMouseExited(e  -> b.setOpacity(1.0));
    }

    // ── Inputs ────────────────────────────────────────────────────────────────

    public static void styleInput(Control c) {
        c.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER +
            ";-fx-border-radius:8;-fx-background-radius:8;" +
            "-fx-padding:8 12 8 12;-fx-font-size:13px;-fx-pref-height:38;");
    }

    public static <T> void styleCombo(ComboBox<T> c) {
        c.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER +
            ";-fx-border-radius:8;-fx-background-radius:8;-fx-font-size:13px;-fx-pref-height:38;");
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    public static VBox card(String title) {
        VBox v = new VBox(14);
        v.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER +
            ";-fx-border-radius:14;-fx-background-radius:14;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(92,107,192,0.10),10,0,0,3);");
        v.setPadding(new Insets(20));
        if (title != null && !title.isEmpty()) {
            Label t = new Label(title);
            t.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + TEXT + ";");
            Separator s = new Separator();
            s.setStyle("-fx-background-color:" + BORDER + ";");
            v.getChildren().addAll(t, s);
        }
        return v;
    }

    // ── Stat Card ─────────────────────────────────────────────────────────────

    public static HBox statCard(String title, String value, String icon, String bg, String accent) {
        VBox inner = new VBox(4);
        inner.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:28px;");
        Label ttl = new Label(title);
        ttl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + accent + "cc;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:19px;-fx-font-weight:bold;-fx-text-fill:" + accent + ";");
        inner.getChildren().addAll(ico, ttl, val);

        HBox h = new HBox(inner);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(16, 20, 16, 20));
        h.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:12;" +
            "-fx-border-color:" + accent + "33;-fx-border-radius:12;-fx-border-width:1;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),6,0,0,2);");
        HBox.setHgrow(h, Priority.ALWAYS);
        return h;
    }

    // ── Form field ────────────────────────────────────────────────────────────

    public static VBox field(String label, Node input) {
        VBox v = new VBox(5);
        Label l = new Label(label);
        l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + MUTED + ";letter-spacing:0.5px;");
        v.getChildren().addAll(l, input);
        return v;
    }

    // ── Page header ───────────────────────────────────────────────────────────

    public static VBox pageHeader(String title, String sub) {
        VBox v = new VBox(3);
        v.setPadding(new Insets(0, 0, 6, 0));
        Label t = new Label(title);
        t.setStyle("-fx-font-size:23px;-fx-font-weight:bold;-fx-text-fill:" + TEXT + ";");
        Label s = new Label(sub);
        s.setStyle("-fx-font-size:13px;-fx-text-fill:" + MUTED + ";");
        v.getChildren().addAll(t, s);
        return v;
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    public static Label banner(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:" + fg +
            ";-fx-font-size:13px;-fx-font-weight:bold;" +
            "-fx-padding:12 18 12 18;-fx-background-radius:9;" +
            "-fx-border-color:" + fg + "44;-fx-border-width:1.5;-fx-border-radius:9;");
        return l;
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    public static ScrollPane scroll(Node n) {
        ScrollPane sp = new ScrollPane(n);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        return sp;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    public static VBox sidebar(String user, String active,
            Runnable toDash, Runnable toIncome, Runnable toExpense,
            Runnable toBudget, Runnable toSummary, Runnable toLogout) {
        VBox sb = new VBox(4);
        sb.setPrefWidth(210);
        sb.setStyle("-fx-background-color:" + SIDEBAR_BG + ";");

        // Logo
        VBox logo = new VBox(3);
        logo.setPadding(new Insets(24, 16, 16, 16));
        logo.getChildren().addAll(
            sbl("💰", 32),
            sbl("Budget Planner", 15, true, "white"),
            sbl("👤 " + user, 11, false, "rgba(255,255,255,0.60)")
        );

        Separator sep = new Separator();
        sep.setPadding(new Insets(2, 12, 6, 12));
        sep.setStyle("-fx-background-color:rgba(255,255,255,0.15);");

        // Nav items
        VBox nav = new VBox(3);
        nav.setPadding(new Insets(0, 10, 0, 10));
        nav.getChildren().addAll(
            sbBtn("🏠", "Dashboard",    active.equals("dash"),    toDash),
            sbBtn("💵", "Add Income",   active.equals("income"),  toIncome),
            sbBtn("💸", "Add Expense",  active.equals("expense"), toExpense),
            sbBtn("🎯", "Set Budget",   active.equals("budget"),  toBudget),
            sbBtn("📊", "View Summary", active.equals("summary"), toSummary)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox bot = new VBox();
        bot.setPadding(new Insets(10));
        bot.getChildren().add(sbBtn("🚪", "Exit / Logout", false, toLogout));

        sb.getChildren().addAll(logo, sep, nav, spacer, bot);
        return sb;
    }

    private static Button sbBtn(String icon, String label, boolean active, Runnable action) {
        Button b = new Button(icon + "   " + label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        String on  = "-fx-background-color:" + SIDEBAR_ACT + ";-fx-text-fill:white;" +
            "-fx-font-size:13px;-fx-padding:11 14;-fx-background-radius:9;-fx-font-weight:bold;";
        String off = "-fx-background-color:transparent;-fx-text-fill:rgba(255,255,255,0.85);" +
            "-fx-font-size:13px;-fx-padding:11 14;-fx-background-radius:9;-fx-cursor:hand;";
        String hov = "-fx-background-color:rgba(255,255,255,0.12);-fx-text-fill:white;" +
            "-fx-font-size:13px;-fx-padding:11 14;-fx-background-radius:9;-fx-cursor:hand;";
        b.setStyle(active ? on : off);
        if (!active) {
            b.setOnMouseEntered(e -> b.setStyle(hov));
            b.setOnMouseExited(e  -> b.setStyle(off));
            if (action != null) b.setOnAction(e -> action.run());
        }
        return b;
    }

    private static Label sbl(String t, int sz) {
        Label l = new Label(t); l.setStyle("-fx-font-size:" + sz + "px;"); return l;
    }
    private static Label sbl(String t, int sz, boolean bold, String col) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:" + sz + "px;-fx-font-weight:" + (bold?"bold":"normal") + ";-fx-text-fill:" + col + ";");
        return l;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    public static void styleTable(TableView<?> t) {
        t.setStyle("-fx-background-color:white;-fx-border-color:" + BORDER +
            ";-fx-border-radius:10;-fx-background-radius:10;");
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ── Grid helpers ──────────────────────────────────────────────────────────

    public static ColumnConstraints pct(double p) {
        ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(p); return c;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static String rupees(double v) { return String.format("₹%.2f", v); }

    public static void ok(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("✅ Success"); a.setHeaderText(null); a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-font-size:13px;-fx-background-color:white;");
        a.showAndWait();
    }
    public static void err(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("❌ Error"); a.setHeaderText(null); a.setContentText(msg);
        a.getDialogPane().setStyle("-fx-font-size:13px;-fx-background-color:white;");
        a.showAndWait();
    }
}
