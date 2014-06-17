package org.tarent.osiam;

import com.yammer.dropwizard.config.Environment;
import org.junit.Before;
import org.junit.Test;
import org.tarent.osiam.config.OsiamConfiguration;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: mley
 * Date: 03.04.14
 * Time: 14:28
 * To change this template use File | Settings | File Templates.
 */
public class OsiamDropwizardTest {

    OsiamDropwizard od;
    RedirectUriProvider rup;
    OsiamConfiguration conf;

    @Before
    public void setup() {
        conf = mock(OsiamConfiguration.class);
        rup = mock(RedirectUriProvider.class);
        od = new OsiamDropwizard(conf, rup);

        when(conf.getEndpoint()).thenReturn("http://endpoint:8080");
        when(conf.getClientId()).thenReturn("clientid");
        when(conf.getClientSecret()).thenReturn("secret");
        when(conf.getClientRedirectUri()).thenReturn("http://clientendpoint:8080/oauth");
    }

    @Test
    public void testConfigure() {

        Environment env = mock(Environment.class);
        od.configure(env);

    }

    @Test
    public void testGetLoginUri() {
        String uri = od.getRedirectLoginUri("state");

        assertTrue(uri.endsWith("&state=state"));
    }

}
