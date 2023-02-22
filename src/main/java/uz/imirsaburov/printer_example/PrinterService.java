package uz.imirsaburov.printer_example;

import gui.ava.html.image.generator.HtmlImageGenerator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

@Service
public class PrinterService {

    static Logger logger = Logger.getLogger(PrinterService.class.getName());

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
            PrintService byName = getByName(printRequest.printerName());

            DocPrintJob printJob = byName.createPrintJob();

            DocFlavor flavor = DocFlavor.BYTE_ARRAY.PNG;

            String htmlAsString = getHtmlAsString("/print.html");

            byte[] pngFromHtml = convertFromHtmlToPng(htmlAsString);

            SimpleDoc simpleDoc = new SimpleDoc(pngFromHtml, flavor, null);

            printJob.addPrintJobListener(new MyPrintJobListener());

            printJob.print(simpleDoc, null);

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
