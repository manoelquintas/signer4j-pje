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

import static com.github.utils4j.gui.imp.Dialogs.getInteger;
import static com.github.utils4j.gui.imp.SwingTools.invokeAndWait;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.filehandler4j.IInputFile;
import com.github.filehandler4j.imp.FileWrapper;
import com.github.filehandler4j.imp.InputDescriptor;
import com.github.pdfhandler4j.imp.ByCountPdfSplitter;
import com.github.pdfhandler4j.imp.BySinglePagePdfSplitter;
import com.github.pdfhandler4j.imp.PdfInputDescriptor;
import com.github.pdfhandler4j.imp.event.PdfEndEvent;
import com.github.pdfhandler4j.imp.event.PdfOutputEvent;
import com.github.pdfhandler4j.imp.event.PdfPageEvent;
import com.github.pdfhandler4j.imp.event.PdfReadingEnd;
import com.github.pdfhandler4j.imp.event.PdfReadingStart;
import com.github.pdfhandler4j.imp.event.PdfStartEvent;
import com.github.progress4j.IQuietlyProgress;
import com.github.taskresolver4j.exception.TaskException;
import com.github.utils4j.imp.Params;

import br.jus.cnj.pje.office.task.ITarefaPdfDivisaoContagem;

class PjeByCountPdfSplitterTask extends PjeMediaProcessingTask<ITarefaPdfDivisaoContagem> {
  
  private long totalPaginas;
  
  protected PjeByCountPdfSplitterTask(Params request, ITarefaPdfDivisaoContagem pojo) {
    super(request, pojo);
  }

  @Override
  protected void doValidateTaskParams() throws TaskException, InterruptedException {
    this.totalPaginas = getPojoParams().getTotalPaginas();
    if (this.totalPaginas <= 0) {
      Optional<Integer> total = invokeAndWait(() -> {
        return getInteger(
          "Número máximo de páginas:", 
          10, 
          1, 
          Integer.MAX_VALUE - 1
        );
      });
      this.totalPaginas = total.orElseThrow(InterruptedException::new);
    }
  }
  
  @Override
  protected boolean process(Path file, IQuietlyProgress progress) {
    Path parentFolder = file.getParent();
    IInputFile input = new FileWrapper(file.toFile());
    InputDescriptor desc;
    Path outputFolder = parentFolder.resolve(input.getShortName() + 
        "_(VOLUMES DE " + this.totalPaginas + " PÁGINA" + (totalPaginas > 1 ? "S)" : ")"));
    try {
      desc = new PdfInputDescriptor.Builder()
        .add(input)
        .output(outputFolder)
        .build();
    } catch (IOException e1) {
      progress.abort(e1);
      LOGGER.error("Não foi possível criar pasta " + parentFolder.toString(), e1);
      return false;
    }

    ByCountPdfSplitter splitter = this.totalPaginas == 1 ? 
      new BySinglePagePdfSplitter() : 
      new ByCountPdfSplitter(totalPaginas);

    AtomicBoolean success = new AtomicBoolean(true);
    
    splitter.apply(desc).subscribe(
      e -> {
        if (e instanceof PdfReadingStart) {
          progress.begin(SplitterStage.READING);
        } else if (e instanceof PdfStartEvent) {
          progress.begin(SplitterStage.SPLITING, ((PdfStartEvent)e).getTotalPages());            
        } else if (e instanceof PdfReadingEnd || e instanceof PdfEndEvent) {
          progress.end();
        } else if (e instanceof PdfPageEvent) {
          progress.step(e.getMessage());
        } else if (e instanceof PdfOutputEvent && totalPaginas == 1){
          progress.step(e.getMessage());
        } else {
          progress.info(e.getMessage());  
        }
      },
      e -> {
        success.set(false);
        progress.abort(e);
        outputFolder.toFile().delete();
      }
    ); 
    return success.get();
  }
}
