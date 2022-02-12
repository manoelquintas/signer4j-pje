package br.jus.cnj.pje.office.core.imp;

import static java.security.Security.addProvider;

import java.util.Optional;
import java.util.function.Consumer;

import com.github.signer4j.imp.Config;

import br.jus.cnj.pje.office.core.IPjeConfigPersister;
import br.jus.cnj.pje.office.core.IPjeServerAccess;
import br.jus.cnj.pje.office.gui.PjeImages;
import br.jus.cnj.pje.office.provider.PJeProvider;
import br.jus.cnj.pje.office.signer4j.IPjeAuthStrategy;

public class PjeConfig extends Config {
  
  static {
    addProvider(new PJeProvider());
  }
  
  private PjeConfig() {}
  
  public static void setup() {
    setup(PjeImages.PJE_ICON.asImage(), new PJeConfigPersister());
  }
  
  protected static IPjeConfigPersister persister() {
    return (IPjeConfigPersister)Config.config();
  }
  
  public static void loadServerAccess(Consumer<IPjeServerAccess> add) {
    persister().loadServerAccess(add);
  }

  public static Optional<String> authStrategy() {
    return persister().authStrategy();
  } 

  public static void save(IPjeServerAccess... access) {
    persister().save(access);
  }

  public static void save(IPjeAuthStrategy strategy) {
    persister().save(strategy);
  }
  
  public static void overwrite(IPjeServerAccess... access) {
    persister().overwrite(access);
  }

  public static void delete(IPjeServerAccess access) {
    persister().delete(access);
  }
}
