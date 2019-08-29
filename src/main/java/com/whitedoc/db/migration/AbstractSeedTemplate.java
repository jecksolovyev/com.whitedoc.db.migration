package com.whitedoc.db.migration;

import java.sql.SQLException;

public abstract class AbstractSeedTemplate extends AbstractSeed {

    public void up() throws SQLException {
        /*
        try (Connection conn = this.getDataSource().getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement stm2 = conn.prepareStatement("INSERT INTO Users (UserId, Name) VALUES (?, ?)");
            stm2.setInt(1, 1);
            stm2.setString(2, "Jack");

            stm2.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
        }
        */
    }
}
