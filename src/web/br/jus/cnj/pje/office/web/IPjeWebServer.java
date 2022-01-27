package br.jus.cnj.pje.office.web;

import java.io.IOException;

import io.reactivex.Observable;

public interface IPjeWebServer {
  
  int HTTP_PORT = 8800;
  
  int HTTPS_PORT = 8801; 

  String BASE_END_POINT = "/pjeOffice/";
  
  String SHUTDOWN_ENDPOINT = BASE_END_POINT + "shutdown/";  
  
  enum LifeCycle {
    STARTUP,
    SHUTDOWN,
    KILL
  }
  
  void setAllowLocalRequest(boolean allow);
  
  void start() throws IOException;
  
  void stop(boolean force) throws IOException;
  
  String getTaskEndpoint();
  
  boolean isStarted();
  
  Observable<LifeCycle> lifeCycle();
}
