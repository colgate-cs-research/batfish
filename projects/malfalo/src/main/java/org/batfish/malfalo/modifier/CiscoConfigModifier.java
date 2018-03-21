package org.batfish.malfalo.modifier;

import org.batfish.malfalo.configuration.ConfigurationFile;

public class CiscoConfigModifier extends  ConfigModifier implements IModifier{

    public CiscoConfigModifier(ConfigurationFile config) {
        super(config);
    }

    @Override
    public void apply(Modification modification) {

    }
}
