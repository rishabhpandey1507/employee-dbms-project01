// package com.dbms.controller;

// public class ExportServlet {
    
// }
package com.dbms.controller;

import com.dbms.dao.EmployeeDAO;
import com.dbms.model.Employee;

// iText imports for PDF
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// Apache POI imports for Excel
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Servlet imports
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Java standard imports
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/export/*")
public class ExportServlet extends HttpServlet {
    private EmployeeDAO employeeDAO;

    @Override
    public void init() throws ServletException {
        employeeDAO = new EmployeeDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Export type required");
            return;
        }
        
        List<Employee> employees = employeeDAO.getAllEmployees();
        
        try {
            if (pathInfo.equals("/excel")) {
                exportToExcel(response, employees);
            } else if (pathInfo.equals("/pdf")) {
                exportToPDF(response, employees);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid export type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Error generating export: " + e.getMessage());
        }
    }

    // Export to Excel
    private void exportToExcel(HttpServletResponse response, List<Employee> employees) 
            throws IOException {
        
        System.out.println("📊 Exporting to Excel - " + employees.size() + " employees");
        
        // Set response headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", 
            "attachment; filename=employees_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx");

        // Create workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Use FULL PACKAGE NAME to avoid conflict with iText Font
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Department", "Position", "Salary (₹)", 
                           "Email", "Phone", "Hire Date"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (Employee emp : employees) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(emp.getId());
            row.createCell(1).setCellValue(emp.getName());
            row.createCell(2).setCellValue(emp.getDepartment());
            row.createCell(3).setCellValue(emp.getPosition());
            row.createCell(4).setCellValue(emp.getSalary().doubleValue());
            row.createCell(5).setCellValue(emp.getEmail());
            row.createCell(6).setCellValue(emp.getPhone());
            row.createCell(7).setCellValue(emp.getHireDate().toString());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to response
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
        
        System.out.println("✅ Excel export completed successfully!");
    }

    // Export to PDF
    private void exportToPDF(HttpServletResponse response, List<Employee> employees) 
            throws IOException, DocumentException {
        
        System.out.println("📄 Exporting to PDF - " + employees.size() + " employees");
        
        // Set response headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", 
            "attachment; filename=employees_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf");

        // Create document
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, response.getOutputStream());
        
        document.open();

        // Add title - Use FULL PACKAGE NAME to avoid conflict with POI Font
        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Employee Database Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Add date
        com.itextpdf.text.Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph date = new Paragraph("Generated on: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new Date()), dateFont);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(20);
        document.add(date);

        // Create table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Set column widths
        float[] columnWidths = {1f, 2f, 2f, 2f, 1.5f, 2.5f, 2f, 2f};
        table.setWidths(columnWidths);

        // Add headers
        com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        String[] headers = {"ID", "Name", "Department", "Position", "Salary (₹)", 
                           "Email", "Phone", "Hire Date"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(198, 119, 0)); // Orange color matching theme
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Add data
        com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Employee emp : employees) {
            table.addCell(new Phrase(String.valueOf(emp.getId()), cellFont));
            table.addCell(new Phrase(emp.getName(), cellFont));
            table.addCell(new Phrase(emp.getDepartment(), cellFont));
            table.addCell(new Phrase(emp.getPosition(), cellFont));
            table.addCell(new Phrase("₹" + emp.getSalary().toString(), cellFont));
            table.addCell(new Phrase(emp.getEmail(), cellFont));
            table.addCell(new Phrase(emp.getPhone(), cellFont));
            table.addCell(new Phrase(emp.getHireDate().toString(), cellFont));
        }

        document.add(table);

        // Add footer
        Paragraph footer = new Paragraph("Total Employees: " + employees.size(), dateFont);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
        
        System.out.println("✅ PDF export completed successfully!");
    }
}
