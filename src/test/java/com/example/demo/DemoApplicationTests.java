package com.example.demo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.findify.s3mock.S3Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStreamReader;
import java.io.StringWriter;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

	@Autowired
	S3Mock s3Server;

	@Autowired
	AmazonS3 client;

	@Autowired

	@Before
	public void setup() {


	}

	@After
	public void teardown(){
		s3Server.stop();
	}

	@Test
	public void test(){

		s3Server.start();

		client.createBucket("testbucket");
		client.putObject("testbucket", "test.xml", new FileSystemResource("src/test/resources/testSoap.xml").getFile());
		S3Object obj = client.getObject(new GetObjectRequest("testbucket", "test.xml"));


		System.out.println(obj.getObjectContent());

		DocumentBuilderFactory docFactory = null;

		DocumentBuilder docBuilder = null;
		Document document = null;
		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true);
			docBuilder = docFactory.newDocumentBuilder();
			InputSource is = new InputSource(new InputStreamReader(obj.getObjectContent()));

			document = docBuilder.parse(is);
		}
		catch(Exception e){
		}
		System.out.println("Request xml generated Parsed succesffully ");


		//NodeList nodes =  document.getDocumentElement().getElementsByTagNameNS("http://www.ci.lmig.com/dial/3/0","Body");
		//NodeList nodes =  document.getDocumentElement().getElementsByTagNameNS("x","Envelope");
		//System.err.println("Here: " + nodes.item(0).getChildNodes().item(0).getNodeValue());


		System.out.println(document.getChildNodes().item(0).getChildNodes().getLength());

		for(int i = 0; i < 5 ; i++){
			System.out.println(document.getChildNodes().item(0).getChildNodes().item(i));
		}

		for(int i = 0; i < 3 ; i++){
			System.out.println(document.getDocumentElement().getElementsByTagNameNS("x", "Body").item(0).getChildNodes().item(i));
		}
		System.out.println(nodeToString(document.getDocumentElement().getElementsByTagName("note").item(0)));

	}

	private static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}

}

//https://stackoverflow.com/questions/42219347/get-soap-element-by-tag-with-namespace-in-java
//https://coderanch.com/t/520979/languages/parse-SOAP-Xml-java-code
//https://stackoverflow.com/questions/4412848/xml-node-to-string-in-java