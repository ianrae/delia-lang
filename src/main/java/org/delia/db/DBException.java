package org.delia.db;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;

import java.util.List;

/**
 * unexpected database error
 *
 * @author Ian Rae
 */
public class DBException extends DeliaException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DBException(DeliaError err) {
        super(err);
    }

    public DBException(List<DeliaError> errL) {
        super(errL);
    }

}