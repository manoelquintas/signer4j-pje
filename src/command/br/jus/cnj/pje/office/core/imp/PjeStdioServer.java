package br.jus.cnj.pje.office.core.imp;

import static br.jus.cnj.pje.office.core.imp.SimpleContext.of;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.github.signer4j.IFinishable;

import br.jus.cnj.pje.office.core.IPjeContext;

public class PjeStdioServer extends PjeTextServer {

  public PjeStdioServer(IFinishable finishingCode) {
    super(finishingCode, "stdio://native-messaging");
  }

  protected IPjeContext createContext(String input) throws Exception {
    return of(new PjeSysinRequest(input), new PjeSysoutResponse());
  }
  
  @Override
  protected IPjeContext createContext() throws InterruptedException, Exception {
    try(BufferedReader bf = new BufferedReader(new InputStreamReader(System.in))) {
      do {
        while (!bf.ready()) { //TODO isso não me parece portável em diferentes plataformas!
          Thread.sleep(600);
        }
        String input = bf.readLine();
        return createContext(input);
      }while(true);
    }
  }
}

