package com.whitedoc.db.migration;

import org.reflections.Reflections;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;

final public class Migrator {

    private DataSource ds;
    private String versionsTableName = "migrations";
    private HashSet<String> doneMigrations = new HashSet<>();
    private String migrationsClasspath = Migrator.class.getPackage().getName();
    private String seedsClasspath = Migrator.class.getPackage().getName().replace("migration", "seed");

    public Migrator(DataSource ds) {
        this.ds = ds;
    }

    public Migrator(DataSource ds, String versionsTableName) {
        this.ds = ds;
        this.versionsTableName = versionsTableName;
    }

    public static <T> T instantiate(final String className, final Class<T> type) {
        try {
            return type.cast(Class.forName(className).getDeclaredConstructor().newInstance());
        } catch (InstantiationException |
                IllegalAccessException |
                ClassNotFoundException |
                NoSuchMethodException |
                InvocationTargetException e
        ) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * For migrations `M20190823000001_CreateUsersTable` -> `20190823000001`
     * For seeds `S1001_CreateUsers` -> `1001`
     */
    public static String getVersion(Class clazz) {
        return clazz.getSimpleName().substring(1, clazz.getSimpleName().indexOf("_"));
    }

    public void setMigrationsClasspath(String migrationsClasspath) {
        this.migrationsClasspath = migrationsClasspath;
    }

    public void setSeedsClasspath(String seedsClasspath) {
        this.seedsClasspath = seedsClasspath;
    }

    private void initVersions() {
        try (Connection conn = this.ds.getConnection()) {
            PreparedStatement stm = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + this.versionsTableName + " (" +
                            "  version varchar(14) NOT NULL" +
                            ") ENGINE=InnoDB"
            );

            stm.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void loadVersions() {
        try (Connection conn = this.ds.getConnection()) {
            PreparedStatement stm = conn.prepareStatement("select version from " + this.versionsTableName);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                this.doneMigrations.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void setVersion(String version) {
        try (Connection conn = this.ds.getConnection()) {
            PreparedStatement stm = conn.prepareStatement(
                    "INSERT INTO " + this.versionsTableName + " (version) VALUES (" + version + ")"
            );

            stm.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void complete() {
        run();
        seed();
    }

    /**
     * Search for seeds at `seedsClasspath` and run them
     */
    public void seed() {
        new Reflections(this.seedsClasspath)
                .getSubTypesOf(Seed.class)
                .stream()
                .filter(o -> !Modifier.isAbstract(o.getModifiers()))
                .sorted(Comparator.comparing(o -> Long.valueOf(Migrator.getVersion(o))))
                .forEach(m -> {
                    try {
                        System.out.println("Started seed of " + m.getName());
                        // instantiate migration class
                        Seed mg = Migrator.instantiate(m.getName(), Seed.class);
                        mg.setDataSource(this.ds);
                        // migrate
                        mg.up();
                        // done
                        System.out.println("Ended migration of " + m.getName());

                    } catch (SQLException e) {
                        // exit if error
                        e.printStackTrace();
                        System.exit(0);
                    }
                });
    }

    /**
     * Search for migrations at `migrationsClasspath` and run them
     */
    public void run() {

        this.initVersions();
        this.loadVersions();

        new Reflections(this.migrationsClasspath)
                .getSubTypesOf(Migration.class)
                .stream()
                .filter(o -> !Modifier.isAbstract(o.getModifiers()))
                .sorted(Comparator.comparing(o -> Long.valueOf(Migrator.getVersion(o))))
                .filter(o -> !this.doneMigrations.contains(Migrator.getVersion(o)))
                .forEach(m -> {
                    try {
                        System.out.println("Started migration of " + m.getName());
                        // instantiate migration class
                        Migration mg = Migrator.instantiate(m.getName(), Migration.class);
                        mg.setDataSource(this.ds);
                        // migrate
                        mg.up(); // @todo down?!
                        // store version
                        this.setVersion(Migrator.getVersion(mg.getClass()));
                        // done
                        System.out.println("Ended migration of " + m.getName());

                    } catch (SQLException e) {
                        // exit if error
                        e.printStackTrace();
                        System.exit(0);
                    }
                });
    }
}
