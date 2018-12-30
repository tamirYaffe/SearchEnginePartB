package SearchEngineTools.ParsingTools;

import SearchEngineTools.ParsingTools.Term.*;
import SearchEngineTools.ParsingTools.TokenList.ITokenList;

import javax.swing.text.html.parser.Entity;
import java.util.*;

public class EntityParse extends Parse {





    private PriorityQueue<ATerm> maxQueue = new PriorityQueue<>(new Comparator<ATerm>() {
        @Override
        public int compare(ATerm o1, ATerm o2) {
            return Integer.compare(o2.getOccurrences(),o1.getOccurrences());
        }
    });
    private PriorityQueue<ATerm> minQueue = new PriorityQueue<>(new Comparator<ATerm>() {
        @Override
        public int compare(ATerm o1, ATerm o2) {
            return Integer.compare(o1.getOccurrences(),o2.getOccurrences());
        }
    });


    /**
     * Add term to map of terms
     * @param term term to add
     * @param occurrencesList map to add to
     */
    protected void addTermToOccurrencesList(ATerm term, Map<String, ATerm> occurrencesList){
        if(isEntity(term))
            super.addTermToOccurrencesList(term,occurrencesList);
    }
    @Override
    protected List<ATerm> getFinalTermCollection(Map<String, ATerm> occurrencesOfTerms) {
        ArrayList<ATerm> toReturn = new ArrayList<>(occurrencesOfTerms.size());
        clearQueues();
        for (String termString:occurrencesOfTerms.keySet()) {
            ATerm term = occurrencesOfTerms.get(termString);
            if((term instanceof WordTerm) && Character.isUpperCase(termString.charAt(0)))
                add((WordTerm) term);
        }
        while (!maxQueue.isEmpty()){
            toReturn.add(maxQueue.poll());
        }
        return toReturn;
    }

    private void clearQueues(){
        maxQueue.clear();
        minQueue.clear();
    }

    private void add(WordTerm term){
        if(maxQueue.size()<5){
            addTermToQueues(term);
        }
        else {
            ATerm toCheck = minQueue.peek();
            if(toCheck.getOccurrences()<term.getOccurrences()){
                removeTermFromQueues(toCheck);
                addTermToQueues(term);
            }
        }
    }

    private void addTermToQueues(ATerm term){
        maxQueue.add(term);
        minQueue.add(term);
    }

    private void removeTermFromQueues(ATerm term){
        minQueue.remove(term);
        maxQueue.remove(term);
    }

    protected boolean isEntity(ATerm term){
        return (term instanceof WordTerm) && Character.isUpperCase(term.getTerm().charAt(0));
    }
}


