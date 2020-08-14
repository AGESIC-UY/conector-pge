/**
 * 
 */
package gub.agesic.connector.services.wsdlparser;

import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import gub.agesic.connector.exceptions.ConnectorException;
import gub.agesic.connector.services.filemanager.DefaultFileManagerService;
import gub.agesic.connector.services.xpathparser.DefaultXPathParserService;
import gub.agesic.connector.services.xpathparser.XPathParserService;


/**
 * @author guzman.llambias
 *
 */
public class DefaultWSDLParserServiceTest {

    @Test
    public void testCommentXSDImportTags() throws ConnectorException, URISyntaxException, IOException {
        XPathParserService xpathService = new DefaultXPathParserService();
        DefaultWSDLParserService service = new DefaultWSDLParserService(new DefaultFileManagerService("", xpathService), xpathService);
        Path wsdlPath = Paths.get(getClass().getClassLoader().getResource("wsdl-with-one-import.wsdl").toURI());
        Path wsdlPathBackup = Paths.get(getClass().getClassLoader().getResource("wsdl-with-one-import.wsdl.bak").toURI());

        FileUtils.copyFile(wsdlPath.toFile(), wsdlPathBackup.toFile());

        service.commentXSDImportTags(wsdlPath);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Node wsdl = xpathService.getFileDocumentElement(factory, wsdlPath);
        List<Node> imports = xpathService.getXPathResultNodeList("//schema/import", wsdl);
        Assert.assertThat("El wsdl tiene importa un xsd", imports, empty());
        
        FileUtils.deleteQuietly(wsdlPath.toFile());
        FileUtils.moveFile(wsdlPathBackup.toFile(), wsdlPath.toFile());
        FileUtils.writeLines(wsdlPathBackup.toFile(), new ArrayList<String>());
    }

    @Test
    public void testCommentXSDMultipleImportTags() throws ConnectorException, URISyntaxException, IOException {
        XPathParserService xpathService = new DefaultXPathParserService();
        DefaultWSDLParserService service = new DefaultWSDLParserService(new DefaultFileManagerService("", xpathService), xpathService);
        Path wsdlPath = Paths.get(getClass().getClassLoader().getResource("wsdl-with-many-import.wsdl").toURI());
        Path wsdlPathBackup = Paths.get(getClass().getClassLoader().getResource("wsdl-with-many-import.wsdl.bak").toURI());

        FileUtils.copyFile(wsdlPath.toFile(), wsdlPathBackup.toFile());

        service.commentXSDImportTags(wsdlPath);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Node wsdl = xpathService.getFileDocumentElement(factory, wsdlPath);
        List<Node> imports = xpathService.getXPathResultNodeList("//schema/import", wsdl);
        Assert.assertThat("El wsdl tiene importa un xsd", imports, empty());
        
        FileUtils.deleteQuietly(wsdlPath.toFile());
        FileUtils.moveFile(wsdlPathBackup.toFile(), wsdlPath.toFile());
        FileUtils.writeLines(wsdlPathBackup.toFile(), new ArrayList<String>());
    }
    
    @Test
    public void testCommentXSDIncludeTags() throws ConnectorException, URISyntaxException, IOException {
        XPathParserService xpathService = new DefaultXPathParserService();
        DefaultWSDLParserService service = new DefaultWSDLParserService(new DefaultFileManagerService("", xpathService), xpathService);
        Path wsdlPath = Paths.get(getClass().getClassLoader().getResource("wsdl-with-one-include.wsdl").toURI());
        Path wsdlPathBackup = Paths.get(getClass().getClassLoader().getResource("wsdl-with-one-include.wsdl.bak").toURI());

        FileUtils.copyFile(wsdlPath.toFile(), wsdlPathBackup.toFile());

        service.commentXSDImportTags(wsdlPath);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Node wsdl = xpathService.getFileDocumentElement(factory, wsdlPath);
        List<Node> imports = xpathService.getXPathResultNodeList("//schema/include", wsdl);
        Assert.assertThat("El wsdl tiene importa un xsd", imports, empty());
        
        FileUtils.deleteQuietly(wsdlPath.toFile());
        FileUtils.moveFile(wsdlPathBackup.toFile(), wsdlPath.toFile());
        FileUtils.writeLines(wsdlPathBackup.toFile(), new ArrayList<String>());
    }

}
