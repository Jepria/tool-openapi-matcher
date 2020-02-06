package org.jepria.tools.matcher;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import java.util.ArrayList;
import java.util.List;

// методы считаются одинаковыми, если у них одинаковое значение аннотации @Path и
// у них одинаковая аннотация типа http запроса (например @GET и @Path("/path") - есть у обоих)

public class JerseyMethodDeclaration {

  MethodDeclaration methodDeclaration;

  private String endpoint;
  private String type;

  private static ArrayList<String> TYPES;

  private JerseyMethodDeclaration(MethodDeclaration methodDeclaration, String endpoint, String type) {
    this.methodDeclaration = methodDeclaration;
    this.endpoint          = endpoint;
    this.type              = type;
  }

  static {
    TYPES = new ArrayList<>();
    TYPES.add("GET");
    TYPES.add("POST");
    TYPES.add("PUT");
    TYPES.add("DELETE");
  }

  static List<JerseyMethodDeclaration> findJerseyMethods(List<MethodDeclaration> methods) {
    List<JerseyMethodDeclaration> result = new ArrayList<>();

    for (MethodDeclaration method : methods) {
      String type     = null;
      String endpoint = null;
      for (AnnotationExpr annotationExpr : method.getAnnotations()) {
        if (TYPES.contains(annotationExpr.getNameAsString())) {
          type = annotationExpr.getNameAsString();
        } else if ("Path".equals(annotationExpr.getNameAsString())) {
          endpoint = annotationExpr.toString();
        }
      }
      if (null != type) {
        result.add(new JerseyMethodDeclaration(method, endpoint, type));
      }
    }

    return result;
  }

  /**
   * Jersey methods are equals if they have same @Path(endpoint) and same httpMethod(type). P.S. @Path("") and null are equals.
   */
  boolean isSameMethod(JerseyMethodDeclaration method) {
    boolean result = false;
    if (type.equals(method.type)) {
      if ((null == endpoint || endpoint.equals("@Path(\"\")"))
          && ((null == method.endpoint) || (method.endpoint.equals("@Path(\"\")")))
      ) {
        result = true;
      } else if (null != endpoint) {
        result = endpoint.equals(method.endpoint);
      }
    }
    return result;
  }

}
