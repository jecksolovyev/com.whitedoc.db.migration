package com.whitedoc.db.migration;

import javax.sql.DataSource;
import java.sql.SQLException;

public interface DataParcel {

    void up() throws SQLException;

    DataSource getDataSource();

    void setDataSource(DataSource dataSource);
}
