package com.nonoo.homersekletwidget.tata;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Bundle;

public class XMLHandler extends DefaultHandler {

	private String location = "";
	private Bundle atts = new Bundle();

	public String temperature = "n/a";
	public Long timestamp = 0L;
	public String dataLocation = "nonoo.hu";
	
    @Override
    public void startElement(String namespaceURI, String localName,
    				String qName, Attributes atts) throws SAXException {
    	localName = localName.trim();
    	if(localName.length() > 0)
    		location += "/" + localName;

    	this.atts.clear();
    	for(int i = 0; i < atts.getLength(); i++) {
    		this.atts.putString(atts.getLocalName(i), atts.getValue(i));
    	}
    }
   
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
					throws SAXException {

    	// removing the last element from the location
    	localName = localName.trim();
    	if(localName.length() > 0) {
	    	String[] locs = location.split("/");
	    	location = "";
	    	for (int i = 0; i < locs.length - 1; i++)
	    	{
	    		if(locs[i].trim().length() > 0)
	    			location += "/" + locs[i];
	    	}
    	}
   	    }
   
    @Override
    public void characters(char ch[], int start, int length) {
    	String value = new String( ch, start, length ).trim();

    	if(value.length() > 0) {
    		if(location.equals("/data/temperature")) {
    			Float f = Float.parseFloat(value);
    			temperature = String.valueOf(Math.round(f*10.0)/10.0);
    		}
    		if(location.equals("/data/timestamp")) { timestamp = Long.parseLong(value); }
    		if(location.equals("/data/location")) { dataLocation = value; }
    	}
    }

	@Override
	public void startDocument() throws SAXException {}
	@Override
    public void endDocument() throws SAXException {}

}
