package bank.payday.accounts.components.account.dataloader;

import bank.payday.accounts.components.account.Account;
import bank.payday.accounts.components.account.AccountService;
import com.netflix.graphql.dgs.DgsDataLoader;
import lombok.RequiredArgsConstructor;
import org.dataloader.MappedBatchLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@DgsDataLoader(name = "ACCOUNTS_CREDIT_FOR_CUSTOMERS")
@RequiredArgsConstructor
public class AccountsCreditByCutomerDataLoader implements MappedBatchLoader<String, List<Account>> {
    private final AccountService accountService;

    @Override
    public CompletionStage<Map<String, List<Account>>> load(Set<String> customerIds) {
        return CompletableFuture.supplyAsync(() -> this.accountService.getAccountsByCustomerAndTypeAndStatus(new ArrayList<>(customerIds), "credit", 1));
    }
}