package smartthings.dw.cassandra

import brave.Tracing
import brave.cassandra.driver.CassandraClientTracing
import brave.cassandra.driver.TracingSession
import com.codahale.metrics.health.HealthCheck
import com.datastax.driver.core.Session
import com.datastax.driver.mapping.MappingManager
import com.datastax.driver.mapping.TracedMappingManager
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.TypeLiteral
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import smartthings.dw.guice.AbstractDwModule
import smartthings.dw.zipkin.ZipkinConfiguration

import static org.mockito.Mockito.eq
import static org.mockito.Mockito.anyObject
import static org.mockito.Mockito.when

@RunWith(PowerMockRunner)
@PrepareForTest([ZipkinCassandraModule, TracingSession, TracedMappingManager])
class ZipkinCassandraModuleSpec {

    String svcName  ="ZipkinCassandraModuleSpec"
    @Mock
    ZipkinConfiguration zipkinConfig
    @Mock
    Tracing tracing
    @Mock
    CassandraConfiguration config
    @Mock
    Session session
    @Mock
    Session tracedSession
    @Mock
    TracedMappingManager tracedMappingManager

    private static class DepModule extends AbstractDwModule {
        private final Tracing tracing
        private final ZipkinConfiguration config
        DepModule(ZipkinConfiguration config, Tracing tracing) {
            this.tracing = tracing
            this.config = config
        }
        @Override
        protected void configure() {
            bind(Tracing).toInstance(tracing)
            bind(ZipkinConfiguration).toInstance(config)
        }
    }

    @Before
    void setup() {
        MockitoAnnotations.initMocks(this)
        PowerMockito.mockStatic(TracingSession)
        PowerMockito.whenNew(TracedMappingManager).withAnyArguments().thenReturn(tracedMappingManager)
        when(TracingSession.create((CassandraClientTracing)anyObject(), eq(session))).thenReturn(tracedSession)
        when(config.buildSession()).thenReturn(session)
        when(zipkinConfig.getServiceName()).thenReturn(svcName)
    }

    @Test
    void 'register Cassandra health check, provide TracedSession and TracedMappingManager'() {
        Injector injector = Guice.createInjector(
            new DepModule(zipkinConfig, tracing),
            new ZipkinCassandraModule(config)
        )

        def cassandraTracing = injector.getInstance(CassandraClientTracing)

        assert cassandraTracing.tracing() == tracing

        def checksBindings = injector.findBindingsByType(new TypeLiteral<Set<HealthCheck>>() {})

        assert checksBindings.first().provider.get().find { it instanceof CassandraHealthCheck }

        Session sessionInst = injector.getInstance(Session)

        assert sessionInst == tracedSession

        MappingManager mappingManager = injector.getInstance(MappingManager)

        assert mappingManager == tracedMappingManager
    }
}
