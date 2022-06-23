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

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.progress4j.IQuietlyProgress;
import com.github.utils4j.imp.Params;
import com.github.videohandler4j.imp.VideoDescriptor;
import com.github.videohandler4j.imp.WebmConverter;

import br.jus.cnj.pje.office.task.ITarefaMedia;

class PjeWebmConverterTask extends PjeBasicConverterTask<ITarefaMedia> {
  
  protected PjeWebmConverterTask(Params request, ITarefaMedia pojo) {
    super(request, pojo, "WEBM-");
  }
  
  @Override
  protected String getExtension() {
    return ".webm";
  }

  @Override
  protected void execute(IQuietlyProgress progress, VideoDescriptor desc, AtomicBoolean success) {
    new WebmConverter()
    .apply(desc)
    .subscribe(
      e -> progress.info(e.getMessage()),
      e -> {
        success.set(false);
        progress.abort(e);
      }
    );    
  }
}
