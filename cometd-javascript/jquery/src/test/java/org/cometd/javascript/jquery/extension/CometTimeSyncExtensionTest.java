package org.cometd.javascript.jquery.extension;

import java.net.URL;

import org.cometd.Bayeux;
import org.cometd.javascript.jquery.AbstractJQueryCometTest;
import org.cometd.server.ext.TimesyncExtension;
import org.testng.annotations.Test;

/**
 * @version $Revision$ $Date$
 */
public class CometTimeSyncExtensionTest extends AbstractJQueryCometTest
{
    protected void customizeBayeux(Bayeux bayeux)
    {
        bayeux.addExtension(new TimesyncExtension());
    }

    @Test
    public void testTimeSync() throws Exception
    {
        URL timesyncExtensionURL = new URL(contextURL + "/org/cometd/TimeSyncExtension.js");
        evaluateURL(timesyncExtensionURL);

        evaluateScript("$.cometd.setLogLevel('debug');");
        evaluateScript("$.cometd.registerExtension('timesync', new org.cometd.TimeSyncExtension());");

        evaluateScript("var inTimeSync = undefined;");
        evaluateScript("var outTimeSync = undefined;");
        evaluateScript("$.cometd.registerExtension('test', {" +
                "incoming: function(message)" +
                "{" +
                "    var channel = message.channel;" +
                "    if (channel && channel.indexOf('/meta/') == 0)" +
                "    {" +
                "        inTimeSync = message.ext && message.ext.timesync;" +
                "    }" +
                "}," +
                "outgoing: function(message)" +
                "{" +
                "    var channel = message.channel;" +
                "    if (channel && channel.indexOf('/meta/') == 0)" +
                "    {" +
                "        outTimeSync = message.ext && message.ext.timesync;" +
                "    }" +
                "}" +
                "});");
        evaluateScript("$.cometd.init('" + cometURL + "');");
        Thread.sleep(500); // Wait for the long poll

        // Both client and server should support timesync
        Object outTimeSync = get("outTimeSync");
        assert outTimeSync != null;
        Object inTimeSync = get("inTimeSync");
        assert inTimeSync != null;

        evaluateScript("var timesync = $.cometd.getExtension('timesync');");
        evaluateScript("var networkLag = timesync.getNetworkLag();");
        evaluateScript("var timeOffset = timesync.getTimeOffset();");
        int networkLag = ((Number)get("networkLag")).intValue();
        assert networkLag > 0;

        evaluateScript("$.cometd.disconnect();");
        Thread.sleep(500); // Wait for the disconnect to return
    }
}