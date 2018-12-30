package SearchEngineTools.ParsingTools;


import SearchEngineTools.ParsingTools.Term.*;
import SearchEngineTools.ParsingTools.TokenList.DocumentTokenList;
import SearchEngineTools.ParsingTools.TokenList.ITokenList;
import SearchEngineTools.ParsingTools.TokenList.TextTokenList;
import SearchEngineTools.datamuse.DatamuseQuery;
import javafx.util.Pair;
import sun.awt.Mutex;
import SearchEngineTools.ParsingTools.Term.Value;
import java.util.*;

public class Parse {

    //all words that depict a value after the number, and the values they represent
    //for example <m, 1000000>, <Thousand, 1000>
    private Map<String, Value> valuesAfterNumber;

    //all frases that depict a currency and their currency symbol
    //for example <U.S dollar, $>, <Dollars, $>
    private ParsingHashMap currencyTypes;

    //all currency symbols the parser will recognize
    //for example $
    private Collection<Character> currencySymbols;

    //months and their values
    //for example <december, 12>
    protected Map<String, Integer> months;

    //months and last day
    //i.g <1,31>, <2,29>
    protected Map<Integer, Integer> lastDayInMonth;


    //all the words that represent percentages
    //for example %, percent, etc...
    private Collection<String> percentWords;

    //stop words to be removed
    protected Collection<String> stopWords;

    //stop words that are unique to list
    protected Collection<String> uniqueStopWords = new ArrayList<>();

    //characters to be removed from beginning and end of words
    private Collection<Character> necessaryChars;

    private ParsingHashMap years;

    private List<String> delimeters;

    private Collection<Character> delimitersToSplitWordBy;

    private ParsingHashMap cityNames;

    private int lastParsedDocumentLength = -1;

    public int getLastParsedDocumentLength() {
        return lastParsedDocumentLength;
    }
//private static CountryService countryService = CountryService.getInstance();

    protected Mutex mutex = new Mutex();

    private DocumentTokenList tokenList;

    private DatamuseQuery datamuseQuery= new DatamuseQuery();

    /**
     * Initializes parser
     * @param stopWords - words to ignore
     */
    public Parse(Collection<String> stopWords){
        initializeDataStructures();
        this.stopWords = stopWords;
        this.tokenList = new DocumentTokenList();
    }
    /**
     * default constructor, no list of stop words
     */
    //initializes data structures
    public Parse(){
        initializeDataStructures();
        this.stopWords = new ArrayList<>();
    }
    /**
     * all strings that represent value, (i.g thousand, million)
     * @return
     */
    private Collection<String> getValueKeywords(){
        return valuesAfterNumber.keySet();
    }

    /**
     *
     * @returnall strings that represent a currency
     * for example: US Dollar, Dollar, etc...
     */
    protected Collection<String> getCurrencyStrings(){
        return  currencyTypes.keySet();
    }

    /**
     *
     * @return all symbols that represent currency (i.g $)
     */
    private Collection<Character> getCurrencySymbols(){
        return currencySymbols;
    }

    protected Collection<String> getMonthWords(){
        return months.keySet();
    }

    private static Collection<String> allDocumentLanguages;




    //initiazlize diffrent data structures
    //////////////////////////////////////////////////////
    private void initializeValuesAfterNumber(){
        this.valuesAfterNumber = new HashMap<>();
        valuesAfterNumber.put("thousand", Value.THOUSAND);
        valuesAfterNumber.put("Thousand", Value.THOUSAND);
        valuesAfterNumber.put("Million", Value.MILLION);
        valuesAfterNumber.put("million", Value.MILLION);
        valuesAfterNumber.put("m", Value.MILLION);
        valuesAfterNumber.put("M", Value.MILLION);
        valuesAfterNumber.put("billion", Value.BILLION);
        valuesAfterNumber.put("Billion", Value.BILLION);
        valuesAfterNumber.put("bn", Value.BILLION);
        valuesAfterNumber.put("trillion", Value.TRILLION);
        valuesAfterNumber.put("Trillion", Value.TRILLION);
    }

    private void initializeCurrencyTypes() {
        this.currencyTypes = new ParsingHashMap();
        currencyTypes.put("Dollars","Dollars");
        currencyTypes.put("U.S Dollars","Dollars");
        currencyTypes.put("$","Dollars");
    }
    private void initializeDataStructures(){
        initializeValuesAfterNumber();
        initializeCurrencyTypes();
        initializeCurrencySymbols();
        initializeMonths();
        initializeLastDaysInMonth();
        initializePercentWords();
        initializeNecessaryChars();
        initializeDelimitersToSplitWordBy();
    }

