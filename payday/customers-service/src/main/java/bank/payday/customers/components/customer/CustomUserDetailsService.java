package bank.payday.customers.components.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    CustomerRepository jwtUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Customer> jwtUser = jwtUserRepository.findByEmail(email);
        if (jwtUser.isEmpty()) {
            throw new UsernameNotFoundException("email Not found" + email);
        }
        return new User(jwtUser.get().getEmail(), jwtUser.get().getPassword(), new ArrayList<>());
    }
}
