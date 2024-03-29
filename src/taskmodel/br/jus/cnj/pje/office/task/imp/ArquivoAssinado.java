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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;

import com.github.signer4j.IByteProcessor;
import com.github.signer4j.ISignedData;
import com.github.signer4j.imp.exception.OutOfMemoryException;
import com.github.signer4j.imp.exception.Signer4JException;
import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.OpenByteArrayOutputStream;
import com.github.utils4j.imp.Strings;

import br.jus.cnj.pje.office.core.imp.UnsupportedCosignException;
import br.jus.cnj.pje.office.task.IArquivo;
import br.jus.cnj.pje.office.task.IArquivoAssinado;

class ArquivoAssinado extends ArquivoWrapper implements IArquivoAssinado {

  protected final File notSignedFile; //original file!
  
  private ISignedData signedData;
  
  ArquivoAssinado(IArquivo arquivo, File notSignedFile) {
    super(arquivo);
    this.notSignedFile = Args.requireNonNull(notSignedFile, "notSignedFile is null");
    this.signedData = null;
  }

  @Override
  public Optional<ISignedData> getSignedData() {
    return Optional.ofNullable(signedData);
  }
  
  @Override
  public void dispose() {
    signedData = null;
  }

  @Override
  public String toString() {
    return notSignedFile.getAbsolutePath();
  }

  @Override
  public void sign(IByteProcessor signer) throws Signer4JException, IOException, UnsupportedCosignException {
    if (signedData == null) {
      Args.requireNonNull(signer, "signer is null");
      try {
        UnsupportedCosignException rethrow = null;
        try(OpenByteArrayOutputStream out = new OpenByteArrayOutputStream(notSignedFile.length())) {
          Files.copy(notSignedFile.toPath(), out);
          try(InputStream content = out.toInputStream()){ //Essa estratégia evita copias de byte[]'s
            new CMSSignedData(content).getSignedContent();
            rethrow = new UnsupportedCosignException("Arquivo já se encontra assinado: " + notSignedFile.getCanonicalPath());
          } catch (CMSException e) {
            rethrow = null;
          } 
        }
        if (rethrow != null) {
          throw rethrow;
        }
        signedData = signer.config(isTerAtributosAssinados()).process(notSignedFile);
      }catch(OutOfMemoryError e) {
        throw new OutOfMemoryException("Arquivo " + notSignedFile.getCanonicalPath() + " muito grande!", e);
      }
    }
  }
  
  @Override
  public String getFileFieldName() {
    Optional<String> fieldName = getParamsEnvio()
      .stream()
      .map(s -> Strings.trim(s))
      .filter(s -> s.startsWith("nomeDoCampoDoArquivo") && s.indexOf('=') > 0)
      .map(s -> s.substring(s.indexOf('=') + 1).trim())
      .findFirst();
    return !fieldName.isPresent() || fieldName.get().isEmpty() ? "arquivo" :  fieldName.get(); 
  }
}
