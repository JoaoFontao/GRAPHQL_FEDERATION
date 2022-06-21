package bank.payday.accounts.components.account;

import bank.payday.accounts.components.account.dataloader.AccountsCreditByCutomerDataLoader;
import bank.payday.accounts.components.account.dataloader.AccountsDebitByCutomerDataLoader;
import bank.payday.accounts.generated.types.Customer;
import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.dataloader.DataLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class AccountDataFetcher {
    private final AccountService accountService;

    @DgsQuery
    public List<Account> accounts(@InputArgument String type, @RequestHeader("user") String user) {
        Integer customer = Integer.parseInt(user.replaceAll("\"",""));
        return this.accountService.getAllAccounts(customer, type);
    }

    @DgsQuery
    public Account account(@InputArgument String id) {
        return this.accountService.getAccountById(Integer.parseInt(id));
    }

    @DgsMutation
    public Account createAccount(@InputArgument String name , @InputArgument String type, @RequestHeader("user") String user){
        Integer customer = Integer.parseInt(user.replaceAll("\"",""));
        Account account = this.accountService.addNewAccount(new Account(customer, name, type), user);
        return account;
    }

    @DgsMutation
    public Account disableAccount(@InputArgument String id, @RequestHeader("user") String user){
        Integer customer = Integer.parseInt(user.replaceAll("\"",""));
        return this.accountService.disableAccountById(Integer.parseInt(id), customer);
    }

    @DgsEntityFetcher(name = "Customer")
    public Customer customer(Map<String, Object> values) {
        String id = String.valueOf(Long.parseLong((String) values.get("id")));
        return Customer.newBuilder().id(id).build();
    }

    @DgsData(parentType = "Customer", field = "debitAccounts")
    public CompletableFuture<Account> accountDebitByCustomer(DgsDataFetchingEnvironment dfe) {
        DataLoader<String, Account> accountByCustomerDataLoader = dfe.getDataLoader(AccountsDebitByCutomerDataLoader.class);

        Customer customer = dfe.getSource();

        return accountByCustomerDataLoader.load(customer.getId());
    }

    @DgsData(parentType = "Customer", field = "creditAccounts")
    public CompletableFuture<Account> accountCreditByCustomer(DgsDataFetchingEnvironment dfe) {
        DataLoader<String, Account> accountByCustomerDataLoader = dfe.getDataLoader(AccountsCreditByCutomerDataLoader.class);

        Customer customer = dfe.getSource();

        return accountByCustomerDataLoader.load(customer.getId());
    }

}