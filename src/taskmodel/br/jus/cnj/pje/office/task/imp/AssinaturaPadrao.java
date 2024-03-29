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

import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.signer4j.IByteProcessor;
import com.github.signer4j.ICMSSigner;
import com.github.taskresolver4j.exception.TaskException;
import com.github.utils4j.IConstants;
import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.Objects;

import br.jus.cnj.pje.office.signer4j.IPjeToken;
import br.jus.cnj.pje.office.task.IAssinaturaPadrao;
import br.jus.cnj.pje.office.task.ITarefaAssinador;

public enum AssinaturaPadrao implements IAssinaturaPadrao {
  ENVELOPED(".xml",  "application/xml", IConstants.UTF_8) {
    @Override
    public IByteProcessor getByteProcessor(IPjeToken token, ITarefaAssinador params) {
      Args.requireNonNull(token, "token is null");
      return token.xmlSignerBuilder().build();
    }
  },
  
  NOT_ENVELOPED(".p7s", "application/pkcs7-signature"){
    @Override
    public IAssinaturaPadrao checkIfDependentParamsIsPresent(ITarefaAssinador params) throws TaskException  {
      PjeTaskChecker.checkIfNull(params, "params is null");
      PjeTaskChecker.checkIfPresent(params.getAlgoritmoHash(), "algoritmoHash");
      PjeTaskChecker.checkIfPresent(params.getTipoAssinatura(), "tipoAssinatura");
      return this;
    }

    @Override
    public IByteProcessor getByteProcessor(IPjeToken token, ITarefaAssinador params) {
      Args.requireNonNull(token, "token is null");
      Args.requireNonNull(params, "param is null");
      return token.cmsSignerBuilder()
        .usingSignatureAlgorithm(params.getAlgoritmoHash().get())
        .usingSignatureType(params.getTipoAssinatura().get())
        .usingConfig((p, o) -> ((ICMSSigner)p).usingAttributes((Boolean)o))
        .build();
    }
  }; 
  
  private final String extension;
  private final String mimeType;
  private final Charset charset;
  
  private AssinaturaPadrao(String extension, String mimeType) {
    this(extension, mimeType, null);
  }

  private AssinaturaPadrao(String extension, String mimeType, Charset charset) {
    this.extension = extension;
    this.mimeType = mimeType;
    this.charset = charset;
  }

  @Override
  public IAssinaturaPadrao checkIfDependentParamsIsPresent(ITarefaAssinador params) throws TaskException  {
    return this;
  }
  
  @JsonCreator
  public static AssinaturaPadrao fromString(final String key) {
    return key == null ? NOT_ENVELOPED : valueOf(key.toUpperCase());
  }

  @JsonValue
  public String getKey() {
    return this.name().toLowerCase();
  }
  
  @Override
  public String getExtension() {
    return extension;
  }

  @Override
  public String getMineType() {
    return mimeType;
  }

  @Override
  public String getCharset() {
    return Objects.toString(charset, null);
  }
}


