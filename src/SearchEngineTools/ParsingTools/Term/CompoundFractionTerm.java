package SearchEngineTools.ParsingTools.Term;

public class CompoundFractionTerm extends FractionTerm {
    private NumberTerm whole;

    /**
     * Get Whole Number part of the term
     * @return
     */
    NumberTerm getWholeNumber(){
        return whole;
    }

    /**
     * Constructor for the CompoundFractionTerm
     * @param whole
     * @param fractionTerm
     */
    public CompoundFractionTerm(NumberTerm whole, FractionTerm fractionTerm){
        super(fractionTerm.getNumerator(),fractionTerm.getDenominator());
        addPositions(whole);
        this.whole = whole;
    }

    @Override
    protected String createTerm() {
        return whole.createTerm() + " "+ super.createTerm();
    }
}
