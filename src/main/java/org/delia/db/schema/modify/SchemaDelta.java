package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;

public class SchemaDelta {
	public List<SxTypeDelta> typesI = new ArrayList<>();
	public List<SxTypeDelta> typesU = new ArrayList<>();
	public List<SxTypeDelta> typesD = new ArrayList<>();
	public List<SxOtherDelta> othersI = new ArrayList<>();
	public List<SxOtherDelta> othersU = new ArrayList<>();
	public List<SxOtherDelta> othersD = new ArrayList<>();
	
	public boolean isEmpty() {
		if (typesI.isEmpty() && typesU.isEmpty() && typesD.isEmpty()) {
			if (othersI.isEmpty() && othersD.isEmpty() && othersD.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("I:%d,U:%d,D:%d", typesI.size(), typesU.size(), typesD.size());
	}
}