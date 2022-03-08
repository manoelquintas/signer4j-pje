package br.jus.cnj.pje.office.task.imp;

import static br.jus.cnj.pje.office.task.imp.TarefaAssinadorReader.AssinadorArquivo.newInstance;
import static com.github.signer4j.gui.alert.MessageAlert.display;
import static com.github.utils4j.gui.imp.SwingTools.invokeAndWait;
import static com.github.utils4j.imp.Dates.stringNow;
import static com.github.utils4j.imp.Throwables.tryRun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import javax.swing.JFileChooser;

import com.github.progress4j.IProgressView;
import com.github.signer4j.ISignedData;
import com.github.taskresolver4j.ITaskResponse;
import com.github.taskresolver4j.exception.TaskException;
import com.github.utils4j.gui.imp.DefaultFileChooser;
import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.Params;

import br.jus.cnj.pje.office.core.IPjeResponse;
import br.jus.cnj.pje.office.core.imp.PjeTaskResponse;
import br.jus.cnj.pje.office.task.IArquivoAssinado;
import br.jus.cnj.pje.office.task.ITarefaAssinador;

class PjeAssinadorLocalTask extends PjeAssinadorTask {
  
  private static final String PJE_DESTINATION_PARAM = "PjeAssinadorLocalTask.destinationDir";

  PjeAssinadorLocalTask(Params request, ITarefaAssinador pojo) {
    super(request, pojo, true);
  }

  @Override
  protected void validateParams() throws TaskException {
    super.validateParams();
    //insert here new validations
  }

  @Override
  protected final ITaskResponse<IPjeResponse> doGet() throws TaskException {
    runAsync(() -> {
      IProgressView progress = newProgress();
      progress.display();
      tryRun(super::doGet);
      progress.undisplay();
      progress.stackTracer(s -> LOGGER.info(s.toString()));
      progress.dispose();
    });
    return success();
  }

  @Override
  protected IArquivoAssinado[] selectFiles() throws TaskException, InterruptedException {
    DefaultFileChooser chooser = new DefaultFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.setDialogTitle("Selecione o(s) arquivo(s) a ser(em) assinado(s)");
    if (JFileChooser.CANCEL_OPTION == chooser.showOpenDialog(null)) {
      throwCancel();
    }
    return collectFiles(chooser.getSelectedFiles());
  }

  protected IArquivoAssinado[] collectFiles(File[] files) throws TaskException {
    int size;
    if (files == null || (size = files.length) == 0) {
      throw new TaskException("Nenhum arquivo selecionado");
    }
    IArquivoAssinado[] filesToSign = new IArquivoAssinado[size];
    int i = 0;
    for(File file: files) {
      filesToSign[i++]= new ArquivoAssinado(newInstance(file, "ASSINADO_EM_" + stringNow()), file);
    }
    return filesToSign;
  }
  
  protected File chooseDestination() throws InterruptedException {
    final JFileChooser chooser = new DefaultFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle("Selecione onde será(ão) gravado(s) o(s) arquivo(s) assinado(s)");
    switch(chooser.showOpenDialog(null)) {
      case JFileChooser.APPROVE_OPTION:
        return chooser.getSelectedFile(); 
      default:
        throwCancel();
        return null;
    }
  }

  @Override
  protected PjeTaskResponse send(IArquivoAssinado arquivo) throws TaskException, InterruptedException {
    Args.requireNonNull(arquivo, "arquivo is null");
    
    Optional<ISignedData> signature = arquivo.getSignedData();
    if (!signature.isPresent()) {
      throw new TaskException("Arquivo não foi assinado!");
    }
    
    final ISignedData signedData = signature.get();
    
    File destination;
    do {
      destination = params.isPresent(PJE_DESTINATION_PARAM) ?  
        params.getValue(PJE_DESTINATION_PARAM) : 
        chooseDestination();
      if (destination.canWrite()) 
        break;
      showCanNotWriteMessage(destination);
      params.of(PJE_DESTINATION_PARAM, Optional.empty());
    }while(true);
      
    params.of(PJE_DESTINATION_PARAM, destination);
    
    final String fileName = arquivo.getNome().get();

    final File saved = new File(destination, fileName);
    try(OutputStream output = new FileOutputStream(saved)){ 
      signedData.writeTo(output);
    } catch (IOException e) {
      saved.delete();
      throw new TaskException("Não foi possível salvar o arquivo assinado.", e);
    }
    return success(arquivo.getUrl().get());
  }

  protected void showCanNotWriteMessage(File destination) {
    invokeAndWait(() -> display("Não há permissão de escrita na pasta:\n" + destination.getCanonicalPath() + "\nEscolha uma nova!"));
  }
}
