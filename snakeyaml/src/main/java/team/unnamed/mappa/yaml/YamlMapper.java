package team.unnamed.mappa.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;
import team.unnamed.mappa.throwable.InvalidFormatException;

public class YamlMapper {
    protected final DumperOptions options = new DumperOptions();
    protected final Representer representer = new Representer();
    protected final Yaml yaml = new Yaml(new SafeConstructor(), representer, options);

    public void load() throws InvalidFormatException {

    }
}
