package at.oenb.dlt;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import at.oenb.dltrouting.DltRoutingApplication;

@RunWith(SpringRunner.class)
// @ActiveProfiles({ "OrgAustriaProfile" })
// @ActiveProfiles({ "OrgGermanyProfile" })
// @ActiveProfiles({ "OrgItalyProfile" })
@ActiveProfiles({ "OrgNetherlandsProfile" })
@ContextConfiguration(classes = DltRoutingApplication.class, loader = AnnotationConfigContextLoader.class, initializers = ConfigFileApplicationContextInitializer.class)
public abstract class AbstractDltTest {
}
