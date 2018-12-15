package SearchEngineTools.ParsingTools.Term;


import eu.fayder.restcountries.v1.domain.Country;

import SearchEngineTools.ParsingTools.Parse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CityTerm extends WordTerm {
    protected static Parse parse;
    private ATerm statePopulation;
    private ATerm countryCurrency;
    private Country country;

    /**
     * Get positions of this term in document
     * @return
     */
    public List<Integer> getPositions(){
        return positions;
    }


    /**
     * CityTerm, represents a city
     * @param cityName cityName
     * @param country country of city
     */
    public CityTerm(String cityName, Country country){
        super(cityName);
        parse = parse==null ? new Parse() : parse;
        this.country=country;
        positions = new ArrayList<>();
        addPopulationTerm(country);
        addCurrency(country);
        isNumber=false;
    }


    private void addPopulationTerm(Country country){
        List<String> statePopulationList = new ArrayList<>();
        statePopulationList.add(country.getPopulation().toString());
        Collection<ATerm> statePopulationTerm = parse.parseText(statePopulationList);
        if(statePopulationTerm.size()==1){
            for (ATerm statePopulation:statePopulationTerm) {
                this.statePopulation = statePopulation;
            }
        }
    }

    private void addCurrency(Country country){
        List<String> currencyStringList = new ArrayList<>();
        currencyStringList.addAll(country.getCurrencies());
        Collection<ATerm> currencyTerms = parse.parseText(currencyStringList);
        for (ATerm term:currencyTerms) {
            countryCurrency=term;
        }
    }


    @Override
    protected String createTerm() {
        return term;
    }

    /**
     * get population of country
     * @return
     */
    public String getStatePopulation(){
        if(statePopulation==null)
            addPopulationTerm(country);
        return statePopulation.getTerm();
    }

    /**
     * get currency of country
     * @return
     */
    public String getCountryCurrency(){
        if(countryCurrency==null)
            addCurrency(country);
        return countryCurrency.getTerm();
    }


    public boolean equals(Object o){
        if(o instanceof CityTerm){
            CityTerm other = (CityTerm)o;
            return this.getTerm().equals(other.getTerm());
        }
        return false;
    }


    public String toString(){
        List<Integer> positions = getPositions()==null ? new ArrayList<>() : getPositions();
        String toReturn ="CityTerm:"+getTerm()+"~"+"\nOccurences: "+(getOccurrences())+
                "\nCountry: "+country.getName()+"\nPopulation size: "+getStatePopulation()+
                "\nCurrency: "+getCountryCurrency()+"\nPositions:";
        for (int i = 0; i < positions.size(); i++) {
            toReturn+=","+positions.get(i);
        }
        return toReturn;
    }

    /**
     * get name of country the city is located in
     * @return
     */
    public String getCountryName(){
        return country.getName();
    }

    public String getTerm(){
        return this.term.toUpperCase();
    }
}
