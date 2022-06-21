package bank.payday.transactions.components.transaction;

import bank.payday.transactions.generated.types.Account;
import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@DgsComponent
@RequiredArgsConstructor
public class TransactionDataFetcher {

    private final TransactionService transactionService;
    @DgsQuery
    public List<Transaction> transactions(@InputArgument String account, @RequestHeader("user") String user) {
        Integer customer = Integer.parseInt(user.replaceAll("\"",""));
        return this.transactionService.getAllCustomerTransactions(Integer.parseInt(account), customer);
    }

    @DgsMutation
    public Transaction createTransaction(@InputArgument String account ,@InputArgument Integer amount, @InputArgument String description, @RequestHeader("user") String user) {
        Integer customer = Integer.parseInt(user.replaceAll("\"",""));
        this.transactionService.validateAccount(account);
        return this.transactionService.createNewTransaction(Integer.parseInt(account), customer, amount, description);
    }

    @DgsEntityFetcher(name = "Account")
    public Account account(Map<String, Object> values) {
        String id = String.valueOf(Long.parseLong((String) values.get("id")));

        return Account.newBuilder().id(id).build();
    }

    @DgsData(parentType = "Account", field = "transactions")
    public CompletableFuture<Account> transactionsByAccount(DgsDataFetchingEnvironment dfe) {
        DataLoader<String, Account> transactionByAccountDataLoader = dfe.getDataLoader(TransactionsForAccountsDataLoader.class);

        Account account = dfe.getSource();

        return transactionByAccountDataLoader.load(account.getId());
    }


}
