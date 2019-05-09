package gub.agesic.connector.integration;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uy.red.pge.servicios.agesic.artee.descargarexpediente.DescargarExpedienteIn;
import uy.red.pge.servicios.agesic.artee.descargarexpediente.DescargarExpedienteOut;
import uy.red.pge.servicios.agesic.artee.descargarexpediente.ServiceDownloadExpedient;
import uy.red.pge.servicios.agesic.artee.descargarexpediente.ServicioDescargarExpediente;
import uy.red.pge.servicios.agesic.artee.enviarexpediente.EnviarExpedienteIn;
import uy.red.pge.servicios.agesic.artee.enviarexpediente.EnviarExpedienteOut;
import uy.red.pge.servicios.agesic.artee.enviarexpediente.ServiceSendExpedientStream;
import uy.red.pge.servicios.agesic.artee.enviarexpediente.ServicioEnviarExpediente;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MTOMIntegTest {

	private static final String PATH_TO_UPLOAD_FILE = "/home/abrusco/Descargas/fruta.txt";

	private static final String PATH_TO_DOWNLOAD_FILE = "/home/abrusco/Descargas/fruta.txt";

	@Test
	public void uploadFileWithMTOM() throws IOException {
		final ServiceSendExpedientStream service = new ServiceSendExpedientStream();
		final ServicioEnviarExpediente port = service.getCustomBindingServicioEnviarExpediente();

		final BindingProvider bindingProvider = (BindingProvider) port;
		final SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
		soapBinding.setMTOMEnabled(true);
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"http://testing.hg.red.uy:9800/connector-runtime/ARTEE/EnviarExpediente");

		final Binding binding = ((BindingProvider) port).getBinding();
		final List<Handler> handlerList = binding.getHandlerChain();
		handlerList.add(new Handler1());
		binding.setHandlerChain(handlerList);

		final EnviarExpedienteIn request = new EnviarExpedienteIn();
		request.setExpediente(readFile(PATH_TO_UPLOAD_FILE));

		final long before = System.currentTimeMillis();
		final EnviarExpedienteOut response = port.enviarExpediente(request);
		final long after = System.currentTimeMillis();
		System.out.println((after - before) / 1000);
		assertThat(response.getResultado().getCodigo(), equalTo("OK"));
	}

	@Test
	public void downloadWithMTOM() throws IOException {
		final ServiceDownloadExpedient service = new ServiceDownloadExpedient();
		final ServicioDescargarExpediente port = service.getCustomBindingServicioDescargarExpediente();

		final DescargarExpedienteIn request = new DescargarExpedienteIn();

		request.setDominioOrigen("test_descarga.expediente.red.uy");
		request.setSubdominio("mesaentradados");

		final BindingProvider bindingProvider = (BindingProvider) port;
		final SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
		soapBinding.setMTOMEnabled(true);
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"http://testing.hg.red.uy:9800/connector-runtime/ARTEE/DescargarExpediente");

		final Binding binding = ((BindingProvider) port).getBinding();
		final List<Handler> handlerList = binding.getHandlerChain();
		handlerList.add(new Handler2());
		binding.setHandlerChain(handlerList);

		final DescargarExpedienteOut response = port.descargarExpediente(request);
		FileUtils.writeByteArrayToFile(new File(PATH_TO_DOWNLOAD_FILE), response.getExpediente());
		assertTrue(response.getExpediente().length == 311693);

	}

	private static byte[] readFile(final String path) throws IOException {
		final File file = new File(path);
		final byte[] bytesArray = new byte[(int) file.length()];
		final FileInputStream fis = new FileInputStream(file);
		fis.read(bytesArray);
		fis.close();

		return bytesArray;
	}

	private class Handler2 implements SOAPHandler<SOAPMessageContext> {

		@Override
		public boolean handleFault(final SOAPMessageContext arg0) {
			return true;
		}

		@Override
		public boolean handleMessage(final SOAPMessageContext context) {
			final Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			if (isRequest) {
				final SOAPMessage soapMsg = context.getMessage();

				try {
					soapMsg.writeTo(System.out);
				}
				catch (final SOAPException e) {
					throw new RuntimeException("Error logging message", e);
				}
				catch (final IOException e) {
					throw new RuntimeException("Error logging message", e);
				}
			}
			return true;

		}

		@Override
		public void close(final MessageContext context) {
		}

		@Override
		public Set<QName> getHeaders() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private class Handler1 implements SOAPHandler<SOAPMessageContext> {

		@Override
		public void close(final MessageContext arg0) {

		}

		@Override
		public boolean handleFault(final SOAPMessageContext arg0) {
			return true;
		}

		@Override
		public boolean handleMessage(final SOAPMessageContext context) {
			final Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			if (isRequest) {
				final SOAPMessage soapMsg = context.getMessage();

				try {
					final SOAPEnvelope soapEnv = soapMsg.getSOAPPart().getEnvelope();
					SOAPHeader soapHeader = soapEnv.getHeader();
					// if no header, add one
					if (soapHeader == null) {
						soapHeader = soapEnv.addHeader();
					}
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "UnidadEjecutoraAnterior", "env", "2",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "UnidadEjecutora", "env", "2",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "SubdominioDestino", "env",
							"mesaentradados", soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "SeccionOrigen", "env", "Computacion",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "SeccionDestino", "env", "oficina",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "NumeroAnterior", "env", "00139",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Numero", "env", "00139", soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "IncisoAnterior", "env", "10",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Inciso", "env", "10", soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "ElementosFisicos", "env", "false",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "DominioOrigen", "env",
							"test_envio.expediente.red.uy", soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "DominioDestino", "env",
							"test_descarga.expediente.red.uy", soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Confidencial", "env", "false",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "AnioAnterior", "env", "2016",
							soapHeader);
					addHeader("http://servicios.pge.red.uy/agesic/artee/EnviarExpediente", "Anio", "env", "2016", soapHeader);

					soapMsg.saveChanges();

					// tracking
					soapMsg.writeTo(System.out);
				}
				catch (final SOAPException e) {
					throw new RuntimeException("Error adding wsaddressing header", e);
				}
				catch (final IOException e) {
					throw new RuntimeException("Error adding wsaddressing header", e);
				}

			}
			return true;

		}

		private void addHeader(final String namespace, final String elementName, final String prefix, final String value,
				final SOAPHeader soapHeader) throws SOAPException {
			final QName wsaTo = new QName(namespace, elementName, prefix);
			final SOAPHeaderElement header = soapHeader.addHeaderElement(wsaTo);
			header.addTextNode(value);
		}

		@Override
		public Set<QName> getHeaders() {
			return null;
		}

	}
}
