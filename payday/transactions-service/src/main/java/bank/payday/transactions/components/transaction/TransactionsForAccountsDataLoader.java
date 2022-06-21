package bank.payday.transactions.components.transaction;

import com.netflix.graphql.dgs.DgsDataLoader;
import lombok.RequiredArgsConstructor;
import org.dataloader.MappedBatchLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@DgsDataLoader(name = "TRANSACTION_FOR_ACCOUNTS")
@RequiredArgsConstructor
public class TransactionsForAccountsDataLoader implements MappedBatchLoader<String, List<Transaction>> {
    private final TransactionService transactionService;

    @Override
    public CompletionStage<Map<String, List<Transaction>>> load(Set<String> accountIds) {
        return CompletableFuture.supplyAsync(() -> this.transactionService.getTransactionsByAccounts(new ArrayList<>(accountIds)));
    }
}
