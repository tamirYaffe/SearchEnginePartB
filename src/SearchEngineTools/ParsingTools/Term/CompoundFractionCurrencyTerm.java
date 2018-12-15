package SearchEngineTools.ParsingTools.Term;

public class CompoundFractionCurrencyTerm extends CurrencyTerm {

    private FractionTerm fraction;


    /**
     * Constructor of compound currency term
     * @param compoundFractionTerm
     * @param currency
     */
    public CompoundFractionCurrencyTerm(CompoundFractionTerm compoundFractionTerm, String currency){
        super(compoundFractionTerm.getWholeNumber(), currency);
        addPositions(compoundFractionTerm);
        this.fraction = new FractionTerm(compoundFractionTerm.getNumerator(), compoundFractionTerm.getDenominator());
    }


    @Override
    protected String createTerm() {
        return this.numberTerm.getTerm()+" "+fraction.getTerm()+" "+currency;
    }
}
