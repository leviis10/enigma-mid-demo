package enigma.midtrans.controller;

import enigma.midtrans.dto.TransactionDTO;
import enigma.midtrans.model.Transaction;
import enigma.midtrans.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody TransactionDTO transactionDTO
    ) {
        Transaction createdTransaction = transactionService.create(transactionDTO);
        return ResponseEntity.status(201).body(createdTransaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findTransactionById(
            @PathVariable Integer id
    ) {
        Transaction foundTransaction = transactionService.findById(id);
        return ResponseEntity.ok(foundTransaction);
    }
}
