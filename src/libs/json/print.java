package json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ObjectTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
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

        JsonElement json;
        if(args[objectIndex].isString()){ // when coming from cartago
            StringTerm jsonString = (StringTerm)args[objectIndex];
            json = new JsonParser().parse(jsonString.getString());
        } else {
            json = (JsonElement)((ObjectTerm)args[objectIndex]).getObject();
        }

        string.append("[");
        string.append(ts.getAgArch().getAgName());
        string.append("] ");
        string.append(json.toString());
        System.out.println(string.toString());
        return true;
    }
}
