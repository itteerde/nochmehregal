package de.bklaiber.inference;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.junit.Test;

import de.bklaiber.testqueries.AbstractQueryTest;
import edu.cs.ai.log4KR.logical.syntax.Atom;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.RelationalAtom;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.signature.Constant;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.signature.Predicate;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.signature.Sort;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.syntax.RelationalConditional;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.syntax.RelationalFact;

public class IsReflexiveTest extends AbstractQueryTest {

	private Sort type;
	private Sort[] argumentSorts;
	//Formula<AtomicConstraint> reflexiveConstraint = null;

	@Test
	public void testIsReflexive() {
		CanonicalMinimumGeneralization generalization = new CanonicalMinimumGeneralization();
		Collection<Atom<RelationalAtom>> atomsOfQuery = null;
		Collection<RelationalConditional> reflexiveClassConditionals = null;
		type = new Sort("letter");
		argumentSorts = new Sort[2];
		argumentSorts[0] = type;
		argumentSorts[1] = type;

		Constant a = new Constant("a", type);
		Constant b = new Constant("b", type);
		Constant c = new Constant("c", type);
		String P = "P";

		RelationalAtom atomAa = new RelationalAtom(new Predicate(P, argumentSorts), a, a);
		RelationalAtom atomAb = new RelationalAtom(new Predicate(P, argumentSorts), b, b);
		RelationalAtom atomAc = new RelationalAtom(new Predicate(P, argumentSorts), c, c);

		atomsOfQuery = new HashSet<>();
		atomsOfQuery.add(atomAa);
		atomsOfQuery.add(atomAb);
		atomsOfQuery.add(atomAc);

		RelationalConditional aA = new RelationalFact(atomAa, 0.0);
		RelationalConditional bB = new RelationalFact(atomAb, 0.0);
		RelationalConditional cC = new RelationalFact(atomAc, 0.0);

		reflexiveClassConditionals = new Vector<RelationalConditional>();
		reflexiveClassConditionals.add(aA);
		reflexiveClassConditionals.add(bB);
		reflexiveClassConditionals.add(cC);

		assertTrue(generalization.isReflexive(reflexiveClassConditionals));

	}

}
