package com.mrprez.gencross.impl.vampire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.mrprez.gencross.Personnage;
import com.mrprez.gencross.PoolPoint;
import com.mrprez.gencross.Property;
import com.mrprez.gencross.history.ConstantHistoryFactory;
import com.mrprez.gencross.history.HistoryItem;
import com.mrprez.gencross.history.HistoryUtil;
import com.mrprez.gencross.history.LevelToReachHistoryFactory;
import com.mrprez.gencross.history.TabHistoryFactory;
import com.mrprez.gencross.value.IntValue;
import com.mrprez.gencross.value.Value;

public class Vampire extends Personnage {
	
	
	@Override
	public void calculate() {
		super.calculate();
		if(phase.equals("Clan")){
			calculateClan();
		} else if(phase.equals("Creation")){
			calculateAttributs();
			calculateTalents();
			calculatePointPools();
			calculateViceEtVertu();
			calculateAvantages();
			calculateDisciplines();
		}
	}
	
	private void calculateClan(){
		if(getProperty("Clan").getValue().getString().equals("")){
			errors.add("Vous devez choisir un clan");
		}else{
			String attributBonus[] = determineAttributsBonus();
			if(getProperty("Attributs#"+attributBonus[0]).getValue().getInt()==1
					&& getProperty("Attributs#"+attributBonus[1]).getValue().getInt()==1){
				errors.add("Vous devez choisir votre bonus de clan en "+attributBonus[0].replace('#', '/')+" ou "+attributBonus[1].replace('#', '/'));
			}
			
		}
	}
	
	private void calculateViceEtVertu(){
		if(getProperty("Vice").getValue().toString().equals("") || getProperty("Vertu").getValue().toString().equals("")){
			errors.add("Vous devez choisir une vertu et un vice");
		}
	}
	
	private void calculatePointPools(){
		String error = null;
		Iterator<PoolPoint> it = getPointPools().values().iterator();
		while(it.hasNext() && error==null){
			if(it.next().getRemaining()!=0){
				error = "Il reste des points à dépenser";
			}
		}
		if(error!=null){
			errors.add(error);
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
	
	public void creationFinished(){
		pointPools.put("Experience", new PoolPoint("Experience", 0));
		
		Property attributs = getProperty("Attributs");
		for(Property attributGroup : attributs.getSubProperties()){
			attributGroup.setEditable(false);
			for(Property attribut : attributGroup.getSubProperties()){
				attribut.setHistoryFactory(new LevelToReachHistoryFactory(5, "Experience"));
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
		Property moralite = getProperty("Humanité");
		moralite.setEditable(true);
		moralite.setHistoryFactory(new LevelToReachHistoryFactory(3, "Experience"));
		
		for(Property discipline : getProperty("Disciplines").getSubProperties()){
			int factor;
			if(getDisciplineCommune().contains(discipline.getName())){
				factor = 5;
			} else if(getDisciplinesClan().contains(discipline.getName())){
				factor = 5;
			} else {
				factor = 7;
			}
			discipline.setHistoryFactory(new LevelToReachHistoryFactory(factor, "Experience"));
		}
		for(Property discipline : getProperty("Disciplines").getSubProperties().getOptions().values()){
			int factor;
			if(getDisciplineCommune().contains(discipline.getName())){
				factor = 5;
			} else if(getDisciplinesClan().contains(discipline.getName())){
				factor = 5;
			} else {
				factor = 7;
			}
			discipline.setHistoryFactory(new LevelToReachHistoryFactory(factor, "Experience"));
		}
	}
	
	public void clanChoosen(){
		for(Property attGroup : getProperty("Attributs").getSubProperties()){
			attGroup.setEditable(true);
			for(Property attribut : attGroup.getSubProperties()){
				attribut.setEditable(true);
				attribut.setMax(new IntValue(5));
				attribut.setMin();
				attribut.setHistoryFactory(new TabHistoryFactory(new Integer[]{-1,0,1,2,3,5}, "Attributs Intermediaire"));
			}
		}
		getProperty("Clan").setEditable(false);
	}
	
	public void changeClan(Property clan, Value oldValue){
		for(Property attGroup : getProperty("Attributs").getSubProperties()){
			for(Property attribut : attGroup.getSubProperties()){
				attribut.setEditable(false);
				attribut.setMax(new IntValue(5));
			}
		}
		String attributsBonus[] = determineAttributsBonus();
		for(int i=0;i<attributsBonus.length; i++){
			getProperty("Attributs#"+attributsBonus[i]).setEditable(true);
			getProperty("Attributs#"+attributsBonus[i]).setMax(new IntValue(2));
		}
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
	
	public Boolean addMagie(Property magie){
		Property owner = (Property)magie.getOwner();
		String statutName;
		if(owner.getName().equals("Cruac")){
			statutName = "Cercle de la Sorcière";
		}else{
			statutName = "Lancea Sanctum";
		}
		Property statut = getProperty("Avantages#Statut - "+statutName);
		if(statut==null){
			actionMessage = "Vous devez avoir du Statut - "+statutName;
			return false;
		}
		int max = 0;
		for(Property p : owner.getSubProperties()){
			if(p.getValue().getInt()>max){
				max = p.getValue().getInt();
			}
		}
		if(magie.getValue().getInt() > max + 1){
			actionMessage = "Vous devez prendre chaque niveau de "+owner.getName();
			return false;
		}
		if(phase.equals("Damné")){
			if( magie.getValue().getInt() == max + 1 ){
				magie.setHistoryFactory(new ConstantHistoryFactory("Experience", magie.getValue().getInt() * 7));
			} else {
				magie.setHistoryFactory(new ConstantHistoryFactory("Experience", magie.getValue().getInt() * 2));
			}
		}
		
		return true;
	}
	
	public Boolean deleteMagie(Property magie){
		Property owner = (Property)magie.getOwner();
		int max = 0;
		boolean existeMemeNiveau = false;
		for(Property p : owner.getSubProperties()){
			if(p.getValue().getInt()>max){
				max = p.getValue().getInt();
			}
			if(p.getValue().getInt() == magie.getValue().getInt()
					&& !p.getFullName().equals(magie.getFullName())){
				existeMemeNiveau = true;
			}
		}
		if(magie.getValue().getInt() < max && !existeMemeNiveau){
			actionMessage = "Vous devez avoir chaque niveau de "+owner.getName();
			return false;
		}
		if(phase.equals("Damné")){
			if( existeMemeNiveau ){
				magie.setHistoryFactory(new ConstantHistoryFactory("Experience", magie.getValue().getInt() * 2));
			} else {
				magie.setHistoryFactory(new ConstantHistoryFactory("Experience", magie.getValue().getInt() * 7));
			}
		}
		
		return true;
	}
	
	public Boolean addAnneau(Property newAnneau){
		Property statut = getProperty("Avantages#Statut - Ordo Dracul");
		if(statut==null){
			actionMessage = "Vous devez avoir du Statut - Ordo Dracul";
			return false;
		}
		return true;
	}
	
	public Boolean changeAnneau(Property anneau, Value newValue){
		if(newValue.getInt() < anneau.getValue().getInt()){
			return true;
		}
		Property statut = getProperty("Avantages#Statut - Ordo Dracul");
		if(statut==null){
			actionMessage = "Vous devez avoir du Statut - Ordo Dracul";
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
	
	

}
