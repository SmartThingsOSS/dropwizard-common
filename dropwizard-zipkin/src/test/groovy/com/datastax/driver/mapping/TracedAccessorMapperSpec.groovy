package com.datastax.driver.mapping

import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.Session
import com.datastax.driver.mapping.MethodMapper.ParamMapper
import com.google.common.util.concurrent.SettableFuture
import smartthings.brave.cassandra.driver.NamedPreparedStatement
import smartthings.dw.cassandra.StuffAccessor
import spock.lang.Specification

import java.lang.reflect.Method
import java.lang.reflect.Proxy

class TracedAccessorMapperSpec extends Specification {
    AccessorMapper<Object> delegate
    TracedAccessorMapper<Object> mapper

    def "create proxy should delegate"() {
        given:
        delegate = Mock(AccessorMapper)
        mapper = new TracedAccessorMapper<Object>(delegate)
        Object mock = new Object()

        when:
        Object result = mapper.createProxy()

        then:
        1 * delegate.createProxy() >> mock
        result == mock
    }

    def "prepare should wrap delegate method with NamedPreparedStatements"() {
        given:
        Method method = StuffAccessor.class.getMethod("findMyStuff")
        String query = "this is a query"
        def paramMappers = [] as ParamMapper[]
        PreparedStatement preparedStatement = Mock(PreparedStatement)
        MethodMapper methodMapper = Spy(MethodMapper, constructorArgs: [method, query, paramMappers, null, 0, false, null]) {
            1 * prepare(_, _) >> { MappingManager manager, PreparedStatement ps ->
                assert ps instanceof Proxy
                assert ps.h instanceof NamedPreparedStatement
                assert ps.h.name == "find-my-stuff"
                assert ps.h.target == preparedStatement
            }
        }
        SettableFuture future = SettableFuture.create()
        future.set(preparedStatement)
        MappingManager manager = Mock(MappingManager)
        Session session = Mock(Session)
        manager.getSession() >> session
        delegate = Spy(AccessorMapper, constructorArgs: [Object, [methodMapper]])
        mapper = new TracedAccessorMapper<Object>(delegate)

        when:
        mapper.prepare(manager)

        then:
        1 * session.prepareAsync(query) >> future
    }

}
