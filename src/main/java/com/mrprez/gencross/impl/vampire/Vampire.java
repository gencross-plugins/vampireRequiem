package com.mrprez.gencross.impl.vampire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.mrprez.gencross.Personnage;
import com.mrprez.gencross.Property;
import com.mrprez.gencross.history.ConstantHistoryFactory;
import com.mrprez.gencross.history.HistoryFactory;
import com.mrprez.gencross.history.HistoryItem;
import com.mrprez.gencross.history.HistoryUtil;
import com.mrprez.gencross.history.LevelToReachHistoryFactory;
import com.mrprez.gencross.history.ProportionalHistoryFactory;
import com.mrprez.gencross.value.IntValue;
import com.mrprez.gencross.value.Value;

public class Vampire extends Personnage {
	
	
	@Override
	public void calculate() {
		super.calculate();
		calculateAvantages();
		if(phase.equals("Humain")){
			calculateAttributs();
			calculateTalents();
			calculatePointPools();
			calculateViceEtVertu();
		}else if(phase.equals("Clan")){
			if(getProperty("Clan").getValue().getString().equals("")){
				errors.add("Vous devez choisir un clan");
			}
		}else if(phase.equals("Etincelle de non vie")){
			calculateBonusClan();
			calculateDisciplines();
		}
	}
	
	public void calculateBonusClan(){
		Property property0 = getProperty("Attributs#"+determineAttributsBonus()[0]);
		Property property1 = getProperty("Attributs#"+determineAttributsBonus()[1]);
		int sum = property0.getValue().getInt()-property0.getMin().getInt()
				+ property1.getValue().getInt()-property1.getMin().getInt();
		if(sum!=1){
			errors.add("Vous devez ajouter un point dans l'attribut "+property0.getName()+" ou "+property1.getName() );
		}
	}
	
	private void calculateViceEtVertu(){
		if(getProperty("Vice").getValue().toString().equals("") || getProperty("Vertu").getValue().toString().equals("")){
			errors.add("Vous devez choisir une vertu et un vice");
		}
	}
	
	private void calculatePointPools(){
		List<String> poolsToEmpty = Arrays.asList("Attributs Majeur", "Attributs Intermediaire", "Attributs Mineur", 
				"Talents Mineur", "Talents Intermediaire", "Talents Majeur", "Spécialités");
		for(String poolPointName : poolsToEmpty){
			if(getPointPools().get(poolPointName).getRemaining()!=0){
				errors.add("Il reste des points à dépenser (Attributs, Talents et Spécialités)");
				return;
			}
		}
	}
	
	private void calculateAttributs(){
		String mental = getProperty("Attributs#Mental").getValue().toString();
		String physique = getProperty("Attributs#Physique").getValue().toString();
		String social = getProperty("Attributs#Social").getValue().toString();
		if(mental.equals(physique) || mental.equals(social) || physique.equals(social)){
			errors.add("Vous devez hiérarchiser vos groupes d'Attributs");
		}
	}
	
	private void calculateTalents(){
		String mental = getProperty("Talents#Mental").getValue().toString();
		String physique = getProperty("Talents#Physique").getValue().toString();
		String social = getProperty("Talents#Social").getValue().toString();
		if(mental.equals(physique) || mental.equals(social) || physique.equals(social)){
			errors.add("Vous devez hiérarchiser vos groupes de Talents");
		}
	}
	
	private void calculateAvantages(){
		for(Property avantage : getProperty("Avantages").getSubProperties().getProperties().values()){
			String errorMessage = checkAvantage(avantage);
			if(errorMessage!=null){
				errors.add(avantage.getFullName()+": "+errorMessage);
			}
		}
	}
	
	private void calculateDisciplines(){
		int compte = 0;
		for(String disciplineClanName : getDisciplinesClan()){
			Property discipline = getProperty("Disciplines").getSubProperty(disciplineClanName);
			if(discipline != null){
				compte = compte + discipline.getValue().getInt();
			}
		}
		if(compte < 2){
			errors.add("Vous devez dépenser au moins 2 points dans vos disciplines de clan "+getDisciplinesClan());
		}
		
	}
	
