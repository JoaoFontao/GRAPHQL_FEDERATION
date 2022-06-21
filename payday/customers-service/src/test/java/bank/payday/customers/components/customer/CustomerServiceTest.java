package bank.payday.customers.components.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class CustomerServiceTest {

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    @Autowired
    private CustomerService customerService;
    private Customer customer1;
    private Customer customer2;
    private Customer customer3;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(this.customerRepository);

        customer1 = new Customer(1, "Joao", "Fontao", "123456789", "joaofontao12@gmail.com", "man", "2022-02-02", "joaofontao");
        customer2 = new Customer(2, "Luis", "Monteiro", "987654321", "luismonteiro12@gmail.com", "woman", "2022-02-02", "luismonteiro");
        customer3 = new Customer(3, "Miguel", "Pinto", "254871099", "miguelpinto12@gmail.com", "man", "2019-02-02", "miguelpinto");
    }

    @Test
    void getAllCustomersTest() {
        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2, customer3));
        assertEquals(List.of(customer1, customer2, customer3), customerService.getAllCustomers());
    }

    @Test
    void getAllCustomersEmptyListTest() {
        when(customerRepository.findAll()).thenReturn(List.of());
        assertEquals(List.of(), customerService.getAllCustomers());
    }

    @Test
    void getAllCustomersOnlyOneTest() {
        when(customerRepository.findAll()).thenReturn(List.of(customer1));
        assertEquals(List.of(customer1), customerService.getAllCustomers());
    }

    @Test
    void getCustomerByIdTest() {
        int customerid =1;
        when(customerRepository.findById(customerid)).thenReturn(Optional.of(customer1));
        assertEquals(customer1, customerService.getCustomerById(customerid));
    }
    @Test
    void getCustomerByIdReturnNullTest() {
        int customerid =3;
        when(customerRepository.findById(customerid)).thenReturn(Optional.empty());
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.getCustomerById(customerid);
        });
        assertEquals("Invalid Customer", exception.getMessage());
    }

    @Test
    void validateCustomerTest() {
        when(customerRepository.findByPhone(customer1.getEmail())).thenReturn(Optional.empty());
        String password = customer1.getPassword();
        String encPassword = customer1.encPassword();
        customer1.setPassword(encPassword);
        when(customerRepository.findByEmail(customer1.getEmail())).thenReturn(Optional.of(customer1));
        assertEquals(customer1, customerService.validateCustomer(customer1.getEmail(), password));
    }

    @Test
    void validateCustomerEmailNotExistsTest() {
        when(customerRepository.findByPhone("teste")).thenReturn(Optional.empty());
        String password = customer1.getPassword();
        String encPassword = customer1.encPassword();
        customer1.setPassword(encPassword);
        when(customerRepository.findByEmail("teste")).thenReturn(Optional.empty());
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.validateCustomer("teste", password);
        });
        assertEquals("Invalid username", exception.getMessage());
    }

    @Test
    void validateCustomerWrongPasswordTest() {
        when(customerRepository.findByPhone(customer1.getEmail())).thenReturn(Optional.empty());
        String encPassword = customer1.encPassword();
        customer1.setPassword(encPassword);
        String email = customer1.getEmail();
        when(customerRepository.findByEmail(customer1.getEmail())).thenReturn(Optional.of(customer1));
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.validateCustomer(email, "password");
        });
        assertEquals("Check Login credentials", exception.getMessage());
    }

    /*@Test
    void addNewCustomerTest() {
        when(customerRepository.countByEmailOrPhone(customer1.getEmail(), customer1.getPhone())).thenReturn(0L);
        Customer customer4 = new Customer(customer1);
        customer1.setPassword(customer1.encPassword());

        when(customerRepository.save(customer1)).thenReturn(customer1);
        customer1.setId(0);
        System.out.println("Password: " + customer1.getPassword());
        System.out.println("Password: " + customer4.getPassword());
        assertEquals(customer1, customerService.addNewCustomer(customer4));
    }*/

    @Test
    void addNewCustomerEmailOrPhoneAlreadyExistsTest() {
        when(customerRepository.countByEmailOrPhone(customer1.getEmail(), customer1.getPhone())).thenReturn(1L);
        assertEquals(null, customerService.addNewCustomer(customer1));
    }

    @Test
    void addNewCustomerExistsErrorsTest() {
        customer1.setPhone("123");
        when(customerRepository.countByEmailOrPhone(customer1.getEmail(), customer1.getPhone())).thenReturn(0L);
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.addNewCustomer(customer1);
        });
        assertEquals("Data Incorrect", exception.getMessage());
    }


    @Test
    void deleteCustomerTest() {
        int customerid =1;
        when(customerRepository.findById(customerid)).thenReturn(Optional.of(customer1));
        assertEquals(customer1, customerService.deleteCustomer(customerid));
    }

    @Test
    void deleteCustomerIdNotExistsTest() {
        int customerid =3;
        when(customerRepository.findById(customerid)).thenReturn(Optional.empty());
        CustomerException exception = assertThrows(CustomerException.class, () -> {
            customerService.deleteCustomer(customerid);
        });
        assertEquals("Invalid Customer", exception.getMessage());
    }
}