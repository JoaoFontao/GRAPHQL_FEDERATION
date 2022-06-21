package bank.payday.accounts.components.account;

import bank.payday.accounts.generated.client.*;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import graphql.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DgsAutoConfiguration.class, AccountDataFetcher.class})
class AccountDataFetcherTest {

    @MockBean
    private AccountService accountService;

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    private Account account1;
    private Account account2;
    private Account account3;
    private Account account4;

    private int id1 = 1;
    private int id2 = 2;
    private int id3 = 3;
    private int id4 = 4;

    private int customer1 = 1;
    private int customer2 = 2;

    private String accn = "123456768";

    private String name1 = "account1";
    private String name2 = "account2";
    private String name3 = "account3";
    private String name4 = "account4";

    private int status = 1;

    private int date = 213456567;

    private String typeDebit = "debit";

    private String typeCredit = "credit";

    private final String HEADER_NAME = "user";

    @BeforeEach
    void setUp() {

        account1 = new Account(id1,customer1, accn, name1, status, date, typeDebit);
        account2 = new Account(id2,customer2, accn, name2, status, date, typeCredit);
        account3 = new Account(id3,customer2, accn, name3, status, date, typeDebit);
        account4 = new Account(id4,customer2, accn, name4, status, date, typeDebit);
    }

    @Test
    void accountsTest() {
        when(accountService.getAllAccounts(customer2, typeDebit)).thenReturn(List.of(account3, account4));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new AccountsGraphQLQuery.Builder().type(typeDebit).build(),
                new AccountsProjectionRoot().id().customer().accn().name().status().date_of_creation().type()
        );

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "2");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> ids = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*].id", httpHeaders);
        List<String> customers = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*].customer", httpHeaders);
        List<String> accns = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*].accn", httpHeaders);
        List<String> names = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*].name", httpHeaders);
        List<String> statusL = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*].status", httpHeaders);
        List<Integer> dates = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*].date_of_creation", httpHeaders);
        List<String> types = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*].type", httpHeaders);

        assertEquals(ids,List.of(String.valueOf(id3), String.valueOf(id4)));
        int date2 = (int) date;
        assertEquals(dates,List.of(date2,date2));
        assertEquals(customers,List.of(customer2, customer2));
        assertEquals(accns,List.of(accn, accn));
        assertEquals(names,List.of(name3, name4));
        assertEquals(statusL,List.of(status, status));
        assertEquals(types,List.of(typeDebit, typeDebit));
    }

    @Test
    void accountsEmptyListNotExistsCostumerTest() {
        when(accountService.getAllAccounts(customer2, typeDebit)).thenReturn(List.of());
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new AccountsGraphQLQuery.Builder().type(typeDebit).build(),
                new AccountsProjectionRoot().id().customer().accn().name().status().date_of_creation().type()
        );

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "2");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> result = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.accounts[*]", httpHeaders);
        assertEquals(result, List.of());
    }

    @Test
    void createAccountTest() {
        when(accountService.addNewAccount(new Account(customer1, name1, typeDebit), "1")).thenReturn(account1);
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CreateAccountGraphQLQuery.Builder().name(name1).type(typeDebit).build(),
                new CreateAccountProjectionRoot().id().customer().accn().name().status().date_of_creation().type()
        );

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> values = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.createAccount[*]", httpHeaders);

        assertEquals(values.get(0),String.valueOf(id1));
        assertEquals(values.get(1),customer1);
        assertEquals(values.get(2),accn);
        assertEquals(values.get(3),name1);
        assertEquals(values.get(4),status);
        assertEquals(values.get(5),(int)date);
        assertEquals(values.get(6),typeDebit);
    }

    @Test
    void createAccountReturnErrorTest() {
        when(accountService.addNewAccount(new Account(customer1, name1, typeDebit), "1")).thenThrow(new AccountException("Invalid Account"));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CreateAccountGraphQLQuery.Builder().name(name1).type(typeDebit).build(),
                new CreateAccountProjectionRoot().id().customer().accn().name().status().date_of_creation().type()
        );

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>(), new HashMap<>(), httpHeaders);
        String expected = "bank.payday.accounts.components.account.AccountException: Invalid Account";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }

    @Test
    void disableAccountTest() {
        account1.setStatus(0);
        when(accountService.disableAccountById(id1, customer1)).thenReturn(account1);
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new DisableAccountGraphQLQuery.Builder().id(String.valueOf(id1)).build(),
                new DisableAccountProjectionRoot().id().customer().accn().name().status().date_of_creation().type()
        );

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> values = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.disableAccount[*]", httpHeaders);

        assertEquals(values.get(0),String.valueOf(id1));
        assertEquals(values.get(1),customer1);
        assertEquals(values.get(2),accn);
        assertEquals(values.get(3),name1);
        assertEquals(values.get(4),0);
        assertEquals(values.get(5),(int)date);
        assertEquals(values.get(6),typeDebit);
    }

    @Test
    void disableAccountReturnInvalidAccountTest() {
        account1.setStatus(0);
        when(accountService.disableAccountById(id1, customer1)).thenThrow(new AccountException("Invalid Account"));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new DisableAccountGraphQLQuery.Builder().id(String.valueOf(id1)).build(),
                new DisableAccountProjectionRoot().id().customer().accn().name().status().date_of_creation().type()
        );

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>(), new HashMap<>(), httpHeaders);
        String expected = "bank.payday.accounts.components.account.AccountException: Invalid Account";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }
}