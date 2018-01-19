package com.datastax.driver.mapping;

import com.datastax.driver.core.PreparedStatement;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import smartthings.brave.cassandra.driver.NamedPreparedStatement;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class TracedAccessorMapper<T> {

    private static final Converter<String, String> caseFormatConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN);
    private final AccessorMapper<T> deletgate;

    public TracedAccessorMapper(AccessorMapper<T> deletgate) {
        this.deletgate = deletgate;
    }

    T createProxy() {
        return deletgate.createProxy();
    }

    public void prepare(MappingManager manager) {
        List<ListenableFuture<Void>> statements = deletgate.methods.stream().map ( method -> Futures.transform(
            manager.getSession().prepareAsync(method.queryString),
            new Function<PreparedStatement, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable PreparedStatement input) {
                    method.prepare(
                        manager,
                        NamedPreparedStatement.from(input, caseFormatConverter.convert(method.method.getName()))
                    );
                    return null;
                }
            }
        )).collect(Collectors.toList());

        try {
            Futures.allAsList(statements).get();
        } catch (Exception e) {
            throw new RuntimeException("Error preparing queries for accessor " + deletgate.daoClass.getSimpleName(), e);
        }
    }
}
