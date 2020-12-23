package com.arqisoft.chemicalsearch.api;

import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType
public class SearchRequest {
    public String SmileFilter;
    public SearchType SearchType;
    public float Threshold;
    public Integer Limit;
}
