package org.sonar.plugins.communitydelphi.api.check;

import au.com.integradev.delphi.check.MetadataResourcePathImpl;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ServerSide
@ScannerSide
@SonarLintSide
public interface MetadataResourcePath {
  String forRepository(String repositoryKey);

  static String repository(String repositoryKey) {
    return new MetadataResourcePathImpl().forRepository(repositoryKey);
  }
}