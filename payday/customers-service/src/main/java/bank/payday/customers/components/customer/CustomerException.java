package bank.payday.customers.components.customer;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomerException extends RuntimeException implements GraphQLError {

    private String invalidField;

    public CustomerException(String message, String invalidField) {
        super(message);
        this.invalidField = invalidField;
    }

    public CustomerException(String message) {
        super(message);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return null;
    }

    @Override
    public List<Object> getPath() {
        return GraphQLError.super.getPath();
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Collections.singletonMap("invalidField", invalidField);
    }
}
