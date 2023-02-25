package uz.imirsaburov.printer_example;

import com.itextpdf.html2pdf.HtmlConverter;
import gui.ava.html.image.generator.HtmlImageGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

@Service
public class PrinterService {


    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final static NumberFormat nf = NumberFormat.getInstance(new Locale("sk", "SK"));

    public List<PrinterDTO> getPrinterList() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        return Arrays.stream(printServices).map(this::toDTO).toList();
    }

    private PrinterDTO toDTO(PrintService service) {

        return new PrinterDTO(
                service.getName()
        );
    }

    private PrintService getByName(String name) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        return Arrays.stream(printServices).filter(e -> e.getName().equals(name)).toList().get(0);
    }


    public void print(@RequestBody PrintRequest printRequest) {

        try {

            LocalDateTime now = LocalDateTime.now();


            ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();

            String htmlAsString = getHtmlAsString("/print.html")
                    .replace("$argCheckNumber", RandomStringUtils.random(6, "0123456789"))
                    .replace("$argDate", dateFormatter.format(now))
                    .replace("$argTime", timeFormatter.format(now))
                    .replace("$argPrice", nf.format(printRequest.sum()));

            HtmlConverter.convertToPdf(htmlAsString, pdfOutput);

            PDDocument document = PDDocument.load(pdfOutput.toByteArray());

            PrintService myPrintService = getByName(printRequest.printerName());

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(myPrintService);
            job.setPageable(new PDFPageable(document));
            Paper paper = new Paper();
            paper.setSize(306, 396);
            paper.setImageableArea(-25, 0, paper.getWidth(), paper.getHeight());
            PageFormat pageFormat = new PageFormat();
            pageFormat.setPaper(paper);
            Book book = new Book();
            book.append(new PDFPrintable(document), pageFormat, document.getNumberOfPages());
            job.setPageable(book);
            job.print();

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private String getHtmlAsString(String resourcePath) throws IOException {
        InputStream inputStream = new ClassPathResource(resourcePath).getInputStream();
        String text = null;
        try (Scanner scanner = new Scanner(inputStream)) {
            text = scanner.useDelimiter("\\A").next();
        }
        return text;
    }

    private InputStream getHtmlInputStream(String resourcePath) throws IOException {
        return new ClassPathResource(resourcePath).getInputStream();
    }

    private byte[] convertFromHtmlToPng(String html) throws IOException {
        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
        imageGenerator.loadHtml(html);
        BufferedImage bufferedImage = imageGenerator.getBufferedImage();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    static class MyPrintJobListener implements PrintJobListener {
        public void printDataTransferCompleted(PrintJobEvent pje) {
            System.out.println("printDataTransferCompleted");
        }

        public void printJobCanceled(PrintJobEvent pje) {
            System.out.println("The print job was cancelled");
        }

        public void printJobCompleted(PrintJobEvent pje) {
            System.out.println("The print job was completed");
        }

        public void printJobFailed(PrintJobEvent pje) {
            System.out.println("The print job has failed");
        }

        public void printJobNoMoreEvents(PrintJobEvent pje) {
        }

        public void printJobRequiresAttention(PrintJobEvent pje) {
        }
    }
}
