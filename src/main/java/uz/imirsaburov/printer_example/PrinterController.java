package uz.imirsaburov.printer_example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.print.PrintException;
import javax.print.PrintService;
import java.io.IOException;
import java.util.List;

@RestController
public class PrinterController {

    private final PrinterService printerService;

    public PrinterController(PrinterService printerService) {
        this.printerService = printerService;
    }

    @GetMapping("/printers")
    public List<PrinterDTO> getPrinterList() {
        return printerService.getPrinterList();
    }


    @PostMapping("print")
    public void print(@RequestBody PrintRequest printRequest) throws PrintException, IOException {
        printerService.print(printRequest);
    }
}
