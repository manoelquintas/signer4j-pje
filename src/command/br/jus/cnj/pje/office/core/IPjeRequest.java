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

import java.util.Optional;

public interface IPjeRequest {
  String PJE_REQUEST_INSTANCE = IPjeRequest.class.getSimpleName(); 
  
  String PJE_REQUEST_IS_POST =  PJE_REQUEST_INSTANCE + ".isPost";

  String PJE_REQUEST_PARAMETER_NAME = "r";

  String PJE_REQUEST_PARAMETER_CACHE = "u";
  
  String getId(); 
  
  Optional<String> getParameterR();
  
  Optional<String> getParameterU();
  
  Optional<String> getUserAgent();

  Optional<String> getOrigin();
  
  boolean isPost();
  
  boolean isInternal();
}
