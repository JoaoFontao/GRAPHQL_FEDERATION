package bank.payday.customers.components.customer;

import bank.payday.customers.JwtUtil;
import bank.payday.customers.generated.client.*;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {DgsAutoConfiguration.class, CustomerDataFetcher.class})
class CustomerDataFetcherTest {

    @MockBean
    private CustomerService customerService;
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;

    private int id1 = 1;
    private int id2 = 2;
    private int id3 = 3;

    private String name1 = "Joao";
    private String name2 = "Luis";
    private String name3 = "Miguel";

    private String lastName1 = "Fontao";
    private String lastName2 = "Monteiro";
    private String lastName3 = "Pinto";

    private String phone1 = "123456789";
    private String phone2 = "987654321";
    private String phone3 = "254871099";

    private String email1 = "joaofontao12@gmail.com";
    private String email2 = "luismonteiro12@gmail.com";
    private String email3 = "miguelpinto12@gmail.com";

    private String gender1 = "man";
    private String gender2 = "woman";
    private String gender3 = "man";

    private String date1 = "2022-02-02";
    private String date2 = "2022-02-02";
    private String date3 = "2019-02-02";

    private String password1 = "joaofontao";
    private String password2 = "luismonteiro";
    private String password3 = "miguelpinto";

    private final String HEADER_NAME = "user";

    @BeforeEach
    public void before() {
        customer1 = new Customer(id1, name1, lastName1, phone1, email1, gender1, date1, password1);
        customer2 = new Customer(id2, name2, lastName2, phone2, email2, gender2, date2, password2);
        customer3 = new Customer(id3, name3, lastName3, phone3, email3, gender3, date3, password3);

    }

    @Test
    void customersTest() {
        when(customerService.getAllCustomers()).thenReturn(List.of(customer1, customer2));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CustomersGraphQLQuery.Builder().build(),
                new CustomersProjectionRoot().id().date_of_birth().email().gender().last_name().name().phone()
        );

