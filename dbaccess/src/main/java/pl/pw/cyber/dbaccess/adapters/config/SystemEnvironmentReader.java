package pl.pw.cyber.dbaccess.adapters.config;

class SystemEnvironmentReader implements EnvironmentReader {

    @Override
    public String getEnv(String key) {
        return System.getenv(key);
    }
}
