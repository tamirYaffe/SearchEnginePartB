package View;

import SearchEngineTools.datamuse.DatamuseQuery;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import java.util.*;

public class NaturalQueryDisplayer extends MenuBar {
    
//    private DatamuseQuery datamuseQuery = new DatamuseQuery();
    private String[] query;
    private Map<String,List<String>> suggestedQuery;
    private Menu[] queryMenu;
    private Map<Menu,Integer> locationsOfMenus=new LinkedHashMap<>();
    
    
    public NaturalQueryDisplayer(List<String> originalQuery){
        super();
        this.query= getOriginalQuery(originalQuery);
        suggestedQuery = getSuggestedQuery(originalQuery);
        queryMenu = setQueryMenu(originalQuery);
        getMenus().addAll(queryMenu);
    }

    private String[] getOriginalQuery(List<String> oq){
        String[] ans = new String[oq.size()];
        for (int i = 0; i < ans.length; i++) {
            ans[i] = oq.get(i);
        }
        return ans;
    }

    private Menu[] setQueryMenu(List<String> originalQuery) {
        Menu[] toReturn = new Menu[originalQuery.size()];
        for (int i = 0; i < originalQuery.size(); i++) {
            String originalWord = originalQuery.get(i);
            List<String> suggested = suggestedQuery.get(originalWord);
            toReturn[i] = createMenu(originalWord,suggested);
            this.locationsOfMenus.putIfAbsent(toReturn[i],i);
        }
        return toReturn;
    }

    private Menu createMenu(String name, List<String> suggested) {
        Menu toReturn = new Menu(suggested.get(0));
        for (int i = 0; i < suggested.size(); i++) {
            MenuItem menuItem = new MenuItem(suggested.get(i));
            final String s = new String(suggested.get(i));
            menuItem.setOnAction(event-> {
                changeQuery(toReturn,s);
            });
            toReturn.getItems().addAll(menuItem);
        }
        return toReturn;
    }

    private void changeQuery(Menu toReturn, String s) {
        int i = this.locationsOfMenus.get(toReturn);
        query[i]=s;
    }

    private Map<String,List<String>> getSuggestedQuery(List<String> originalQuery) {
        HashMap<String,List<String>> suggestedQuery = new HashMap<>();
        DatamuseQuery datamuseQuery = new DatamuseQuery();
        for (int i = 0; i < originalQuery.size(); i++) {
            String word = originalQuery.get(i);
            List<String> spelledLike = datamuseQuery.spelledSimilar(word,4);
            List<String> related = new ArrayList<>();
            related.addAll(spelledLike);
            List<String> soundsLike = datamuseQuery.soundSimilar(word,2);
            for (String s:soundsLike){
                if(!related.contains(s))
                    related.add(s);
            }
            if(related.size()<6 && spelledLike!=null && !spelledLike.isEmpty()){
                List<String> extraAsList = datamuseQuery.soundSimilar(spelledLike.get(0),1);
                String extra = extraAsList==null||extraAsList.isEmpty() ? null : extraAsList.get(0);
                if(extra!=null && !related.contains(extra))
                    related.add(extra);
            }
            if(!related.contains(word.toLowerCase()))
                related.add(word);
            if(Character.isUpperCase(word.charAt(0))){
                List<String> upperCase = new ArrayList<>(related.size());
                for (int j = 0; j < related.size(); j++) {
                    upperCase.add(related.get(j).toUpperCase());
                }
                related = upperCase;
            }
            if(!query[i].toLowerCase().equals(related.get(0).toLowerCase()))
                query[i]=related.get(0);
            suggestedQuery.put(originalQuery.get(i),related);
        }
        return suggestedQuery;
    }

    public String getQuery(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String word:query) {
            stringBuilder.append(word);
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }
}
