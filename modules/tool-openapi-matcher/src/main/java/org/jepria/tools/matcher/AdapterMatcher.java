package org.jepria.tools.matcher;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.jepria.tools.codegen.openapi.ProjectGenerator;
import org.jepria.tools.matcher.diffTypes.MethodDiff;

public class AdapterMatcher {

  public void matchAdapter2Spec(String adapterLocation, String specLocation, OutputStream outStream) throws IOException {

    //Generate java file from spec!

    OpenAPI openAPI = null;
    openAPI = new OpenAPIV3Parser().read(specLocation);

    ProjectGenerator projectGenerator = new ProjectGenerator();

    List<String> adapters = projectGenerator.createAdapters(openAPI);

    //Match 2 java files

    String adapterBase = new String(Files.readAllBytes(Paths.get(adapterLocation)));

    for (String adapter : adapters) {
      List<MethodDiff> diffList = matchAdapters(adapterBase, adapter, outStream);

      MatchingResultParser parser = new MatchingResultParser(outStream);
      parser.parse(diffList);
    }
  }

  private List<MethodDiff> matchAdapters(String adapterOld, String adapterNew, OutputStream outStream) throws IOException {
    YamlJavaMatcher matcher = new YamlJavaMatcher();
    return matcher.match(adapterOld, adapterNew);
  }

}
