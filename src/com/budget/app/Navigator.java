package com.budget.app;

import com.budget.app.pages.*;
import javafx.stage.Stage;

public class Navigator {
    private final Stage stage;
    public  final AppState state;

    public Navigator(Stage stage, AppState state) {
        this.stage = stage;
        this.state = state;
    }

    public void login()    { new LoginPage(stage, state, this).show(); }
    public void dashboard(){ new DashboardPage(stage, state, this).show(); }
    public void income()   { new IncomePage(stage, state, this).show(); }
    public void expense()  { new ExpensePage(stage, state, this).show(); }
    public void budget()   { new BudgetPage(stage, state, this).show(); }
    public void summary()  { new SummaryPage(stage, state, this).show(); }
    public void logout()   {
        state.saveUserData();
        state.currentUser = null;
        login();
    }
}
