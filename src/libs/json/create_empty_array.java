package json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import com.google.gson.JsonObject;
import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Term;
import utils.AbstractInternalAction;

/**
 * Use as create_empty_array(obj)
 * - obj must be an unbound variable
 *
 * Bind the variable to a new JsonObject reference
 */
public class create_empty_array extends AbstractInternalAction {

    //action as singleton
    public static InternalAction create() {
        if (singleton == null)
            singleton = new create_empty_object();
        return singleton;
    }

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
        checkArguments(args);
        JsonElement object = new JsonArray();
        ObjectTerm result = new ObjectTermImpl(object);
        return un.unifies(result, args[0]);
    }
}
