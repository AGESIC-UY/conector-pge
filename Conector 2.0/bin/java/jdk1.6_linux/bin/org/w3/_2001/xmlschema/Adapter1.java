//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.06.30 at 03:38:58 PM UYT 
//


package org.w3._2001.xmlschema;

import java.sql.Timestamp;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Adapter1
    extends XmlAdapter<String, Timestamp>
{


    public Timestamp unmarshal(String value) {
        return (biz.ideasoft.schemas.adapter.TimestampAdapter.unmarshal(value));
    }

    public String marshal(Timestamp value) {
        return (biz.ideasoft.schemas.adapter.TimestampAdapter.marshal(value));
    }

}