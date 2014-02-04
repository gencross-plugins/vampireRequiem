package com.mrprez.gencross.impl.vampire.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mrprez.gencross.Personnage;
import com.mrprez.gencross.Property;
import com.mrprez.gencross.disk.PersonnageFactory;

public class ExampleTest {
	private Personnage vampire;
	
	@Before
	public void init() throws Exception{
		PersonnageFactory personnageFactory = new PersonnageFactory();
		vampire = personnageFactory.buildNewPersonnage("Vampire");

	}
	
	
	@Test
	public void test() throws Exception {
		setNewValue("Clan", "Daeva");
		setNewValue("Attributs#Physique#Agilité", 2);
		
		vampire.passToNextPhase();
		Assert.assertEquals("Creation", vampire.getPhase());
		Assert.assertEquals(5, vampire.getErrors().size());
		setNewValue("Attributs#Physique", "Majeur");
		setNewValue("Attributs#Mental", "Mineur");
		Assert.assertEquals(4, vampire.getErrors().size());
		setNewValue("Attributs#Mental#Astuce", 4);
		setNewValue("Attributs#Physique#Force", 4);
		setNewValue("Attributs#Physique#Agilité", 4);
		setNewValue("Attributs#Social#Présence", 4);
		setNewValue("Attributs#Social#Calme", 2);
		
		setNewValue("Talents#Physique", "Majeur");
		setNewValue("Talents#Mental", "Mineur");
		Assert.assertEquals(3, vampire.getErrors().size());
		setNewValue("Talents#Mental#Erudition", 2);
		setNewValue("Talents#Mental#Politique", 2);
		setNewValue("Talents#Physique#Mélée", 4);
		addProperty("Talents#Physique#Mélée#Epee");
		setNewValue("Talents#Physique#Athlétisme", 4);
		setNewValue("Talents#Physique#Conduite", 3);
		addProperty("Talents#Physique#Conduite#Equitation");
		setNewValue("Talents#Social#Persuasion", 3);
		addProperty("Talents#Social#Persuasion#Séduction");
		setNewValue("Talents#Social#Intimidation", 3);
		setNewValue("Talents#Social#Empathie", 1);
		
		addProperty("Disciplines#Majesté");
		setNewValue("Disciplines#Majesté", 2);
		addProperty("Disciplines#Puissance");
		
		addProperty("Avantages#Beauté fatale");
		setNewValue("Avantages#Beauté fatale", 4);
		addProperty("Avantages#Cascadeur");
		
		setNewValue("Vertu", "Justice");
		setNewValue("Vice", "Colère");
		
		Assert.assertEquals(vampire.getErrors().toString(), 0, vampire.getErrors().size());
		
		vampire.passToNextPhase();
		
		vampire.getPointPools().get("Experience").add(27);
		addProperty("Disciplines#Cruac");
		addProperty("Avantages#Statut", "Cercle de la Sorcière");
		addProperty("Disciplines#Cruac#2", "Azerty");
		Assert.assertEquals("Vous devez prendre chaque niveau de Cruac", vampire.getActionMessage());
		vampire.setActionMessage(null);
		addProperty("Disciplines#Cruac#1", "A");
		Assert.assertNull("Action message is not null:"+vampire.getActionMessage(), vampire.getActionMessage());
		Assert.assertEquals(18, vampire.getPointPools().get("Experience").getRemaining());
		
		addProperty("Disciplines#Cruac#1", "B");
		Assert.assertEquals(16, vampire.getPointPools().get("Experience").getRemaining());
		
		addProperty("Disciplines#Cruac#2", "A");
		Assert.assertEquals(2, vampire.getPointPools().get("Experience").getRemaining());
		
		addProperty("Disciplines#Cruac#2", "B");
		Assert.assertEquals(-2, vampire.getPointPools().get("Experience").getRemaining());
		
		removeProperty("Disciplines#Cruac#1 - A");
		Assert.assertEquals(0, vampire.getPointPools().get("Experience").getRemaining());
		
		removeProperty("Disciplines#Cruac#1 - B");
		Assert.assertEquals("Vous devez avoir chaque niveau de Cruac", vampire.getActionMessage());
		Assert.assertEquals(0, vampire.getPointPools().get("Experience").getRemaining());
		
		vampire.getPointPools().get("Experience").add(57);
		setNewValue("Disciplines#Majesté", 3);
		Assert.assertEquals(42, vampire.getPointPools().get("Experience").getRemaining());
		addProperty("Disciplines#Domination");
		Assert.assertEquals(35, vampire.getPointPools().get("Experience").getRemaining());
		setNewValue("Disciplines#Domination", 3);
		Assert.assertEquals(0, vampire.getPointPools().get("Experience").getRemaining());
		
		vampire.getPointPools().get("Experience").add(44);
		addProperty("Avantages#Statut - Ordo Dracul");
		Assert.assertEquals(42, vampire.getPointPools().get("Experience").getRemaining());
		addProperty("Disciplines#Anneaux du dragon");
		setNewValue("Disciplines#Anneaux du dragon", 2);
		Assert.assertEquals(2, vampire.getPointPools().get("Anneaux du dragon").getRemaining());
		Assert.assertEquals(21, vampire.getPointPools().get("Experience").getRemaining());
		addProperty("Disciplines#Anneaux du dragon#Anneaux du Fléau");
		Assert.assertEquals(1, vampire.getPointPools().get("Anneaux du dragon").getRemaining());
		setNewValue("Disciplines#Anneaux du dragon#Anneaux du Fléau", 2);
		Assert.assertEquals(0, vampire.getPointPools().get("Anneaux du dragon").getRemaining());
		Assert.assertEquals(21, vampire.getPointPools().get("Experience").getRemaining());
		addProperty("Disciplines#Anneaux du dragon#Anneaux custom");
		Assert.assertEquals(-1, vampire.getPointPools().get("Anneaux du dragon").getRemaining());
		setNewValue("Disciplines#Anneaux du dragon", 3);
		Assert.assertEquals(0, vampire.getPointPools().get("Experience").getRemaining());
		
		
		Assert.assertTrue("Errors exists:"+vampire.getErrors(), vampire.getErrors().isEmpty());
		
	}
	
