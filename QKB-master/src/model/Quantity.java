package model;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import loader.QKBLoader;

@ManagedBean(name="quantity")
@SessionScoped
public class Quantity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManagedProperty(value="#{unit}")
	private Unit unit;
	@ManagedProperty(value="#{measure}")
	private Measure measure;
	
	@ManagedProperty(value="#{qkbLoader}")
	QKBLoader qkbLoader;
	
	@ManagedProperty(value="#{quantity_class}")
	private Class_ quantity_class;
	
	
	private String value;
	private String class_;
	private String id;
	public void init(String class_name, String measure_name, String unit_name, String value) throws InvalidQuantityException {
		quantity_class = qkbLoader.get_quantity_class(class_name);
		unit = qkbLoader.getUnit(unit_name);
		measure = qkbLoader.getMeasure(measure_name);
		this.class_ = class_name;
		
		if(class_name!=null && !class_name.isEmpty() && quantity_class ==null){
			throw new InvalidQuantityException("Invalid Quantity: Class Not Found");
		}
		if(measure_name!=null && !measure_name.isEmpty() && measure == null){
			throw new InvalidQuantityException("Invalid Quantity: Measure Not Found");
		}			
		if(unit_name !=null &&  !unit_name.isEmpty() && unit==null){
			throw new InvalidQuantityException("Invalid Quantity: Unit Not Found");
		}
		if(unit_name!=null && !unit_name.isEmpty() ){
			if(measure_name !=null && !measure_name.isEmpty() && !measure.getName().equals(unit.getMeasure())){
				throw new InvalidQuantityException("Invalid Quantity: Unit and Measure Do Not match");
			}else if(measure_name == null){
				measure = qkbLoader.getMeasure(unit.getMeasure());
			}
			if(class_name!=null && !class_name.isEmpty() && !quantity_class.getName().equals(unit.getClass_name())){
				throw new InvalidQuantityException("Invalid Quantity: Class and Unit Do Not match");
			}else if(class_name == null){
				quantity_class  = qkbLoader.get_quantity_class(unit.getClass_name());
			}
			this.class_ = unit.getClass_name();
		}
		if(measure_name!=null && !measure_name.isEmpty()){
			if(class_name!=null && !class_name.isEmpty() && !quantity_class.getName().equals( measure.getClass_name())){
				throw new InvalidQuantityException("Invalid Quantity: Class and Measure Do Not match");
			}	
			this.class_ = measure.getClass_name();
		}
		
		this.value = value;
		
		this.setId("<"+this.class_+","+measure!=null?measure.getName():"NULL"+","+unit!=null?unit.getName():"NULL"+","+value+">");
	}
	public Quantity(){
		
	}
	
	
	public QKBLoader getQkbLoader() {
		return qkbLoader;
	}
	public void setQkbLoader(QKBLoader qkbLoader) {
		this.qkbLoader = qkbLoader;
	}
	public Class_ getQuantity_class() {
		return quantity_class;
	}
	public void setQuantity_class(Class_ quantity_class) {
		this.quantity_class = quantity_class;
	}
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public Measure getMeasure() {
		return measure;
	}
	public void setMeasure(Measure measure) {
		this.measure = measure;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getClass_() {
		return class_;
	}
	public void setClass_(String class_) {
		this.class_ = class_;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
