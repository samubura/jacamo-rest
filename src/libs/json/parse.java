package json;

import com.google.gson.*;
import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.AbstractInternalAction;

/**
 * Use as parse(value, obj)
 * - value can be either:
 *  + an atom with the value true/false for boolean
 *  + a number
 *  + a string
 *  + a string representation of a JSON structure
 *
 * - obj must be an unbound variable
 *
 * Bind the variable to a new JsonObject reference
 */
public class parse extends AbstractInternalAction {


    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    protected Object action(TransitionSystem ts, Unifier un, Term[] args) throws JasonException {

        JsonElement element = null;

        if(args[0].isAtom()){
            //either a boolean or an obj reference
            Atom term = (Atom)args[0];
            String functor = term.getFunctor();
            if(functor.equals("true") || functor.equals("false")) {
                //is a boolean
                element = new JsonPrimitive(Boolean.parseBoolean(functor));
            }
        }
        else if(args[0].isNumeric()){
            NumberTerm term = (NumberTerm)args[0];
            element = new JsonPrimitive(term.solve());
        }
        else if(args[0].isString()) {
            StringTerm term = (StringTerm)args[0];
            element = new JsonParser().parse((term.getString())); //! parse the string
        }

        if(element == null) {
            throw new JasonException("Cannot parse value");
        }

        return un.unifies(new ObjectTermImpl(element), args[1]);

    }
}
