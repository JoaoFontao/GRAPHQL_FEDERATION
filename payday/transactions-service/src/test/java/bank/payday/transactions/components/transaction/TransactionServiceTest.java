package bank.payday.transactions.components.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class TransactionServiceTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    @Autowired
    private TransactionService transactionService;
    private Transaction transaction;
    private Transaction transaction2;
    private Transaction transaction3;
    private Instant instant;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(this.transactionRepository);
        instant = Instant.now();
        long timeStamp = instant.toEpochMilli();
        transaction = new Transaction("New Transaction", 1, 2, 1 , 10, String.valueOf(timeStamp));
        transaction2 = new Transaction("New Transaction2", 1, 2, 2 , 100, String.valueOf(timeStamp));
        transaction3 = new Transaction("New Transaction3", 2, 1, 3 , 1, String.valueOf(timeStamp));
    }

    @Test
    void getAllCustomerTransactionsTest() {
        int account =2;
        int customer = 1;
        when(transactionRepository.findByAccountAndCustomerOrderByDateOfTransactionDesc(account, customer)).thenReturn(List.of(transaction, transaction2));
        assertEquals(List.of(transaction, transaction2), transactionService.getAllCustomerTransactions(2,1));
    }

    @Test
    void getOnlyOneCustomerTransactionsTest() {
        int account =1;
        int customer = 2;
        when(transactionRepository.findByAccountAndCustomerOrderByDateOfTransactionDesc(account, customer)).thenReturn(List.of(transaction3));
        assertEquals(List.of(transaction3), transactionService.getAllCustomerTransactions(account,customer));
    }

    @Test
    void getNoneCustomerTransactionsTest() {
        int account =1;
        int customer = 2;
        int account1 = 2;
        int customer1 = 1;
        when(transactionRepository.findByAccountAndCustomerOrderByDateOfTransactionDesc(account, customer)).thenReturn(List.of(transaction3));
        when(transactionRepository.findByAccountAndCustomerOrderByDateOfTransactionDesc(account1, customer1)).thenReturn(List.of(transaction, transaction2));
        int accountResult = 3;
        int customerResult = 1;
        assertEquals(List.of(), transactionService.getAllCustomerTransactions(accountResult,customerResult));
    }

    @Test
    void getTransactionTest() {
        String id ="1";
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        assertEquals(transaction, transactionService.getTransaction(id));
    }

    @Test
    void getTransactionIdNotExistsTest() {
        String id ="4";
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        TransactionException exception = assertThrows(TransactionException.class, () -> {
            transactionService.getTransaction(id);
        });
        assertEquals("Invalid Transaction ID", exception.getMessage());
    }

    /*@Test
    void createNewTransaction() {
        int account =1;
        int customer = 2;
        int amount = 1;
        String description = "New Transaction3";

        Transaction trans = new Transaction(customer, account, instant.toEpochMilli(), amount, description);

        when(transactionRepository.save(trans)).thenReturn(trans);
        Transaction result = transactionService.createNewTransaction(account, customer, amount, description);
        assertEquals(trans.getAccount(), result.getAccount());
        assertEquals(trans.getCustomer(), result.getCustomer());
        assertEquals(trans.getAmount(), result.getAmount());
        assertEquals(trans.getDescription(), result.getDescription());
    }*/

    @Test
    void createNewTransactionErrorAccount() {
        int account =0;
        int customer = 2;
        int amount = 1;
        String description = "New Transaction3";

        Transaction trans = new Transaction(customer, account, instant.toEpochMilli(), amount, description);

        when(transactionRepository.save(trans)).thenReturn(trans);
        Transaction result = transactionService.createNewTransaction(account, customer, amount, description);
        assertEquals(null, result);
    }

    @Test
    void createNewTransactionErrorCustomer() {
        int account =1;
        int customer = 0;
        int amount = 1;
        String description = "New Transaction3";

        Transaction trans = new Transaction(customer, account, instant.toEpochMilli(), amount, description);

        when(transactionRepository.save(trans)).thenReturn(trans);
        Transaction result = transactionService.createNewTransaction(account, customer, amount, description);
        assertEquals(null, result);
    }

    @Test
    void createNewTransactionErrorAmount() {
        int account =1;
        int customer = 1;
        int amount = 0;
        String description = "New Transaction3";

        Transaction trans = new Transaction(customer, account, instant.toEpochMilli(), amount, description);

        when(transactionRepository.save(trans)).thenReturn(trans);
        Transaction result = transactionService.createNewTransaction(account, customer, amount, description);
        assertEquals(null, result);
    }


    @Test
    void getTransactionsByAccountsTest() {
        int account =1;
        int account2 = 2;
        when(transactionRepository.findByAccountIn(List.of(account, account2))).thenReturn(List.of(transaction, transaction2, transaction3));
        Map<String, List<Transaction>> expected = new LinkedHashMap<>();
        expected.put("1", List.of(transaction3));
        expected.put("2", List.of(transaction, transaction2));
        assertEquals(expected, transactionService.getTransactionsByAccounts(List.of(String.valueOf(account), String.valueOf(account2))));
    }

    @Test
    void getTransactionsByAccountsReturnEmptyTransactionTest() {
        int account =3;
        when(transactionRepository.findByAccountIn(List.of(account))).thenReturn(List.of());
        Map<String, List<Transaction>> expected = new LinkedHashMap<>();
        expected.put("3", List.of());
        assertEquals(expected, transactionService.getTransactionsByAccounts(List.of(String.valueOf(account))));
    }

    @Test
    void getTransactionsByAccountsReturnOneEmptyListTest() {
        int account =3;
        int account2 = 2;
        when(transactionRepository.findByAccountIn(List.of(account, account2))).thenReturn(List.of(transaction, transaction2));
        Map<String, List<Transaction>> expected = new LinkedHashMap<>();
        expected.put("3", List.of());
        expected.put("2", List.of(transaction, transaction2));
        assertEquals(expected, transactionService.getTransactionsByAccounts(List.of(String.valueOf(account), String.valueOf(account2))));
    }

}