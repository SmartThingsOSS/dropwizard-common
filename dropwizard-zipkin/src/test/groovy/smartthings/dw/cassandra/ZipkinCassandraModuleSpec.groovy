package smartthings.dw.cassandra

import com.codahale.metrics.health.HealthCheck
import com.datastax.driver.core.Session
import com.datastax.driver.mapping.MappingManager
import com.datastax.driver.mapping.TracedMappingManager
import com.github.kristofa.brave.Brave
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
import smartthings.brave.cassandra.TracedSession
import smartthings.dw.guice.AbstractDwModule
import smartthings.dw.zipkin.ZipkinConfiguration

import static org.mockito.Mockito.when

@RunWith(PowerMockRunner)
@PrepareForTest([ZipkinCassandraModule, TracedSession, TracedMappingManager])
class ZipkinCassandraModuleSpec {

    String svcName  ="ZipkinCassandraModuleSpec"
    @Mock
    ZipkinConfiguration zipkinConfig
    @Mock
    Brave brave
    @Mock
    CassandraConfiguration config
    @Mock
    Session session
    @Mock
    Session tracedSession
    @Mock
    TracedMappingManager tracedMappingManager

    private static class DepModule extends AbstractDwModule {
        private final Brave brave
        private final ZipkinConfiguration config
        DepModule(ZipkinConfiguration config, Brave brave) {
            this.brave = brave
            this.config = config
        }
        @Override
        protected void configure() {
            bind(Brave).toInstance(brave)
            bind(ZipkinConfiguration).toInstance(config)
        }
    }

    @Before
    void setup() {
        MockitoAnnotations.initMocks(this)
        PowerMockito.mockStatic(TracedSession)
        PowerMockito.whenNew(TracedMappingManager).withAnyArguments().thenReturn(tracedMappingManager)
        when(TracedSession.create(session, brave, svcName)).thenReturn(tracedSession)
        when(config.buildSession()).thenReturn(session)
        when(zipkinConfig.getServiceName()).thenReturn(svcName)
    }

    @Test
    void 'register Cassandra health check, provide TracedSession and TracedMappingManager'() {
        Injector injector = Guice.createInjector(
            new DepModule(zipkinConfig, brave),
            new ZipkinCassandraModule(config)
        )

        def checksBindings = injector.findBindingsByType(new TypeLiteral<Set<HealthCheck>>() {})

        assert checksBindings.first().provider.get().find { it instanceof CassandraHealthCheck }

        Session sessionInst = injector.getInstance(Session)

        assert sessionInst == tracedSession

        MappingManager mappingManager = injector.getInstance(MappingManager)

        assert mappingManager == tracedMappingManager
    }
}
