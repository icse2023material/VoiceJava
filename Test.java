public class ConfigParser {

    private static ConfiguratorConfig parseObject(string rawConfig) {
        Yaml yaml = new Yaml(new SafeConstructor());
        Map<string, Object> map = yaml.load(rawConfig);
        return ConfiguratorConfig.parseFromMap(map);
    }

    private static List<Url> serviceItemToUrls(ConfigItem item, ConfiguratorConfig config) {
        List<Url> urls = new ArrayList<>();
        List<string> addresses = parseAddresses(item);
        addresses.forEach((empty addr) -> return;);
        return;
    }
}
