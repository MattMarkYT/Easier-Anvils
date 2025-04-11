package mat.easieranvils.config;

import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;

// Original Programmer on YouTube: "Modding by Kaupenjoe"
// https://www.youtube.com/watch?v=w2wyfFnPPmY
public class EasierConfigProvider implements SimpleConfig.DefaultConfig {

    private String configContents = "";

    public List<Pair> getConfigsList() { return configsList; }

    private List<Pair> configsList = new ArrayList<>();

    public void addKeyValuePair(Pair<String,?> keyValuePair){
        configsList.add(keyValuePair);
        configContents += keyValuePair.getFirst() + "=" + keyValuePair.getSecond() + "\n";
    }
    public void resetConfigsList(){
        List<Pair> configsList = new ArrayList<>();
        configContents = "";
    }

    @Override
    public String get(String namespace) {
        return configContents;
    }
}
