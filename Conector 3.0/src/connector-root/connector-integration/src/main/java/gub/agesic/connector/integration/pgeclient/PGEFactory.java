package gub.agesic.connector.integration.pgeclient;

import gub.agesic.connector.integration.pgeclient.opensaml.AssertionManagerImpl;

public class PGEFactory {

    public static AssertionManager getAssertionManager() {
        return new AssertionManagerImpl();
    }
}
