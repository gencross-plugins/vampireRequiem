package com.mrprez.gencross.impl.vampire;

import com.mrprez.gencross.Property;
import com.mrprez.gencross.Version;
import com.mrprez.gencross.history.ProportionalHistoryFactory;
import com.mrprez.gencross.migration.MigrationPersonnage;
import com.mrprez.gencross.migration.Migrator;
import com.mrprez.gencross.value.IntValue;

public class MigrationTo1_2 implements Migrator {

	@Override
	public MigrationPersonnage migrate(MigrationPersonnage migrationPersonnage) throws Exception {
		if(migrationPersonnage.getPhase().equals("Damné")){
			migrationPersonnage.getFormulaManager().removeFormula("Volonté");
			migrationPersonnage.getProperty("Volonté").setEditable(true);
			migrationPersonnage.getProperty("Volonté").setHistoryFactory(new ProportionalHistoryFactory("Experience", 8));
			migrationPersonnage.getProperty("Volonté").setMin(new IntValue(0));
		}
		
		Property puissanceSg = new Property("Puissance du Sang", migrationPersonnage);
		migrationPersonnage.addProperty(puissanceSg);
		puissanceSg.setValue(new IntValue(1));
		puissanceSg.setMin();
		puissanceSg.setRenderer(new MdTRenderer());
		puissanceSg.setEditable(migrationPersonnage.getPhase().equals("Damné"));
		
		migrationPersonnage.getPluginDescriptor().setVersion(new Version(1, 2));
		
		return migrationPersonnage;
	}

}
