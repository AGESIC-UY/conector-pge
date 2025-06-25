package uy.gub.agesic.pge;

import uy.gub.agesic.pge.opensaml.AssertionManagerImpl;

public class PGEFactory {

    public static AssertionManager getAssertionManager() {
        return new AssertionManagerImpl();
    }
}