	private Collection<String> getDisciplinesClan(){
		String clan = getProperty("Clan").getValue().getString();
		if (clan.equals("Daeva")) {
			return Arrays.asList("Célérité", "Majesté", "Puissance");
		} else if (clan.equals("Gangrel")) {
			return Arrays.asList("Animalisme", "Métamorphose", "Invulnérabilité");
		} else if (clan.equals("Mekhet")) {
			return Arrays.asList("Auspex", "Célérité", "Dissimulation");
		} else if (clan.equals("Nosferatu")) {
			return Arrays.asList("Cauchemard", "Dissimulation", "Puissance");
		} else if (clan.equals("Ventru")) {
			return Arrays.asList("Animalisme", "Domination", "Invulnérabilité");
		}
		return new ArrayList<String>();
	}
	
	private Collection<String> getDisciplineCommune(){
		return Arrays.asList("Célérité", "Puissance", "Invulnérabilité", "Dissimulation", "Animalisme");
	}
	
	
	public void changeAttributGroupValue(Property attributGroup, Value oldValue){
		String newPoolPoint = "Attributs "+attributGroup.getValue().toString();
		String oldPoolPoint = "Attributs "+oldValue.toString();
		int transfertCost = 0;
		for(Property attribut : attributGroup.getSubProperties().getProperties().values()){
			attribut.getHistoryFactory().setPointPool(newPoolPoint);
			List<HistoryItem> subHistory = HistoryUtil.getSubHistory(history, attribut);
			for(HistoryItem subHistoryItem : subHistory){
				transfertCost = transfertCost + subHistoryItem.getCost();
				subHistoryItem.setPointPool(newPoolPoint);
			}
		}
		getPointPools().get(oldPoolPoint).spend(-transfertCost);
		getPointPools().get(newPoolPoint).spend(transfertCost);
	}
	
	public void changeTalentGroupValue(Property talentGroup, Value oldValue){
		String newPoolPoint = "Talents "+talentGroup.getValue().toString();
		String oldPoolPoint = "Talents "+oldValue.toString();
		int transfertCost = 0;
		for(Property talent : talentGroup.getSubProperties().getProperties().values()){
			talent.getHistoryFactory().setPointPool(newPoolPoint);
			List<HistoryItem> subHistory = HistoryUtil.getSubHistory(history, talent);
			for(HistoryItem subHistoryItem : subHistory){
				transfertCost = transfertCost + subHistoryItem.getCost();
				subHistoryItem.setPointPool(newPoolPoint);
			}
		}
		getPointPools().get(oldPoolPoint).spend(-transfertCost);
		getPointPools().get(newPoolPoint).spend(transfertCost);
	}
	
	public Boolean addAvantage(Property avantage){
		String erreur = checkAvantage(avantage);
		if(erreur!=null){
			actionMessage = erreur;
			return false;
		}
		return true;
	}
	
	
	private int compteLangues(Property langue){
		int compte = 0;
		for(Property avantage : getProperty("Avantages").getSubProperties().getProperties().values()){
			if(avantage.getName().equals("Langue étrangère")){
				compte++;
			}
		}
		if(getProperty("Avantages").getSubProperty(langue.getFullName())==null){
			compte++;
		}
		return compte;
	}
	
	public void addAnneau(Property property){
		HistoryItem historyItem = getHistory().get(getHistory().size()-1);
		int sum = sumAnneaux();
		getPointPools().get(historyItem.getPointPool()).spend(-historyItem.getCost());
		historyItem.setCost(sum*7);
		getPointPools().get(historyItem.getPointPool()).spend(historyItem.getCost());
	}
	
	public void changeAnneau(Property property, Value oldValue){
		HistoryItem historyItem = getHistory().get(getHistory().size()-1);
		int sumAfter = sumAnneaux();
		getPointPools().get(historyItem.getPointPool()).spend(-historyItem.getCost());
		
		int sumBefore = sumAfter - historyItem.getNewValue().getInt() + historyItem.getOldValue().getInt();
		int cost = 7 * ( sumAfter*(sumAfter+1)/2-sumBefore*(sumBefore+1)/2 ); 
		
		historyItem.setCost(cost);
		getPointPools().get(historyItem.getPointPool()).spend(cost);
	}
	
	public void deleteAnneau(Property property){
		HistoryItem historyItem = getHistory().get(getHistory().size()-1);
		int sumAfter = sumAnneaux();
		getPointPools().get(historyItem.getPointPool()).spend(-historyItem.getCost());
		
		int sumBefore = sumAfter + property.getValue().getInt();
		int cost = 7 * ( sumAfter*(sumAfter+1)/2-sumBefore*(sumBefore+1)/2 ); 
		
		historyItem.setCost(cost);
		getPointPools().get(historyItem.getPointPool()).spend(cost);
	}
	
	public void updateSumAnneau(Property anneau){
		getProperty("Disciplines#Anneaux du dragon").setValue(new IntValue(sumAnneaux()));
	}
	
