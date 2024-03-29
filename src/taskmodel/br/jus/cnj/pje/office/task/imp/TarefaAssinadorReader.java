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

import static br.jus.cnj.pje.office.task.imp.AssinaturaPadrao.NOT_ENVELOPED;
import static br.jus.cnj.pje.office.task.imp.MainRequestReader.MAIN;
import static br.jus.cnj.pje.office.task.imp.PjeSignMode.DEFINIDO;
import static br.jus.cnj.pje.office.task.imp.PjeTaskReader.CNJ_ASSINADOR;
import static com.github.signer4j.imp.SignatureAlgorithm.SHA1withRSA;
import static com.github.signer4j.imp.SignatureType.ATTACHED;
import static com.github.utils4j.imp.Strings.optional;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.signer4j.ISignatureAlgorithm;
import com.github.signer4j.ISignatureType;
import com.github.signer4j.imp.SignatureAlgorithm;
import com.github.signer4j.imp.SignatureType;
import com.github.taskresolver4j.ITask;
import com.github.taskresolver4j.imp.AbstractRequestReader;
import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.Params;
import com.github.utils4j.imp.Strings;

import br.jus.cnj.pje.office.task.IArquivo;
import br.jus.cnj.pje.office.task.IAssinaturaPadrao;
import br.jus.cnj.pje.office.task.IJsonTranslator;
import br.jus.cnj.pje.office.task.IPjeSignMode;
import br.jus.cnj.pje.office.task.ITarefaAssinador;

class TarefaAssinadorReader extends AbstractRequestReader<Params, ITarefaAssinador> implements IJsonTranslator {

  public static final TarefaAssinadorReader INSTANCE = new TarefaAssinadorReader();

  static final class TarefaAssinador implements ITarefaAssinador {

    private PjeSignMode modo;
    
    private String enviarPara;

    private boolean deslogarKeyStore = true;

    private AssinaturaPadrao padraoAssinatura = AssinaturaPadrao.NOT_ENVELOPED;
    
    private SignatureType tipoAssinatura = SignatureType.ATTACHED;
    
    private SignatureAlgorithm algoritmoHash = SignatureAlgorithm.SHA1withRSA;//HashAlgorithm.DIGEST_SHA1; TODO revisar essa inicialização
    
    private List<AssinadorArquivo> arquivos = new ArrayList<>();
   
    @Override
    public final boolean isDeslogarKeyStore() {
      return this.deslogarKeyStore;
    }
    
    @Override
    public final Optional<IPjeSignMode> getModo() {
      return ofNullable(this.modo);
    }
    
    @Override
    public final Optional<String> getEnviarPara() {
      return optional(this.enviarPara);
    }

    @Override
    public final Optional<ISignatureAlgorithm> getAlgoritmoHash() {
      return ofNullable(this.algoritmoHash);
    }

    @Override
    public final Optional<IAssinaturaPadrao> getPadraoAssinatura() {
      return ofNullable(this.padraoAssinatura);
    }

    @Override
    public final Optional<ISignatureType> getTipoAssinatura() {
      return ofNullable(this.tipoAssinatura);
    }
    
    @Override
    public final List<IArquivo> getArquivos() {
      return this.arquivos == null ? emptyList() : unmodifiableList(this.arquivos);
    }
  }

  final static class AssinadorArquivo implements IArquivo {

    public static IArquivo newInstance(File file, String prefix) {
      return new AssinadorArquivo(file, () -> file.getName() + Strings.trim(prefix) + ".p7s");
    }
    
    private String nome;
    private String url;
    private boolean terAtributosAssinados = true;
    private List<String> paramsEnvio = new ArrayList<>();

    private AssinadorArquivo(){
    }
    
    private AssinadorArquivo(File file) { 
      this(file, file::getName);
    }
    
    private AssinadorArquivo(File file, Supplier<String> nome) {
      Args.requireNonNull(file, "file is null");
      Args.requireNonNull(nome, "nome is null");
      this.nome = nome.get();
      this.url = file.getAbsolutePath();
    }
    
    @Override
    public final Optional<String> getUrl() {
      return optional(this.url);
    }
    
    @Override
    public final Optional<String> getNome() {
      return optional(this.nome);
    }

    @Override
    public final boolean isTerAtributosAssinados() {
      return this.terAtributosAssinados;
    }

    @Override
    public final List<String> getParamsEnvio() {
      return this.paramsEnvio == null ? emptyList() : unmodifiableList(this.paramsEnvio);
    }
  }
  
  private TarefaAssinadorReader() {
    super(TarefaAssinador.class);
  }

  @Override
  protected ITask<?> createTask(Params output, ITarefaAssinador pojo) throws IOException {
    Optional<IPjeSignMode> mode = pojo.getModo();
    if (!mode.isPresent()) {
      throw new IOException("Parameter 'modoAssinatura' (local/remoto) not found!");
    }
    return mode.get().getTask(output, pojo);
  }

  @Override
  public String toJson(Params input) throws Exception {
    TarefaAssinador ta = new TarefaAssinador();
    ta.modo = PjeSignMode.fromString(input.orElse("modo", DEFINIDO.getKey()));
    ta.padraoAssinatura = AssinaturaPadrao.fromString(input.orElse("padraoAssinatura", NOT_ENVELOPED.getKey()));
    ta.tipoAssinatura = SignatureType.fromString(input.orElse("tipoAssinatura", ATTACHED.getKey()));
    ta.algoritmoHash = SignatureAlgorithm.fromString(input.orElse("algoritmoHash", SHA1withRSA.getKey()));
    ta.enviarPara = input.getValue("enviarPara");
    ta.arquivos = input.orElse("arquivos", Collections.<String>emptyList()).stream()
    .map(a -> new File(a))
    .map(f -> new AssinadorArquivo(f))
    .collect(toList());
    input.of("tarefaId", CNJ_ASSINADOR.getId())
         .of("tarefa", ta);
    return MAIN.toJson(input);
  }
}
