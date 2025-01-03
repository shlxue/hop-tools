package org.apache.hop.maven.extensions;

import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.apache.maven.shared.utils.logging.MessageBuilder;
import org.apache.maven.shared.utils.logging.MessageUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
@Component(role = ProfileActivator.class, hint = "xwt-profile")
public class XwtProfileActivator implements ProfileActivator {
  private static final String XWT_TYPE = "xwt.type";
  private static final String PROFILE_PREFIX = "dep-";
  private static final String[] PROFILES = new String[] {"dep-swt", "dep-rwt"};
  private final Logger log = LoggerFactory.getLogger(XwtProfileActivator.class);
  private int counter = 0;

  @Override
  public boolean isActive(
      Profile profile,
      ProfileActivationContext context,
      ModelProblemCollector modelProblemCollector) {
    boolean active = false;
    if (nonActivation(profile)) {
      String xwtType =
          xwtType(context.getUserProperties())
              .orElse(
                  xwtType(context.getSystemProperties())
                      .orElse(xwtType(context.getProjectProperties()).orElse(null)));
      active = match(xwtType, profile);
      if (active) {
        if (counter == 0) {
          log.info("{}", message(profile));
        }
        counter++;
      }
    }
    return active;
  }

  @Override
  public boolean presentInConfig(
      Profile profile,
      ProfileActivationContext context,
      ModelProblemCollector modelProblemCollector) {
    return isDepProfile(profile);
  }

  static Optional<String> xwtType(Map<String, String> map) {
    return Optional.ofNullable(map.get(XWT_TYPE));
  }

  static boolean match(String xwtType, Profile profile) {
    String id = profile.getId();
    if (xwtType == null) {
      return id.equals(PROFILES[0]);
    }
    if (xwtType.startsWith("!")) {
      xwtType = PROFILE_PREFIX + xwtType.substring(1);
      return !id.equals(xwtType);
    }
    xwtType = PROFILE_PREFIX + xwtType;
    return id.equals(xwtType);
  }

  static boolean isDepProfile(Profile profile) {
    String id = profile.getId();
    return id.startsWith("dep-") && (id.equals(PROFILES[0]) || id.equals(PROFILES[1]));
  }

  static boolean nonActivation(Profile profile) {
    Activation act = profile.getActivation();
    return act == null
        || !(act.isActiveByDefault()
            || act.getJdk() != null
            || act.getOs() != null
            || act.getProperty() != null
            || act.getFile() != null);
  }

  private MessageBuilder message(Profile profile) {
    return MessageUtils.buffer().a("Auto active profile: ").success(profile.getId());
  }
}
