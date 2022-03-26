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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.Charset;

import com.github.utils4j.IConstants;
import com.github.utils4j.imp.Args;

import br.jus.cnj.pje.office.core.IPjeResponse;

class PjeClipResponse implements IPjeResponse {
  
  private final Charset charset;

  public PjeClipResponse() {
    this(IConstants.DEFAULT_CHARSET);
  }
  
  public PjeClipResponse(Charset charset) {
    this.charset = Args.requireNonNull(charset, "charset is null");
  }

  @Override
  public void flush() throws IOException {
    ; //nothing to do (empty statement)
  }
  
  @Override
  public void write(byte[] data) throws IOException {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(new String(data, charset)), null);
  }
}
