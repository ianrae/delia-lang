package org.delia.hld;

/**
 * These can be used in an update statement
 *    update Address[100] { insert cust:56}
 * Used mainly in ManyToMany relations to add/delete/update rows in the assoc table
 */
public enum CrudAction {
    INSERT,
    UPDATE,
    DELETE
}
