package utils;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.InternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

/**
 * Class that wrap error management for a Default Internal Action.
 * Subclasses should implement the static create method in order to be used as singletons.
 */
public abstract class AbstractInternalAction extends DefaultInternalAction {

    protected static InternalAction singleton = null;

    @Override
    public abstract int getMaxArgs();

    @Override
    public abstract int getMinArgs();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        try {
            return action(ts, un, args);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("Too few arguments");
        } catch (ClassCastException e) {
            throw new JasonException("Parameter was the wrong type");
        }
    }

    protected abstract Object action(TransitionSystem ts, Unifier un, Term[] args) throws JasonException;
}
