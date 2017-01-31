package com.datastax.driver.mapping

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ProtocolVersion
import com.datastax.driver.core.Session
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import smartthings.dw.cassandra.StuffAccessor

@RunWith(PowerMockRunner)
@PrepareForTest([TracedMappingManager, TracedAccessorMapper])
class TracedMappingManagerSpec {

    @Mock
    StuffAccessor accessor
    @Mock
    TracedAccessorMapper tracedAccessorMapper
    @Mock
    Cluster cluster
    @Mock
    Session session

    @Before
    void setup() {
        MockitoAnnotations.initMocks(this)
        Mockito.when(tracedAccessorMapper.createProxy()).thenReturn(accessor)
        Mockito.when(session.getCluster()).thenReturn(cluster)
        PowerMockito.whenNew(TracedAccessorMapper).withAnyArguments().thenReturn(tracedAccessorMapper)
    }

    @Test
    void "create accessor should use TracedAccessorMapper"() {
        TracedMappingManager tracedMappingManager = new TracedMappingManager(session, ProtocolVersion.V3)
        StuffAccessor result = tracedMappingManager.createAccessor(StuffAccessor)
        assert result == accessor
    }
}
