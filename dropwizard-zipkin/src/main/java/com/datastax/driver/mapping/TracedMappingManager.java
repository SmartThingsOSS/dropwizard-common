package com.datastax.driver.mapping;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TracedMappingManager extends MappingManager {
    private volatile Map<Class<?>, Object> accessors = Collections.emptyMap();

    public TracedMappingManager(Session session) {
        super(session);
    }

    public TracedMappingManager(Session session, ProtocolVersion protocolVersion) {
        super(session, protocolVersion);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createAccessor(Class<T> klass) {
        T accessor = (T) accessors.get(klass);
        if (accessor == null) {
            synchronized (accessors) {
                accessor = (T) accessors.get(klass);
                if (accessor == null) {
                AccessorMapper<T> mapper = AnnotationParser.parseAccessor(klass, this);
                    TracedAccessorMapper<T> tracedMapper = new TracedAccessorMapper(mapper);
                    tracedMapper.prepare(this);
                    accessor = tracedMapper.createProxy();
                    Map<Class<?>, Object> newAccessors = new HashMap<>(accessors);
                    newAccessors.put(klass, accessor);
                    accessors = newAccessors;
                }
            }
        }
        return accessor;
    }
}
