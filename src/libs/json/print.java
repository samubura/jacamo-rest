package json;

import com.google.gson.JsonElement;
import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import utils.AbstractInternalAction;

import java.util.logging.Level;

/**
 * Use as print(obj) or print(prefix, obj)
 * - obj must be an object reference
 * - prefix must be a string
 *
 * Prints the json object with the given prefix if set
 */
public class print extends AbstractInternalAction {
    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    protected Object action(TransitionSystem ts, Unifier un, Term[] args) throws JasonException {
        checkArguments(args);
        StringBuilder string = new StringBuilder();
        int objectIndex = 0;
        if(args.length == 2){
            //first argument is a prefix
            StringTerm prefix = (StringTerm)args[0];
            string.append(prefix.getString());
            string.append(" ");
            //second argument is the object
            objectIndex = 1;
        }
        ts.getLogger().log(Level.INFO, "OK");
        ObjectTerm obj = (ObjectTerm)args[objectIndex];
        ts.getLogger().log(Level.INFO, "OK");
        JsonElement json = (JsonElement)obj.getObject();
        string.append("[");
        string.append(ts.getAgArch().getAgName());
        string.append("] ");
        string.append(json.toString());
        ;
        System.out.println(string.toString());
        return true;
    }
}
