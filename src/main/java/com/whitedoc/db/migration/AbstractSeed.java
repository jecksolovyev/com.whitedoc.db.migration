package com.whitedoc.db.migration;

import javax.sql.DataSource;

public abstract class AbstractSeed implements Seed {

    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
