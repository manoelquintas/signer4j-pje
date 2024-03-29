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


package br.jus.cnj.pje.office.core.imp;

import java.util.List;

import com.github.signer4j.ISignedData;
import com.github.utils4j.ICanceller;
import com.github.utils4j.IContentType;
import com.github.utils4j.IDownloadStatus;
import com.github.utils4j.imp.Args;

import br.jus.cnj.pje.office.core.IGetCodec;
import br.jus.cnj.pje.office.core.IPjeClient;
import br.jus.cnj.pje.office.task.IArquivoAssinado;
import br.jus.cnj.pje.office.task.IAssinadorBase64ArquivoAssinado;
import br.jus.cnj.pje.office.task.IAssinadorHashArquivo;
import br.jus.cnj.pje.office.task.IDadosSSO;
import br.jus.cnj.pje.office.task.IPjeTarget;

class PjeClientWrapper implements IPjeClient {

  private final IPjeClient client;
  
  protected PjeClientWrapper(IPjeClient client) {
    this.client = Args.requireNonNull(client, "client is null");
  }
  
  @Override
  public void down(IPjeTarget target, IDownloadStatus status) throws PJeClientException {
    client.down(target, status);
  }

  @Override
  public PjeTaskResponse send(IPjeTarget target, ISignedData signedData) throws PJeClientException {
    return client.send(target, signedData);
  }

  @Override
  public PjeTaskResponse send(IPjeTarget target, ISignedData signedData, IAssinadorHashArquivo file) throws PJeClientException {
    return client.send(target,  signedData, file);
  }

  @Override
  public PjeTaskResponse send(IPjeTarget target, IArquivoAssinado file, IContentType contentType) throws PJeClientException {
    return client.send(target, file, contentType);
  }

  @Override
  public PjeTaskResponse send(IPjeTarget target, List<IAssinadorBase64ArquivoAssinado> files) throws PJeClientException {
    return client.send(target,  files);
  }

  @Override
  public PjeTaskResponse send(IPjeTarget target, String certificateChain64) throws PJeClientException {
    return client.send(target, certificateChain64);
  }

  @Override
  public PjeTaskResponse send(IPjeTarget target, IDadosSSO dadosSSO) throws PJeClientException {
    return client.send(target, dadosSSO);
  }

  @Override
  public void close() throws Exception {
    client.close();
  }

  @Override
  public void setCanceller(ICanceller canceller) {
    client.setCanceller(canceller);
  }

  @Override
  public IGetCodec getCodec() {
    return client.getCodec();
  }
}
