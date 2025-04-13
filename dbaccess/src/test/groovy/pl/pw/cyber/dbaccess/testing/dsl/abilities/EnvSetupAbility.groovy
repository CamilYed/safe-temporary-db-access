package pl.pw.cyber.dbaccess.testing.dsl.abilities

import pl.pw.cyber.dbaccess.testing.dsl.builders.EnvVariableDatabaseBuilder

trait EnvSetupAbility {

    private final Map<String, String> originalEnv = [:]

    void thereIs(EnvVariableDatabaseBuilder envDatabase) {
        def prefix = envDatabase.getName()
        storeOriginal(prefix)

        setEnv(prefix + "_DB_URL", envDatabase.url)
        setEnv(prefix + "_DB_USERNAME", envDatabase.username)
        setEnv(prefix + "_DB_PASSWORD", envDatabase.password)
    }

    void clearDatabaseEnv(String prefix) {
        clearEnv(prefix + "_DB_URL")
        clearEnv(prefix + "_DB_USERNAME")
        clearEnv(prefix + "_DB_PASSWORD")
    }

    void clearAllDatabaseEnvs() {
        originalEnv.each { key, originalValue ->
            if (originalValue != null) {
                setEnv(key, originalValue)
            } else {
                System.clearProperty(key)
            }
        }
        originalEnv.clear()
    }

    private void storeOriginal(String prefix) {
        [
                prefix + "_DB_URL",
                prefix + "_DB_USERNAME",
                prefix + "_DB_PASSWORD"
        ].each {
            originalEnv.putIfAbsent(it, System.getProperty(it))
        }
    }

    private void setEnv(String key, String value) {
        System.setProperty(key, value)
    }

    private void clearEnv(String key) {
        System.clearProperty(key)
    }
}