package enigma.midtrans.service;

import enigma.midtrans.dto.TransactionDTO;
import enigma.midtrans.model.Transaction;

public interface TransactionService {
    Transaction create(TransactionDTO transactionDTO);

    Transaction findById(Integer id);
}
