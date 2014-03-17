package biz.ideasoft.soa.esb.http.failover.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.exalead.io.failover.FailoverHttpClient;

/**
 * A simple program demonstrating the use of the failover client
 */
public class SimpleFailoverTest {
	private static Logger logger = Logger.getLogger("log");

	FailoverHttpClient relay;

	class MyThread extends Thread {
		public void run() {
//			while (true) {
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//				}

				/*
				 * 
				 * GetMethod httpMethod = new GetMethod("/exascript/Ping"); try
				 * { logger.info("********** START method"); int retcode =
				 * relay.executeMethod(httpMethod);
				 * logger.info("********** DONE"); InputStream is =
				 * httpMethod.getResponseBodyAsStream(); is.close(); } catch
				 * (IOException e) { logger.warn("**************** " +
				 * System.currentTimeMillis() + ": MAIN EXCEPTION", e); }
				 */

				boolean success = false;
				for (int i = 0; i < 3; i++) {
//					GetMethod httpMethod = new GetMethod("/ode/processes/CreacionEmpresa/CreacionEmpresa/CreacionEmpresa/Client?wsdl");
					
					PostMethod httpMethod = new PostMethod("/ode/processes/CreacionEmpresa/CreacionEmpresa/CreacionEmpresa/Client");
					httpMethod.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
			        String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cre=\"http://www.notarios.org.pe/CreacionEmpresa/\">   <soapenv:Header/>   <soapenv:Body>      <cre:CrearEmpresaRequest>         <TipoEmpresa>?</TipoEmpresa>         <Distrito>?</Distrito>         <Notario>            <Nombre>?</Nombre>            <Direccion>?</Direccion>            <Telefonos>?</Telefonos>         </Notario>         <Solicitante>            " +
			        		"<DNI>25</DNI>            <Nombres>?</Nombres>            <ApellidoPaterno>?</ApellidoPaterno>            <ApellidoMaterno>?</ApellidoMaterno>            <FechaNacimiento>?</FechaNacimiento>            <Email>?</Email>         </Solicitante>         <Empresa>            <TituloReserva>?</TituloReserva>            <RazonSocial>?</RazonSocial>            <DenominacionAbreviada>?</DenominacionAbreviada>            <Departamento>?</Departamento>           <Provincia>?</Provincia>            <ObjetoSocial>?</ObjetoSocial>            <TipoAporte>?</TipoAporte>            <Monto>?</Monto>            <ValorAccion>?</ValorAccion>            <CancelacionCapital>?</CancelacionCapital>            <Cancelado>?</Cancelado>           <Participantes>               <Participante>                  <DNI>?</DNI>                  <Nombres>?</Nombres>                  <ApellidoPaterno>?</ApellidoPaterno>                  <ApellidoMaterno>?</ApellidoMaterno>                  <FechaNacimiento>?</FechaNacimiento>                  <Email>?</Email>               </Participante>            </Participantes>         </Empresa>         <ActoConstitutivo>cid:842028745847</ActoConstitutivo>      </cre:CrearEmpresaRequest>   </soapenv:Body></soapenv:Envelope>";
			        httpMethod.setRequestEntity(new StringRequestEntity(request));
			        
					try {
						// logger.info("********** START method");
						System.out.println("********** START method");
						relay.executeMethod(httpMethod, 60000);
						// logger.info("********** DONE");
						System.out.println("********** DONE");
						InputStream is = httpMethod.getResponseBodyAsStream();
						String str = convertStreamToString(is);
						System.out.println("Response : " + str);
//						is.close();
						success = true;
						break;
					} catch (IOException e) {
						logger.warn("MAIN EXCEPTION - RETRY");
						e.printStackTrace();
						continue;
					}
				}
				if (!success) {
					logger.error("****************** RETRY CLIENT COULD NOT RETRY **************");
				}
				System.out.println("********** END thread");
//			}
		}

		public String convertStreamToString(InputStream is) throws IOException {
			if (is != null) {
				Writer writer = new StringWriter();

				char[] buffer = new char[1024];
				try {
					Reader reader = new BufferedReader(new InputStreamReader(
							is, "UTF-8"));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} finally {
					is.close();
				}
				return writer.toString();
			} else {
				return "";
			}
		}

	}

	public void run() throws Exception {
		BasicConfigurator.configure();
		Logger.getLogger("org").setLevel(Level.INFO);
		Logger.getLogger("httpclient").setLevel(Level.INFO);
		Logger.getLogger("com.exalead").setLevel(Level.INFO);
		// Logger.getLogger("org").setLevel(Level.TRACE);

		relay = new FailoverHttpClient();
		relay.setConnectionAcquireFailTimeout(300);
		// relay.addHost("esb", 8080, 1);
		relay.addHost("localhost", 8080, 1);
		relay.startMonitoring(1);

		List<MyThread> threads = new ArrayList<MyThread>();
		for (int i = 0; i < 3; i++) {
			MyThread t = new MyThread();
			t.start();
			threads.add(t);
		}
		
		System.out.println("ENDDING THREADS");
		for (MyThread mt : threads) {
			mt.join();
		}
		System.out.println("THE END");
	}

	public static void main(String[] args) throws Exception {
		new SimpleFailoverTest().run();
	}
}