    private void initializeDelimitersToSplitWordBy() {
        this.delimitersToSplitWordBy = new ArrayList<>();
        delimitersToSplitWordBy.add('-');
    }

    private void initializeNecessaryChars() {
        necessaryChars = new HashSet<>();
        necessaryChars.add('+');
        necessaryChars.add('-');
        necessaryChars.addAll(currencySymbols);
    }

    private void initializePercentWords() {
        this.percentWords = new HashSet();
        percentWords.add("%");
        percentWords.add("percent");
        percentWords.add("Percent");
        percentWords.add("PERCENT");
        percentWords.add("percentage");
        percentWords.add("Percentage");
        percentWords.add("PERCENTAGE");
    }

    private void initializeLastDaysInMonth() {
        this.lastDayInMonth = new HashMap<>();
        lastDayInMonth.put(1,31);
        lastDayInMonth.put(2,29);
        lastDayInMonth.put(3,31);
        lastDayInMonth.put(4,31);
        lastDayInMonth.put(5,30);
        lastDayInMonth.put(6,31);
        lastDayInMonth.put(7,31);
        lastDayInMonth.put(8,31);
        lastDayInMonth.put(9,30);
        lastDayInMonth.put(10,31);
        lastDayInMonth.put(11,30);
        lastDayInMonth.put(12,31);
    }

    private void initializeMonths() {
        this.months = new HashMap<>();
        String [] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        for (int i = 0; i < months.length; i++) {
            this.months.put(months[i],i+1);
            this.months.put(months[i].toUpperCase(),i+1);
        }
    }

    private void initializeCurrencySymbols(){
        this.currencySymbols = new HashSet<>();
        currencySymbols.add('$');
    }

    /////////////////////////////////////////////


    public List<ATerm> parseQuery(List<String> query, boolean spellCheck, int maxSynonyms){
        TextTokenList queryTokenList = new TextTokenList();
        queryTokenList.initialize(query,currencySymbols,delimitersToSplitWordBy,stopWords);
        return parse(queryTokenList,new HashMap<>(),spellCheck,maxSynonyms);
    }
    /**
     * Parse document with tags
     * @param document lines of document
     * @return all terms
     */
    public Collection<ATerm> parseDocument(List<String> document){
        if(tokenList==null)
            tokenList = new DocumentTokenList();
        //initialize datastructures
        tokenList.initialize(document,currencySymbols,delimitersToSplitWordBy,uniqueStopWords);
        Map<String, ATerm> occurrencesAndPositionsOfTerms = new HashMap<>();
        //get all terms in document and their occurrences
        addAllTermsToOccurrancesOfTerms(occurrencesAndPositionsOfTerms,tokenList,false,0);
        //get document language and city
        String documentLanguage = tokenList.getDocLanguage();
        addDocumentLanguage(documentLanguage);
        CityTerm documentCity = tokenList.getCityTerm();
        getDocCity(documentCity,occurrencesAndPositionsOfTerms);
        //get final collection of terms
        Collection<ATerm> toReturn = getFinalTermCollection(occurrencesAndPositionsOfTerms);
        //clear tokenlist
        clear();
        //return terms
        return toReturn;

    }

    protected void clear(){
        tokenList.clear();
    }
    private void getDocCity(CityTerm documentCityTerm,Map<String, ATerm> occurrencesAndPositionsOfTerms){
        if(documentCityTerm==null)
            return;
        String cityName = documentCityTerm.getTerm().toLowerCase();
        boolean addedCityTerm = addCityNameToOccurrencesMap(documentCityTerm,cityName,occurrencesAndPositionsOfTerms);
        if(addedCityTerm)
            return;
        cityName=cityName.toUpperCase();
        addedCityTerm = addCityNameToOccurrencesMap(documentCityTerm,cityName,occurrencesAndPositionsOfTerms);
        if(addedCityTerm)
            return;
        occurrencesAndPositionsOfTerms.put(documentCityTerm.getTerm(),documentCityTerm);
    }

    private boolean addCityNameToOccurrencesMap(CityTerm cityTerm,String cityTermCandidateName,Map<String, ATerm> occurrencesAndPositionsOfTerms){
        if(occurrencesAndPositionsOfTerms.keySet().contains(cityTermCandidateName)){
            ATerm term = occurrencesAndPositionsOfTerms.remove(cityTermCandidateName);
            cityTerm.addPositions(term);
            occurrencesAndPositionsOfTerms.put(cityTerm.getTerm(),cityTerm);
            return true;
        }
        return false;
    }
    public Collection<String> getAllDocumentLanguages(){
        if(allDocumentLanguages==null)
            return new ArrayList<>();
        return allDocumentLanguages;
    }
    private void addDocumentLanguage(String language){
        if(language==null)
            return;
        if(allDocumentLanguages==null)
            allDocumentLanguages = new HashSet<>();
        String languageUpperCase = language.toUpperCase();
        if(!allDocumentLanguages.contains(languageUpperCase)){
            allDocumentLanguages.add(languageUpperCase);
        }
    }

