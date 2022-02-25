package br.jus.cnj.pje.office.core;

import com.github.utils4j.IConstants;

public enum Version {
  _2_0_0("2.0.0"); //remember: last version must be first enum position
  
  private String version;

  private Version(String version) {
    this.version = version;
  }
  
  private static final Version[] VALUES = Version.values();
  
  public static Version current() {
    return VALUES[0];
  }
  
  public static byte[] jsonBytes() {
    return current().toJson().getBytes(IConstants.DEFAULT_CHARSET);
  }
  
  public String toJson() {
    return "{ \"versao\": \"" + this + "\" }";
  }
  
  @Override
  public String toString() {
    return version;
  }
}
