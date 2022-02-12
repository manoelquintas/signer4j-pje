
package br.jus.cnj.pje.office.imp;  

import static br.jus.cnj.pje.office.imp.PjeOfficeFrontEnd.getBest;
import static com.github.signer4j.imp.SwingTools.invokeLater;

import br.jus.cnj.pje.office.IPjeFrontEnd;
import br.jus.cnj.pje.office.web.imp.PjeCommandFactory;

public class PjeOfficeWeb extends PjeOfficeClassic {

  private static PjeOfficeClassic createInstance(IPjeFrontEnd front) {
    return new PjeOfficeWeb(front);
  }

  public static void main(String[] args) {
    invokeLater(() ->  createInstance(getBest()).start());
  }

  private PjeOfficeWeb(IPjeFrontEnd frontEnd) {
    super(frontEnd, PjeCommandFactory.WEB);
  }

  @Override
  protected PjeOfficeClassic newInstance(IPjeFrontEnd front) {
    return createInstance(front);
  }
}