    /**
     * parse text, without tags
     * @param text text to parse
     * @return all unique terms
     */
    public Collection<ATerm> parseText(List<String> text){
        TextTokenList tokenList = new TextTokenList();
        tokenList.initialize(text,currencySymbols,delimitersToSplitWordBy,uniqueStopWords);
        return parse(tokenList);
    }

    /**
     * Parses tokenlist
     * @param tokenList
     * @return
     */
    public Collection<ATerm> parse(ITokenList tokenList){
        //Map<ATerm,OccurrencesListPair> occurrencesOfTerms = new HashMap<>();
        Map<String, ATerm> occurrencesAndPositionsOfTerms = new HashMap<>();
        return parse(tokenList,occurrencesAndPositionsOfTerms,false,0);
    }

    /**
     * Parse tokenlist
     * @param tokenList
     * @param occurrencesAndPositionsOfTerms
     * @return
     */
    protected List<ATerm> parse(ITokenList tokenList, Map<String, ATerm> occurrencesAndPositionsOfTerms,boolean spellCheck,int maxSynonyms){
        addAllTermsToOccurrancesOfTerms(occurrencesAndPositionsOfTerms,tokenList,spellCheck,maxSynonyms);
        List<ATerm> toReturn = getFinalTermCollection(occurrencesAndPositionsOfTerms);
        return toReturn;
    }


    /**
     * returns final collection from list
     * @param occurrencesOfTerms
     * @return
     */
    protected List<ATerm> getFinalTermCollection(Map<String, ATerm> occurrencesOfTerms) {
        ArrayList<ATerm> toReturn = new ArrayList<>(occurrencesOfTerms.size());
        lastParsedDocumentLength = 0;
        for (String termString:occurrencesOfTerms.keySet()) {
            ATerm term = occurrencesOfTerms.get(termString);
            toReturn.add(term);
            lastParsedDocumentLength+=term.getOccurrences();
        }
        return toReturn;
    }


    /**
     * Adds all terms and their occurrences to list
     * @param occurrencesOfTerms
     * @param tokenList
     */
    private void addAllTermsToOccurrancesOfTerms(Map<String, ATerm> occurrencesOfTerms, ITokenList tokenList, boolean spellcheck, int maxSynonyms) {
        while (!tokenList.isEmpty()){
            getNextTerm(occurrencesOfTerms,tokenList,spellcheck,maxSynonyms);
        }
    }

    /**
     * gets the next term from list, adds itto occurrences
     * @param occurrencesOfTerms
     * @param tokenList
     */
    private void getNextTerm(Map<String, ATerm> occurrencesOfTerms, ITokenList tokenList,boolean spellCheck, int maxSynonyms){
        ATerm nextTerm = null;
        //if list is empty, no tokens
        if(tokenList.isEmpty())
            return;
        Token token = tokenList.pop();
        if(token==null)
            return;
        String tokenString = token.getTokenString();
        //if is number
        if(tokenString!=null && isNumber(tokenString)) {
            nextTerm = new NumberTerm(tokenString);
            AddNextNumberTerm(tokenList, nextTerm,occurrencesOfTerms);
        }
        //word
        else {
            addWordTerm(tokenList, token,occurrencesOfTerms,spellCheck,maxSynonyms);
        }
    }

    /**
     * checks if s is a number
     * @param s
     * @return true if s is a number, false otherwise
     */
    protected boolean isNumber(String s){
        float [] floats = getNumberValue(s);
        return floats != null;
    }

