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

import static br.jus.cnj.pje.office.task.imp.PjeTaskReader.VIDEO_EXTRACT_AUDIO;

import java.io.IOException;
import java.util.Optional;

import com.github.taskresolver4j.ITask;
import com.github.utils4j.imp.Params;
import com.github.utils4j.imp.Strings;

import br.jus.cnj.pje.office.task.ITarefaVideoExtracaoAudio;


/*************************************************************************************
 * Leitor para extração de audios de VÍDEOS
/*************************************************************************************/

class TarefaVideoExtracaoAudioReader extends TarefaMediaReader<ITarefaVideoExtracaoAudio> {

  public static final TarefaVideoExtracaoAudioReader INSTANCE = new TarefaVideoExtracaoAudioReader();

  final static class TarefaVideoExtracaoAudio extends TarefaMedia implements ITarefaVideoExtracaoAudio {
    private String tipo;

    @Override
    public Optional<String> getTipo() {
      return Strings.optional(tipo);
    }
  }
  
  private TarefaVideoExtracaoAudioReader() {
    super(TarefaVideoExtracaoAudio.class);
  }

  @Override
  protected ITask<?> createTask(Params output, ITarefaVideoExtracaoAudio pojo) throws IOException {
    return new PjeAudioExtractorTask(output, pojo);
  }

  @Override
  protected String getTarefaId() {
    return VIDEO_EXTRACT_AUDIO.getId();
  }

  @Override
  protected Object getTarefa(Params param) {
    TarefaVideoExtracaoAudio tarefa= new TarefaVideoExtracaoAudio();
    tarefa.arquivos = param.getValue("arquivos");    
    tarefa.tipo = param.getValue("tipo");
    return tarefa;
  }
}
