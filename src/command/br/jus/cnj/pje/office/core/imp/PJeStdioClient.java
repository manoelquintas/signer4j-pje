package br.jus.cnj.pje.office.core.imp;

import static com.github.signer4j.imp.Strings.trim;

import java.io.IOException;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.json.JSONObject;

import com.github.signer4j.IContentType;
import com.github.signer4j.IDownloadStatus;
import com.github.signer4j.ISignedData;
import com.github.signer4j.imp.Objects;
import com.github.signer4j.imp.Pair;
import com.github.signer4j.imp.function.Runnable;
import com.github.signer4j.imp.function.Supplier;

import br.jus.cnj.pje.office.core.Version;
import br.jus.cnj.pje.office.task.IArquivoAssinado;
import br.jus.cnj.pje.office.task.IAssinadorHashArquivo;
import br.jus.cnj.pje.office.web.IPjeHeaders;

public class PJeStdioClient extends AstractPjeClient<JSONObject> {

  PJeStdioClient(Version version) {
    super(version);
  }
  
  @Override
  protected <R extends JSONObject> R createOutput(R request, String session, String userAgent) {
    request.put(HttpHeaders.COOKIE, session);
    request.put(IPjeHeaders.VERSION, version.toString());
    request.put(HttpHeaders.USER_AGENT, userAgent);
    return request;
  }
  
  private JSONObject createOutput(String endPoint, String session, String userAgent) {
    JSONObject out = createOutput(new JSONObject(), session, userAgent);
    out.put("endPoint", endPoint);
    return out;
  }

  @Override
  protected JSONObject createOutput(String endPoint, String session, String userAgent, ISignedData signedData) throws Exception {
    JSONObject out = createOutput(endPoint, session, userAgent);
    out.put("assinatura", signedData.getSignature64());
    out.put("cadeiaCertificado", signedData.getCertificateChain64());
    return out;
  }

  @Override
  protected JSONObject createOutput(String endPoint, String session, String userAgent, ISignedData signedData, IAssinadorHashArquivo file) throws Exception {
    JSONObject out = createOutput(endPoint, session, userAgent);
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
  protected JSONObject createOutput(String endPoint, String session, String userAgent, IArquivoAssinado file, IContentType contentType) throws Exception {
    JSONObject out = createOutput(endPoint, session, userAgent);
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
  protected JSONObject createOutput(String endPoint, String session, String userAgent, String certificateChain64) throws Exception {
    JSONObject out = createOutput(endPoint, session, userAgent);
    out.put("cadeiaDeCertificadosBase64", certificateChain64);
    return out;
  }

  @Override
  protected JSONObject createOutput(String endPoint, String session, String userAgent, Object pojo) throws Exception {
    JSONObject out = createOutput(endPoint, session, userAgent);
    out.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    out.put("pojo", Objects.toJson(pojo));
    return out;
  }
  
  @Override
  public void down(String endPoint, String session, String userAgent, IDownloadStatus status) throws PJeClientException {
    throw new PJeClientException("download not implemented");
  }

  @Override
  public void close() throws IOException {
    
  }

  @Override
  protected void post(Supplier<JSONObject> supplier, Runnable<String, PJeClientException> checkResults) throws PJeClientException {
    try {
      JSONObject json = supplier.get();
      System.out.println(json.toString(2));
    } catch (Exception e) {
      throw new PJeClientException(e); 
    }
  }
}
