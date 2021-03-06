package org.jepria.tools.openapi.matcher;

import static org.jepria.tools.openapi.matcher.MatcherCli.JAVA_PATH_OPT;
import static org.jepria.tools.openapi.matcher.MatcherCli.SPEC_OPT;
import static org.jepria.tools.openapi.matcher.MatcherCli.match;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

public class MatcherCliTest {

  @Test
  public void matchTest() throws IOException, ParseException {

    String adapterLocation = new File(getClass().getClassLoader().getResource("FeatureJaxrsAdapter.java").getPath()).getCanonicalPath();
    String specLocation    = new File(getClass().getClassLoader().getResource("swagger.json").getPath()).getCanonicalPath();

    String[] argv = {"-" + SPEC_OPT, specLocation, "-" + JAVA_PATH_OPT, adapterLocation};
    match(argv);
  }
}