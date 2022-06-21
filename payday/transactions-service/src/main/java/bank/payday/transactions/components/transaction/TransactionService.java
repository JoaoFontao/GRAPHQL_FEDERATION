package bank.payday.transactions.components.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> getAllCustomerTransactions(int account, int customer) {
        return transactionRepository.findByAccountAndCustomerOrderByDateOfTransactionDesc(account, customer);
    }

    public Transaction getTransaction(String id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if(transaction.isEmpty()) {
            throw new TransactionException("Invalid Transaction ID");
        }
        return transaction.get();

    }

    public boolean validateAccount(String account) {
        String query = "{\n" + "    \"query\": \"query { account(id:"+ account + ") { id  }}\"\n"
                + "}";
        String body = callGraphQLAccountService(query);
        String expectedResponse = "{\"data\":{\"account\":{\"id\":\"" + account + "\"}}}";
        if(body.equals(expectedResponse)){
            return true;
        }
        throw new TransactionException("Invalid Account");
    }


    public String callGraphQLAccountService(String query){
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/json");

            ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:9001/graphql", new HttpEntity<>(query, headers), String.class);
            if(response.getBody() == null){
                throw new TransactionException("Invalid Account");
            }
            if(response.getBody().isEmpty() || response.getBody().contains("null")){
                throw new TransactionException("Invalid Account");
            }
            return response.getBody();
        }catch (Exception e){
            throw new TransactionException(e.getMessage());
        }
    }

    public Transaction createNewTransaction(int account, int customer, int amount, String description) {

        Instant instant = Instant.now();
        long timeStamp = instant.toEpochMilli();
        Transaction transaction = new Transaction(customer, account, timeStamp, amount, description);


        // !!! We will assume that posted data already validated.
        if (transaction.getCustomer() < 1) return null; // here can be sent a request to customers service for validation else.
        if (transaction.getAccount() < 1)  return null; // here can be sent a request to customers and account services to validate that account belongs to given customer.
        if (transaction.getAmount() <= 0)   return null;

        return transactionRepository.save(new Transaction(transaction));
    }

    public Map<String, List<Transaction>> getTransactionsByAccounts(List<String> accounts) {
        List<Integer> list = accounts.stream().map(accont -> Integer.parseInt(accont)).collect(Collectors.toList());
        List<Transaction> transactions = this.transactionRepository.findByAccountIn(list);
        Map<String, List<Transaction>> response = new HashMap<>();
        accounts.forEach(accountId -> {
            List<Transaction> accountsTransaction = transactions.stream().filter(r -> r.getAccount() == Integer.parseInt(accountId))
                    .collect(Collectors.toList());
            response.put(accountId, accountsTransaction);
        });

        return response;
    }

}
