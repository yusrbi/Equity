package knowledgebase;

public class Unit {

	private String unit_key;
//	private String[] unit_aliases;
//	private String[] unit_abbreviations;
	private String class_;
	private String dimension;
	private String dimension_wiki_title;
	private String unit_wiki_title;
	private String name;
	
	public String getName() {
		return name;
	}

	public String getDimension() {
		return dimension;
	}


	public String getDimension_wiki_title() {
		return dimension_wiki_title;
	}

	
	
	public Unit( String name, String unit_key, String wiki_title, String class_name,
			String dimension, String dimension_wiki_title) {
		this.name= name;
		if(unit_key!=null && !unit_key.isEmpty())
			this.unit_key = unit_key;
		else
			this.unit_key = wiki_title;
		this.dimension = dimension;
		this.dimension_wiki_title = dimension_wiki_title;
		if(class_name != null){
			this.class_ = class_name;
		}
		this.unit_wiki_title = wiki_title;
	}
	
	
//	public String getFreebaseid() {
//		return freebaseid;
//	}

	public String getUnit_key() {
		return unit_key;
	}

//	public String[] getUnit_aliases() {
//		return unit_aliases;
//	}
//
//	public String[] getUnit_abbreviations() {
//		return unit_abbreviations;
//	}

	public String getClassName() {
		return class_;
	}

//	public String[] getUnit_wiki_links() {
//		return unit_wiki_links;
//	}

	public String getUnit_wiki_title() {
		return unit_wiki_title;
	}

	public String getUnitID(){
		String unit = this.unit_wiki_title;
		if(unit == null || unit.isEmpty()){
			unit= this.unit_key;
		}
		return unit;
	}
	public String getfullyQualifiedName() {
		String unit = this.unit_wiki_title;
		if(unit == null || unit.isEmpty()){
			unit= this.unit_key;
		}
		return String.format("%s(%s,%s)", this.class_,  this.dimension, unit);
	}

	


}
