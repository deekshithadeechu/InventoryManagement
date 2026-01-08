# Smart Inventory Management System

A modern, feature-rich Inventory Management System built with **Java 17+**, **JavaFX**, and **MySQL** featuring a beautiful Material Design UI, dark mode support, and enterprise-grade functionality.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 🌟 Features

### 🔐 Authentication
- Secure login and signup
- BCrypt password hashing
- Role-based access control (Admin/Staff)
- Session management

### 📦 Inventory Management
- Full CRUD operations for products
- Search and filter by name, SKU, category, supplier
- Real-time stock updates
- Stock adjustment with logging

### 🚨 Alert System
- Low-stock alerts (configurable threshold)
- Expiry alerts (7 days before expiry)
- Visual badges and notifications
- Alert severity levels

### 📊 Dashboard
- Statistics cards with live data
- Category distribution pie chart
- Stock level bar chart
- Recent activity feed
- Low stock & expiring items lists

### 🧾 Reports
- PDF export with styled tables
- CSV export for data analysis
- Filtered report generation
- Quick export options

### 🎨 Modern UI
- Material Design inspired
- Dark mode support
- Smooth animations
- Responsive layouts
- Icon-based navigation

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17+ |
| UI Framework | JavaFX 21 |
| Database | MySQL 8.0 |
| Connection Pool | HikariCP |
| Password Hashing | BCrypt |
| PDF Generation | iText |
| CSV Export | OpenCSV |
| Logging | SLF4J + Logback |
| Icons | Ikonli (FontAwesome5, MaterialDesign2) |
| Build Tool | Maven |

## 📁 Project Structure

```
InventoryManagementSystem/
├── pom.xml
├── sql/
│   └── schema.sql
├── src/main/java/com/inventory/
│   ├── Main.java
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── SignupController.java
│   │   ├── MainController.java
│   │   ├── DashboardController.java
│   │   ├── ProductController.java
│   │   ├── ReportsController.java
│   │   └── AlertsController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── ProductService.java
│   │   ├── AlertService.java
│   │   └── DashboardService.java
│   ├── dao/
│   │   ├── UserDAO.java, UserDAOImpl.java
│   │   ├── ProductDAO.java, ProductDAOImpl.java
│   │   ├── CategoryDAO.java, CategoryDAOImpl.java
│   │   ├── SupplierDAO.java, SupplierDAOImpl.java
│   │   └── InventoryLogDAO.java, InventoryLogDAOImpl.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Category.java
│   │   ├── Supplier.java
│   │   └── InventoryLog.java
│   └── util/
│       ├── DatabaseUtil.java
│       ├── PasswordUtil.java
│       ├── SessionManager.java
│       ├── AlertUtil.java
│       └── ReportGenerator.java
├── src/main/resources/
│   ├── views/
│   │   ├── LoginView.fxml
│   │   ├── SignupView.fxml
│   │   ├── MainView.fxml
│   │   ├── DashboardView.fxml
│   │   ├── ProductsView.fxml
│   │   ├── ReportsView.fxml
│   │   ├── AlertsView.fxml
│   │   └── ...
│   ├── css/
│   │   └── styles.css
│   ├── database.properties
│   └── logback.xml
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- **Java JDK 17+** - [Download](https://adoptium.net/)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)

### Database Setup

1. Start MySQL server
2. Run the database schema:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
3. Update `src/main/resources/database.properties` with your MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/inventory_db
   db.username=root
   db.password=your_password
   ```

### Running the Application

1. Clone the repository or navigate to the project directory:
   ```bash
   cd InventoryManagementSystem
   ```

2. Build the project:
   ```bash
   mvn clean compile
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```


### Getting Started with Login

**Recommended**: Click "Create one" on the login screen to sign up with your own account.

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |

> **Note**: If the default admin credentials don't work, simply create a new account using the Sign Up feature. All new accounts are created as Staff by default.

## 📸 Screenshots

### Login Screen
Modern glassmorphism login card with gradient background.

### Dashboard
Statistics cards, charts, and activity feeds at a glance.

### Products Management
Full-featured table with search, filters, and inline actions.

### Alerts
Visual alert cards with severity badges and product details.

## 🔧 Configuration

### Database Configuration
Edit `src/main/resources/database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/inventory_db
db.username=root
db.password=your_password
```

### Alert Thresholds
Modify `sql/schema.sql` or update via the application:
```sql
UPDATE alert_settings SET setting_value = '15' WHERE setting_key = 'low_stock_threshold';
UPDATE alert_settings SET setting_value = '14' WHERE setting_key = 'expiry_alert_days';
```

## 🏗️ Architecture

The application follows **MVC + DAO** architectural pattern:

- **Model**: Java POJOs representing database entities
- **View**: FXML files with CSS styling
- **Controller**: JavaFX controllers handling UI logic
- **Service**: Business logic layer
- **DAO**: Data Access Objects for database operations
- **Util**: Utility classes (Database, Password, Session, Alerts, Reports)

## 📝 Database Schema

```
┌─────────────────┐     ┌─────────────────┐
│     users       │     │   categories    │
├─────────────────┤     ├─────────────────┤
│ id              │     │ id              │
│ username        │     │ name            │
│ email           │     │ description     │
│ password_hash   │     │ color           │
│ role            │     └────────┬────────┘
└─────────────────┘              │
                                 │
┌─────────────────┐     ┌────────┴────────┐
│   suppliers     │     │    products     │
├─────────────────┤     ├─────────────────┤
│ id              │────▶│ id              │
│ name            │     │ sku             │
│ contact_person  │     │ name            │
│ email           │     │ category_id     │◀────┐
│ phone           │     │ supplier_id     │     │
└─────────────────┘     │ quantity        │     │
                        │ price           │     │
                        │ expiry_date     │     │
                        └────────┬────────┘     │
                                 │              │
                        ┌────────┴────────┐     │
                        │ inventory_logs  │     │
                        ├─────────────────┤     │
                        │ id              │     │
                        │ product_id      │─────┘
                        │ user_id         │
                        │ action          │
                        │ quantity_change │
                        └─────────────────┘
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- JavaFX community
- Material Design inspiration
- Open source libraries used in this project

---

**Built using Java, JavaFX, and MySQL**
