package biz.ideasoft.soa.esb.ws.mtom.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class MultipartTest {
	public static void main(String[] args) throws Exception {
		FileReader reader = new FileReader("/home/apereiro/Desktop/test.part");
		BufferedReader br = new BufferedReader(reader);
		
		StringBuilder b = new StringBuilder();
		String line;
		
		while ((line = br.readLine()) != null) {
			b.append(line).append("\n");
		}
		
		System.out.println(b);
		
		final String contentType = "multipart/related; type=\"text/xml\"; start=\"<rootpart@soapui.org>\"; boundary=\"----=_Part_10_28391229.1289224972109\"";
		final byte[] data = b.toString().getBytes();
		MimeMultipart m = new MimeMultipart(new DataSource() {
			
			public OutputStream getOutputStream() throws IOException {
				throw new IOException("not supported");
			}
			
			public String getName() {
				return "name";
			}
			
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(data);
			}
			
			public String getContentType() {
				return contentType;
			}
		});
		
		System.out.println(m.getContentType());
		System.out.println(m.getCount());
		
		DataHandler dh = m.getBodyPart(0).getDataHandler();
		dh = m.getBodyPart(1).getDataHandler();
		
		System.out.println(m.getBodyPart(0).getContentType());
		System.out.println(m.getBodyPart(0).getContent());
		
		System.out.println(m.getBodyPart(1).getContentType());
		
		BodyPart envelope = m.getBodyPart(0);
		m.removeBodyPart(0);
		
		MimeBodyPart mimePart = new MimeBodyPart();
		
		final String contentType2 = envelope.getContentType();
		final byte[] data2 = "<hello>aaaa</hello>".getBytes();
		DataHandler dh2 = new DataHandler(new DataSource() {
			
			public OutputStream getOutputStream() throws IOException {
				throw new IOException("not supported");
			}
			
			public String getName() {
				return "name";
			}
			
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(data2);
			}
			
			public String getContentType() {
				return contentType2;
			}
		});
		
		mimePart.setDataHandler(dh2);

		Enumeration enumeration = envelope.getAllHeaders();
		while (enumeration.hasMoreElements()) {
			Header h = (Header) enumeration.nextElement();
			//System.out.println(h.getName());
			mimePart.setHeader(h.getName(), h.getValue());
		}
		m.addBodyPart(mimePart, 0);
		
		dh = m.getBodyPart(0).getDataHandler();
		dh = m.getBodyPart(1).getDataHandler();
		
		m.writeTo(System.out);
	}
}
