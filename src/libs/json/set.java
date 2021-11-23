package json;

import com.google.gson.*;
import jason.JasonException;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.*;
import utils.AbstractInternalAction;

/**
 * Use as set_field(json_obj, type, key, value)
 * - json_obj must be an object reference of a JsonObject or a JsonArray
 * - type must be either ["number", "integer", "string", "object", "array", "boolean"] or _ and get the default behaviour
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
        return 4;
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    protected Object action(TransitionSystem ts, Unifier un, Term[] args) throws JasonException {
        checkArguments(args);
        String type = ((StringTerm)args[1]).getString();
        if(args[2].isString()){
            this.setObjectField((ObjectTerm)args[0], (StringTerm)args[2], type, args[3]);
        } else if(args[2].isNumeric()){
            this.setArrayElement((ObjectTerm)args[0], (NumberTerm)args[2], type,  args[3]);
        } else if(args[2].isUnnamedVar()) {
            this.appendArrayElement((ObjectTerm)args[0],type, args[3]);
        }
        return true;
    }

    private void setObjectField(ObjectTerm objTerm, StringTerm keyTerm, String type, Term valueTerm) throws JasonException {
        try {
            JsonObject json = ((JsonElement) objTerm.getObject()).getAsJsonObject();
            String key = keyTerm.getString();
            json.add(key, generateJsonElement(type, valueTerm));
        } catch (IllegalStateException e){
            throw new JasonException("First argument was not a JsonObject");
        }
    }

    private void setArrayElement(ObjectTerm objTerm, NumberTerm indexTerm, String type, Term valueTerm) throws JasonException {
        try{
            JsonArray json = ((JsonElement)objTerm.getObject()).getAsJsonArray();
            int index = (int)indexTerm.solve();
            json.set(index, generateJsonElement(type, valueTerm));
        } catch (IllegalStateException e){
            throw new JasonException("First argument was not a JsonArray");
        }
    }

    private void appendArrayElement(ObjectTerm objTerm, String type, Term valueTerm) throws JasonException {
        try{
            JsonArray json = ((JsonElement)objTerm.getObject()).getAsJsonArray();
            json.add(generateJsonElement(type, valueTerm));
        } catch (IllegalStateException e){
            throw new JasonException("First argument was not a JsonArray");
        }
    }

    private JsonElement generateJsonElement(String type, Term valueTerm) throws JasonException {
        JsonElement value = null;
        if(valueTerm.isAtom()){
            //either a boolean or an obj reference
            Atom term = (Atom)valueTerm;
            String functor = term.getFunctor();
            if(type.equals("boolean")){
                //is a boolean
                value = new JsonPrimitive(Boolean.parseBoolean(functor));
            }
        }
        else if(valueTerm.isNumeric()){
            NumberTerm term = (NumberTerm)valueTerm;
            double argument = term.solve();
            if(type.equals("integer")) {
                value = new JsonPrimitive((int)argument);
            } else {
                value = new JsonPrimitive(argument);
            }

        }
        else if(valueTerm.isString()) {
            StringTerm term = (StringTerm) valueTerm;
            value = new JsonParser().parse("\""+term.getString() +"\"");
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
