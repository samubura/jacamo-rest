package json;

import com.google.gson.*;
import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.AbstractInternalAction;

/**
 * Use as set_field(json_obj, key, value)
 * - json_obj must be an object reference of a JsonObject or a JsonArray
 * - key must be a string or a number or a _ to append to an array ignoring the index
 * - value can be either:
 *  + an atom with the value true/false for boolean
 *  + an object reference of a JsonElement
 *  + a number
 *  + a string
 *  + a string representation of a JSON structure
 *
 * Add or set the field to the given json object
 */
public class set extends AbstractInternalAction {
    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    protected Object action(TransitionSystem ts, Unifier un, Term[] args) throws JasonException {
        if(args[1].isString()){
            this.setObjectField((ObjectTerm)args[0], (StringTerm)args[1], args[2]);
        } else if(args[1].isNumeric()){
            this.setArrayElement((ObjectTerm)args[0], (NumberTerm)args[1], args[2]);
        } else if(args[1].isUnnamedVar()) {
            this.appendArrayElement((ObjectTerm)args[0], args[2]);
        }
        return true;
    }

    private void setObjectField(ObjectTerm objTerm, StringTerm keyTerm, Term valueTerm) throws JasonException {
        try {
            JsonObject json = ((JsonElement) objTerm.getObject()).getAsJsonObject();
            String key = keyTerm.getString();
            json.add(key, generateJsonElement(valueTerm));
        } catch (IllegalStateException e){
            throw new JasonException("First argument was not a JsonObject");
        }
    }

    private void setArrayElement(ObjectTerm objTerm, NumberTerm indexTerm, Term valueTerm) throws JasonException {
        try{
            JsonArray json = ((JsonElement)objTerm.getObject()).getAsJsonArray();
            int index = (int)indexTerm.solve();
            json.set(index, generateJsonElement(valueTerm));
        } catch (IllegalStateException e){
            throw new JasonException("First argument was not a JsonArray");
        }
    }

    private void appendArrayElement(ObjectTerm objTerm, Term valueTerm) throws JasonException {
        try{
            JsonArray json = ((JsonElement)objTerm.getObject()).getAsJsonArray();
            json.add(generateJsonElement(valueTerm));
        } catch (IllegalStateException e){
            throw new JasonException("First argument was not a JsonArray");
        }
    }

    private JsonElement generateJsonElement(Term valueTerm) throws JasonException {
        JsonElement value = null;
        if(valueTerm.isAtom()){
            //either a boolean or an obj reference
            Atom term = (Atom)valueTerm;
            String functor = term.getFunctor();
            if(functor.equals("true") || functor.equals("false")) {
                //is a boolean
                value = new JsonPrimitive(Boolean.parseBoolean(functor));
            }
        }
        else if(valueTerm.isNumeric()){
            NumberTerm term = (NumberTerm)valueTerm;
            value = new JsonPrimitive(term.solve());
        }
        else if(valueTerm.isString()) {
            StringTerm term = (StringTerm) valueTerm;
            value = new JsonParser().parse(term.getString());
        }
        else {
            //TODO is there any better way to actually check if this is an object reference??
            //is an obj reference so it must be a json element
            ObjectTerm objTerm = (ObjectTerm)valueTerm;
            value = (JsonElement)objTerm.getObject();
        }
        if(value == null){
            throw new JasonException("Cannot parse value");
        }
        return value;
    }
}
