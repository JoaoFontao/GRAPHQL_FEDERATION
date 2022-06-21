package bank.payday.transactions.components.transaction;

import bank.payday.transactions.generated.client.CreateTransactionGraphQLQuery;
import bank.payday.transactions.generated.client.CreateTransactionProjectionRoot;
import bank.payday.transactions.generated.client.TransactionsGraphQLQuery;
import bank.payday.transactions.generated.client.TransactionsProjectionRoot;
import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import graphql.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.http.HttpHeaders;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DgsAutoConfiguration.class, TransactionDataFetcher.class})
class TransactionDataFetcherTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    private Transaction transaction;
    private Transaction transaction2;
    private Transaction transaction3;
    private Instant instant;
    private final String HEADER_NAME = "user";

    private String description1 = "New Transaction";
    private String description2 = "New Transaction2";
    private String description3 = "New Transaction3";
    private int customer1 = 1;
    private int customer2 = 2;
    private int account1 = 1;
    private int account2 =2;
    private int id1 = 1;
    private int id2 = 2;
    private int id3 = 3;
    private int amount1 = 10;
    private int amount2 = 100;
    private int amount3 = 1;


    @BeforeEach
    public void before() {
        instant = Instant.now();
        long timeStamp = instant.toEpochMilli();
        transaction = new Transaction(description1, customer1, account2, id1, amount1, String.valueOf(timeStamp));
        transaction2 = new Transaction(description2, customer1, account2, id2 , amount2, String.valueOf(timeStamp));
        transaction3 = new Transaction(description3, customer2, account1, id3 , amount3, String.valueOf(timeStamp));

    }

    @Test
    void transactions() {
        int account =2;
        int customer = 1;
        when(transactionService.getAllCustomerTransactions(account, customer)).thenReturn(List.of(transaction, transaction2));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new TransactionsGraphQLQuery.Builder().account("2").build(),
                new TransactionsProjectionRoot().description().id().account().amount().customer()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> descriptions = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.transactions[*].description", httpHeaders);
        List<String> ids = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.transactions[*].id", httpHeaders);
        List<Integer> customers = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.transactions[*].customer", httpHeaders);
        List<String> amounts = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.transactions[*].amount", httpHeaders);
        List<String> accounts = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.transactions[*].account", httpHeaders);
        assertEquals(descriptions,List.of(description1, description2));
        assertEquals(ids,List.of(String.valueOf(id1), String.valueOf(id2)));
        assertEquals(customers,List.of(customer1, customer1));
        assertEquals(amounts,List.of(amount1, amount2));
        assertEquals(accounts,List.of(account2, account2));
    }

    @Test
    void transactionsReturnEmptyListTest() {
        int account =2;
        int customer = 1;
        when(transactionService.getAllCustomerTransactions(account, customer)).thenReturn(List.of());
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new TransactionsGraphQLQuery.Builder().account("2").build(),
                new TransactionsProjectionRoot().description().id().account().amount().customer()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> result = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.transactions[*]", httpHeaders);
        assertEquals(result, List.of());
    }

    @Test
    void createTransaction() {
        int account =2;
        int customer = 1;
        when(transactionService.createNewTransaction(account, customer, amount1, description1)).thenReturn(transaction);

        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CreateTransactionGraphQLQuery.Builder().account(String.valueOf(account)).amount(amount1).description(description1).build(),
                new CreateTransactionProjectionRoot().description().id().account().amount().customer()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);
        List<String> values = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.createTransaction[*]", httpHeaders);

        assertEquals(values.get(0),description1);
        assertEquals(values.get(1),String.valueOf(id1));
        assertEquals(values.get(2),account);
        assertEquals(values.get(3),amount1);
        assertEquals(values.get(4),customer);
    }

    @Test
    void createTransactionReturnInvalidAccountTest() {
        int account =8;
        int customer = 1;
        when(transactionService.createNewTransaction(account, customer, amount1, description1)).thenThrow(new TransactionException("Invalid Account"));

        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CreateTransactionGraphQLQuery.Builder().account(String.valueOf(account)).amount(amount1).description(description1).build(),
                new CreateTransactionProjectionRoot().description().id().account().amount().customer()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>(), new HashMap<>(), httpHeaders);
        String expected = "bank.payday.transactions.components.transaction.TransactionException: Invalid Account";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }

}