
# Delia

[Delia](https://delia-lang.org) is a data language for creating, managing, and querying SQL databases.

```
type Customer struct {
 id int primaryKey,
 firstName string,
 lastName string
 birthDate date optional
 relation address Address one optional
} end
```

Delia lets you fully manage your data:
 * define a complete schema. Define types, fields, relations, and validation rules.
 * update the schema. Delia will perform schema SQL migrations automatically, as needed.
 * define your queries.  It's a compiled language so they're type-safe.
 * insert your seed data (i.e. initial data).
 * perform CRUD operations from your application programming language

#### Why Delia?
 * Simpler object-based approach to data access. 
 * Manage schema, CRUD, and queries in one place.
 * the Delia compiler catches many SQL errors at compile time.
 * Delia code is just text, and can be modified dynamically in your application.

Tutorial and documentation: https://delia-lang.org

