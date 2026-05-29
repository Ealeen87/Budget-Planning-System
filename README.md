# Budget Planning System

A comprehensive, feature-rich desktop application built using **Java** and **JavaFX** to help users track income, manage expenses, and visualize their financial goals in real-time. This application features a modern UI, real-time budget tracking, and an automated alert system to encourage financial discipline.

---

## Project Structure & Architecture

The project follows a clean object-oriented design and is organized into logical folders for easy maintenance and scalability:

* **`src/`**: The core source folder containing all the Java logic and packages:
  * **`application`**: Contains `Main.java` which launches the JavaFX lifecycle and applies the global styles (`application.css`).
  * **`controllers`**: Contains the controller classes (`DashboardController.java`, `MainController.java`) handling the UI logic and user interactions.
  * **`models`**: Houses the data structures (`Account.java`, `Budget.java`, `Expense.java`, `Income.java`) that manage the application's underlying data.
  * **`views`**: Contains the modular JavaFX design layout classes (`DashboardView.java`, `ExpenseView.java`, `IncomeView.java`, `NavbarView.java`, `ReportView.java`, `BudgetView.java`).
* **`lib/`**: Contains the external JavaFX library `.jar` files required to build and run the graphical user interface.

---

## Features

* **Secure Authentication Flow**: Simple sign-up and sign-in functionality allowing unique user accounts.
* **Smart Dashboard Overview**: A clean financial snapshot tracking Total Income, Expenses, and current Balance instantly.
* **Dynamic Budget Tracking**: Set strict budget limits for custom categories (like Food, Bills, Travel, and even unique ones like Crochet!).
* **Live Status Alerts**: Automated notifications that change dynamically based on spending habits, warning users instantly if a category goes **"Over Budget"**.
* **Rich Data Visualizations**: Interactive pie charts for expense distribution by category and comparison bar graphs for Income vs. Expense vs. Budget.
* **Monthly & Yearly Reports**: Detailed structured breakdowns of historical transactions to keep track of seasonal spending habits.

---

## Tech Stack

* **Language:** Java
* **GUI Framework:** JavaFX
* **Architecture:** Model-View-Controller (MVC) Pattern
* **Styling:** CSS (Cascading Style Sheets) for JavaFX

---

## Getting Started

### Prerequisites
Make sure you have the following installed on your machine:
* Java Development Kit (JDK) 11 or higher
* An IDE (Eclipse, IntelliJ IDEA, or NetBeans)

### How to Run the Project
1. Clone this repository to your local machine:
   ```bash
   git clone [https://github.com/YOUR_USERNAME/Budget-Planning-System.git](https://github.com/YOUR_USERNAME/Budget-Planning-System.git)
## License & Usage

This project is open for anyone to download, view, and run for personal or educational purposes! However, all original code, design assets, and architecture belong to the authors. Please provide proper attribution if you are referencing this work.