	private void setNewValue(String propertyName, int value) throws Exception{
		vampire.setNewValue(propertyName, value);
	}
	
	private void setNewValue(String propertyName, String value) throws Exception{
		vampire.setNewValue(propertyName, value);
	}
	
	private void addProperty(String absolutePropertyName) throws Exception{
		addProperty(absolutePropertyName, null);
	}
	
	private void addProperty(String absolutePropertyName, String specification) throws Exception{
		String motherPropertyName = absolutePropertyName.substring(0, absolutePropertyName.lastIndexOf('#'));
		String propertyName = absolutePropertyName.substring(absolutePropertyName.lastIndexOf('#')+1);
		Property motherProperty = vampire.getProperty(motherPropertyName);
		if(motherProperty.getSubProperties().isFixe()){
			Assert.fail(motherPropertyName + " is fix");
		}
		if(motherProperty.getSubProperties().getOptions() != null
				&& specification == null
				&& motherProperty.getSubProperties().getOptions().containsKey(propertyName)){
			Property property = motherProperty.getSubProperties().getOptions().get(propertyName).clone();
			vampire.addPropertyToMotherProperty(property);
		}else if(motherProperty.getSubProperties().getOptions() != null
				&& specification != null
				&& motherProperty.getSubProperties().getOptions().containsKey(propertyName + Property.SPECIFICATION_SEPARATOR)){
			Property property = motherProperty.getSubProperties().getOptions().get(propertyName + Property.SPECIFICATION_SEPARATOR).clone();
			property.setSpecification(specification);
			vampire.addPropertyToMotherProperty(property);
		}else if (motherProperty.getSubProperties().isOpen()){
			Property property = motherProperty.getSubProperties().getDefaultProperty().clone();
			property.setName(propertyName);
			vampire.addPropertyToMotherProperty(property);
		}else{
			Assert.fail(motherPropertyName + " is not open");
		}
	}
	
	private void removeProperty(String absolutePropertyName) throws Exception{
		vampire.removePropertyFromMotherProperty(vampire.getProperty(absolutePropertyName));
	}
	
	
	

}
