package com.corevent.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.corevent.entity.ParticipantInfo;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExportService {
    
    public enum ExportFormat {
        CSV, EXCEL, PDF
    }
    
    public byte[] exportToCSV(List<ParticipantInfo> participants) {
        StringBuilder csv = new StringBuilder();
        
        // Add headers
        csv.append("Participant ID,Full Name,Email,Phone Number,Institution,Ticket Status,Attendance Status,Registration Date,Payment Status,Amount Paid\n");
        
        // Add data rows
        for (ParticipantInfo participant : participants) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f\n",
                participant.getParticipantId(),
                participant.getFullName(),
                participant.getEmail(),
                participant.getPhoneNumber(),
                participant.getInstitution(),
                participant.getTicketStatus(),
                participant.getAttendanceStatus(),
                participant.getRegistrationDate(),
                participant.getPaymentStatus(),
                participant.getAmountPaid() != null ? participant.getAmountPaid() : 0.0
            ));
        }
        
        return csv.toString().getBytes();
    }
    
    public byte[] exportToExcel(List<ParticipantInfo> participants) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Participants");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Participant ID", "Full Name", "Email", "Phone Number", "Institution", 
                              "Ticket Status", "Attendance Status", "Registration Date", "Payment Status", "Amount Paid"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // Add data rows
            int rowNum = 1;
            for (ParticipantInfo participant : participants) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(participant.getParticipantId());
                row.createCell(1).setCellValue(participant.getFullName());
                row.createCell(2).setCellValue(participant.getEmail());
                row.createCell(3).setCellValue(participant.getPhoneNumber());
                row.createCell(4).setCellValue(participant.getInstitution());
                row.createCell(5).setCellValue(participant.getTicketStatus());
                row.createCell(6).setCellValue(participant.getAttendanceStatus());
                row.createCell(7).setCellValue(participant.getRegistrationDate().toString());
                row.createCell(8).setCellValue(participant.getPaymentStatus());
                row.createCell(9).setCellValue(participant.getAmountPaid() != null ? participant.getAmountPaid() : 0.0);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Error exporting to Excel", e);
            throw new RuntimeException("Error exporting to Excel", e);
        }
    }
    
    public byte[] exportToPDF(List<ParticipantInfo> participants) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add title
            document.add(new Paragraph("Event Participants List"));
            
            // Create table
            Table table = new Table(10);
            
            // Add headers
            String[] headers = {"Participant ID", "Full Name", "Email", "Phone Number", "Institution", 
                              "Ticket Status", "Attendance Status", "Registration Date", "Payment Status", "Amount Paid"};
            for (String header : headers) {
                table.addCell(new Cell().add(new Paragraph(header)));
            }
            
            // Add data rows
            for (ParticipantInfo participant : participants) {
                table.addCell(new Cell().add(new Paragraph(participant.getParticipantId())));
                table.addCell(new Cell().add(new Paragraph(participant.getFullName())));
                table.addCell(new Cell().add(new Paragraph(participant.getEmail())));
                table.addCell(new Cell().add(new Paragraph(participant.getPhoneNumber())));
                table.addCell(new Cell().add(new Paragraph(participant.getInstitution())));
                table.addCell(new Cell().add(new Paragraph(participant.getTicketStatus())));
                table.addCell(new Cell().add(new Paragraph(participant.getAttendanceStatus())));
                table.addCell(new Cell().add(new Paragraph(participant.getRegistrationDate().toString())));
                table.addCell(new Cell().add(new Paragraph(participant.getPaymentStatus())));
                table.addCell(new Cell().add(new Paragraph(
                    participant.getAmountPaid() != null ? String.format("%.2f", participant.getAmountPaid()) : "0.00"
                )));
            }
            
            document.add(table);
            document.close();
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Error exporting to PDF", e);
            throw new RuntimeException("Error exporting to PDF", e);
        }
    }
} 