package br.jus.cnj.pje.office.task;

import com.github.signer4j.imp.Params;
import com.github.signer4j.task.ITask;

import br.jus.cnj.pje.office.core.IPjeResponse;

public interface IPjeSignMode {
  ITask<IPjeResponse> getTask(Params output, ITarefaAssinador pojo);
}