	public void updateSumAnneau(Property anneau, Value oldValue){
		getProperty("Disciplines#Anneaux du dragon").setValue(new IntValue(sumAnneaux()));
	}
	
	public int sumAnneaux(){
		int sum = 0;
		for(Property anneau : getProperty("Disciplines#Anneaux du dragon").getSubProperties()){
			sum = sum + anneau.getValue().getInt();
		}
		return sum;
	}
	
	
	public boolean checkDeleteMagie(Property magie){
		boolean hasUpperRituel=false;
		boolean hasOtherLevelRituel=false;
		for(Property property : ((Property)magie.getOwner()).getSubProperties()){
			if( ! property.getName().equals(magie.getName())){
				if(property.getValue().getInt()==magie.getValue().getInt()){
					hasOtherLevelRituel=true;;
				}
				if(property.getValue().getInt()==magie.getValue().getInt()+1){
					hasUpperRituel=true;
				}
			}
		}
		if(hasUpperRituel && !hasOtherLevelRituel){
			actionMessage = "Vous devez supprimer les rituels de niveau supérieur avant";
			return false;
		}
		return true;
	}
	
	
	public boolean checkDeleteMotherMagie(Property motherMagie){
		if( ! motherMagie.getSubProperties().isEmpty()){
			actionMessage = "Vous devez d'abord supprimer les rituels de "+motherMagie.getName();
			return false;
		}
		return true;
	}
	
	
	public void deleteMagie(Property magie){
		int count = 0;
		int max = 0;
		for(Property property : ((Property)magie.getOwner()).getSubProperties()){
			if(property.getValue().getInt()==magie.getValue().getInt()){
				count++;
			}
			if(max < property.getValue().getInt()){
				max = property.getValue().getInt();
			}
		}
		((Property)magie.getOwner()).setValue(new IntValue(max));
		if(count>0){
			HistoryItem historyItem = getHistory().get(getHistory().size()-1);
			getPointPools().get(historyItem.getPointPool()).spend(-historyItem.getCost());
			historyItem.setCost(historyItem.getCost()/7*2);
			getPointPools().get(historyItem.getPointPool()).spend(historyItem.getCost());
		}
	}
	
	
	public boolean checkAddMagie(Property magie){
		if(magie.getValue().getInt()==1){
			return true;
		}
		for(Property property : ((Property)magie.getOwner()).getSubProperties()){
			if(property.getValue().getInt()==magie.getValue().getInt()-1){
				return true;
			}
		}
		actionMessage = "Vous devez prendre un rituel de niveau "+(magie.getValue().getInt()-1)+" avant";
		return false;
	}
	
	
	public void addMagie(Property magie){
		int count = 0;
		int max = 0;
		for(Property property : ((Property)magie.getOwner()).getSubProperties()){
			if(property.getValue().getInt()==magie.getValue().getInt()){
				count++;
			}
			if(max < property.getValue().getInt()){
				max = property.getValue().getInt();
			}
		}
		((Property)magie.getOwner()).setValue(new IntValue(max));
		if(count>1){
			HistoryItem historyItem = getHistory().get(getHistory().size()-1);
			getPointPools().get(historyItem.getPointPool()).spend(-historyItem.getCost());
			historyItem.setCost(historyItem.getCost()/7*2);
			getPointPools().get(historyItem.getPointPool()).spend(historyItem.getCost());
		}
	}
	
