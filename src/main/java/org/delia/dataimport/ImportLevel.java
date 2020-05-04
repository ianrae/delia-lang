package org.delia.dataimport;

public enum ImportLevel {
	ONE, //DRY RUN, no relation rules enforced
	TWO, //DRY RUN, relation rules enforced within group of csv files.
	THREE, //DRY RUN, level TWO + enforce relations by querying real db
	FOUR, //NORMAL RUN. import into database
}
