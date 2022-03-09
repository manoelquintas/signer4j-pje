
package br.jus.cnj.pje.office.imp;  

import static br.jus.cnj.pje.office.imp.PjeOfficeFrontEnd.getBest;
import static com.github.utils4j.gui.imp.SwingTools.invokeLater;

import br.jus.cnj.pje.office.IPjeFrontEnd;
import br.jus.cnj.pje.office.core.imp.PjeLifeCycleFactory;

public class PjeOfficeStdioClassic extends PjeOfficeClassic {

  private static PjeOfficeClassic createInstance(IPjeFrontEnd front) {
    return new PjeOfficeStdioClassic(front);
  }

  public static void main(String[] args) {
    invokeLater(() ->  createInstance(getBest()).start());
  }

  private PjeOfficeStdioClassic(IPjeFrontEnd frontEnd) {
    super(frontEnd, PjeLifeCycleFactory.STDIO);
  }

  @Override
  protected PjeOfficeClassic newInstance(IPjeFrontEnd front, String origin) {
    return createInstance(front);
  }
}
