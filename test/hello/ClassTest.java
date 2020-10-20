package hello;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;

import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Stephan Krusche (krusche@in.tum.de)
 * @version 3.0 (25.09.2019)
 * <br><br>
 * This test evaluates the hierarchy of the class, i.e. if the class is abstract or an interface or an enum and also if the class extends another superclass and if
 * it implements the interfaces and annotations, based on its definition in the structure oracle (test.json).
 */
@RunWith(Parameterized.class)
public class ClassTest extends StructuralTest {

    public ClassTest(String expectedClassName, String expectedPackageName, JSONObject expectedClassJSON) {
        super(expectedClassName, expectedPackageName, expectedClassJSON);
    }

    /**
     * This method collects the classes in the structure oracle file for which at least one class property is specified.
     * These classes are packed into a list, which represents the test data.
     * @return A list of arrays containing each class' name, package and the respective JSON object defined in the structure oracle.
     */
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> findClasses() throws IOException {
        List<Object[]> testData = new ArrayList<Object[]>();

        if (structureOracleJSON == null) {
            fail("The ClassTest test can only run if the structural oracle (test.json) is present. If you do not provide it, delete ClassTest.java!");
        }

        for (int i = 0; i < structureOracleJSON.length(); i++) {
            JSONObject expectedClassJSON = structureOracleJSON.getJSONObject(i);
            JSONObject expectedClassPropertiesJSON = expectedClassJSON.getJSONObject(JSON_PROPERTY_CLASS);

            // Only test the classes that have additional properties (except name and package) defined in the structure oracle.
            if (expectedClassPropertiesJSON.has(JSON_PROPERTY_NAME) && expectedClassPropertiesJSON.has(JSON_PROPERTY_PACKAGE) && hasAdditionalProperties(expectedClassPropertiesJSON)) {
                String expectedClassName = expectedClassPropertiesJSON.getString(JSON_PROPERTY_NAME);
                String expectedPackageName = expectedClassPropertiesJSON.getString(JSON_PROPERTY_PACKAGE);
                testData.add(new Object[] { expectedClassName, expectedPackageName, expectedClassJSON });
            }
        }
        if (testData.size() == 0) {
            fail("No tests for classes available in the structural oracle (test.json). Either provide attributes information or delete ClassTest.java!");
        }
        return testData;
    }

    private static boolean hasAdditionalProperties(JSONObject jsonObject) {
        List<String> keys = new ArrayList<String>(jsonObject.keySet());
        keys.remove(JSON_PROPERTY_NAME);
        keys.remove(JSON_PROPERTY_PACKAGE);
        return keys.size() > 0;
    }

    /**
     * This test loops over the list of the test data generated by the method findClasses(), checks if each class is found
     * at all in the assignment and then proceeds to check its properties.
     */
    @Test(timeout = 1000)
    public void testClass() {
        Class<?> observedClass = findClassForTestType("class");

        JSONObject expectedClassPropertiesJSON = expectedClassJSON.getJSONObject(JSON_PROPERTY_CLASS);

        if (expectedClassPropertiesJSON.has("isAbstract") && !Modifier.isAbstract(observedClass.getModifiers())) {
            fail("The class '" + expectedClassName + "' is not abstract as it is expected.");
        }

        if (expectedClassPropertiesJSON.has("isEnum") && !observedClass.isEnum()) {
            fail("The type '" + expectedClassName + "' is not an enum as it is expected.");
        }

        if (expectedClassPropertiesJSON.has("isInterface") && !Modifier.isInterface(observedClass.getModifiers())) {
            fail("The type '" + expectedClassName + "' is not an interface as it is expected.");
        }

        if(expectedClassPropertiesJSON.has("isEnum") && !observedClass.isEnum()) {
            fail("The type '" + expectedClassName + "' is not an enum as it is expected.");
        }

        if(expectedClassPropertiesJSON.has(JSON_PROPERTY_SUPERCLASS)) {
            // Filter out the enums, since there is a separate test for them
            if(!expectedClassPropertiesJSON.getString(JSON_PROPERTY_SUPERCLASS).equals("Enum")) {
                String expectedSuperClassName = expectedClassPropertiesJSON.getString(JSON_PROPERTY_SUPERCLASS);
                String actualSuperClassName = observedClass.getSuperclass().getSimpleName();

                String failMessage = "The class '" + expectedClassName + "' is not a subclass of the class '"
                    + expectedSuperClassName + "' as expected. Implement the class inheritance properly.";
                if (!expectedSuperClassName.equals(actualSuperClassName)) {
                    fail(failMessage);
                }
            }
        }

        if(expectedClassPropertiesJSON.has(JSON_PROPERTY_INTERFACES)) {
            JSONArray expectedInterfaces = expectedClassPropertiesJSON.getJSONArray(JSON_PROPERTY_INTERFACES);
            Class<?>[] observedInterfaces = observedClass.getInterfaces();

            for (int i = 0; i < expectedInterfaces.length(); i++) {
                String expectedInterface = expectedInterfaces.getString(i);
                boolean implementsInterface = false;

                for (Class<?> observedInterface : observedInterfaces) {
                    //TODO: this does not work with the current implementation of the test oracle generator (which does not print the simple but the full qualified name including the package)
                    if(expectedInterface.equals(observedInterface.getSimpleName())) {
                        implementsInterface = true;
                        break;
                    }
                }

                if (!implementsInterface) {
                    fail("The class '" + expectedClassName + "' does not implement the interface '" + expectedInterface + "' as expected."
                        + " Implement the interface and its methods.");
                }
            }
        }

        if(expectedClassPropertiesJSON.has(JSON_PROPERTY_ANNOTATIONS)) {
            JSONArray expectedAnnotations = expectedClassPropertiesJSON.getJSONArray(JSON_PROPERTY_ANNOTATIONS);
            Annotation[] observedAnnotations = observedClass.getAnnotations();

            boolean annotationsAreRight = checkAnnotations(observedAnnotations, expectedAnnotations);
            if (!annotationsAreRight) {
                fail("The annotation(s) of the class '" + expectedClassName + "' are not implemented as expected.");
            }
        }
    }
}
