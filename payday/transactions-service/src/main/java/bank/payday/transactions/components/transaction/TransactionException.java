package bank.payday.transactions.components.transaction;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TransactionException extends RuntimeException implements GraphQLError {

    private String invalidField;

    public TransactionException(String message, String invalidField) {
        super(message);
        this.invalidField = invalidField;
    }

    public TransactionException(String message) {
        super(message);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return new ArrayList<>();
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
