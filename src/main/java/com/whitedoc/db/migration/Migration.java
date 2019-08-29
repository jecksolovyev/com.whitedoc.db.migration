package com.whitedoc.db.migration;

import java.sql.SQLException;

public interface Migration extends DataParcel {
    void down() throws SQLException;
}
