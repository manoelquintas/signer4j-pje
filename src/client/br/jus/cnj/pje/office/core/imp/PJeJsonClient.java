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

import static com.github.utils4j.imp.Strings.trim;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.json.JSONObject;

import com.github.signer4j.ISignedData;
import com.github.utils4j.IContentType;
import com.github.utils4j.imp.Pair;

import br.jus.cnj.pje.office.core.IPjeHeaders;
import br.jus.cnj.pje.office.core.ISocketCodec;
import br.jus.cnj.pje.office.core.Version;
import br.jus.cnj.pje.office.task.IArquivoAssinado;
import br.jus.cnj.pje.office.task.IAssinadorHashArquivo;
import br.jus.cnj.pje.office.task.IPjeTarget;

class PJeJsonClient extends AstractPjeClient<JSONObject> {

  public PJeJsonClient(Version version, ISocketCodec<JSONObject> postCodec) {
    super(version, postCodec);
  }
  
  protected <R extends JSONObject> R createOutput(R request, IPjeTarget target) {
    request.put(HttpHeaders.COOKIE, target.getSession());
    request.put(IPjeHeaders.VERSION, version.toString());
    return request;
  }
  
  private JSONObject createOutput(IPjeTarget target) {
    JSONObject out = createOutput(new JSONObject(), target);
    out.put("endPoint", target.getEndPoint());
    return out;
  }

  @Override
  protected JSONObject createOutput(IPjeTarget target, ISignedData signedData) throws Exception {
    JSONObject out = createOutput(target);
    out.put("assinatura", signedData.getSignature64());
    out.put("cadeiaCertificado", signedData.getCertificateChain64());
    return out;
  }

  @Override
  protected JSONObject createOutput(IPjeTarget target, ISignedData signedData, IAssinadorHashArquivo file) throws Exception {
    JSONObject out = createOutput(target);
    out.put("assinatura", signedData.getSignature64());
    out.put("cadeiaCertificado", signedData.getCertificateChain64());
    out.put("id", file.getId().orElse(""));
    out.put("codIni", file.getCodIni().orElse(""));
    out.put("hash", file.getHash().get());
    if (file.getIdTarefa().isPresent())
      out.put("idTarefa", file.getIdTarefa().get().toString());
    return out;
  }

  @Override
  protected JSONObject createOutput(IPjeTarget target, IArquivoAssinado file, IContentType contentType) throws Exception {
    JSONObject out = createOutput(target);
    JSONObject body = new JSONObject();
    body.put("mimeType", contentType.getMineType());
    body.put("charset", contentType.getCharset());
    body.put("fileName", file.getNome().get() + contentType.getExtension());
    body.put("signature", file.getSignedData().get().getSignature());
    out.put(file.getFileFieldName(), body);
    file.getParamsEnvio().stream().map(param -> {
      int idx = (param = trim(param)).indexOf('=');
      return Pair.of(
        idx < 0 ? param : param.substring(idx),  
        idx < 0 ? ""    : param.substring(idx + 1)
      );
    }).forEach(nv -> out.put(nv.getKey(), nv.getValue()));
    return out;
  }
  
  @Override
  protected JSONObject createOutput(IPjeTarget target, String certificateChain64) throws Exception {
    JSONObject out = createOutput(target);
    out.put("cadeiaDeCertificadosBase64", certificateChain64);
    return out;
  }

  @Override
  protected JSONObject createOutput(IPjeTarget target, Object pojo) throws Exception {
    JSONObject out = createOutput(target);
    out.put("contentType", ContentType.APPLICATION_JSON.getMimeType());
    out.put("pojo", pojo);
    return out;
  }
}
