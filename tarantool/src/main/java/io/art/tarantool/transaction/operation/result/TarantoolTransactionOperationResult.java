package io.art.tarantool.transaction.operation.result;

import io.art.tarantool.exception.TarantoolDaoException;
import io.art.tarantool.transaction.operation.dependency.TransactionFieldDependency;
import io.art.tarantool.transaction.operation.dependency.TarantoolTransactionDependency;
import io.art.tarantool.transaction.operation.dependency.TransactionTupleDependency;
import io.art.tarantool.exception.TarantoolTransactionException;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static io.art.core.caster.Caster.cast;
import static io.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.GET_RESULT_OF_NOT_COMMITTED_TRANSACTION;
import static io.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.UNABLE_TO_GET_RESPONSE;
import static io.art.tarantool.model.TarantoolResponseMapping.transactionResponseToTuple;
import static java.util.Objects.isNull;


public class TarantoolTransactionOperationResult<T> implements TarantoolOperationResult<T>{
    private final int transactionEntryNumber;
    private CompletableFuture<Optional<T>> futureResult;
    private final Function<List<?>, Optional<T>> responseMapper;

    public TarantoolTransactionOperationResult(int transactionEntryNumber, Function<List<?>, Optional<?>> responseMapper){
        this.transactionEntryNumber = transactionEntryNumber;
        this.responseMapper = cast(responseMapper);
    }

    public void setFutureTransactionResponse(CompletableFuture<List<?>> futureResponse) {
        this.futureResult = futureResponse
                .thenApply(response -> transactionResponseToTuple(response, transactionEntryNumber))
                .thenApply(responseMapper);
    }

    @Delegate
    public Optional<T> getOptional(){
        if (isNull(futureResult)) throw new TarantoolTransactionException(GET_RESULT_OF_NOT_COMMITTED_TRANSACTION);
        try {
            return futureResult.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new TarantoolDaoException(UNABLE_TO_GET_RESPONSE);
        }
    }

    @Override
    public TarantoolTransactionDependency useResult(){
        return new TransactionTupleDependency(transactionEntryNumber);
    }

    @Override
    public TarantoolTransactionDependency useResultField(String fieldName){
        return new TransactionFieldDependency(transactionEntryNumber, fieldName);
    }

    @Override
    public boolean isDone() {
        return futureResult.isDone();
    }

    public TarantoolTransactionOperationResult<T> synchronize(){
        getOptional();
        return this;
    }

}
