package smartthings.dw.cassandra.aspect

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ColumnDefinitions
import com.datastax.driver.core.DataType
import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.SimpleStatement
import org.aspectj.lang.ProceedingJoinPoint
import spock.lang.Specification



class PruneCqlAspectTest extends Specification {

    BoundStatement boundStatement
    PreparedStatement preparedStatement
    PruneCqlAspect pruneCqlAspect
    ProceedingJoinPoint proceedingJoinPoint
    ColumnDefinitions columnDefinitions
    ColumnDefinitions.Definition definition1
    ColumnDefinitions.Definition definition2
    ColumnDefinitions.Definition definition3
    Iterator<ColumnDefinitions.Definition> iterator
    ColumnDefinitions.Definition [] definitionArray

    def setup () {
        boundStatement = Mock()
        preparedStatement = Mock()
        proceedingJoinPoint = Mock()
        columnDefinitions = Mock()

        pruneCqlAspect = new PruneCqlAspect()
    }

    def "should return the incoming BoundStatement when no values are null for insert"(){

        given:
        definition1 = new ColumnDefinitions.Definition("marconi", "device", "hubid", DataType.varchar())
        definition2 = new ColumnDefinitions.Definition("marconi", "device", "deviceid", DataType.varchar())
        definition3 = new ColumnDefinitions.Definition("marconi", "device", "canopyid", DataType.varchar())
        definitionArray = new ColumnDefinitions.Definition [3]
        definitionArray.putAt(0, definition1)
        definitionArray.putAt(1, definition2)
        definitionArray.putAt(2, definition3)
        iterator = definitionArray.iterator()
        proceedingJoinPoint.proceed() >> boundStatement
        boundStatement.preparedStatement() >> preparedStatement
        preparedStatement.getQueryString() >> "INSERT INTO table(hub_id, device_id, canopy_id) VALUES (:hubid, :deviceid, :canopyid)"
        preparedStatement.getVariables() >> columnDefinitions
        columnDefinitions.iterator() >> iterator
        boundStatement.getObject("hubid") >> "1"
        boundStatement.getObject("deviceid") >> "2"
        boundStatement.getObject("canopyid") >> "3"

        when:
         def returnObj = pruneCqlAspect.around(proceedingJoinPoint)

        then:
        assert returnObj instanceof BoundStatement
    }

    def "should return the incoming BoundStatement when some values are null for insert"(){

        given:
        definition1 = new ColumnDefinitions.Definition("marconi", "device", "hubid", DataType.varchar())
        definition2 = new ColumnDefinitions.Definition("marconi", "device", "deviceid", DataType.varchar())
        definition3 = new ColumnDefinitions.Definition("marconi", "device", "canopyid", DataType.varchar())
        definitionArray = new ColumnDefinitions.Definition [3]
        definitionArray.putAt(0, definition1)
        definitionArray.putAt(1, definition2)
        definitionArray.putAt(2, definition3)
        iterator = definitionArray.iterator()
        proceedingJoinPoint.proceed() >> boundStatement
        boundStatement.preparedStatement() >> preparedStatement
        preparedStatement.getQueryString() >> "INSERT INTO table(hub_id, device_id, canopy_id) VALUES (:hubid, :deviceid, :canopyid)"
        preparedStatement.getVariables() >> columnDefinitions
        columnDefinitions.iterator() >> iterator
        boundStatement.getObject("hubid") >> "1"
        boundStatement.getObject("deviceid") >> "2"
        boundStatement.getObject("canopyid") >> null

        when:
        def returnObj = pruneCqlAspect.around(proceedingJoinPoint)

        then:
        assert returnObj instanceof SimpleStatement
    }

    def "should return the incoming BoundStatement when no values are null for update"(){

        given:
        definition1 = new ColumnDefinitions.Definition("marconi", "device", "deviceid", DataType.varchar())
        definition2 = new ColumnDefinitions.Definition("marconi", "device", "canopyid", DataType.varchar())
        definition3 = new ColumnDefinitions.Definition("marconi", "device", "hubid", DataType.varchar())
        definitionArray = new ColumnDefinitions.Definition [3]
        definitionArray.putAt(0, definition1)
        definitionArray.putAt(1, definition2)
        definitionArray.putAt(2, definition3)
        iterator = definitionArray.iterator()
        proceedingJoinPoint.proceed() >> boundStatement
        boundStatement.preparedStatement() >> preparedStatement
        preparedStatement.getQueryString() >> "UPDATE INTO table SET device_id = :deviceId, canopy_id = :canopyId WHERE hub_id = :hubId"
        preparedStatement.getVariables() >> columnDefinitions
        columnDefinitions.iterator() >> iterator
        boundStatement.getObject("hubid") >> "1"
        boundStatement.getObject("deviceid") >> "2"
        boundStatement.getObject("canopyid") >> "3"

        when:
        def returnObj = pruneCqlAspect.around(proceedingJoinPoint)

        then:
        assert returnObj instanceof BoundStatement
    }

    def "should return the incoming BoundStatement when some values are null for update"(){

        given:
        definition1 = new ColumnDefinitions.Definition("marconi", "device", "deviceid", DataType.varchar())
        definition2 = new ColumnDefinitions.Definition("marconi", "device", "canopyid", DataType.varchar())
        definition3 = new ColumnDefinitions.Definition("marconi", "device", "hubid", DataType.varchar())
        definitionArray = new ColumnDefinitions.Definition [3]
        definitionArray.putAt(0, definition1)
        definitionArray.putAt(1, definition2)
        definitionArray.putAt(2, definition3)
        iterator = definitionArray.iterator()
        proceedingJoinPoint.proceed() >> boundStatement
        boundStatement.preparedStatement() >> preparedStatement
        preparedStatement.getQueryString() >> "UPDATE INTO table SET device_id = :deviceId, canopy_id = :canopyId WHERE hub_id = :hubId"
        preparedStatement.getVariables() >> columnDefinitions
        columnDefinitions.iterator() >> iterator
        boundStatement.getObject("hubid") >> "1"
        boundStatement.getObject("deviceid") >> "2"
        boundStatement.getObject("canopyid") >> null

        when:
        def returnObj = pruneCqlAspect.around(proceedingJoinPoint)

        then:
        assert returnObj instanceof SimpleStatement
    }
}