	private String[] determineAttributsBonus(){
		String clan = getProperty("Clan").getValue().getString();
		String attributBonus[] = new String[2];
		if (clan.equals("Daeva")) {
			attributBonus[0] = "Physique#Agilité";
			attributBonus[1] = "Social#Manipulation";
		} else if (clan.equals("Gangrel")) {
			attributBonus[0] = "Social#Calme";
			attributBonus[1] = "Physique#Vigueur";
		} else if (clan.equals("Mekhet")) {
			attributBonus[0] = "Mental#Intelligence";
			attributBonus[1] = "Mental#Astuce";
		} else if (clan.equals("Nosferatu")) {
			attributBonus[0] = "Social#Calme";
			attributBonus[1] = "Physique#Force";
		} else if (clan.equals("Ventru")) {
			attributBonus[0] = "Social#Présence";
			attributBonus[1] = "Mental#Résolution";
		}
		return attributBonus;
	}
	
	
	private String checkAvantage(Property avantage){
		if(avantage.getName().equals("Langue étrangère")){
			if(compteLangues(avantage)>getProperty("Attributs#Mental#Intelligence").getValue().getInt()){
				return "Vous ne pouvez avoir plus de Langues étrangères que votre Intelligence";
			}
		}
		if(avantage.getName().equals("Conscience de l’invisible")){
			if(getProperty("Attributs#Mental#Astuce").getValue().getInt()<2){
				return "Vous devez avoir Astuce à 2";
			}
		}
		if(avantage.getName().equals("Capacité pulmonaire")){
			if(getProperty("Talents#Physique#Athlétisme").getValue().getInt()<3){
				return "Vous devez avoir Athlétisme à 3";
			}
		}
		if(avantage.getName().equals("Cascadeur")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
		}
		if(avantage.getName().equals("Désarmer")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<2){
				return "Vous devez avoir Mélée à 2";
			}
		}
		if(avantage.getName().equals("Dos musclé")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
		}
		if(avantage.getName().equals("Esquive (armes blanches)")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<1){
				return "Vous devez avoir Mélée à 1";
			}
		}
		if(avantage.getName().equals("Esquive (mains nues)")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
			if(getProperty("Talents#Physique#Bagarre").getValue().getInt()<1){
				return "Vous devez avoir Bagarre à 1";
			}
		}
		if(avantage.getName().equals("Estomac en béton")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
		}
		if(avantage.getName().equals("Flingueur")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Armes à feu").getValue().getInt()<3){
				return "Vous devez avoir Armes à feu à 3";
			}
		}
		if(avantage.getName().equals("Guérison rapide")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<4){
				return "Vous devez avoir Vigueur à 4";
			}
		}
		if(avantage.getName().equals("Immunité innée")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
		}
		if(avantage.getName().equals("Réflexes rapides")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
		}
		if(avantage.getName().equals("Résistance aux toxines")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<3){
				return "Vous devez avoir Vigueur à 3";
			}
		}
		if(avantage.getName().equals("Santé de fer")){
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<3
					&& getProperty("Attributs#Physique#Résolution").getValue().getInt()<3){
				return "Vous devez avoir Vigueur ou Résolution à 3";
			}
		}
		if(avantage.getName().equals("Sprinteur")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
		}
		if(avantage.getName().equals("Style de combat : boxe")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<3){
				return "Vous devez avoir Force à 3";
			}
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
			if(getProperty("Talents#Physique#Bagarre").getValue().getInt()<2){
				return "Vous devez avoir Bagarre à 2";
			}
		}
		if(avantage.getName().equals("Style de combat : Kung-fu")){
			if(getProperty("Attributs#Physique#Force").getValue().getInt()<2){
				return "Vous devez avoir Force à 2";
			}
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<2){
				return "Vous devez avoir Agilité à 2";
			}
			if(getProperty("Attributs#Physique#Vigueur").getValue().getInt()<2){
				return "Vous devez avoir Vigueur à 2";
			}
			if(getProperty("Talents#Physique#Bagarre").getValue().getInt()<2){
				return "Vous devez avoir Bagarre à 2";
			}
		}
		if(avantage.getName().equals("Style de combat : deux armes blanches")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<3){
				return "Vous devez avoir Mélée à 3";
			}
		}
		if(avantage.getName().equals("Techniques de combat")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
			if(getProperty("Talents#Physique#Mélée").getValue().getInt()<2){
				return "Vous devez avoir Mélée à 2";
			}
		}
		if(avantage.getName().equals("Tir rapide")){
			if(getProperty("Attributs#Physique#Agilité").getValue().getInt()<3){
				return "Vous devez avoir Agilité à 3";
			}
		}
		if(avantage.getName().equals("Tour de chauffe")){
			if(getProperty("Avantages#Réflexes rapides")==null || getProperty("Avantages#Réflexes rapides").getValue().getInt()<2){
				return "Vous devez avoir Réflexes rapides à 2";
			}
		}
		if(avantage.getName().equals("Inspiration")){
			if(getProperty("Attributs#Social#Présence").getValue().getInt()<4){
				return "Vous devez avoir Présence à 4";
			}
		}
		if(avantage.getName().equals("Renommée")){
			if(getProperty("Attributs#Mental#Astuce").getValue().getInt()<5){
				return "Vous devez avoir Astuce à 5";
			}
		}
		return null;
	}
	
	
	public void phaseHumainFinished(){
		for(Property attributCat : getProperty("Attributs").getSubProperties()){
			attributCat.setEditable(false);
			for(Property attribut : attributCat.getSubProperties()){
				attribut.setEditable(false);
			}
		}
		for(Property talentCat : getProperty("Talents").getSubProperties()){
			talentCat.setEditable(false);
			for(Property talent : talentCat.getSubProperties()){
				talent.setEditable(false);
				talent.getSubProperties().setFixe(true);
			}
		}
		getProperty("Clan").setEditable(true);
	}
	
	public void phaseClanFinished(){
		getProperty("Clan").setEditable(false);
		Property property0 = getProperty("Attributs#"+determineAttributsBonus()[0]);
		Property property1 = getProperty("Attributs#"+determineAttributsBonus()[1]);
		property0.setEditable(true);
		property1.setEditable(true);
		property0.setHistoryFactory(HistoryFactory.FREE_HISTORY_FACTORY);
		property1.setHistoryFactory(HistoryFactory.FREE_HISTORY_FACTORY);
		property0.setMin();
		property1.setMin();
		property0.setMax(new IntValue(Math.min(property0.getValue().getInt()+1, 5)));
		property1.setMax(new IntValue(Math.min(property1.getValue().getInt()+1, 5)));
		
		getProperty("Disciplines").getSubProperties().setFixe(false);
		getPointPools().get("Disciplines").setToEmpty(true);
		
		getPointPools().get("Avantages").setToEmpty(true);
	}
	
	
	public void phaseEtincelleFinished(){
		Property attributs = getProperty("Attributs");
		for(Property attributGroup : attributs.getSubProperties()){
			for(Property attribut : attributGroup.getSubProperties()){
				attribut.setHistoryFactory(new LevelToReachHistoryFactory(5, "Experience"));
				attribut.setMin(new IntValue(1));
				attribut.setMax(new IntValue(5));
			}
		}
		Property talents = getProperty("Talents");
		for(Property talentGroup : talents.getSubProperties()){
			talentGroup.setEditable(false);
			for(Property talent : talentGroup.getSubProperties()){
				talent.setHistoryFactory(new LevelToReachHistoryFactory(3, "Experience"));
				talent.getSubProperties().getDefaultProperty().setHistoryFactory(new ConstantHistoryFactory("Experience", 3));
			}
		}
		Property avantages = getProperty("Avantages");
		avantages.getSubProperties().getDefaultProperty().setHistoryFactory(new LevelToReachHistoryFactory(2, "Expérience"));
		for(Property avantage : avantages.getSubProperties()){
			avantage.setHistoryFactory(new LevelToReachHistoryFactory(2, "Experience"));
		}
		for(Property option : avantages.getSubProperties().getOptions().values()){
			option.setHistoryFactory(new LevelToReachHistoryFactory(2, "Experience"));
		}
		Property humanite = getProperty("Humanité");
		humanite.setEditable(true);
		humanite.setHistoryFactory(new LevelToReachHistoryFactory(3, "Experience"));
		
		for(Property discipline : getProperty("Disciplines").getSubProperties()){
			if(getDisciplineCommune().contains(discipline.getName())){
				discipline.setHistoryFactory(new LevelToReachHistoryFactory(5, "Experience"));
			} else if(getDisciplinesClan().contains(discipline.getName())){
				discipline.setHistoryFactory(new LevelToReachHistoryFactory(5, "Experience"));
			} else if(discipline.getName().equals("Cruac") || discipline.getName().equals("Thaumaturgie thébaine")){
				discipline.setHistoryFactory(new ProportionalHistoryFactory("Experience", 7));
			} else if(discipline.getName().equals("Anneaux du dragon")){
				discipline.setHistoryFactory(new ProportionalHistoryFactory("Experience", 7));
			} else {
				discipline.setHistoryFactory(new LevelToReachHistoryFactory(7, "Experience"));
			}
		}
		for(Property discipline : getProperty("Disciplines").getSubProperties().getOptions().values()){
			int factor;
			if(getDisciplineCommune().contains(discipline.getName())){
				factor = 5;
			} else if(getDisciplinesClan().contains(discipline.getName())){
				factor = 5;
			} else if(discipline.getName().equals("Cruac") || discipline.getName().equals("Thaumaturgie thébaine")){
				discipline.setHistoryFactory(new ProportionalHistoryFactory("Experience", 7));
			} else if(discipline.getName().equals("Anneaux du dragon")){
				discipline.setHistoryFactory(new ProportionalHistoryFactory("Experience", 7));
			} else {
				factor = 7;
			}
			discipline.setHistoryFactory(new LevelToReachHistoryFactory(factor, "Experience"));
		}
	}
	
	

}
