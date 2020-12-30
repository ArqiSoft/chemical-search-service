package com.arqisoft.chemicalsearch.api;

import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType
public class ResultRecord {
    public String Id;
    public float Score;
    public String ExternalId;
    public String InChIKey;
    public String InChI;
    public String MW;
    public String Name;
}
