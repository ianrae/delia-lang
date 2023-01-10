package org.delia.migration;

public enum MigrationAction {
    NONE,  //delia does not create any tables or do any DDL
    GENERATE, //delia creates tables (i.e. assumes database is empty)
    AUTO_MIGRATE //delia manages the schema
    //TODO support AUTO_MIGRATE later
}
