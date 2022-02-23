package br.jus.cnj.pje.office.core.pdf;

import java.util.function.Function;

import io.reactivex.Observable;

public interface IPdfHandler extends Function<IInputDescriptor, Observable<IPdfStatus>> {

  void reset();
}
