package bank.payday.transactions.components.transaction;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByCustomerOrderByDateOfTransactionDesc(int customer);
    List<Transaction> findByAccountAndCustomerOrderByDateOfTransactionDesc(int account, int customer);

    List<Transaction> findByAccountIn(List<Integer> account);

}