import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the setting of environment variables for the duration of the JVM's instance.
 *
 * @author Zach Wilson
 */
public class EnvironmentVariablesSetter {

    /**
     * SLF4J logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentVariablesSetter.class);

    /**
     * Sets a field's access privilege to <tt>true</tt>.
     *
     * @param f the field who's access to be set to <tt>true</tt>
     */
    private static void allowAccessTo(final Field f) {
        AccessController.doPrivileged((PrivilegedAction<?>) () -> {
            f.setAccessible(true);
            return null;
        });
    }

    /**
     * Sets all the environment variables.
     *
     * @param newEnvironmentVariables the new environment variable pairs to be set. The key
     *        corresponds to the variable and the value corresponds to the file system path.
     * @return <tt>true</tt> if the changes were successful.
     */
    public static boolean setEnv(final Map<String, String> newEnvironmentVariables) {
        try {
            final Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            final Field environmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            allowAccessTo(environmentField);
            final Map<String, String> env = (Map<String, String>) environmentField.get(null);
            env.putAll(newEnvironmentVariables);
            final Field caseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            allowAccessTo(caseInsensitiveEnvironmentField);
            final Map<String, String> caseInsensitiveEnvironment = (Map<String, String>) caseInsensitiveEnvironmentField.get(null);
            caseInsensitiveEnvironment.putAll(newEnvironmentVariables);
        } catch (final NoSuchFieldException nsfe) {
            LOG.error("Encountered no such field exception", nsfe);
            try {
                final Class[] classes = Collections.class.getDeclaredClasses();
                final Map<String, String> env = System.getenv();
                for (final Class c : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(c.getName())) {
                        final Field field = c.getDeclaredField("m");
                        allowAccessTo(field);
                        final Object object = field.get(env);
                        final Map<String, String> map = (Map<String, String>) object;
                        map.clear();
                        map.putAll(newEnvironmentVariables);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.error("Encountered error", e);
            }
        } catch (final Exception exeption) {
            LOG.error("Encountered error", exeption);
            return false;
        }
        return true;
    }

}
