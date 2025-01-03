package org.apache.hop.maven.extensions;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.utils.logging.MessageBuilder;
import org.apache.maven.shared.utils.logging.MessageUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Locale;
import java.util.Properties;

@Singleton
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "osgi-platform")
public class SwtPlatformExtension extends AbstractMavenLifecycleParticipant {
  private static final String SWT_OSGI_PLATFORM = "osgi.platform";
  private final Logger log = LoggerFactory.getLogger(SwtPlatformExtension.class);
  private final String osgiPlatform =
      getSwtPlatform(
          normalizeOs(System.getProperty("os.name")), normalizeArch(System.getProperty("os.arch")));

  @Override
  public void afterSessionStart(MavenSession session) {
    Properties props = new Properties();
    props.putIfAbsent(SWT_OSGI_PLATFORM, osgiPlatform);
    injectDynamicProps(session, props);
    log.info("{}", message(SWT_OSGI_PLATFORM, osgiPlatform));
  }

  private void injectDynamicProps(MavenSession session, Properties props) {
    MavenExecutionRequest request = session.getRequest();
    request.setUserProperties(merge(request.getUserProperties(), props));
    ProjectBuildingRequest buildingRequest = session.getProjectBuildingRequest();
    buildingRequest.setUserProperties(merge(buildingRequest.getUserProperties(), props));
    if (session.getRepositorySession() instanceof DefaultRepositorySystemSession rss) {
      rss.setUserProperties(props);
    }
  }

  private Properties merge(Properties origin, Properties props) {
    Properties clone = new Properties();
    clone.putAll(origin);
    clone.putAll(props);
    return clone;
  }

  private String getSwtPlatform(String os, String arch) {
    String osPrefix;
    if (os.contains("osx")) {
      osPrefix = "cocoa.macosx";
    } else if (os.contains("windows")) {
      osPrefix = "win32.win32";
    } else if (os.contains("linux")) {
      osPrefix = "gtk.linux";
    } else {
      throw new UnsupportedOperationException("SWT library don't support os: " + os);
    }
    return String.format("%s.%s", osPrefix, arch);
  }

  static String normalizeOs(String value) {
    value = normalize(value);
    if (value.startsWith("aix")) return "aix";
    if (value.startsWith("hpux")) return "hpux";
    if (value.startsWith("os400") && (value.length() <= 5 || !Character.isDigit(value.charAt(5))))
      return "os400";
    String linux = "linux";
    if (value.startsWith(linux)) return linux;
    if (value.startsWith("macosx") || value.startsWith("osx")) return "osx";
    if (value.startsWith("freebsd")) return "freebsd";
    if (value.startsWith("openbsd")) return "openbsd";
    if (value.startsWith("netbsd")) return "netbsd";
    if (value.startsWith("solaris") || value.startsWith("sunos")) return "sunos";
    String win = "windows";
    if (value.startsWith(win)) return win;
    if (value.startsWith("zos")) return "zos";
    return "unknown";
  }

  @SuppressWarnings("java:S3776")
  static String normalizeArch(String value) {
    value = normalize(value);
    if (value.matches("(x8664|amd64|ia32e|em64t|x64)")) return "x86_64";
    if (value.matches("(x8632|x86|i[3-6]86|ia32|x32)")) return "x86_32";
    if (value.matches("(ia64w?|itanium64)")) return "itanium_64";
    if ("ia64n".equals(value)) return "itanium_32";
    if (value.matches("(sparc|sparc32)")) return "sparc_32";
    if (value.matches("(sparcv9|sparc64)")) return "sparc_64";
    if (value.matches("(arm|arm32)")) return "arm_32";
    if ("aarch64".equals(value)) return "aarch64";
    if (value.matches("(mips|mips32)")) return "mips_32";
    if (value.matches("(mipsel|mips32el)")) return "mipsel_32";
    if ("mips64".equals(value)) return "mips_64";
    if ("mips64el".equals(value)) return "mipsel_64";
    if (value.matches("(ppc|ppc32)")) return "ppc_32";
    if (value.matches("(ppcle|ppc32le)")) return "ppcle_32";
    if ("ppc64".equals(value)) return "ppc_64";
    if ("ppc64le".equals(value)) return "ppcle_64";
    if ("s390".equals(value)) return "s390_32";
    if ("s390x".equals(value)) return "s390_64";
    if ("riscv".equals(value)) return "riscv";
    return "unknown";
  }

  private static String normalize(String value) {
    if (value == null) {
      return "";
    }
    return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
  }

  private MessageBuilder message(String key, String value) {
    return MessageUtils.buffer()
        .a("Native ")
        .strong("SWT")
        .a(" library artifact id by ")
        .failure(key)
        .a(": ")
        .success(value);
  }
}
