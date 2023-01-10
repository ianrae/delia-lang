package org.delia.type;

import java.util.Objects;

public class DTypeName {
    private String schema;
    private String typeName;

    public DTypeName(String schema, String typename) {
        this.schema = schema;
        this.typeName = typename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DTypeName dTypeName = (DTypeName) o;
        return Objects.equals(schema, dTypeName.schema) && typeName.equals(dTypeName.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, typeName);
    }

    @Override
    public String toString() {
        if (schema == null) {
            return typeName;
        }
        return String.format("%s.%s", schema, typeName);
    }

    public String getSchema() {
        return schema;
    }
    public String getTypeName() {
        return typeName;
    }

    public boolean isEqual(String typeName) {
        if (schema != null) return false;
        return this.typeName.equalsIgnoreCase(typeName);
    }
}