    private float[] getNumberValue(String s){
        //check if it is already a number
        try {
            float toReturn = Float.parseFloat(s);
            float[] floats = {toReturn};
            return floats;
        }
        //not a double
        catch (Exception e){
            //check if is because of commas
            String [] split = s.split(",");
            StringBuilder toCheck = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                toCheck.append(split[i]);
            }
            //check if it is a number
            try {
                float toReturn = Float.parseFloat(toCheck.toString());
                float[] floats = {toReturn};
                return floats;
            }
            catch (Exception e2){
                return null;
            }
        }
    }

    /**
     * add next number term from tokens list
     * @param tokenList
     * @param nextTerm
     * @param occurrencesOfTerms
     */
    protected void AddNextNumberTerm(ITokenList tokenList, ATerm nextTerm, Map<String, ATerm> occurrencesOfTerms){
        //get next word
        if(!tokenList.isEmpty()) {
            Token nextToken = tokenList.peek();
            String nextTokenString = nextToken==null ? null : nextToken.getTokenString();
            //check if percentage
            if (nextTokenString!=null && percentWords.contains(nextTokenString)) {
                nextTerm = new PercentageTerm((NumberTerm)nextTerm);
                tokenList.pop();
            }
            //check if month or year
            else if(nextTokenString!=null &&
                    (((NumberTerm) nextTerm).isInteger(((NumberTerm) nextTerm))) //number is integer
                    && getMonthWords().contains(nextTokenString) && ((((NumberTerm) nextTerm).getValueOfNumber()>0) && //number is at least one
                        ((NumberTerm) nextTerm).getValueOfNumber()<=lastDayInMonth.get(months.get(nextTokenString))))
            { //number is smaller than last day in month
                    nextTerm = new DateTerm(months.get(nextTokenString),(int)((NumberTerm) nextTerm).getValueOfNumber());
                    tokenList.pop();
            }
            else {
                boolean isFraction = false;
                //check if value
                if (nextTokenString!=null && getValueKeywords().contains(nextTokenString)) {
                    Value val = valuesAfterNumber.get(nextTokenString);
                    ((NumberTerm) nextTerm).multiply(val);
                    //remove keyword after use
                    tokenList.pop();

                }
                //check if fraction
                else if(nextTokenString!=null && isFraction(nextTokenString)){
                    nextTerm = new CompoundFractionTerm((NumberTerm)nextTerm,getFractionTerm(nextTokenString));
                    tokenList.pop();
                    isFraction = true;
                }
                //check if currency
                Pair<String,Integer> currencyNameAndLocation = null;
                if(!tokenList.isEmpty()) {
                    currencyNameAndLocation = getNextRelevantTerm(tokenList,currencyTypes);
                }
                if(currencyNameAndLocation != null){
                    nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm)nextTerm, this.currencyTypes.get(currencyNameAndLocation.getKey()))
                            : new CurrencyTerm((NumberTerm)nextTerm, currencyTypes.get(currencyNameAndLocation.getKey()));
                    for (int i = 0; i<=currencyNameAndLocation.getValue(); i++){
                        tokenList.pop();
                    }
                }
            }
        }
        //no suitable next word found, return number
        addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
    }

    /**
     * checks if s is a fraction
     * @param s
     * @return true if s is a fraction, false otherwise
     */
    protected boolean isFraction(String s){
        String[] split = null;
        if(s.contains("/")) {
            split = s.split("/");
        }
        //check two different parts
        if(split==null || split.length!=2)
            return false;
        //check both are numbers
        return isNumber(split[0]) && isNumber(split[1]);
    }


    /**
     * checks is s is an integer
     * @param s
     * @return true if s is an integer, false otherwise
     */
    protected boolean isInteger(CharSequence s){
        String string;
        if(s instanceof String)
            string = (String)s;
        else{
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                sb.append(s.charAt(i));
            }
            string = sb.toString();
        }
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * get next relevant term from tokens list, from parsing hashmap
     * @param tokens
     * @param toGetFrom
     * @return string of term, position of term in tokens. Null if no such Token
     */
    private static Pair<String, Integer> getNextRelevantTerm(ITokenList tokens, ParsingHashMap toGetFrom){
        Pair<String,Integer> toReturn = null;
        String toCheck = "";
        Collection<String> keys = toGetFrom.keySet();
        List<Token> toPrepend = new ArrayList<>(toGetFrom.getWordsInLongestKey());
        for (int i = 0; i < toGetFrom.getWordsInLongestKey() && !tokens.isEmpty(); i++) {
            Token token = tokens.pop();
            if(token==null)
                break;
            toPrepend.add(token);
            String toAdd = token!=null ? copy(token.getTokenString()):null;
            if(i!=0)
                toCheck+=(" "+toAdd);
            else
                toCheck+=toAdd;
            if(keys.contains(toCheck)) {
                toReturn = new Pair<>(toCheck, i);
                break;
            }
        }
        tokens.prependValidTokens(toPrepend);
        return toReturn;
    }

    private static String copy(CharSequence sequence){
        if(sequence==null)
            return null;
        StringBuilder builder = new StringBuilder(sequence.length());
        String s = sequence.toString();
        for (int i = 0; i < s.length(); i++) {
            builder.append(s.charAt(i));
        }
        return builder.toString();
    }

    /**
     * create fraction term from string s
     * @param s valid fraction
     * @return fraction term that corresponds to string s
     */
    protected FractionTerm getFractionTerm(String s){
        String[] split = s.split("/");
        NumberTerm numerator = new NumberTerm(split[0]);
        NumberTerm denominator = new NumberTerm(split[1]);
        return new FractionTerm(numerator,denominator);
    }

    /**
     * Add term to map of terms
     * @param term term to add
     * @param occurrencesList map to add to
     */
    protected void addTermToOccurrencesList(ATerm term, Map<String, ATerm> occurrencesList){
        if(term instanceof WordTerm){
            addWordTermToOccurrencesList((WordTerm) term,occurrencesList,Character.isLowerCase(term.getTerm().charAt(0)));
        }
        else {
            addTermToOccurrencesList(term, occurrencesList, true);
        }
    }

    /**
     * Add term to map of terms. if no specialCase then add Term to Map w/out checking validity
     * @param term
     * @param occurrencesList
     * @param noSpecialCase
     */
    private void addTermToOccurrencesList(ATerm term, Map<String, ATerm> occurrencesList, boolean noSpecialCase){
        if(term instanceof WordTerm && !noSpecialCase)
            addTermToOccurrencesList(term,occurrencesList);
        else{
            String termString = term.getTerm();
            if(occurrencesList.keySet().contains(termString)){
                ATerm termInList = occurrencesList.get(termString);
                termInList.incrementOccurrences();
                termInList.addPositions(term);
            }
            else {
                term.setOccurrences(1);
                occurrencesList.put(termString,term);
            }
        }
    }

    /**
     * Add word term to occurrences if terms
     * @param term term to add
     * @param occurrencesOfTerms map to add to
     * @param isLowerCase true if word is lower case, false otherwise
     */
    private void addWordTermToOccurrencesList(WordTerm term, Map<String, ATerm> occurrencesOfTerms, boolean isLowerCase){
        if(term instanceof CityTerm){
            addTermToOccurrencesList(term,occurrencesOfTerms,true);
            return;
        }
        String termString = term.getTerm();
        String upperCaseTermString = termString.toUpperCase();
        String lowerCaseTermString = termString.toLowerCase();
        if(stopWords.contains(lowerCaseTermString))
            return;
        boolean existsLowercase = occurrencesOfTerms.containsKey(lowerCaseTermString);
        boolean existsUppercase = occurrencesOfTerms.containsKey(upperCaseTermString);


        if(isLowerCase && existsUppercase){
            WordTerm old = (WordTerm) occurrencesOfTerms.get(upperCaseTermString);
            old.toLowerCase();
            old.incrementOccurrences();
            occurrencesOfTerms.remove(upperCaseTermString);
            occurrencesOfTerms.put(lowerCaseTermString,old);
        }
        else if(isLowerCase || existsLowercase){
            term.toLowerCase();
            addTermToOccurrencesList(term,occurrencesOfTerms,true);
        }
        else {
            term.toUperCase();
            addTermToOccurrencesList(term, occurrencesOfTerms, true);
        }
    }

    /**
     * If Term is not a number, add creates trem and adds it to list
     * @param tokens
     * @param token
     * @param occurrencesOfTerms
     */
    protected void addWordTerm(ITokenList tokens, Token token, Map<String, ATerm> occurrencesOfTerms, boolean spellCheck, int maxSynonyms){
        ATerm nextTerm = null;
        String tokenString = token.getTokenString();
        //check percentage
        if(isPercentage(tokenString)){
            nextTerm = getPercentageTerm(tokenString);
            addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
            return;
        }
        //check currency
        else if(isCurrency(tokenString)){
            nextTerm = getCurrencyTerm(tokenString,tokens);
            //check if next is value
            addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
            return;
        }
        //check month
        else if(getMonthWords().contains(tokenString)){
            Token nextToken = tokens.isEmpty() ? null : tokens.peek();
            String nextTokenString = nextToken==null ? null : nextToken.getTokenString();
            if(nextTokenString!=null && isNumber(nextTokenString) && isInteger(nextTokenString)){
                int monthPair = Integer.parseInt(nextTokenString);
                if(monthPair>0 && monthPair<lastDayInMonth.get(months.get(tokenString))){
                    nextTerm = new DateTerm(months.get(tokenString),monthPair);
                    addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                    tokens.pop();
                    return;
                }
                else {
                    nextTerm = new YearTerm(months.get(tokenString),monthPair);
                    addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                    tokens.pop();
                    return;
                }
            }
        }
        //check hyphenated word
        else if(isHyphenatedWord(tokenString)){
            List<ATerm> toAdd = getHyphenatedTokens(token,tokens,spellCheck,maxSynonyms);
            for (ATerm termToAdd:toAdd) {
                addTermToOccurrencesList(termToAdd,occurrencesOfTerms);
            }
            return;
        }
        boolean isNumber = false;
        boolean isFraction = false;
        //check number with value
        if(isNumberWithValue(tokenString)){
            isNumber =true;
            nextTerm = splitWord(tokenString);
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                return;
            }
            else{
                tokenString = tokens.get(0).getTokenString();
            }
        }
        //check fraction
        if(isFraction(tokenString)){
            isFraction = true;
            nextTerm = isNumber ? new CompoundFractionTerm((NumberTerm) nextTerm, getFractionTerm(tokenString)) : getFractionTerm(tokenString);
            if(isNumber)
                tokens.pop();
            isNumber = true;
            //if list is now empty, return, else switch token to next word
            if(tokens.isEmpty()){
                addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
                return;
            }
            else{
                token = tokens.get(0);
            }
        }
        //check currency
        if(isNumber && getCurrencyStrings().contains(token)){
            nextTerm = isFraction ? new CompoundFractionCurrencyTerm((CompoundFractionTerm) nextTerm,tokenString) : new CurrencyTerm((NumberTerm) nextTerm,tokenString);
            tokens.pop();
            addTermToOccurrencesList(nextTerm,occurrencesOfTerms);
            return;
        }
        /*//check city
        boolean isCity = createCityTerm(token,occurrencesOfTerms,tokens);
        if(isCity)
            return;*/

        //split word by non numbers and letter
        List<ATerm> toAdd = getFinalWordTermList(token,tokens,spellCheck,maxSynonyms);
        for (ATerm termToAdd:toAdd) {
            addTermToOccurrencesList(termToAdd,occurrencesOfTerms);
        }
        return;
    }

    /**
     * Create Percentage Term from String s
     * @param s
     * @return
     */
    private PercentageTerm getPercentageTerm(String s){
        if(isPercentage(s)){
            NumberTerm term = new NumberTerm(s.substring(0,s.length()-1));
            return new PercentageTerm(term);
        }
        else if(isNumber(s)){
            NumberTerm term = new NumberTerm(s);
            return new PercentageTerm(term);
        }
        return null;
    }

    /**
     *
     * @param s
     * @return
     */
    protected boolean isPercentage(String s) {
        if(s.length()>1 && s.charAt(s.length()-1)=='%'){
            String number = s.substring(0,s.length()-1);
            if(number!=null && number.length()>0 && isNumber(number))
                return true;
        }
        return false;
    }

    /**
     * checks if string is currency
     * @param s
     * @return true if string is currency, false otherwise
     */
    protected boolean isCurrency(String s){
        if(s.length()>1){
            char first = s.charAt(0);
            if(getCurrencySymbols().contains(first) && (isNumber(s.substring(1)) || isNumberWithValue(s.substring(1))))
                return true;
        }
        return false;
    }

    /**
     * Seperates terms into multiple terms if contain delimiters
     * @param s
     * @param tokens
     * @return
     */
    protected List<ATerm> getFinalWordTermList(Token s, ITokenList tokens, boolean spellCheck, int maxSynonyms){
        return getFinalWordTermList(s,tokens,new ArrayList<>(0),spellCheck,maxSynonyms);
    }

    /**
     *
     * @param token
     * @param tokens
     * @param delimitersToIgnore
     * @return
     */
    protected List<ATerm> getFinalWordTermList(Token token, ITokenList tokens, Collection<Character> delimitersToIgnore, boolean spellCheck, int maxSynonyms) {
        List<ATerm> toReturn = new ArrayList<>();
        String s = token.getTokenString();
        int position = token.getPosition();
        //get all desired substrings
        //split the word into parts
        List<Pair<Integer,Integer>> desiredSubstrings = new ArrayList<>();
        for (int i = 0, firstDesiredIndex=0; i < s.length(); i++) {
            if(!Character.isLetter(s.charAt(i)) && !Character.isDigit(s.charAt(i)) && !delimitersToIgnore.contains(s.charAt(i))){
                int newFirstDesiredIndex;
                if(currencySymbols.contains(s.charAt(i))) {
                    if(i==firstDesiredIndex)
                        continue;
                    newFirstDesiredIndex = i;
                }
                else
                    newFirstDesiredIndex=i+1;

                if(i==s.length()-1){
                    if(i>firstDesiredIndex)
                        desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                }
                else
                    desiredSubstrings.add(new Pair<>(firstDesiredIndex,i));
                firstDesiredIndex = newFirstDesiredIndex;
            }
            else if(i==s.length()-1)
                desiredSubstrings.add(new Pair<>(firstDesiredIndex,i+1));
        }
        //check if only one string
        if(desiredSubstrings.isEmpty())
            return toReturn;
        else if(desiredSubstrings.size()==1){
            token.setTokenString(s.substring(desiredSubstrings.get(0).getKey(),desiredSubstrings.get(0).getValue()));
            if(spellCheck)
                spellCheck(token,toReturn,maxSynonyms);
            if(maxSynonyms>0)
                addSynonyms(token,maxSynonyms,toReturn);
            WordTerm term = createWordTerm(token);
            if(term!=null)
                toReturn.add(term);
            return toReturn;
        }
        List<Token> tokensToAdd = new ArrayList<>();

        for (Pair<Integer,Integer> substring:desiredSubstrings) {
            String tokenString = s.substring(substring.getKey(),substring.getValue());
            Token tokenToAdd = new Token(tokenString,position);
            if(tokenToAdd!= null)
                tokensToAdd.add(tokenToAdd);
        }
        //prepend desired tokens to list
        tokens.prepend(tokensToAdd);
        return toReturn;
    }

    private void addSynonyms(Token token, int maxSynonyms, List<ATerm> addTo) {
        String tokenString = token.getTokenString();
        List<String> synonyms = Character.isUpperCase(tokenString.charAt(0))?null:datamuseQuery.synonyms(tokenString,maxSynonyms);
        for(int i=0; synonyms!=null && i<synonyms.size(); i++){
            WordTerm synonym = createWordTerm(new Token(synonyms.get(i),-1));
            addTo.add(synonym);
        }
    }

    private void spellCheck(Token token,List<ATerm> addTo,int maxSynonyms) {
        boolean addSynonyms = maxSynonyms>0;
        String tokenString = token.getTokenString();
        List<String> correctSpellingAsList = datamuseQuery.spelledSimilar(tokenString,1);
        String correctSpelling = correctSpellingAsList.isEmpty()?null:correctSpellingAsList.get(0);
        if(correctSpelling!=null && !tokenString.toLowerCase().equals(correctSpelling)){
            correctSpelling = Character.isUpperCase(tokenString.charAt(0)) ? correctSpelling.toUpperCase() : correctSpelling;
            WordTerm correctlySpelledTerm = createWordTerm(new Token(correctSpelling,token.getPosition()));
            if(correctlySpelledTerm!=null){
                addTo.add(correctlySpelledTerm);
                if(addSynonyms)
                    addSynonyms(new Token(correctSpelling,token.getPosition()),maxSynonyms,addTo);
            }
        }
    }

    protected WordTerm createWordTerm(Token token) {
        String tokenString = token.getTokenString();
        boolean necessary = !(tokenString==null || tokenString.length()<=0 && stopWords.contains(tokenString.toLowerCase()));
        if(necessary)
            return new WordTerm(token);
        return null;
    }

    private boolean createCityTerm(Token token, Map<String, ATerm> occurrencesOfTerms, ITokenList tokenList) {
        Collection<String> cityTerms = cityNames.keySet();
        int longestTerm = cityNames.getWordsInLongestKey();
        List<Token> toPrepend = new ArrayList<>();
        String cityName=token.getTokenString();
        cityName = getCityName(cityName,occurrencesOfTerms);
        boolean foundCityTerm = cityName!=null;
        for (int i = 0; i < longestTerm-1 && !foundCityTerm && !tokenList.isEmpty(); i++) {
            Token t = tokenList.pop();
            cityName += " "+t.getTokenString();
            cityName = getCityName(cityName,occurrencesOfTerms);
            foundCityTerm = cityName!=null;
            toPrepend.add(t);
        }
        if(foundCityTerm){
            addCityToOccurenceList(cityName,token.getPosition(),occurrencesOfTerms);
            return true;
        }
        else
            tokenList.prependValidTokens(toPrepend);
        return false;
    }

    private String getCityName(String cityNameCandidate, Map<String, ATerm> occurrencesOfTerms){
        String cityNameUpperCase = cityNameCandidate.toUpperCase();
        String cityNameLowerCase = cityNameCandidate.toLowerCase();
        Collection<String> cityNames = getCityNames();
        Collection<String> termNames = occurrencesOfTerms.keySet();
        boolean foundCityTerm = cityNames.contains(cityNameCandidate) || cityNames.contains(cityNameLowerCase) || cityNames.contains(cityNameUpperCase);
        if(foundCityTerm){
            String cityName = termNames.contains(cityNameCandidate) ? cityNameCandidate : null;
            cityName = termNames.contains(cityNameLowerCase) ? cityNameLowerCase : cityName;
            cityName = termNames.contains(cityNameUpperCase) ? cityNameUpperCase : cityName;
            return cityName;
        }
        return null;
    }

    private Collection<String> getCityNames(){
        return cityNames.keySet();
    }

    private void addCityToOccurenceList(String cityName, int position, Map<String, ATerm> occurrencesOfTerms) {
        CityTerm cityTerm = (CityTerm) occurrencesOfTerms.get(cityName);
        cityTerm.addPosition(position);
        cityTerm.incrementOccurrences();
    }

    protected boolean isStopWord(String s){
        mutex.lock();
        boolean isStopWord = stopWords.contains(s.toLowerCase());
        mutex.unlock();
        return isStopWord;
    }

    protected boolean isHyphenatedWord(String token) {
        for (char delimiter:delimitersToSplitWordBy) {
            if(token.contains(""+delimiter)){
                String[] split = token.split(""+delimiter);
                return (split!=null && split.length>1 && split[0].length()>0 && split[1].length()>0);
            }

        }
        return false;
    }

    private CurrencyTerm getCurrencyTerm(String s, ITokenList tokens){
        Value val = null;
        if(!tokens.isEmpty() && getValueKeywords().contains(tokens.peek().getTokenString()))
            val = valuesAfterNumber.get(tokens.pop().getTokenString());
        if(isCurrency(s)){
            String currencySymbol = s.substring(0,1);
            String currency = currencyTypes.get(currencySymbol);
            String number = s.substring(1);
            boolean isNumberWithValue = isNumberWithValue(number);
            NumberTerm term = isNumberWithValue ? splitWord(number) : new NumberTerm(number);
            if(val!=null)
                term.multiply(val);
            return new CurrencyTerm(term,currency);
        }
        return null;
    }

    protected List<ATerm> getHyphenatedTokens(Token token, ITokenList tokens, boolean spellCheck, int maxSynonyms) {
        List<ATerm> hyphenatedToken = getFinalWordTermList(token,tokens,delimitersToSplitWordBy,spellCheck,maxSynonyms);
        if(!(hyphenatedToken==null || hyphenatedToken.isEmpty())){
            hyphenatedToken.addAll(getFinalWordTermList(token,tokens,spellCheck,maxSynonyms));
        }
        return hyphenatedToken;
    }

    protected boolean isNumberWithValue(String s){
        //get last index of number
        boolean number = true;
        int pointer = 0;
        while (pointer<s.length() && number){
            number = Character.isDigit(s.charAt(pointer)) || s.charAt(pointer)=='.';
            if(number)
                pointer++;
        }
        //check if is number and word after it represents value
        if(pointer>0 && pointer<s.length()){
            String numString = s.substring(0,pointer);
            String word = s.substring(pointer);
            if(isNumber(numString) && getValueKeywords().contains(word))
                return true;
            return false;
        }
        return false;
    }

    protected NumberTerm splitWord(String s){
        boolean number = true;
        int pointer = 0;
        while (pointer<s.length() && number){
            number = Character.isDigit(s.charAt(pointer)) || s.charAt(pointer)=='.';
            if(number)
                pointer++;
        }
        String numString = s.substring(0,pointer);
        String word = s.substring(pointer);

        NumberTerm toReturn = new NumberTerm(numString);
        toReturn.multiply(valuesAfterNumber.get(word));
        return toReturn;
    }


    public void setStopWords(Collection<String> stopWords){
        this.stopWords=stopWords;
        setUniqueStopWords();
    }

    private void setUniqueStopWords(){
        this.uniqueStopWords = new HashSet<>(stopWords.size());
        uniqueStopWords.addAll(stopWords);
        List<Collection<String>> toRemoveFrom = new ArrayList<>();
        toRemoveFrom.add(this.percentWords);
        if(cityNames!=null)
            toRemoveFrom.add(this.cityNames.keySet());
        toRemoveFrom.add(this.months.keySet());
        toRemoveFrom.add(this.valuesAfterNumber.keySet());
        List<String> currencySymbols = new ArrayList<>();
        for (char c:this.currencySymbols) {
            currencySymbols.add(""+c);
        }
        toRemoveFrom.add(currencySymbols);
        for (Collection<String> c:toRemoveFrom) {
            removeFromUniqueStopWords(uniqueStopWords,c);
        }
    }

    private static void removeFromUniqueStopWords(Collection<String> removeFrom,Collection<String> ToRemove){
        for (String s : ToRemove){
            String lowerCase = s.toLowerCase();
            if(removeFrom.contains(lowerCase))
                removeFrom.remove(lowerCase);
        }
    }

    public void setCityNames(Collection<String> cityNames){
        this.cityNames = new ParsingHashMap(cityNames.size());
        for (String cityName:cityNames) {
            this.cityNames.put(cityName,cityName);
        }
    }
}
