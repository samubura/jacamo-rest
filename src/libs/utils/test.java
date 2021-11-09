package utils;

import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;

import java.util.logging.Level;

public class test extends AbstractInternalAction {

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    protected Object action(TransitionSystem ts, Unifier un, Term[] args) throws JasonException {
        if(args[0].isAtom()){
            Atom term = (Atom)args[0];
            ts.getLogger().log(Level.INFO, "test: "+term.getFunctor());
        }
        if(args[0].isNumeric()){
            NumberTerm term = (NumberTerm)args[0];
            ts.getLogger().log(Level.INFO, "test: "+term.solve());
        }
        if(args[0].isString()){
            StringTerm term = (StringTerm)args[0];
            ts.getLogger().log(Level.INFO, "test: "+term.getString());
        }

        un.unifies(new ObjectTermImpl(new Object()), args[1]);

        return true;
    }
}
