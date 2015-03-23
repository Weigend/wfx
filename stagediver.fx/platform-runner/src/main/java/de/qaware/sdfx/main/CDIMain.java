package de.qaware.sdfx.main;

import de.qaware.sdfx.lookup.cdi.CDILookupStrategy;

/**
 * stagediver.fx application startup class with cdi as lookup strategy.
 *
 * @author christian.fritz
 */
public class CDIMain extends Main {

    @Override
    public void init() throws Exception {
        CDILookupStrategy.initLookup();
        super.init();
    }
}
