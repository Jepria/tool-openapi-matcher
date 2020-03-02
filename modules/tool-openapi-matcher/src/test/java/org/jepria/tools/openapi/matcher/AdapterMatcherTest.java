package org.jepria.tools.openapi.matcher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

public class AdapterMatcherTest {

  @Test
  public void matchAdapter2SpecTest() throws IOException {

    String adapterLocation = new File(getClass().getClassLoader().getResource("feature/FeatureJaxrsAdapter.java").getPath()).getCanonicalPath();
    String specLocation    = new File(getClass().getClassLoader().getResource("feature/swagger.json").getPath()).getCanonicalPath();

    OutputStream outStream = System.out;

    AdapterMatcher matcher = new AdapterMatcher();

    matcher.matchAdapter2Spec(adapterLocation, specLocation, outStream);
  }
}