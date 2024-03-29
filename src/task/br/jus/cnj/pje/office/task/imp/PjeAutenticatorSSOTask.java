/*
* MIT License
* 
* Copyright (c) 2022 Leonardo de Lima Oliveira
* 
* https://github.com/l3onardo-oliv3ira
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/


package br.jus.cnj.pje.office.task.imp;

import java.io.Serializable;
import java.security.cert.CertificateException;

import com.github.signer4j.ISignedData;
import com.github.taskresolver4j.exception.TaskException;
import com.github.utils4j.imp.Params;

import br.jus.cnj.pje.office.core.imp.PjeTaskResponse;
import br.jus.cnj.pje.office.task.IDadosSSO;
import br.jus.cnj.pje.office.task.ITarefaAutenticador;

class PjeAutenticatorSSOTask extends PjeAutenticatorTask {

  public PjeAutenticatorSSOTask(Params request, ITarefaAutenticador pojo) {
    super(request, pojo);
  }
  
  protected String token;

  @Override
  protected void validateTaskParams() throws TaskException {
    super.validateTaskParams();
    this.token = PjeTaskChecker.checkIfPresent(getPojoParams().getToken(), "token"); 
  }
  
  @Override
  protected PjeTaskResponse send(ISignedData signedData) throws Exception {
    return getPjeClient().send(
      getTarget(enviarPara),
      new DadosSSO(token, mensagem, signedData)
    );
  }
  
  private static class DadosSSO implements Serializable, IDadosSSO {

    private static final long serialVersionUID = 1L;
    
    private String uuid;
    private String mensagem;
    private String assinatura;
    private String certChain;

    public DadosSSO(String token, String mensagem, ISignedData signedData) throws CertificateException {
      super();
      this.uuid = token;
      this.mensagem = mensagem;
      this.assinatura = signedData.getSignature64();
      this.certChain = signedData.getCertificateChain64();
    }
    
    @Override
    public String getUuid() {
      return uuid;
    }

    @Override
    public String getMensagem() {
      return mensagem;
    }

    @Override
    public String getAssinatura() {
      return assinatura;
    }

    @Override
    public String getCertChain() {
      return certChain;
    }
  }
}
