import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public class EnvironmentVariablesSetter {

    /**
     * Sets all the environment variables.
     *
     * @param environmentVariablePairs the new environment variable pairs to be set
     * @return <code>true</code> if the changes were successful
     */
    public static boolean setEnv(Map<String, String> environmentVariablePairs) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(environmentVariablePairs);
            Field caseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            caseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> caseInsensitiveEnvironment = (Map<String, String>) caseInsensitiveEnvironmentField.get(null);
            caseInsensitiveEnvironment.putAll(environmentVariablePairs);
        } catch (NoSuchFieldException nsfe) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class c : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(c.getName())) {
                        Field field = c.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(environmentVariablePairs);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}