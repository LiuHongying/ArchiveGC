package com.ecm.portal.archivecd.entity;

import java.util.ArrayList;
import java.util.List;

public class TemplateTreeEntity {
	private String name;
	private String id;
	private List<TemplateTreeEntity> children=new ArrayList<TemplateTreeEntity>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<TemplateTreeEntity> getChildren() {
		return children;
	}
	public void setChildren(List<TemplateTreeEntity> children) {
		this.children = children;
	}
	
	public void addChild(TemplateTreeEntity child) {
		this.children.add(child);
	}
	public TemplateTreeEntity getChild(int i) {
		return this.children.get(i);
	}
}
