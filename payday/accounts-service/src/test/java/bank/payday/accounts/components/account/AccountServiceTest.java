package bank.payday.accounts.components.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class AccountServiceTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    @Autowired
    private AccountService accountService;
    private Account account1;
    private Account account2;
    private Account account3;
    private Account account4;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(this.accountRepository);

        account1 = new Account(1,1, "123456768", "account1", 1, 213456567, "debit");
        account2 = new Account(2,2, "123456768", "account2", 1, 213456567, "credit");
        account3 = new Account(3,2, "123456768", "account3", 1, 213456567, "debit");
        account4 = new Account(4,2, "123456768", "account4", 1, 213456567, "debit");
    }


    @Test
    void getAllAccountsTest() {
        when(accountRepository.findByCustomerAndTypeAndStatusOrderByIdDesc(2, "debit", 1)).thenReturn(List.of(account3, account4));
        assertEquals(List.of(account3, account4), accountService.getAllAccounts(2, "debit"));
    }

    @Test
    void getAllAccountsEmptyListNotExistsCostumerTest() {
        when(accountRepository.findByCustomerAndTypeAndStatusOrderByIdDesc(3, "debit", 1)).thenReturn(List.of());
        assertEquals(List.of(), accountService.getAllAccounts(3, "debit"));
    }

    @Test
    void getAllAccountsEmptyListNotExistsTypeTest() {
        when(accountRepository.findByCustomerAndTypeAndStatusOrderByIdDesc(1, "credit", 1)).thenReturn(List.of());
        assertEquals(List.of(), accountService.getAllAccounts(1, "credit"));
    }

    @Test
    void getAllAccountsOnlyOneTest() {
        when(accountRepository.findByCustomerAndTypeAndStatusOrderByIdDesc(1, "debit", 1)).thenReturn(List.of(account1));
        assertEquals(List.of(account1), accountService.getAllAccounts(1, "debit"));
    }

    @Test
    void getAccountByIdTest() {
        when(accountRepository.findById(1)).thenReturn(Optional.of(account1));
        assertEquals(account1, accountService.getAccountById(1));
    }

    @Test
    void getAccountByIdNotExistsTest() {
        when(accountRepository.findById(3)).thenReturn(Optional.empty());
        AccountException exception = assertThrows(AccountException.class, () -> {
            accountService.getAccountById(3);
        });
        assertEquals("Invalid Account ID", exception.getMessage());
    }

    @Test
    void getAccountsByCustomerAndTypeAndStatusTest() {
        int customer = 1;
        int customer2 = 2;
        String type = "debit";
        int status = 1;
        when(accountRepository.findByCustomerInAndTypeAndStatusOrderByIdDesc(List.of(customer, customer2), type, status)).thenReturn(List.of(account1, account3, account4));

        Map<String, List<Account>> expected = new LinkedHashMap<>();
        expected.put("1", List.of(account1));
        expected.put("2", List.of(account3, account4));

        assertEquals(expected, accountService.getAccountsByCustomerAndTypeAndStatus(List.of(String.valueOf(customer), String.valueOf(customer2)), type, status));
    }

    @Test
    void getAccountsByCustomerAndTypeAndStatusReturnEmpty() {
        int customer = 1;
        String type = "credit";
        int status = 1;
        when(accountRepository.findByCustomerInAndTypeAndStatusOrderByIdDesc(List.of(customer), type, status)).thenReturn(List.of());

        Map<String, List<Account>> expected = new LinkedHashMap<>();
        expected.put("1", List.of());

        assertEquals(expected, accountService.getAccountsByCustomerAndTypeAndStatus(List.of(String.valueOf(customer)), type, status));
    }

    @Test
    void getAccountsByCustomerAndTypeAndStatusReturnOneEmptyListTest() {
        int customer = 1;
        int customer2 = 2;
        String type = "credit";
        int status = 1;
        when(accountRepository.findByCustomerInAndTypeAndStatusOrderByIdDesc(List.of(customer, customer2), type, status)).thenReturn(List.of(account2));

        Map<String, List<Account>> expected = new LinkedHashMap<>();
        expected.put("1", List.of());
        expected.put("2", List.of(account2));

        assertEquals(expected, accountService.getAccountsByCustomerAndTypeAndStatus(List.of(String.valueOf(customer), String.valueOf(customer2)), type, status));
    }

    @Test
    void disableAccountByIdTest() {
        when(accountRepository.findById(1)).thenReturn(Optional.of(account1));
        account1.setStatus(0);
        when(accountRepository.save(account1)).thenReturn(account1);
        assertEquals(account1, accountService.disableAccountById(1,1));
    }

    @Test
    void disableAccountByIdReturnNullTest() {
        when(accountRepository.findById(3)).thenReturn(Optional.empty());
        AccountException exception = assertThrows(AccountException.class, () -> {
            accountService.disableAccountById(3,1);
        });
        assertEquals("Invalid Account", exception.getMessage());
    }

    @Test
    void addNewAccountReturnNullTest() {
        account4.setType("debito");
        AccountException exception = assertThrows(AccountException.class, () -> {
            accountService.addNewAccount(account4,"1");
        });
        assertEquals("Invalid Account", exception.getMessage());
    }
}