        List<String> ids = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*].id");
        List<String> dates = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*].date_of_birth");
        List<Integer> emails = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*].email");
        List<String> genders = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*].gender");
        List<String> lastNames = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*].last_name");
        List<String> names = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*].name");
        List<String> phones = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*].phone");

        assertEquals(ids,List.of(String.valueOf(id1), String.valueOf(id2)));
        assertEquals(dates,List.of(date1, date2));
        assertEquals(emails,List.of(email1, email2));
        assertEquals(genders,List.of(gender1, gender2));
        assertEquals(lastNames,List.of(lastName1, lastName2));
        assertEquals(names,List.of(name1, name2));
        assertEquals(phones,List.of(phone1, phone2));
    }

    @Test
    void customersReturnEmptyListTest() {
        when(customerService.getAllCustomers()).thenReturn(List.of());
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CustomersGraphQLQuery.Builder().build(),
                new CustomersProjectionRoot().id().date_of_birth().email().gender().last_name().name().phone()
        );

        List<String> result = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customers[*]");
        assertEquals(result, List.of());
    }

    @Test
    void customerTest() {
        when(customerService.getCustomerById(id1)).thenReturn(customer1);
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CustomerGraphQLQuery.Builder().build(),
                new CustomerProjectionRoot().id().date_of_birth().email().gender().last_name().name().phone()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> values = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.customer[*]", httpHeaders);

        assertEquals(values.get(0),String.valueOf(id1));
        assertEquals(values.get(1),date1);
        assertEquals(values.get(2),email1);
        assertEquals(values.get(3),gender1);
        assertEquals(values.get(4),lastName1);
        assertEquals(values.get(5),name1);
        assertEquals(values.get(6),phone1);
    }

    @Test
    void customerReturnNullTest() {
        when(customerService.getCustomerById(id1)).thenThrow(new CustomerException("Invalid Customer"));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new CustomerGraphQLQuery.Builder().build(),
                new CustomerProjectionRoot().id().date_of_birth().email().gender().last_name().name().phone()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>(), new HashMap<>(), httpHeaders);
        String expected = "bank.payday.customers.components.customer.CustomerException: Invalid Customer";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }

    @Test
    void signup() {
        UserDetails userDetails = new User(customer1.getId().toString(), password1, new ArrayList<>());
        String token = "qwerty";
        when(customerService.addNewCustomer(new Customer(name1, lastName1, phone1, email1, gender1, date1, password1))).thenReturn(customer1);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new SignupGraphQLQuery.Builder().email(email1).date_of_birth(date1).gender(gender1).last_name(lastName1).name(name1).password(password1).phone(phone1).build(),
                new SignupProjectionRoot().token().customer().id()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        List<String> values = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.signup[*]", httpHeaders);
        Map map1 = new LinkedHashMap<>();
        map1.put("id", "1");
        assertEquals(values.get(0),token);
        assertEquals(values.get(1),map1);
    }

    @Test
    void signupReturnCustomerAlreadyExistsTest() {
        when(customerService.addNewCustomer(new Customer(name1, lastName1, phone1, email1, gender1, date1, password1))).thenThrow(new CustomerException("Customer Already exists"));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new SignupGraphQLQuery.Builder().email(email1).date_of_birth(date1).gender(gender1).last_name(lastName1).name(name1).password(password1).phone(phone1).build(),
                new SignupProjectionRoot().token().customer().id()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>(), new HashMap<>(), httpHeaders);
        String expected = "bank.payday.customers.components.customer.CustomerException: Customer Already exists";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }

    @Test
    void signupReturnDataIncorrectTest() {
        when(customerService.addNewCustomer(new Customer(name1, lastName1, phone1, email1, "sds", date1, password1))).thenThrow(new CustomerException("Data Incorrect"));
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new SignupGraphQLQuery.Builder().email(email1).date_of_birth(date1).gender("sds").last_name(lastName1).name(name1).password(password1).phone(phone1).build(),
                new SignupProjectionRoot().token().customer().id()
        );
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HEADER_NAME, "1");
        HttpHeaders httpHeaders = new HttpHeaders(map);

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>(), new HashMap<>(), httpHeaders);
        String expected = "bank.payday.customers.components.customer.CustomerException: Data Incorrect";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }

    @Test
    void login() {
        UserDetails userDetails = new User(customer1.getId().toString(), password1, new ArrayList<>());
        String token = "qwerty";
        when(customerService.validateCustomer(customer1.getEmail(), customer1.getPassword())).thenReturn(customer1);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new LoginGraphQLQuery.Builder().login(email1).password(password1).build(),
                new LoginProjectionRoot().token().customer().id()
        );

        List<String> values = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.login[*]");
        Map map1 = new LinkedHashMap<>();
        map1.put("id", "1");
        assertEquals(values.get(0),token);
        assertEquals(values.get(1),map1);
    }

    @Test
    void loginReturnInvalidCredentialsTest() {
        UserDetails userDetails = new User(customer1.getId().toString(), password1, new ArrayList<>());
        String token = "qwerty";
        when(customerService.validateCustomer(customer1.getEmail(), "erro")).thenThrow(new CustomerException("Check Login credentials"));
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new LoginGraphQLQuery.Builder().login(email1).password("erro").build(),
                new LoginProjectionRoot().token().customer().id()
        );

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>());
        String expected = "bank.payday.customers.components.customer.CustomerException: Check Login credentials";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }

    @Test
    void loginReturnInvalidUsernameTest() {
        UserDetails userDetails = new User(customer1.getId().toString(), password1, new ArrayList<>());
        String token = "qwerty";
        when(customerService.validateCustomer("email", password1)).thenThrow(new CustomerException("Invalid username"));
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                new LoginGraphQLQuery.Builder().login("email").password(password1).build(),
                new LoginProjectionRoot().token().customer().id()
        );

        ExecutionResult result = dgsQueryExecutor.execute(graphQLQueryRequest.serialize(), new LinkedHashMap<>());
        String expected = "bank.payday.customers.components.customer.CustomerException: Invalid username";
        assertEquals(result.getErrors().get(0).getMessage(), expected);
    }
}