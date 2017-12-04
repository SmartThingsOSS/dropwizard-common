package smartthings.dw.cassandra.aspect;

import com.datastax.driver.core.*;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 * Definition of Aspect for processing the Pointcuts. This is done by AspectJ weaver.
 */

@Aspect
@Singleton
public class PruneCqlAspect {

    /**
     * Check for any method that has {@link PruneCql} annotation on it.
     */
    @Pointcut("@annotation(smartthings.dw.cassandra.aspect.PruneCql)")
    public void callPruneCql(){}

    /**
     * The annotation can be anywhere in the package. The requirement is that the method needs to return an
     * implementation of {@link Statement}
     */
    @Pointcut("execution(* *(..))")
    public void duringExecution(){}

    /**
     *
     * @param joinPoint
     * @return object ({@link Statement}
     * @throws Throwable
     */
    @Around("callPruneCql() && duringExecution()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        if(!(joinPoint.proceed() instanceof Statement)){
            return joinPoint.proceed();
        }

        BoundStatement incomingStatement = (BoundStatement) joinPoint.proceed();
        PreparedStatement incomingPrep =  incomingStatement.preparedStatement();
        String [] queryStringArray = incomingPrep.getQueryString().split(" ");
        String firstWord = queryStringArray[0].toUpperCase();

        if(firstWord.equals("INSERT")){
            return evaluateInsert(incomingStatement);
        }else{
            return evaluateUpdate(incomingStatement);
        }
    }

    /**
     * Evaluates an Update CQL
     *
     * @param incomingStatement
     * @return object ({@link Statement}
     *
     */
     Statement evaluateUpdate(BoundStatement incomingStatement) {

        PreparedStatement incomingPrep = incomingStatement.preparedStatement();

        String tableName = incomingPrep.getVariables().getTable(0);

        StringBuffer updateBuffer = new StringBuffer("UPDATE " + tableName + " SET ");

        String queryString = incomingStatement.preparedStatement().getQueryString().toUpperCase();

        String [] valuePairs = queryString.substring(queryString.indexOf("SET") + 3, queryString.indexOf("WHERE")).split(",");

        ImmutableMap.Builder<String, Object> valueMapBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<String, Object> valueMapForWhereBuilder = ImmutableMap.builder();

        int i = 0;
        for (ColumnDefinitions.Definition definition : incomingPrep.getVariables()) {
            Object valueObj = incomingStatement.getObject(definition.getName());
            if(valueObj != null && i < valuePairs.length){
                updateBuffer.append(valuePairs[i++]);
                insertCommaSeparator(updateBuffer, valuePairs, i);
                valueMapBuilder.put(definition.getName(), valueObj);
            }else if(valueObj != null && i>= valuePairs.length){
                valueMapForWhereBuilder.put(definition.getName(), valueObj);
            }else if(valueObj == null){
                i+=1;
            }
        }

        if(valueMapBuilder.build().size() == valuePairs.length){
            return incomingStatement;
        }

        String whereBuffer = queryString.substring(queryString.indexOf("WHERE"), queryString.length());

        String finalQueryString = updateBuffer.append(" " + whereBuffer).toString();

        SimpleStatement statement = new SimpleStatement(finalQueryString, valueMapBuilder.putAll(valueMapForWhereBuilder.build()).build());

        return statement;

    }

    /**
     * Evaluates an INSERT CQL
     *
     * @param incomingStatement
     * @return
     */
     Statement evaluateInsert(BoundStatement incomingStatement) {

        PreparedStatement incomingPrep = incomingStatement.preparedStatement();

        String tableName = incomingPrep.getVariables().getTable(0);

        StringBuffer insertBuffer = new StringBuffer("INSERT INTO " + tableName + " (");

        StringBuffer valuesBuffer = new StringBuffer(" VALUES (");

        String queryString = incomingStatement.preparedStatement().getQueryString().toUpperCase();
        String [] columns = queryString.substring(queryString.indexOf("(") + 1, queryString.indexOf(")")).split(",");

        ImmutableMap.Builder<String, Object> valueMapBuilder = ImmutableMap.builder();

        int i = 0;
        for (ColumnDefinitions.Definition definition : incomingPrep.getVariables()) {
            Object valueObj = incomingStatement.getObject(definition.getName());
            if(valueObj != null){
                insertBuffer.append(columns[i++]);
                insertCommaSeparator(insertBuffer, columns, i);
                valuesBuffer.append(":" + definition.getName());
                insertCommaSeparator(valuesBuffer, columns, i);
                valueMapBuilder.put(definition.getName(), valueObj);
            }else{
                i+=1;
            }
        }

        if(valueMapBuilder.build().size() == columns.length){
            return incomingStatement;
        }

        String finalQueryString = insertBuffer.append(")").append(valuesBuffer).append(")").toString();

        SimpleStatement statement = new SimpleStatement(finalQueryString, valueMapBuilder.build());

        return statement;
    }

    /**
     * Convenience method
     * @param insertBuffer
     * @param columns
     * @param i
     */
    private void insertCommaSeparator(StringBuffer insertBuffer, String[] columns, int i) {
        if(i < columns.length){
            insertBuffer.append(",");
        }
    }

}
