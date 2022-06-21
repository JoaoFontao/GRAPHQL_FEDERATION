package bank.payday.customers.components.customer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        try {
            List<Customer> customers = new ArrayList<>();
            customerRepository.findAll().forEach(customers::add);

            return customers;

        } catch (Exception e) {
            throw new CustomerException(e.getMessage());
        }

    }

    public Customer getCustomerById(Integer id) {
        Optional<Customer> customerData = customerRepository.findById(id);

        if (customerData.isPresent()) {
            return customerData.get();
        } else {
            throw new CustomerException("Invalid Customer");
        }
    }

    public Customer validateCustomer(String login, String password) {
        if (login == null && password == null) {
            throw new CustomerException("Check Login credentials");
        }

        Optional<Customer> customerData;

        customerData = customerRepository.findByPhone(login);
        if(!customerData.isPresent()) {

            customerData = customerRepository.findByEmail(login);
        }


        if (customerData.isPresent()) {
            if (customerData.get().validatePassword(password)) {
                return new Customer(customerData.get().getId(), customerData.get().getName(), customerData.get().getLast_name(), customerData.get().getPhone(), customerData.get().getEmail(), customerData.get().getGender(), customerData.get().getDate_of_birth(), customerData.get().getPassword());
            } else {
                throw new CustomerException("Check Login credentials");
            }


        } else {
            throw new CustomerException("Invalid username");
        }

    }

    public Customer addNewCustomer(Customer customer) {
        Customer u = new Customer(customer);
        List<String> errors = u.validate();

        if (errors.isEmpty()) {

            // check email and phone to be unique
            long count = customerRepository.countByEmailOrPhone(u.getEmail(), u.getPhone());
            if ( count > 0 ) { // not unique
                errors.add("Customer exists.");
                throw new CustomerException("Customer Already exists");
            } else {
                u.setPassword(u.encPassword());
                customerRepository.save(u);
                return u;
            }

        } else {
            throw new CustomerException("Data Incorrect");
        }
    }

    public Customer deleteCustomer(Integer id) {
        Optional<Customer> customerData = customerRepository.findById(id);

        if (customerData.isPresent()) {
            customerRepository.deleteById(id);
            return customerData.get();
        } else {
            throw new CustomerException("Invalid Customer");
        }
    }
}
