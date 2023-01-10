package org.delia.migrationparser;

public class RelationDetails {
    public boolean isParent;
    public boolean isOne;
    public boolean isMany;

    public RelationDetails(boolean isParent, boolean isOne, boolean isMany) {
        this.isParent = isParent;
        this.isOne = isOne;
        this.isMany = isMany;
    }
    public boolean anyAreSet() {
        if (isParent || isOne || isMany) {
            return true;
        }
        return false;
    }
}
