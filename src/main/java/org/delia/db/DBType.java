package org.delia.db;

/**
 * The set of supported databases
 *
 * @author Ian Rae
 *
 */
public enum DBType {
    MEM, //an in-memory database. no jdbc or sql. just java objects. for unit testing only.
    H2,
    POSTGRES
}