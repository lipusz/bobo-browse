package com.browseengine.bobo.demo.cardemo;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Store;

@Entity
@Indexed
@Table(name = "cars")
public class DemoCar implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int _id;
	private float _price;
	private int _mileage;
	private int _year;
	private String _category;
	private String _color;
	private String _city;
	private String _makeModel;
	private List<String> _tags;
	
	public DemoCar(){
		
	}
	
	@Id
	@DocumentId
	public int getId() {
		return _id;
	}
	public void setId(int id) {
		_id = id;
	}
	
	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	@FieldBridge(impl=PaddedFloatBridge.class,params = @Parameter(name="padding", value="10"))
	public float getPrice() {
		return _price;
	}
	public void setPrice(float price) {
		_price = price;
	}
	

	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	@FieldBridge(impl=PaddedIntegerBridge.class,params = @Parameter(name="padding", value="10"))
	public int getMileage() {
		return _mileage;
	}
	public void setMileage(int mileage) {
		_mileage = mileage;
	}
	
	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	@FieldBridge(impl=PaddedIntegerBridge.class,params = @Parameter(name="padding", value="10"))
	public int getYear() {
		return _year;
	}
	public void setYear(int year) {
		_year = year;
	}
	

	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	public String getCategory() {
		return _category;
	}
	public void setCategory(String category) {
		_category = category;
	}
	

	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	public String getColor() {
		return _color;
	}
	public void setColor(String color) {
		_color = color;
	}
	public String getCity() {
		return _city;
	}
	
	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	public void setCity(String city) {
		_city = city;
	}
	

	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	public String getMakeModel() {
		return _makeModel;
	}
	public void setMakeModel(String makeModel) {
		_makeModel = makeModel;
	}
	
	@CollectionOfElements
	@Field(index = Index.UN_TOKENIZED, store = Store.NO)
	@FieldBridge(impl = MultiValuedFieldBridge.class)
	public List<String> getTags() {
		return _tags;
	}
	public void setTags(List<String> tags) {
		_tags = tags;
	}
}
