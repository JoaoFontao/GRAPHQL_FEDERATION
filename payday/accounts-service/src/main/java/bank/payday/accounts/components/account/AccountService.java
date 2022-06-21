package bank.payday.accounts.components.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {


    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts(int customer, String type) {
        try {
            List<Account> accounts = new ArrayList<>();
            accountRepository.findByCustomerAndTypeAndStatusOrderByIdDesc(customer, type, 1).forEach(accounts::add);

            return accounts;
        } catch (Exception e) {
            throw new AccountException(e.getMessage());
        }

    }

    public Account getAccountById(Integer id) {
        Optional<Account> accountData = accountRepository.findById(id);

        if (accountData.isPresent()) {
            return accountData.get();
        } else {
            throw new AccountException("Invalid Account ID");
        }
    }

    public Map<String, List<Account>> getAccountsByCustomerAndTypeAndStatus(List<String> customer, String type, int status) {
        List<Integer> list = customer.stream().map(cust -> Integer.parseInt(cust)).collect(Collectors.toList());
        List<Account> accounts = this.accountRepository.findByCustomerInAndTypeAndStatusOrderByIdDesc(list, type, status);
        Map<String, List<Account>> response = new HashMap<>();

        customer.forEach(customerId -> {
            List<Account> customerAccounts = accounts.stream().filter(r -> r.getCustomer() == Integer.parseInt(customerId))
                    .collect(Collectors.toList());
            response.put(customerId, customerAccounts);
        });

        return response;
    }

    public Account disableAccountById(Integer id, Integer customer) {
        Optional<Account> accountData = accountRepository.findById(id);

        if (accountData.isPresent() && accountData.get().getCustomer() == customer) {
            Account account = accountData.get();
            account.setStatus(0);
            accountRepository.save(account);
            return account;
        } else {
            throw new AccountException("Invalid Account");
        }
    }


    public Account addNewAccount(Account account, String user) {
        Account u = new Account(account);

        Instant instant = Instant.now();
        long timeStamp = instant.toEpochMilli();
        u.setDate_of_creation((int) timeStamp);
        u.setStatus(1);

        // Generate random account number
        // It must be checked for unique
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        int count = 12;
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        String accn =  builder.toString();
        u.setAccn(accn);

        List<String> errors = u.validate();

        if (errors.isEmpty()) {
            String query = "{\n" + "    \"query\": \"query { customer { email  }}\"\n"
                    + "}";
            callSendMailService(query, user, u.getAccn());

            return accountRepository.save(u);

        } else {
            throw new AccountException("Invalid Account");
        }
    }

    public void callSendMailService(String query, String user, String accn){
        try {
            RestTemplate graphTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/json");
            headers.add("user", user);

            ResponseEntity<String> response = graphTemplate.postForEntity("http://localhost:9000/graphql", new HttpEntity<>(query, headers), String.class);

            String body = response.getBody();
            String email = body.substring(30, body.length() - 4);
            String sendMailUrl = "http://localhost:9003/sendmail";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headersRest = new HttpHeaders();
            headersRest.setContentType(MediaType.APPLICATION_JSON);
            JSONObject personJsonObject = new JSONObject();
            personJsonObject.put("to", email);
            personJsonObject.put("subject", "Account created");
            personJsonObject.put("message", "Your accn: " + accn + " accout has been created.");
            HttpEntity<String> request = new HttpEntity<String>(personJsonObject.toString(), headersRest);

            String personResultAsJsonStr = restTemplate.postForObject(sendMailUrl, request, String.class);
        }catch (Exception e){
            throw new AccountException(e.getMessage());
        }
    }

}