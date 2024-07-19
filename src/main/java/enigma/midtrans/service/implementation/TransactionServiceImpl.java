package enigma.midtrans.service.implementation;

import enigma.midtrans.dto.*;
import enigma.midtrans.exceptions.TransactionNotFoundException;
import enigma.midtrans.model.Transaction;
import enigma.midtrans.model.TransactionStatus;
import enigma.midtrans.model.User;
import enigma.midtrans.repository.TransactionRepository;
import enigma.midtrans.service.TransactionService;
import enigma.midtrans.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final RestClient restClient;
    private final ExecutorService executorService;

    @Override
    public Transaction create(TransactionDTO transactionDTO) {
        User foundUser = userService.findById(transactionDTO.getUserId());

        Transaction createdTransaction = transactionRepository.save(Transaction.builder()
                .user(foundUser)
                .amount(transactionDTO.getAmount())
                .status(TransactionStatus.NOT_CHARGED)
                .build()
        );
        TransactionDetails transactionDetails = TransactionDetails.builder()
                .order_id(String.format("test-%d", createdTransaction.getId()))
                .gross_amount(transactionDTO.getAmount())
                .build();
        CustomerDetails customerDetails = CustomerDetails.builder()
                .first_name(foundUser.getName())
                .email(foundUser.getEmail())
                .phone(foundUser.getPhone())
                .build();
        PageExpiry pageExpiry = PageExpiry.builder()
                .duration(5)
                .unit("minutes")
                .build();

        CreateTransactionResponse response = restClient
                .post()
                .uri("https://app.sandbox.midtrans.com/snap/v1/transactions")
                .header("Authorization", "Basic U0ItTWlkLXNlcnZlci1QOVcxQWtKc29rMmFQQ3BfcHdzZ05iVVA6")
                .body(TransactionBody.builder()
                        .transaction_details(transactionDetails)
                        .customer_details(customerDetails)
                        .page_expiry(pageExpiry)
                        .enabled_payments(List.of("credit_card"))
                        .build()
                )
                .retrieve()
                .body(CreateTransactionResponse.class);
        if (response != null) {
            createdTransaction.setRedirectUrl(response.getRedirect_url());
            transactionRepository.save(createdTransaction);
        }

        return createdTransaction;
    }

    @Override
    public Transaction findById(Integer id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }
}
