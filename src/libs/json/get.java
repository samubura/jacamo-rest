package json;

import com.google.gson.JsonElement;
import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.AbstractInternalAction;

import java.util.logging.Level;

/**
 * Use as get(obj, type, key, value)
 * - obj must be a JsonObject or JsonArry reference
 * - type must be a string with the values [string, boolean, number, integer, array, object] or _ to ignore and get a JsonElement
 * - key can be either:
 *  + an int index if obj is an array
 *  + a string key if obj is an object
 * - value must be an unbound variable
 *
 * or get(obj, type, value)
 * - obj should be a JsonPrimitive reference otherwise it might break
 * - type should be a string with the values [string, boolean, number, integer] otherwise it has no effect
 * - value must be an unbound variable
 *
 *
 * Extract a value from the given JSON document
 */
public class get extends AbstractInternalAction {
    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    protected Object action(TransitionSystem ts, Unifier un, Term[] args) throws JasonException {
        checkArguments(args);
        Term feedbackParam = args[args.length-1];
        JsonElement json = (JsonElement)((ObjectTerm)args[0]).getObject();
        String type = null;
        if(args[1].isUnnamedVar()){
            type = "no_type";
        } else if(args[1].isString()){
            type = ((StringTerm)args[1]).getString();
        }

        if(args.length == 4){
            json = getElement(json, args[2]);
        }

        return un.unifies(getResult(json, type), feedbackParam);
    }

    private JsonElement getElement(JsonElement json, Term keyTerm) throws JasonException {
        if(keyTerm.isNumeric()){
            //get an item from an array
            int index = (int)((NumberTerm)keyTerm).solve();
            try {
                json = json.getAsJsonArray().get(index);
            } catch (IllegalStateException e){
                throw new JasonException("Not an array");
            } catch(IndexOutOfBoundsException e){
                throw new JasonException("Provided key was not found in the array");
            }
        } else if(keyTerm.isString()) {
            //get a key from an object
            String key = ((StringTerm)keyTerm).getString();
            try {
                json = json.getAsJsonObject().get(key);
                if (json == null) {
                    throw new JasonException("Provided key was not found in the object");
                }
            } catch (IllegalStateException e){
                throw new JasonException("Not an array");
            }

        }
        return json;
    }

    //get the raw value as the given type from the element
    private Term getResult(JsonElement json, String type) throws JasonException {
        Term result;
        try {
            switch (type) {
                case "string":
                    result = new StringTermImpl(json.getAsString());
                    break;
                case "boolean":
                    result = new Atom(json.getAsBoolean() ? "true" : "false"); //TODO is this ok?
                    break;
                case "number":
                    result = new NumberTermImpl(json.getAsDouble());
                    break;
                case "integer":
                    result = new NumberTermImpl(json.getAsInt());
                    break;
                case "object":
                    result = new ObjectTermImpl(json.getAsJsonObject());
                    break;
                case "array":
                    result = new ObjectTermImpl(json.getAsJsonArray());
                    break;
                case "no_type": {
                    result = new ObjectTermImpl(json);
                    break;
                }
                default:
                    throw new JasonException("Given type is not a valid type");
            }
        } catch (JasonException e){
            throw e;
        } catch (Exception e){
            throw new JasonException("Wrong type was given");
        }
        return result;
    }
}
