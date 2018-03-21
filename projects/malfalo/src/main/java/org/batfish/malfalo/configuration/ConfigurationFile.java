package org.batfish.malfalo.configuration;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.job.ParseVendorConfigurationJob;
import org.batfish.job.ParseVendorConfigurationResult;
import org.batfish.vendor.VendorConfiguration;

import java.nio.file.Path;


/** Represents one network configuration file (for a host) **/
public class ConfigurationFile {

    private ParserRuleContext _parserRuleContext; //Abstract Syntax Tree ??
    private String _rawFileText;
    /* Keep track of vendor-specific configuration file information */
    private VendorConfiguration _vendorConfiguration;

    public ConfigurationFile(Path cfgPath, Settings batfishSettings) {
        _rawFileText = CommonUtil.readFile(cfgPath.toAbsolutePath());

        //Parse the configuration file using batfish.
        ParseVendorConfigurationJob parseJob =
                new ParseVendorConfigurationJob(batfishSettings,
                        _rawFileText,
                        cfgPath,
                        new Warnings(),
                        ConfigurationFormat.CISCO_IOS); //TODO:  Fix Config Format.

        try{
            ParseVendorConfigurationResult parseResult = parseJob.call();
            _parserRuleContext = parseResult.getParserRuleContext();
            _vendorConfiguration = parseResult.getVendorConfiguration();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public VendorConfiguration getVendorConfiguration() {
        return _vendorConfiguration;
    }

    @Override
    public String toString() {
        return String.format("<%s :: ConfigurationFile instance> ",_vendorConfiguration.getHostname());
    }
}
