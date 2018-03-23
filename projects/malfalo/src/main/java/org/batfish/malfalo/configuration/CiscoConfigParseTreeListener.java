package org.batfish.malfalo.configuration;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.batfish.grammar.cisco.CiscoParser;
import org.batfish.grammar.cisco.CiscoParserBaseListener;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.Interface;
import org.batfish.representation.cisco.StandardAccessList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CiscoConfigParseTreeListener extends CiscoParserBaseListener {

    private ConfigurationFile _config;
    private Vocabulary _vocabulary;
    private CiscoConfiguration _ciscoConfiguration;
    private Map<String, Interface> _interfaces;
    private Map<String, Integer> _interfaceLineNumberMap;
    private Map<String, StandardAccessList> _acls;
    private String _hostName;

    public CiscoConfigParseTreeListener(ConfigurationFile cfg){
        _config = cfg;
        _hostName = _config.getVendorConfiguration().getHostname();
        _ciscoConfiguration = (CiscoConfiguration)_config.getVendorConfiguration();
        _interfaces = _ciscoConfiguration.getInterfaces();
        _acls = _ciscoConfiguration.getStandardAcls();
        _vocabulary = _config.getLexerVocabulary();
        _interfaceLineNumberMap = new TreeMap<>();
        printInterfaces();
    }

    public Map<String Integer>

    public Map<String, java.lang.Integer> getInterfaceLinesMap() {
        return _interfaceLineNumberMap;
    }

    private void printInterfaces(){
        for (Map.Entry<String, Interface> entry: _interfaces.entrySet()){
            System.out.println(String.format("IFACE  ->  %s, %s ",
                    entry.getKey(),
                    entry.getValue().getName()));
        }
    }
    public void track(ParserRuleContext ctx, String msg){
        System.out.println(msg);
        List<? extends  Token>  tokens = _config.getTokens(ctx.getStart().getTokenIndex(),
                ctx.getStop().getTokenIndex());

        for (int i =0;i<tokens.size();i++) {
            System.out.println(String.format("<%dth Token Summary> Type : %d (%s); Line : %d ; Text : %s",
                    i+1,
                    tokens.get(i).getType(),
                    _vocabulary.getSymbolicName(tokens.get(i).getType()),
                    tokens.get(i).getLine(),
                    tokens.get(i).getText()));
        }
    }


    @Override
    public void enterS_interface(CiscoParser.S_interfaceContext ctx) {
        track(ctx, "interface stanza (Enter)" + ctx.iname.getText() + " Line : "
         +ctx.getStart().getLine());
        _interfaceLineNumberMap.put(ctx.iname.getText(), ctx.getStart().getLine());
    }

    @Override
    public void enterStandard_access_list_stanza(CiscoParser.Standard_access_list_stanzaContext ctx) {
        track(ctx, "standard acl stanza (ENTER)" + ctx.name + ctx.num );
    }

    //
//
//    @Override
//    public void enterS_interface_line(S_interface_lineContext ctx) {
//        printTokens(ctx);
//    }
//
//    @Override
//    public void exitS_interface_line(S_interface_lineContext ctx) {
//        printTokens(ctx);
//    }
}
