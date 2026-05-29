package com.budget.app.pages;

import com.budget.app.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.util.List;

public class LoginPage {
    private final Stage stage;
    private final AppState state;
    private final Navigator nav;
    private boolean isRegister = false;

    public LoginPage(Stage stage, AppState state, Navigator nav) {
        this.stage = stage; this.state = state; this.nav = nav;
    }

    public void show() {
        // Reload users from disk every time login page is shown
        state.loadUsers();

        // ── Left branding ─────────────────────────────────────────────────────
        VBox left = new VBox(22);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(60, 40, 60, 40));
        left.setPrefWidth(360);
        left.setStyle(
            "-fx-background-color:linear-gradient(to bottom right,#5C6BC0,#7E57C2);"
        );

        Label ico = new Label("💰"); ico.setStyle("-fx-font-size:64px;");
        Label appName = new Label("Budget Planner");
        appName.setStyle("-fx-font-size:27px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label tagline = new Label("Plan smart. Spend wisely.\nSave more every month.");
        tagline.setStyle("-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.85);-fx-text-alignment:center;");
        tagline.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox features = new VBox(10);
        features.setAlignment(Pos.CENTER_LEFT);
        String[] feats = {"✅  Track Income & Expenses", "📊  Visual Budget Charts",
                          "📅  Monthly & Yearly Reports", "💾  Data saved automatically"};
        for (String f : feats) {
            Label fl = new Label(f);
            fl.setStyle("-fx-font-size:13px;-fx-text-fill:rgba(255,255,255,0.90);");
            features.getChildren().add(fl);
        }

        // User count badge
        int userCount = state.getUserCount();
        Label badge = new Label("👥 " + userCount + " registered user" + (userCount == 1 ? "" : "s"));
        badge.setStyle("-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;" +
            "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:7 14 7 14;-fx-background-radius:20;");

        left.getChildren().addAll(ico, appName, tagline, features, badge);

        // ── Right form ────────────────────────────────────────────────────────
        StackPane right = new StackPane();
        right.setStyle("-fx-background-color:#F5F6FA;");
        right.setPrefWidth(400);

        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(360);
        form.setPadding(new Insets(50, 36, 50, 36));

        Label title = new Label("Welcome Back!");
        title.setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:#263238;");
        Label sub = new Label("Sign in to continue");
        sub.setStyle("-fx-font-size:13px;-fx-text-fill:#78909C;");

        // Card
        VBox fc = new VBox(16);
        fc.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-radius:14;" +
            "-fx-effect:dropshadow(three-pass-box,rgba(92,107,192,0.12),14,0,0,4);");
        fc.setPadding(new Insets(26));

        TextField userF = new TextField();
        userF.setPromptText("Enter your username");
        UI.styleInput(userF); userF.setMaxWidth(Double.MAX_VALUE);

        PasswordField passF = new PasswordField();
        passF.setPromptText("Enter your password");
        UI.styleInput(passF); passF.setMaxWidth(Double.MAX_VALUE);

        Button actionBtn = UI.primaryBtn("Sign In");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle(actionBtn.getStyle() + "-fx-font-size:14px;-fx-padding:12 0;");

        Separator div = new Separator();

        Label switchLbl = new Label("Don't have an account?");
        switchLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#78909C;");
        Button switchBtn = new Button("Create Account");
        switchBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#5C6BC0;" +
            "-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0;");

        HBox switchRow = new HBox(6, switchLbl, switchBtn);
        switchRow.setAlignment(Pos.CENTER);

        fc.getChildren().addAll(
            UI.field("Username", userF),
            UI.field("Password", passF),
            actionBtn, div, switchRow
        );
        form.getChildren().addAll(title, sub, fc);
        right.getChildren().add(form);

        // ── Actions ───────────────────────────────────────────────────────────
        actionBtn.setOnAction(e -> {
            String u = userF.getText().trim();
            String p = passF.getText();
            if (u.isEmpty() || p.isEmpty()) { UI.err("Please fill in all fields!"); return; }

            if (isRegister) {
                // REGISTER
                if (state.users.containsKey(u)) {
                    UI.err("Username \"" + u + "\" already exists. Please choose another."); return;
                }
                String hashed = hash(p);
                state.users.put(u, hashed);
                state.saveUsers();           // ← persist immediately
                UI.ok("Account created for \"" + u + "\"!\nYou can now sign in. 🎉");
                isRegister = false;
                updateMode(title, sub, actionBtn, switchLbl, switchBtn);
                userF.clear(); passF.clear();
            } else {
                // LOGIN — reload from disk to be 100% fresh
                state.loadUsers();
                String stored = state.users.get(u);
                if (stored == null) {
                    UI.err("Username \"" + u + "\" not found.\nPlease check and try again."); return;
                }
                String entered = hash(p);
                if (!stored.equals(entered)) {
                    UI.err("Wrong password. Please try again."); return;
                }
                state.currentUser = u;
                state.loadUserData();        // ← load that user's data from disk
                nav.dashboard();
            }
        });

        switchBtn.setOnAction(e -> {
            isRegister = !isRegister;
            updateMode(title, sub, actionBtn, switchLbl, switchBtn);
            userF.clear(); passF.clear();
        });

        // ── Root ──────────────────────────────────────────────────────────────
        HBox root = new HBox(left, right);
        HBox.setHgrow(left,  Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        stage.setScene(new Scene(root, 760, 560));
    }

    private void updateMode(Label title, Label sub, Button btn, Label switchLbl, Button switchBtn) {
        if (isRegister) {
            title.setText("Create Account"); sub.setText("Join us — it's free!");
            btn.setText("Register");
            switchLbl.setText("Already have an account?"); switchBtn.setText("Sign In");
        } else {
            title.setText("Welcome Back!"); sub.setText("Sign in to continue");
            btn.setText("Sign In");
            switchLbl.setText("Don't have an account?"); switchBtn.setText("Create Account");
        }
    }

    private String hash(String pw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(pw.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { return pw; }
    }
}
