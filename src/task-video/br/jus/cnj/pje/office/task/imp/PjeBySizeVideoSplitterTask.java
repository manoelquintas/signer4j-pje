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

import static com.github.utils4j.gui.imp.SwingTools.invokeAndWait;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.progress4j.IQuietlyProgress;
import com.github.taskresolver4j.exception.TaskException;
import com.github.utils4j.gui.imp.Dialogs;
import com.github.utils4j.imp.Params;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.imp.BySizeVideoSplitter;
import com.github.videohandler4j.imp.VideoDescriptor;
import com.github.videohandler4j.imp.VideoTools;
import com.github.videohandler4j.imp.exception.VideoDurationNotFound;

import br.jus.cnj.pje.office.task.ITarefaVideoDivisaoTamanho;

class PjeBySizeVideoSplitterTask extends PjeMediaProcessingTask<ITarefaVideoDivisaoTamanho> {
  
  private long tamanho;
  
  protected PjeBySizeVideoSplitterTask(Params request, ITarefaVideoDivisaoTamanho pojo) {
    super(request, pojo);
  }

  @Override
  protected void doValidateTaskParams() throws TaskException, InterruptedException {
    ITarefaVideoDivisaoTamanho pojo = getPojoParams();
    this.tamanho = pojo.getTamanho();
    if (this.tamanho == 0) {
      Optional<Integer> total = invokeAndWait(() -> {
        return Dialogs.getInteger(
          "Tamanho máximo do arquivo (MB):", 
          90, 
          2, 
          Integer.MAX_VALUE - 1
        );
      });
      this.tamanho = total.orElseThrow(InterruptedException::new);
    }
  }

  @Override
  protected boolean process(Path file, IQuietlyProgress progress) {
    
    progress.begin(SplitterStage.SPLITTING_PATIENT);
    
    IVideoFile video;
    try {
      video = VideoTools.FFMPEG.call(file.toFile());
    } catch (VideoDurationNotFound e) {    
      LOGGER.error("Não foi possível encontrar duração do vídeo ", e);
      progress.abort(e);
      return false;
    }

    final Path output = file.getParent().resolve(video.getShortName() + "_(VÍDEOS DE ATÉ " + tamanho + " MB)");
    
    VideoDescriptor desc;
    try {
      desc = new VideoDescriptor.Builder(".mp4")
        .add(video)
        .output(output)
        .build();
    } catch (IOException e1) {
      LOGGER.error("Não foi possível criar pasta " + output.toString(), e1);
      progress.abort(e1);
      return false;
    }
    
    AtomicBoolean success = new AtomicBoolean(true);
    new BySizeVideoSplitter(video, tamanho * 1000 * 1000)
      .apply(desc)
      .subscribe(
        e -> progress.info(e.getMessage()),
        e -> {
          success.set(false);
          output.toFile().delete();
          progress.abort(e);
        }
      );

    progress.end();

    return success.get();
  }
}
