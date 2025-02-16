/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputPath;
import org.sonarsource.analyzer.commons.ProgressReport;

/** Some utilities */
public final class DelphiUtils {
  private DelphiUtils() {}

  /**
   * Normalizes file name, changes all '\' into '/'
   *
   * @param fileName file name to normalize
   * @return normalized file name
   */
  public static String normalizeFileName(String fileName) {
    return fileName.replace("\\", "/");
  }

  /**
   * Gets the resource from project workspace
   *
   * @param fileName Resource file name
   * @return Resource file
   */
  public static File getResource(String fileName) {
    URL url = DelphiUtils.class.getResource(fileName);
    File file = FileUtils.toFile(url);
    return Objects.requireNonNull(file, "Resource not found: " + fileName);
  }

  /**
   * Adds root directory to path if path is relative, or returns path if absolute
   *
   * @param root Root directory
   * @param path Pathname to resolve
   * @return Resolved file
   */
  public static File resolveAbsolutePath(String root, String path) {
    File file = new File(path);

    if (!file.isAbsolute()) {
      if (!root.endsWith(File.separator)) {
        root = root.concat(File.separator);
      }
      file = new File(root + path);
    }

    return file;
  }

  public static String uriToAbsolutePath(URI uri) {
    String path = uri.getPath();
    if (":".equals(path.substring(2, 3))) {
      path = path.substring(1);
    }
    return path;
  }

  public static List<Path> inputFilesToPaths(Iterable<InputFile> inputFiles) {
    List<Path> result = new ArrayList<>();
    for (InputFile inputFile : inputFiles) {
      result.add(inputFileToPath(inputFile));
    }
    return result;
  }

  public static Path inputFileToPath(InputPath inputFile) {
    return Paths.get(inputFile.uri());
  }

  public static Path resolvePathFromBaseDir(Path baseDir, Path path) {
    if (!path.isAbsolute()) {
      path = Path.of(baseDir.toAbsolutePath().toString(), path.toString());
    }
    return path.toAbsolutePath().normalize();
  }

  public static void stopProgressReport(ProgressReport progressReport, boolean success) {
    if (success) {
      progressReport.stop();
    } else {
      progressReport.cancel();
    }
  }
}
