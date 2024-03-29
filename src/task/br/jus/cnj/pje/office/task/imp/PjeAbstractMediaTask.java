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

import static com.github.utils4j.IConstants.UTF_8;
import static com.github.utils4j.imp.Throwables.tryCall;
import static java.net.URLDecoder.decode;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import com.github.taskresolver4j.exception.TaskException;
import com.github.utils4j.imp.Params;

import br.jus.cnj.pje.office.task.ITarefaMedia;

abstract class PjeAbstractMediaTask<T extends ITarefaMedia> extends PjeAbstractTask<T> {
  
  protected List<String> arquivos;
  
  protected PjeAbstractMediaTask(Params request, T pojo) {
    super(request, pojo, true);
  }
  
  @Override
  protected final void validateTaskParams() throws TaskException, InterruptedException {
    List<String> files = getPojoParams().getArquivos();
    if (files.isEmpty()) {
      files = Stream.of(selectFilesFromDialogs("Selecione os arquivos")).map(File::getAbsolutePath).collect(toList());
    }
    this.arquivos = files.stream().map(s -> tryCall(() -> decode(s, UTF_8.name()), s)).collect(toList());
    doValidateTaskParams();
  }

  protected void doValidateTaskParams() throws TaskException, InterruptedException {}
}
