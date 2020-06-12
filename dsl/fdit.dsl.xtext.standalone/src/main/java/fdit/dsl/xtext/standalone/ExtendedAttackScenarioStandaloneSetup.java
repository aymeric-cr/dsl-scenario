package fdit.dsl.xtext.standalone;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fdit.dsl.AttackScenarioGrammarStandaloneSetup;

class ExtendedAttackScenarioStandaloneSetup extends AttackScenarioGrammarStandaloneSetup {

    @Override
    public Injector createInjector() {
        return Guice.createInjector(new AttackScenarioModule());
    }
}
