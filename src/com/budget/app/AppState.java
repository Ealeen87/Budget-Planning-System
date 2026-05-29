package com.budget.app;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AppState {

    // ── Storage file paths (relative to JAR location) ─────────────────────────
    public static final String DATA_DIR   = "budget_data";
    public static final String USERS_FILE = DATA_DIR + "/users.txt";

    public Map<String, String> users = new LinkedHashMap<>();
    public String currentUser        = null;

    private Map<String, List<Income>>          allIncomes    = new HashMap<>();
    private Map<String, List<Expense>>         allExpenses   = new HashMap<>();
    private Map<String, Map<String, Double>>   allBudgets    = new HashMap<>();
    private Map<String, List<String>>          allCategories = new HashMap<>();

    public static final List<String> DEFAULT_CATS = Arrays.asList(
        "Food", "Travel", "Grocery", "Clothes", "Drinks", "Bills", "Other"
    );

    // ── Init ──────────────────────────────────────────────────────────────────

    public AppState() {
        createDataDir();
        loadUsers();
    }

    // ── Directory & file management ───────────────────────────────────────────

    private void createDataDir() {
        try { Files.createDirectories(Paths.get(DATA_DIR)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    // ── USER persistence ──────────────────────────────────────────────────────

    public void loadUsers() {
        users.clear();
        File f = new File(USERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) users.put(parts[0], parts[1]);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void saveUsers() {
        createDataDir();
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> e : users.entrySet())
                pw.println(e.getKey() + "|" + e.getValue());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int getUserCount() { return users.size(); }

    public List<String> getAllUsernames() { return new ArrayList<>(users.keySet()); }

    // ── USER DATA persistence ─────────────────────────────────────────────────

    private String userFile(String suffix) {
        return DATA_DIR + "/" + currentUser + "_" + suffix + ".txt";
    }

    public void saveUserData() {
        if (currentUser == null) return;
        saveIncomes(); saveExpenses(); saveBudgets(); saveCategories();
    }

    // Incomes
    public void saveIncomes() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(userFile("incomes")))) {
            for (Income i : getIncomes())
                pw.println(escape(i.type) + "|" + escape(i.source) + "|" + i.amount + "|" + escape(i.date));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadIncomes() {
        List<Income> list = new ArrayList<>();
        File f = new File(userFile("incomes"));
        if (!f.exists()) { allIncomes.put(currentUser, list); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", 4);
                if (p.length == 4) list.add(new Income(unescape(p[0]), unescape(p[1]),
                    Double.parseDouble(p[2]), unescape(p[3])));
            }
        } catch (Exception e) { e.printStackTrace(); }
        allIncomes.put(currentUser, list);
    }

    // Expenses
    public void saveExpenses() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(userFile("expenses")))) {
            for (Expense e : getExpenses())
                pw.println(escape(e.category) + "|" + e.amount + "|" + escape(e.date) + "|" + escape(e.note));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadExpenses() {
        List<Expense> list = new ArrayList<>();
        File f = new File(userFile("expenses"));
        if (!f.exists()) { allExpenses.put(currentUser, list); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", 4);
                if (p.length == 4) list.add(new Expense(unescape(p[0]),
                    Double.parseDouble(p[1]), unescape(p[2]), unescape(p[3])));
            }
        } catch (Exception e) { e.printStackTrace(); }
        allExpenses.put(currentUser, list);
    }

    // Budgets
    public void saveBudgets() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(userFile("budgets")))) {
            for (Map.Entry<String, Double> e : getBudgets().entrySet())
                pw.println(escape(e.getKey()) + "|" + e.getValue());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadBudgets() {
        Map<String, Double> map = new LinkedHashMap<>();
        File f = new File(userFile("budgets"));
        if (!f.exists()) { allBudgets.put(currentUser, map); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", 2);
                if (p.length == 2) map.put(unescape(p[0]), Double.parseDouble(p[1]));
            }
        } catch (Exception e) { e.printStackTrace(); }
        allBudgets.put(currentUser, map);
    }

    // Categories
    public void saveCategories() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(userFile("categories")))) {
            for (String c : getCategories()) pw.println(escape(c));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadCategories() {
        List<String> list = new ArrayList<>();
        File f = new File(userFile("categories"));
        if (!f.exists()) { allCategories.put(currentUser, new ArrayList<>(DEFAULT_CATS)); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String c = unescape(line.trim());
                if (!c.isEmpty()) list.add(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (list.isEmpty()) list = new ArrayList<>(DEFAULT_CATS);
        allCategories.put(currentUser, list);
    }

    /** Load all data for the current user from disk */
    public void loadUserData() {
        if (currentUser == null) return;
        loadIncomes(); loadExpenses(); loadBudgets(); loadCategories();
    }

    // ── Escape helpers (keep | safe in text) ─────────────────────────────────

    private String escape(String s)   { return s == null ? "" : s.replace("\\", "\\\\").replace("|", "\\p"); }
    private String unescape(String s) { return s == null ? "" : s.replace("\\p", "|").replace("\\\\", "\\"); }

    // ── In-memory accessors ───────────────────────────────────────────────────

    public List<Income> getIncomes() {
        return allIncomes.computeIfAbsent(currentUser, k -> new ArrayList<>());
    }
    public List<Expense> getExpenses() {
        return allExpenses.computeIfAbsent(currentUser, k -> new ArrayList<>());
    }
    public Map<String, Double> getBudgets() {
        return allBudgets.computeIfAbsent(currentUser, k -> new LinkedHashMap<>());
    }
    public List<String> getCategories() {
        List<String> cats = allCategories.computeIfAbsent(currentUser, k -> new ArrayList<>(DEFAULT_CATS));
        for (String d : DEFAULT_CATS) if (!cats.contains(d)) cats.add(0, d);
        return cats;
    }
    public void addCategory(String cat) {
        if (!getCategories().contains(cat)) { getCategories().add(cat); saveCategories(); }
    }

    // ── Totals ────────────────────────────────────────────────────────────────

    public double totalIncome()   { return getIncomes().stream().mapToDouble(i -> i.amount).sum(); }
    public double totalExpenses() { return getExpenses().stream().mapToDouble(e -> e.amount).sum(); }
    public double balance()       { return totalIncome() - totalExpenses(); }
    public double totalBudget()   { return getBudgets().values().stream().mapToDouble(Double::doubleValue).sum(); }
    public boolean isOverBudget() { return totalBudget() > 0 && totalExpenses() > totalBudget(); }

    // ── Models ────────────────────────────────────────────────────────────────

    public static class Income implements Serializable {
        public String type, source, date;
        public double amount;
        public Income(String type, String source, double amount, String date) {
            this.type = type; this.source = source; this.amount = amount; this.date = date;
        }
    }

    public static class Expense implements Serializable {
        public String category, date, note;
        public double amount;
        public Expense(String category, double amount, String date, String note) {
            this.category = category; this.amount = amount; this.date = date; this.note = note;
        }
    }
}
