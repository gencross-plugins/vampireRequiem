package com.mrprez.gencross.impl.vampire;

import com.mrprez.gencross.history.ProportionalHistoryFactory;
import com.mrprez.gencross.migration.MigrationPersonnage;
import com.mrprez.gencross.migration.Migrator;
import com.mrprez.gencross.value.IntValue;

public class MigrationTo1_1 implements Migrator {

	@Override
	public MigrationPersonnage migrate(MigrationPersonnage migrationPersonnage) throws Exception {
		if(migrationPersonnage.getPhase().equals("Damné")){
			migrationPersonnage.getFormulaManager().removeFormula("Volonté");
			migrationPersonnage.getProperty("Volonté").setEditable(true);
			migrationPersonnage.getProperty("Volonté").setHistoryFactory(new ProportionalHistoryFactory("Experience", 8));
			migrationPersonnage.getProperty("Volonté").setMin(new IntValue(0));
			
			migrationPersonnage.getProperty("Puissance du Sang").setEditable(true);
		}
		
		return migrationPersonnage;
	}

}
