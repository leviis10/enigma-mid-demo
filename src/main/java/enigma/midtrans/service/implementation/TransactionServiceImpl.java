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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final RestClient restClient;
    private final ExecutorService executorService;
    @Value("${midtrans.serverkey}")
    private String midtransServerKey;

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
                .order_id(UUID.randomUUID().toString())
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
                .header("Authorization", "Basic " + midtransServerKey)
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
        executorService.submit(() -> updateTransactionStatus(
                createdTransaction.getId(),
                transactionDetails.getOrder_id(),
                transactionDTO.getUserId(),
                transactionDTO.getAmount()
        ));

        return createdTransaction;
    }

    @Override
    public Transaction findById(Integer id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    private void updateTransactionStatus(Integer id, String orderId, Integer userId, Integer amount) {
        for (int i = 0; i < 20; i++) {
            try {
                GetTransactionDetailResponse response = restClient
                        .get()
                        .uri(String.format("https://api.sandbox.midtrans.com/v2/%s/status", orderId))
                        .header("Authorization", "Basic " + midtransServerKey)
                        .retrieve()
                        .body(GetTransactionDetailResponse.class);
                if (response != null && "capture".equals(response.getTransaction_status())) {
                    Transaction foundTransaction = findById(id);
                    foundTransaction.setStatus(TransactionStatus.CHARGED);
                    transactionRepository.save(foundTransaction);
                    userService.updateBalance(userId, amount);
                    break;
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                log.error("error in updateTransactionStatus() {}", e.getMessage());
            }
        }
        log.info("Exiting updateTransactionStatus()");
    }
}
