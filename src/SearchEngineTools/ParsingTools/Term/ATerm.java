package SearchEngineTools.ParsingTools.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * represents a term in a document
 */
public abstract class ATerm  implements Comparable<ATerm>{

    public ATerm(int position){
        this.positions=new ArrayList<>();
        positions.add(position);
    }

    public ATerm(){
        this.positions=new ArrayList<>();
    }

    /**
     * the term of this string
     */
    protected String term;
    protected List<Integer> positions;
    protected boolean isNumber;
    /**
     * occurrences of term in document
     */
    private int occurrences=0;

    /**
     * get how many times term appeared in document
     * @return bumber of times term appeared in document
     */
    public int getOccurrences(){
        return occurrences;
    }

    /**
     * Set how many times term appeared in document
     * @param occurrences of ATerm in document
     */
    public void setOccurrences(int occurrences){
        this.occurrences = occurrences;
    }

    /**
     * String that shows ATerm's term and ammount of occcurrences
     * @return
     */
    public String toString(){
        return "Term: "+getTerm()+"~ Occurrences: "+getOccurrences();
    }

    /**
     * get Term of ATerm
     * @return term
     */
    public String getTerm(){
        if(term == null)
            term = createTerm();
        return term;
    }

    /**
     * create this ATerm's term
     * @return
     */
    protected abstract String createTerm();


    /**
     * Checks if two ATerms have the same term, does not take into account the amount of times they appear in a document
     * @param other other object
     * @return returns true if other object is an ATerm with the same Term
     */
    public boolean equals(Object other){
        if(other instanceof ATerm)
            return this.getTerm().equals(((ATerm) other).getTerm());
        return false;
    }

    /**
     * Overrides the hashcode method. Hashcode used is this Aterm's term's hashcode
     * @return hashcode of this AT
     */
    @Override
    public int hashCode() {
        String toHash = this.getTerm();
        return toHash.hashCode();
    }

    /**
     * Natural ordering by lexicographical order of terms
     * @param other
     * @return
     */
    public int compareTo(ATerm other){
        return this.getTerm().compareTo(other.getTerm());
    }

    /**
     * Add positions of other term
     * @param other
     */
    public void addPositions(ATerm other){
        if(other.positions==null)
            return;
        else {
            int currentPositionSize = positions==null ? 0 : positions.size();
            ArrayList<Integer> newPositions = new ArrayList<>(other.positions.size()+currentPositionSize);
            if(positions!=null)
                newPositions.addAll(positions);
            newPositions.addAll(other.positions);
            positions = newPositions;
        }
    }

    /**
     * increment occurrences of ATerm in document by 1
     */
    public void incrementOccurrences(){
        occurrences++;
    }

    /**
     * True if Number. Else False
     * @return
     */
    public boolean isNumber(){
        return isNumber;
    }

    /**
     * add position of term in document
     * @param position
     */
    public void addPosition(int position){
        positions.add(position);
    }



}
