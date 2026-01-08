package com.inventory.controller;

import com.inventory.dao.CategoryDAO;
import com.inventory.dao.CategoryDAOImpl;
import com.inventory.dao.SupplierDAO;
import com.inventory.dao.SupplierDAOImpl;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.service.ProductService;
import com.inventory.util.AlertUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the products view.
 */
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> skuColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> supplierColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, BigDecimal> priceColumn;
    @FXML private TableColumn<Product, String> statusColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private ComboBox<Supplier> supplierFilter;
    @FXML private ComboBox<String> statusFilter;
    
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Label totalLabel;
    
    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAOImpl();
    private final SupplierDAO supplierDAO = new SupplierDAOImpl();
    
    private ObservableList<Product> productList;
    private FilteredList<Product> filteredProducts;
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        Platform.runLater(this::loadProducts);
    }
    
    /**
     * Sets up table columns.
     */
    private void setupTableColumns() {
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getCategoryName() != null ? 
                data.getValue().getCategoryName() : "N/A"));
        supplierColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getSupplierName() != null ? 
                data.getValue().getSupplierName() : "N/A"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Status column with badges
        statusColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStockStatus()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("status-badge");
                    if (status.equals("Out of Stock")) {
                        badge.getStyleClass().add("badge-danger");
                    } else if (status.equals("Low Stock")) {
                        badge.getStyleClass().add("badge-warning");
                    } else {
                        badge.getStyleClass().add("badge-success");
                    }
                    setGraphic(badge);
                }
            }
        });
        
        // Actions column
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button stockBtn = new Button("📊");
            private final HBox buttons = new HBox(5, editBtn, stockBtn, deleteBtn);
            
            {
                editBtn.getStyleClass().add("action-btn");
                deleteBtn.getStyleClass().add("action-btn");
                stockBtn.getStyleClass().add("action-btn");
                
                editBtn.setTooltip(new Tooltip("Edit Product"));
                deleteBtn.setTooltip(new Tooltip("Delete Product"));
                stockBtn.setTooltip(new Tooltip("Adjust Stock"));
                
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                stockBtn.setOnAction(e -> handleAdjustStock(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        // Price formatting
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });
        
        // Quantity with color
        quantityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(quantity));
                    Product product = getTableView().getItems().get(getIndex());
                    if (product.isOutOfStock()) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (product.isLowStock()) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        });
    }
    
    /**
     * Sets up filter controls.
     */
    private void setupFilters() {
        // Load categories
        List<Category> categories = categoryDAO.findAllActive();
        categoryFilter.getItems().add(null); // "All" option
        categoryFilter.getItems().addAll(categories);
        categoryFilter.setPromptText("All Categories");
        
        // Load suppliers
        List<Supplier> suppliers = supplierDAO.findAllActive();
        supplierFilter.getItems().add(null);
        supplierFilter.getItems().addAll(suppliers);
        supplierFilter.setPromptText("All Suppliers");
        
        // Status options
        statusFilter.getItems().addAll("All", "In Stock", "Low Stock", "Out of Stock");
        statusFilter.setValue("All");
        
        // Filter listeners
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        categoryFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        supplierFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, old, newVal) -> applyFilters());
    }
    
    /**
     * Loads products from database.
     */
    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        productList = FXCollections.observableArrayList(products);
        filteredProducts = new FilteredList<>(productList, p -> true);
        productsTable.setItems(filteredProducts);
        updateTotalLabel();
        logger.debug("Loaded {} products", products.size());
    }
    
    /**
     * Applies current filters.
     */
    private void applyFilters() {
        filteredProducts.setPredicate(product -> {
            // Search filter
            String search = searchField.getText();
            if (search != null && !search.isEmpty()) {
                String lower = search.toLowerCase();
                if (!product.getName().toLowerCase().contains(lower) &&
                    !product.getSku().toLowerCase().contains(lower) &&
                    (product.getDescription() == null || 
                     !product.getDescription().toLowerCase().contains(lower))) {
                    return false;
                }
            }
            
            // Category filter
            Category category = categoryFilter.getValue();
            if (category != null && 
                (product.getCategoryId() == null || 
                 product.getCategoryId() != category.getId())) {
                return false;
            }
            
            // Supplier filter
            Supplier supplier = supplierFilter.getValue();
            if (supplier != null && 
                (product.getSupplierId() == null || 
                 product.getSupplierId() != supplier.getId())) {
                return false;
            }
            
            // Status filter
            String status = statusFilter.getValue();
            if (status != null && !status.equals("All")) {
                if (!product.getStockStatus().equals(status)) {
                    return false;
                }
            }
            
            return true;
        });
        
        updateTotalLabel();
    }
    
    /**
     * Updates the total label.
     */
    private void updateTotalLabel() {
        if (totalLabel != null) {
            totalLabel.setText("Showing " + filteredProducts.size() + " of " + productList.size() + " products");
        }
    }
    
    /**
     * Handles add product button.
     */
    @FXML
    private void handleAddProduct() {
        showProductDialog(null);
    }
    
    /**
     * Handles edit product.
     */
    private void handleEdit(Product product) {
        showProductDialog(product);
    }
    
    /**
     * Handles delete product.
     */
    private void handleDelete(Product product) {
        if (AlertUtil.showConfirmation("Delete Product", 
            "Are you sure you want to delete '" + product.getName() + "'?\n\nThis action cannot be undone.")) {
            
            ProductService.ServiceResult<Void> result = productService.deleteProduct(product.getId());
            
            if (result.isSuccess()) {
                AlertUtil.showSuccess("Success", "Product deleted successfully");
                loadProducts();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        }
    }
    
    /**
     * Handles stock adjustment.
     */
    private void handleAdjustStock(Product product) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Adjust Stock");
        dialog.setHeaderText("Adjust stock for: " + product.getName());
        
        ButtonType adjustButtonType = new ButtonType("Adjust", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(adjustButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));
        
        Label currentLabel = new Label("Current Stock: " + product.getQuantity() + " " + product.getUnit());
        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter adjustment (+ or -)");
        TextArea reasonField = new TextArea();
        reasonField.setPromptText("Reason for adjustment");
        reasonField.setPrefRowCount(2);
        
        grid.add(currentLabel, 0, 0, 2, 1);
        grid.add(new Label("Adjustment:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Reason:"), 0, 2);
        grid.add(reasonField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == adjustButtonType) {
                try {
                    return Integer.parseInt(quantityField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(adjustment -> {
            if (adjustment != null) {
                ProductService.ServiceResult<Product> result = 
                    productService.adjustStock(product.getId(), adjustment, reasonField.getText());
                
                if (result.isSuccess()) {
                    AlertUtil.showSuccess("Success", "Stock adjusted successfully");
                    loadProducts();
                } else {
                    AlertUtil.showError("Error", result.getMessage());
                }
            } else {
                AlertUtil.showError("Invalid Input", "Please enter a valid number");
            }
        });
    }
    
    /**
     * Shows product add/edit dialog.
     */
    private void showProductDialog(Product product) {
        boolean isEdit = product != null;
        
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Product" : "Add Product");
        dialog.setHeaderText(isEdit ? "Edit product details" : "Enter product details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));
        
        TextField skuField = new TextField(isEdit ? product.getSku() : "");
        TextField nameField = new TextField(isEdit ? product.getName() : "");
        TextArea descField = new TextArea(isEdit && product.getDescription() != null ? product.getDescription() : "");
        descField.setPrefRowCount(2);
        ComboBox<Category> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categoryDAO.findAllActive()));
        ComboBox<Supplier> supplierBox = new ComboBox<>(FXCollections.observableArrayList(supplierDAO.findAllActive()));
        TextField quantityField = new TextField(isEdit ? String.valueOf(product.getQuantity()) : "0");
        TextField priceField = new TextField(isEdit ? product.getPrice().toString() : "0.00");
        TextField thresholdField = new TextField(isEdit ? String.valueOf(product.getLowStockThreshold()) : "10");
        DatePicker expiryPicker = new DatePicker(isEdit ? product.getExpiryDate() : null);
        TextField locationField = new TextField(isEdit && product.getLocation() != null ? product.getLocation() : "");
        
        if (isEdit) {
            categoryBox.getItems().stream()
                .filter(c -> c.getId() == product.getCategoryId())
                .findFirst().ifPresent(categoryBox::setValue);
            supplierBox.getItems().stream()
                .filter(s -> s.getId() == product.getSupplierId())
                .findFirst().ifPresent(supplierBox::setValue);
        }
        
        int row = 0;
        grid.add(new Label("SKU:*"), 0, row);
        grid.add(skuField, 1, row++);
        grid.add(new Label("Name:*"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row);
        grid.add(descField, 1, row++);
        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryBox, 1, row++);
        grid.add(new Label("Supplier:"), 0, row);
        grid.add(supplierBox, 1, row++);
        grid.add(new Label("Quantity:*"), 0, row);
        grid.add(quantityField, 1, row++);
        grid.add(new Label("Price:*"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Low Stock Threshold:"), 0, row);
        grid.add(thresholdField, 1, row++);
        grid.add(new Label("Expiry Date:"), 0, row);
        grid.add(expiryPicker, 1, row++);
        grid.add(new Label("Location:"), 0, row);
        grid.add(locationField, 1, row++);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Product p = isEdit ? product : new Product();
                p.setSku(skuField.getText().trim());
                p.setName(nameField.getText().trim());
                p.setDescription(descField.getText().trim());
                if (categoryBox.getValue() != null) {
                    p.setCategoryId(categoryBox.getValue().getId());
                }
                if (supplierBox.getValue() != null) {
                    p.setSupplierId(supplierBox.getValue().getId());
                }
                try {
                    p.setQuantity(Integer.parseInt(quantityField.getText()));
                    p.setPrice(new BigDecimal(priceField.getText()));
                    p.setLowStockThreshold(Integer.parseInt(thresholdField.getText()));
                } catch (NumberFormatException e) {
                    AlertUtil.showError("Invalid Input", "Please enter valid numbers");
                    return null;
                }
                p.setExpiryDate(expiryPicker.getValue());
                p.setLocation(locationField.getText().trim());
                return p;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(p -> {
            ProductService.ServiceResult<Product> result;
            if (isEdit) {
                result = productService.updateProduct(p);
            } else {
                result = productService.createProduct(p);
            }
            
            if (result.isSuccess()) {
                AlertUtil.showSuccess("Success", isEdit ? "Product updated successfully" : "Product created successfully");
                loadProducts();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });
    }
    
    /**
     * Handles refresh button.
     */
    @FXML
    private void handleRefresh() {
        loadProducts();
        searchField.clear();
        categoryFilter.setValue(null);
        supplierFilter.setValue(null);
        statusFilter.setValue("All");
    }
    
    /**
     * Clears all filters.
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue(null);
        supplierFilter.setValue(null);
        statusFilter.setValue("All");
    }
}
