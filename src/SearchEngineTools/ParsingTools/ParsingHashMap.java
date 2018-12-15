package SearchEngineTools.ParsingTools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParsingHashMap extends HashMap<String,String> {

    private int wordsInLongestKey;

    ParsingHashMap(Map<String,String> m){
        super(m.size());
        putAll(m);

    }
    ParsingHashMap(int initialCapacity, float loadFactor){
        super(initialCapacity, loadFactor);
        wordsInLongestKey=0;
    }
    public ParsingHashMap(int initialCapacity){
        super(initialCapacity);
        wordsInLongestKey=0;
    }
    public ParsingHashMap(){
        super();
        wordsInLongestKey = 0;
    }

    @Override
    public String put(String key, String value) {
        String[] split = key.split(" ");
        wordsInLongestKey = Math.max(wordsInLongestKey,split.length);
        return super.put(key, value);
    }

    public int getWordsInLongestKey() {
        return wordsInLongestKey;
    }

    @Override
    public String putIfAbsent(String key, String value) {
        String[] split = key.split(" ");
        wordsInLongestKey = Math.max(wordsInLongestKey,split.length);
        return super.putIfAbsent(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (String s:m.keySet()) {
            putIfAbsent(s,m.get(s));
        }
    }

    @Override
    public String remove(Object key) {
        Collection<String> keySet = this.keySet();
        if(key instanceof String && ((String) key).split(" ").length==wordsInLongestKey && keySet.contains(key)){
            int max = 0;
            for (String s:keySet()) {
                if(s.equals(key))
                    continue;
                String[] split = s.split(" ");
                if(split.length==wordsInLongestKey) {
                    max = wordsInLongestKey;
                    break;
                }
                else
                    max = Integer.max(max, split.length);
            }
            wordsInLongestKey = max;
        }
        return super.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        if(this.keySet().contains(key) && this.get(key).equals(value)) {
            remove(key);
            return true;
        }
        else
            return false;
    }

    @Override
    public void clear() {
        this.wordsInLongestKey=0;
        super.clear();
    }
}
