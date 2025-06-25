package uy.gub.agesic.pge.core.ws.addressing;

import javax.xml.soap.SOAPMessage;

public interface SOAPAddressingProperties extends AddressingProperties {
    void readHeaders(SOAPMessage paramSOAPMessage) throws AddressingException;

    void writeHeaders(SOAPMessage paramSOAPMessage) throws AddressingException;

    void setMu(boolean paramBoolean);
}