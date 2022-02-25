package br.jus.cnj.pje.office.core.imp;

import java.nio.charset.Charset;

import com.github.utils4j.IConstants;

class PjeClipTaskResponse extends PjeStringTaskResponse {

  public PjeClipTaskResponse(String output) {
    this(output, IConstants.DEFAULT_CHARSET);
  }
  
  public PjeClipTaskResponse(String output, Charset charset) {
    this(output, charset, true);
  }
  
  public PjeClipTaskResponse(String output, Charset charset, boolean success) {
    super(output, charset, success);
  }
}
