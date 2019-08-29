# MySQL Migrator
The idea behind this lib is to migrate your DB before your application starts.

## Create Migrations
Template to use `com.whitedoc.db.migration.AbstractMigrationTemplate`. 

ClassName format is: M`<Long version-number>`_`<ClassName>`

For the sake of completeness, the example class could be: `M20190829000000_CreateUsersTable.java`

Files may be prefixed with `M` (one letter of your choice actually), then migration 
goes version number (I recommend use creation date `YYYYMMDDHHIISS`).

Migrations will be sorted by versions numbers in natural order and then run 
one by one starting from minimal version. 

All successfully run migrations is registered in special `migrations` table into 
your database and won't be run once again.

### Spring Beans Runner
```xml
<bean class="com.whitedoc.db.migration.Migrator" depends-on="dataSource" init-method="run">
    <constructor-arg ref="dataSource"/> <!-- inject DataSource -->
    <property name="migrationsClasspath" value="your.app.package.migrations"/>
</bean>
```

### Run Within Code
```java
Migrator migrator = new Migrator(dataSource);
migrator.setMigrationsClasspath("your.app.package.migrations");
migrator.run();
```

## Create Seeds
Sometimes, you need to seed your database with data, for instance before tests run.

Template to use `com.whitedoc.db.migration.AbstractSeedTemplate`.

ClassName format is: S`<Long run-order-number>`_`<ClassName>`

Files may be prefixed with `S` (one letter of your choice actually), and then run-order number 
(I recommend use creation date `YYYYMMDDHHIISS`).

Seed will be sorted by run-order numbers in natural order and then run 
one by one starting from minimal one. 

### Spring Beans Runner
```xml
<bean class="com.whitedoc.db.migration.Migrator" depends-on="dataSource" init-method="seed">
    <constructor-arg ref="dataSource"/> <!-- inject DataSource -->
    <property name="seedsClasspath" value="your.app.package.seeds"/>
</bean>
```

### Run Within Code
```java
Migrator migrator = new Migrator(dataSource);
migrator.setSeedsClasspath("your.app.package.seeds");
migrator.seed();
```

## Extra! Convenient Method
Method `complete()` makes `run` and then `seed`, thus creates structure and seeds database with data.
```java
Migrator migrator = new Migrator(dataSource);
migrator.setSeedsClasspath("your.app.package.seeds");
migrator.complete();
```

## Where is down()?
That is yet to come =) Feel free to propose your implementation.