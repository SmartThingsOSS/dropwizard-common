package smartthings.dw.cassandra;

import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface StuffAccessor {
    @Query("SELECT * FROM my_stuff")
    Statement findMyStuff();
}
