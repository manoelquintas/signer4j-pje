package br.jus.cnj.pje.office.task;

import java.util.List;
import java.util.Optional;

public interface IArquivo {

  Optional<String> getUrl();

  Optional<String> getNome();

  boolean isTerAtributosAssinados();

  List<String> getParamsEnvio();
  
  default void dispose() {}
}
