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


package br.jus.cnj.pje.office.core;

import com.github.utils4j.IConstants;

/**
 * Alterações de versões:
 * 1 - Enumeração: br.jus.cnj.pje.office.core.Version
 * 2 - Arquivo: packages/windows/infra/inno-setup.iss
 * 3 - Arquivo: packages/mac/setup/Contents/Info.plist
 * 4 - Arquivo: packages/mac/infra/build.sh
 * 
 * TODO AUTOMATIZAR A INFORMAÇÃO DE VERSÓES EM TODOS OS ARQUIVOS E POM.XML
 * */
public enum Version {
  _2_1_0("2.1.0"); //remember: last version must be first enum position
  
  private String version;

  private Version(String version) {
    this.version = version;
  }
  
  private static final Version[] VALUES = Version.values();
  
  public static Version current() {
    return VALUES[0];
  }
  
  public static byte[] jsonBytes() {
    return current().toJson().getBytes(IConstants.DEFAULT_CHARSET);
  }
  
  public String toJson() {
    return "{ \"versao\": \"" + this + "\", \"success\": true }";
  }
  
  @Override
  public String toString() {
    return version;
  }
}
