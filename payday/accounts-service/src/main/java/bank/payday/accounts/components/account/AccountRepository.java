package bank.payday.accounts.components.account;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Integer> {

    // find debit accounts
    List<Account> findByCustomerInAndType(List<Integer> customer, String type);
    // find credit account
    // block account

    List<Account> findByCustomerInAndTypeAndStatusOrderByIdDesc(List<Integer> customer, String type, int status);

    List<Account> findByCustomerAndTypeAndStatusOrderByIdDesc(int customer, String type, int status);


}