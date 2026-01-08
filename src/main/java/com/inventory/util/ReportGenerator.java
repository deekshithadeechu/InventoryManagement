package com.inventory.util;

import com.inventory.model.Product;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating PDF and CSV reports.
 */
public class ReportGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    
    // PDF Colors
    private static final BaseColor PRIMARY_COLOR = new BaseColor(52, 152, 219);
    private static final BaseColor HEADER_COLOR = new BaseColor(44, 62, 80);
    private static final BaseColor ALTERNATE_ROW = new BaseColor(245, 245, 245);
    
    // Fonts
    private static Font titleFont;
    private static Font headerFont;
    private static Font normalFont;
    private static Font boldFont;
    
    static {
        try {
            titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, HEADER_COLOR);
            headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
            boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
        } catch (Exception e) {
            logger.error("Error initializing fonts", e);
        }
    }
    
    /**
     * Generates a PDF inventory report.
     * 
     * @param products List of products to include in the report
     * @param file The output file
     * @param reportTitle The title of the report
     * @throws Exception if report generation fails
     */
    public static void generatePdfReport(List<Product> products, File file, String reportTitle) 
            throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            
            // Add header
            addPdfHeader(document, reportTitle);
            
            // Add summary section
            addPdfSummary(document, products);
            
            // Add product table
            addProductTable(document, products);
            
            // Add footer
            addPdfFooter(document);
            
            logger.info("PDF report generated successfully: {}", file.getAbsolutePath());
            
        } finally {
            document.close();
        }
    }
    
    /**
     * Adds the header section to the PDF.
     */
    private static void addPdfHeader(Document document, String title) throws DocumentException {
        Paragraph header = new Paragraph();
        header.setAlignment(Element.ALIGN_CENTER);
        
        // Title
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        header.add(titlePara);
        
        // Subtitle with date
        Paragraph datePara = new Paragraph(
            "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")),
            normalFont
        );
        datePara.setAlignment(Element.ALIGN_CENTER);
        datePara.setSpacingBefore(10);
        header.add(datePara);
        
        // Company info
        Paragraph companyPara = new Paragraph("Smart Inventory Management System", boldFont);
        companyPara.setAlignment(Element.ALIGN_CENTER);
        companyPara.setSpacingBefore(5);
        header.add(companyPara);
        
        header.setSpacingAfter(30);
        document.add(header);
        
        // Divider line
        LineSeparator line = new LineSeparator();
        line.setLineColor(PRIMARY_COLOR);
        document.add(new Chunk(line));
    }
    
    /**
     * Adds summary statistics to the PDF.
     */
    private static void addPdfSummary(Document document, List<Product> products) 
            throws DocumentException {
        Paragraph summary = new Paragraph();
        summary.setSpacingBefore(20);
        summary.setSpacingAfter(20);
        
        int totalProducts = products.size();
        int lowStockCount = (int) products.stream().filter(Product::isLowStock).count();
        int expiringSoonCount = (int) products.stream().filter(p -> p.isExpiringSoon(7)).count();
        double totalValue = products.stream()
                .mapToDouble(p -> p.getPrice().doubleValue() * p.getQuantity())
                .sum();
        
        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(10);
        
        addSummaryCell(summaryTable, "Total Products", String.valueOf(totalProducts));
        addSummaryCell(summaryTable, "Low Stock Items", String.valueOf(lowStockCount));
        addSummaryCell(summaryTable, "Expiring Soon", String.valueOf(expiringSoonCount));
        addSummaryCell(summaryTable, "Total Value", String.format("$%.2f", totalValue));
        
        document.add(summaryTable);
    }
    
    /**
     * Adds a summary cell to the table.
     */
    private static void addSummaryCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(PRIMARY_COLOR);
        cell.setPadding(15);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", normalFont));
        p.add(new Chunk(value, new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, PRIMARY_COLOR)));
        p.setAlignment(Element.ALIGN_CENTER);
        
        cell.addElement(p);
        table.addCell(cell);
    }
    
    /**
     * Adds the product table to the PDF.
     */
    private static void addProductTable(Document document, List<Product> products) 
            throws DocumentException {
        Paragraph tableTitle = new Paragraph("Product Details", boldFont);
        tableTitle.setSpacingBefore(20);
        tableTitle.setSpacingAfter(10);
        document.add(tableTitle);
        
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2.5f, 1.5f, 1.5f, 1, 1, 1.5f, 1.5f});
        
        // Header row
        String[] headers = {"SKU", "Name", "Category", "Supplier", "Qty", "Price", "Expiry", "Status"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
        
        // Data rows
        boolean alternate = false;
        for (Product product : products) {
            BaseColor rowColor = alternate ? ALTERNATE_ROW : BaseColor.WHITE;
            
            addDataCell(table, product.getSku(), rowColor);
            addDataCell(table, product.getName(), rowColor);
            addDataCell(table, product.getCategoryName() != null ? product.getCategoryName() : "N/A", rowColor);
            addDataCell(table, product.getSupplierName() != null ? product.getSupplierName() : "N/A", rowColor);
            addDataCell(table, String.valueOf(product.getQuantity()), rowColor);
            addDataCell(table, String.format("$%.2f", product.getPrice()), rowColor);
            addDataCell(table, product.getExpiryDate() != null ? product.getExpiryDate().toString() : "N/A", rowColor);
            
            // Status cell with color
            PdfPCell statusCell = new PdfPCell(new Phrase(product.getStockStatus(), normalFont));
            statusCell.setBackgroundColor(rowColor);
            statusCell.setPadding(6);
            if (product.isOutOfStock()) {
                statusCell.setBackgroundColor(new BaseColor(231, 76, 60));
            } else if (product.isLowStock()) {
                statusCell.setBackgroundColor(new BaseColor(241, 196, 15));
            }
            table.addCell(statusCell);
            
            alternate = !alternate;
        }
        
        document.add(table);
    }
    
    /**
     * Adds a data cell to the table.
     */
    private static void addDataCell(PdfPTable table, String text, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, normalFont));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        table.addCell(cell);
    }
    
    /**
     * Adds the footer to the PDF.
     */
    private static void addPdfFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(30);
        
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        footer.add(new Chunk(line));
        
        Paragraph footerText = new Paragraph(
            "This report was generated automatically by Smart Inventory Management System",
            new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY)
        );
        footerText.setAlignment(Element.ALIGN_CENTER);
        footerText.setSpacingBefore(10);
        footer.add(footerText);
        
        document.add(footer);
    }
    
    /**
     * Generates a CSV inventory report.
     * 
     * @param products List of products to include in the report
     * @param file The output file
     * @throws IOException if report generation fails
     */
    public static void generateCsvReport(List<Product> products, File file) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // Header row
            String[] header = {
                "SKU", "Name", "Description", "Category", "Supplier",
                "Quantity", "Unit", "Price", "Cost Price", "Expiry Date",
                "Location", "Low Stock Threshold", "Status"
            };
            writer.writeNext(header);
            
            // Data rows
            for (Product product : products) {
                String[] row = {
                    product.getSku(),
                    product.getName(),
                    product.getDescription() != null ? product.getDescription() : "",
                    product.getCategoryName() != null ? product.getCategoryName() : "",
                    product.getSupplierName() != null ? product.getSupplierName() : "",
                    String.valueOf(product.getQuantity()),
                    product.getUnit(),
                    product.getPrice().toString(),
                    product.getCostPrice() != null ? product.getCostPrice().toString() : "",
                    product.getExpiryDate() != null ? product.getExpiryDate().toString() : "",
                    product.getLocation() != null ? product.getLocation() : "",
                    String.valueOf(product.getLowStockThreshold()),
                    product.getStockStatus()
                };
                writer.writeNext(row);
            }
            
            logger.info("CSV report generated successfully: {}", file.getAbsolutePath());
        }
    }
    
    /**
     * Generates a low stock alert report.
     */
    public static void generateLowStockReport(List<Product> products, File file) throws Exception {
        List<Product> lowStockProducts = products.stream()
                .filter(Product::isLowStock)
                .toList();
        
        generatePdfReport(lowStockProducts, file, "Low Stock Alert Report");
    }
    
    /**
     * Generates an expiring products report.
     */
    public static void generateExpiryReport(List<Product> products, File file, int days) 
            throws Exception {
        List<Product> expiringProducts = products.stream()
                .filter(p -> p.isExpiringSoon(days))
                .toList();
        
        generatePdfReport(expiringProducts, file, "Products Expiring Within " + days + " Days");
    }
}
