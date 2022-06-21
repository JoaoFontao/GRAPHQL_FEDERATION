package bank.payday.customers.components.customer;

import bank.payday.customers.JwtUtil;
import bank.payday.customers.generated.types.AuthPayload;
import com.netflix.graphql.dgs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@RequiredArgsConstructor
@DgsComponent
public class CustomerDataFetcher {

    private final JwtUtil jwtUtil;


    private final AuthenticationManager authenticationManager;

    private final CustomerService customerService;

    @DgsQuery
    public List<Customer> customers() {
        return customerService.getAllCustomers();
    }

    @DgsQuery
    public Customer customer(@RequestHeader("user") String user) {
        Integer customer = Integer.parseInt(user.replaceAll("\"",""));
        return customerService.getCustomerById(customer);
    }

    @DgsMutation
    public AuthPayload signup(@InputArgument String name, @InputArgument String last_name, @InputArgument String password, @InputArgument String email, @InputArgument String gender, @InputArgument String phone, @InputArgument String date_of_birth){
        Customer customer = this.customerService.addNewCustomer(new Customer(name, last_name, phone, email, gender, date_of_birth, password));

        UsernamePasswordAuthenticationToken uspas = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(uspas);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = new User(customer.getId().toString(), password, new ArrayList<>());

        final String jwt = jwtUtil.generateToken(userDetails);
        return AuthPayload.newBuilder().token(jwt).customer(bank.payday.customers.generated.types.Customer.newBuilder().email(customer.getEmail()).id(customer.getId().toString()).gender(customer.getGender()).date_of_birth(customer.getDate_of_birth()).name(customer.getName()).last_name(customer.getLast_name()).phone(customer.getPhone()).build()).build();
    }

    @DgsMutation
    public AuthPayload login(@InputArgument String login, @InputArgument String password){
        Customer customer = this.customerService.validateCustomer(login, password);
        UsernamePasswordAuthenticationToken uspas = new UsernamePasswordAuthenticationToken(login, password);
        Authentication authentication = authenticationManager.authenticate(uspas);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = new User(customer.getId().toString(), password, new ArrayList<>());

        final String jwt = jwtUtil.generateToken(userDetails);
        return AuthPayload.newBuilder().token(jwt).customer(bank.payday.customers.generated.types.Customer.newBuilder().email(customer.getEmail()).id(customer.getId().toString()).gender(customer.getGender()).date_of_birth(customer.getDate_of_birth()).name(customer.getName()).last_name(customer.getLast_name()).phone(customer.getPhone()).build()).build();
    }


}
