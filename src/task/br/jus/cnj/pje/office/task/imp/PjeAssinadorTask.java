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

import java.io.IOException;
import java.util.Optional;

import com.github.progress4j.IProgress;
import com.github.progress4j.IStage;
import com.github.signer4j.IByteProcessor;
import com.github.signer4j.imp.exception.InterruptedSigner4JRuntimeException;
import com.github.signer4j.imp.exception.Signer4JException;
import com.github.taskresolver4j.ITaskResponse;
import com.github.taskresolver4j.exception.TaskException;
import com.github.utils4j.imp.Params;
import com.github.utils4j.imp.TemporaryException;

import br.jus.cnj.pje.office.core.IPjeResponse;
import br.jus.cnj.pje.office.core.imp.PjeTaskResponse;
import br.jus.cnj.pje.office.core.imp.PjeTaskResponses;
import br.jus.cnj.pje.office.core.imp.UnsupportedCosignException;
import br.jus.cnj.pje.office.signer4j.IPjeToken;
import br.jus.cnj.pje.office.task.IArquivoAssinado;
import br.jus.cnj.pje.office.task.IAssinaturaPadrao;
import br.jus.cnj.pje.office.task.IPjeSignMode;
import br.jus.cnj.pje.office.task.ITarefaAssinador;

abstract class PjeAssinadorTask extends PjeAbstractTask<ITarefaAssinador> {

  private static final int MAX_SEQUENTIAL_FAILURE_COUNT = 5;
  
  private static enum Stage implements IStage {
    
    SELECTING_FILES("Seleção de arquivos"),
    
    PROCESSING_FILES("Processamento de arquivos");
    
    private final String message;

    Stage(String message) {
      this.message = message;
    }

    @Override
    public String toString() {
      return message;
    }
  }
  
  protected IPjeSignMode modo;

  protected IAssinaturaPadrao padraoAssinatura;
  
  protected IArquivoAssinado first;
  
  public PjeAssinadorTask(Params request, ITarefaAssinador pojo, boolean internalTask) {
    super(request, pojo, internalTask);
  }
  
  @Override
  protected void validateTaskParams() throws TaskException {
    ITarefaAssinador params = getPojoParams();
    this.modo = PjeTaskChecker.checkIfPresent(params.getModo(), "modo");
    this.padraoAssinatura = PjeTaskChecker.checkIfPresent(params.getPadraoAssinatura(), "padraoAssinatura").checkIfDependentParamsIsPresent(params);
  }
  
  protected final Optional<IArquivoAssinado> getFirst() {
    return Optional.ofNullable(first);
  }
  
  @Override
  protected ITaskResponse<IPjeResponse> doGet() throws TaskException, InterruptedException {
    final ITarefaAssinador params = getPojoParams();
    
    final IProgress progress = getProgress();

    int success = 0, size = -1;
    
    progress.begin(Stage.SELECTING_FILES);
    
    final IArquivoAssinado[] files = selectFiles();
    size = files.length;
    this.first = files[0];
    progress.step("Selecionados '%s' arquivo(s)", size);
    progress.end();
    
    final PjeTaskResponses responses = new PjeTaskResponses();
    
    IPjeToken token = loginToken();
    try {
      int index = 0;
    
      IByteProcessor processor = padraoAssinatura.getByteProcessor(token, params);
      
      progress.begin(Stage.PROCESSING_FILES, 2 * size); //dois passos para cada arquivo (assinar e enviar)

      int failsCount = 0;
      for(final IArquivoAssinado file: files) {
        try {
          final String fileName = file.getNome().orElse(Integer.toString(++index));

          progress.step("Assinando arquivo '%s'", fileName);
          
          try {
            file.sign(processor);
            failsCount = 0;
          } catch (IOException e) {
            String message = "Arquivo ignorado. Não foi possível ler os bytes do arquivo temporário: ";
            LOGGER.warn(message + file.toString());
            progress.info(message + e.getMessage());
            throw new TemporaryException(e);
          } catch (UnsupportedCosignException e) {
            String message = "Arquivo ignorado. Co-assinatura não é suportada: ";
            LOGGER.warn(message + file.toString());
            progress.info(message + e.getMessage());
            throw new TemporaryException(e);
          } catch (Signer4JException e) {
            String message = "Arquivo ignorado:  " + file.toString();
            LOGGER.warn(message, e);
            progress.info(message + " -> " +  e.getMessage());
            throw new TemporaryException(e);
          }
          try {
            progress.step("Enviando arquivo '%s'", fileName);
            responses.add(send(file));
            success++;
          }catch(TaskException e) {
            String message = "Arquivo ignorado:  " + file.toString();
            LOGGER.warn(message, e);
            progress.info(message + " -> " + e.getMessage());
            throw new TemporaryException(e);
          }
        }catch(TemporaryException e) {
          if (++failsCount == MAX_SEQUENTIAL_FAILURE_COUNT) {
            throw showFail("O processo de assinatura foi interrompido!", 
              "Foi alcançado o número máximo de falhas de assinatura consecutivas (" + failsCount + "). " +
              "O token/certificado não está conectado/disponível, ou os arquivos são muito "+ 
              "grandes ou já se encontram assinados.", e);
          }            
          
          progress.abort(e);          
          int remainder = size - index - 1;
          if (remainder >= 0) {
            if (!token.isAuthenticated()) {
              try {
                token = loginToken();
              }catch(InterruptedSigner4JRuntimeException ex) {
                progress.abort(e);
                ex.addSuppressed(e);
                throw showFail("Não foi possível recuperar autenticação do token.", ex);
              }
              processor = padraoAssinatura.getByteProcessor(token, params);
            }
            progress.begin(Stage.PROCESSING_FILES, 2 * remainder); //dois passos para cada arquivo (assinar e enviar)
          }
        }finally {
          file.dispose();
        }
      }
      progress.end();
    } finally {
      token.logout();
    }
    
    if (success != size) {
      throw showFail("Alguns arquivos NÃO puderam ser assinados.", progress.getAbortCause());
    }
    
    if (isInternal()) {
      showInfo("Arquivos assinados com sucesso!", "Ótimo!");
    }
    
    return responses;
  }
  
  protected abstract IArquivoAssinado[] selectFiles() throws TaskException, InterruptedException;

  protected abstract PjeTaskResponse send(IArquivoAssinado arquivo) throws TaskException, InterruptedException;
}
