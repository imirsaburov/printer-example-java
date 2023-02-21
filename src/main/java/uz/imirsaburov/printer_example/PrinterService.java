package uz.imirsaburov.printer_example;

import gui.ava.html.image.generator.HtmlImageGenerator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.imageio.ImageIO;
import javax.print.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Service
public class PrinterService {

    @GetMapping("/printers")
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


    @PostMapping("print")
    public void print(@RequestBody PrintRequest printRequest) throws PrintException, IOException {

        PrintService byName = getByName(printRequest.printerName());

        DocPrintJob printJob = byName.createPrintJob();

        DocFlavor flavor = DocFlavor.BYTE_ARRAY.PNG;

        String htmlAsString = getHtmlAsString();

        byte[] pngFromHtml = convertFromHtmlToPng(htmlAsString);

        SimpleDoc simpleDoc = new SimpleDoc(pngFromHtml, flavor, null);

        printJob.print(simpleDoc, null);
    }

    private String getHtmlAsString() throws IOException {
        return Files.readString(new ClassPathResource("/print.html").getFile().toPath());
    }

    private byte[] convertFromHtmlToPng(String html) throws IOException {
        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
        imageGenerator.loadHtml(html);
        BufferedImage bufferedImage = imageGenerator.getBufferedImage();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
