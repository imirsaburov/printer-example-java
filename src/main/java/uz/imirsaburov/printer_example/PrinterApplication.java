package uz.imirsaburov.printer_example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class PrinterApplication {
    public static void main(String[] args) throws PrinterException, IOException {
        SpringApplication.run(PrinterApplication.class, args);

    }
}
