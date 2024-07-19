package enigma.midtrans.controller;

import enigma.midtrans.dto.InvoiceDTO;
import enigma.midtrans.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> createInvoice() {
        InvoiceDTO response = paymentService.createInvoice();
        return ResponseEntity.status(200).body(response);
    }
}
