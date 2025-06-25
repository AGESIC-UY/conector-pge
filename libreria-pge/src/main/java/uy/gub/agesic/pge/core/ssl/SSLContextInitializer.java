package uy.gub.agesic.pge.core.ssl;

import javax.xml.ws.Dispatch;
import uy.gub.agesic.pge.core.config.PGEConfiguration;

public interface SSLContextInitializer {
  void initSSLContext(Dispatch<?> paramDispatch, PGEConfiguration paramPGEConfiguration);

  void initSSLContextPort(Object paramObject, PGEConfiguration paramPGEConfiguration);
}