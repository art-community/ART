package ru.art.sql.state;

import lombok.*;
import lombok.experimental.*;
import ru.art.core.module.*;
import javax.sql.*;

@Getter
@Setter
@Accessors(fluent = true)
public class SqlModuleState implements ModuleState {
    private volatile DataSource hikariDataSource;
    private volatile DataSource tomcatDataSource;
}
