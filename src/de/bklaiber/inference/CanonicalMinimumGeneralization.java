package de.bklaiber.inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import edu.cs.ai.log4KR.logical.syntax.Atom;
import edu.cs.ai.log4KR.logical.syntax.Conjunction;
import edu.cs.ai.log4KR.logical.syntax.Formula;
import edu.cs.ai.log4KR.logical.syntax.Tautology;
import edu.cs.ai.log4KR.math.types.Fraction;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.RelationalAtom;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.constraints.AtomicConstraint;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.constraints.EqualityConstraint;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.constraints.InequalityConstraint;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.signature.Constant;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.signature.Predicate;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.signature.Term;
import edu.cs.ai.log4KR.relational.classicalLogic.syntax.signature.Variable;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.syntax.RelationalConditional;
import edu.cs.ai.log4KR.relational.probabilisticConditionalLogic.syntax.RelationalFact;

//TODO test, ob generische Konditionale mit der gleichen Wahrscheinlichkeit auch zusammengefasst werden

/**
 * This class is used to generalize the classes.
 * 
 * 
 */
public class CanonicalMinimumGeneralization extends AbstractGeneralization {

	@Override
	/**
	 * Generalizes canonically all the variables with the corresponding
	 * constants (positive generalization). Sorts by size and generalizes with
	 * exclusions (negative generalization. Compares both generalizations and
	 * selects the better one. For example the shorter string representation and
	 * returns the "better" generalization.
	 * 
	 * 
	 * for example: <code>
	 * 
	 * c = flies(X)
	 * 
	 * positive generalization (flies(X))[0.6636035435403175]<((X=Sylvester + X=Kirby) + X=Bully)>);(flies(X))[3.9682291970525344E-9]<X=Tweety>)
	 * negative generalization (flies(X))[0.6636035435403175]<X!=Tweety>)(flies(X))[3.9682291970525344E-9]<X=Tweety>)
	 * 
	 * returns the negative generalization after comparison.
	 * 
	 * 
	 * </code>
	 * 
	 * @return generalization
	 */
	public Collection<RelationalConditional> generalize(RelationalConditional c,
			Collection<Collection<RelationalConditional>> classifiedClasses) {

		Collection<RelationalConditional> generalizationNegative = new ArrayList<RelationalConditional>();
		Collection<RelationalConditional> generalizationPositive = new ArrayList<RelationalConditional>();
		Collection<RelationalConditional> generalization = new ArrayList<RelationalConditional>();
		GeneralizationAsStringComperator comperator = new GeneralizationAsStringComperator();
		int comparison = 0;

		generalizationPositive = generalizePositive(c, classifiedClasses);
		generalizationNegative = generalizeNegative(c, classifiedClasses);

		comparison = comperator.compare(generalizationNegative, generalizationPositive);

		switch (comparison) {
		case 0:
			generalization = generalizationPositive;
			break;
		case 1:
			generalization = generalizationNegative;
			break;
		case 2:
			generalization = generalizationPositive;
			break;
		default:
			break;
		}

		return generalization;
	}

	/**
	 * Generalizes positive canonically. Which means an disjunction of equality
	 * constraints which are generates with each variable and its corresponding
	 * constant. The reflexive knowledge has an exclusive treatment.
	 * 
	 * for example: <code>
	 * 			
	 * c = flies(X)
	 * 
	 * (flies(X))[0.6636035435403175]&lt;((X=Sylvester + X=Kirby) + X=Bully)&gt;);(flies(Tweety))[3.9682291970525344E-9])
	 * 
	 * </code>
	 * 
	 * @param c
	 *            qualified conditional of the query
	 * @param classifiedClasses
	 *            the classified classes of the ground instance (those with
	 *            equal probability)
	 * @return generalization
	 */
	public Collection<RelationalConditional> generalizePositive(RelationalConditional c,
			Collection<Collection<RelationalConditional>> classifiedClasses) {

		Collection<Atom<RelationalAtom>> atomsOfQuery = getAtomsOfQuery(c);
		Collection<RelationalConditional> generalization = new ArrayList<RelationalConditional>();

		for (Iterator<Collection<RelationalConditional>> iterator = classifiedClasses.iterator(); iterator.hasNext();) {
			Collection<RelationalConditional> classification = iterator.next();

			Collection<Collection<Atom<RelationalAtom>>> atomsOfClass = getAtomOfClass(classification);
			Fraction probability = getProbabilitiesOfClass(classification);
			Fraction probability2 = null;
			Formula<AtomicConstraint> constraintOfClass = null;
			Formula<AtomicConstraint> constraintOfSecondClass = null;

			//if there is only one class: quantified conditional with probability = qualified conditional 
			// wenn eine Klasse dann R�ckgabe das quantifizierte Konditional mit Wahrscheinlichkeit
			if (classifiedClasses.size() == 1) {
				RelationalConditional generalizationOfClass = generateConditionalForOne(c, probability);
				generalization.add(generalizationOfClass);
			}

			//if the class only contains one conditional 
			if (classification.size() == 1) {
				RelationalConditional con = null;
				Iterator<RelationalConditional> iterator1 = classification.iterator();
				if (iterator1.hasNext()) {
					con = (RelationalConditional) iterator1.next();
				}
				RelationalConditional generalizationOfClass = generateConditionalForOne(con, probability);
				generalization.add(generalizationOfClass);
				//generalization.add(con);
				//classification = iterator.next();
			}

			//TODO hier muss getestet werden, ob es sich um ein mehrstelliges (zweistelliges) Pr�dikat handelt; bei zweistelligen Pr�dikaten gibt es immer eine relflexive Klasse, d.h. eine Klasse ist reflexiv, die andere negativ reflexiv zu generalisieren
			if (classifiedClasses.size() == 2) {
				if (isReflexive(classification)) {
					constraintOfClass = generateReflexiveConstraint(atomsOfQuery);
					constraintOfSecondClass = generateReflexiveNegativeConstraint(atomsOfQuery);
					//TODO wenn die erste Klassifikation nicht reflexiv ist, ist es die andere; der else Zweig muss modifiziert werden oder sogar entfernt, die beiden Klassen werden so behandelt, dass eine Klasse als reflexiv (Bsp. U=V) und die andere als nicht reflexiv (U<>V) behandelt wird.
				} else {
					Collection<RelationalConditional> nextClassification = null;
					if (iterator.hasNext()) {
						nextClassification = iterator.next();
					}
					Collection<Collection<Atom<RelationalAtom>>> atomsOfSecondClass = getAtomOfClass(
							nextClassification);
					//if the class only contains one conditional 
					if (nextClassification.size() == 1) {
						RelationalConditional con = null;
						Iterator<RelationalConditional> iterator1 = classification.iterator();
						if (iterator1.hasNext()) {
							con = (RelationalConditional) iterator1.next();
						}
						RelationalConditional generalizationOfClass = generateConditionalForOne(con, probability);
						generalization.add(generalizationOfClass);
						generalization.add(con);
						if (iterator.hasNext()) {
							classification = iterator.next();
						}
					}

					//if both of the classes aren�t reflexive
					//bei zweistelligen P�dikaten Klassen muss immer eine refelxiv sein, der Fall, dass beide nicht refleixv sind, kann da nicht eintreten
					if (!isReflexive(classification) && !isReflexive(nextClassification)) {
						constraintOfClass = generatePositiveConstraint(atomsOfClass, atomsOfQuery);
						constraintOfSecondClass = generatePositiveConstraint(atomsOfSecondClass, atomsOfQuery);
						probability2 = getProbabilitiesOfClass(nextClassification);
					} else {
						if (isReflexive(nextClassification)) {
							probability2 = getProbabilitiesOfClass(nextClassification);
							constraintOfSecondClass = generateReflexiveConstraint(atomsOfQuery);
							constraintOfClass = generateReflexiveNegativeConstraint(atomsOfQuery);
						}
					}
				}

			}
			if (classifiedClasses.size() > 2) {
				if (isReflexive(classification)) {
					constraintOfClass = generateReflexiveConstraint(atomsOfQuery);

				} else {
					constraintOfClass = generatePositiveConstraint(atomsOfClass, atomsOfQuery);
				}
			}

			//dies gilt fuer zweistellige Pr�dikate, f�r einstellige muss das untere gelten

			if (classifiedClasses.size() > 1) {

				Formula<RelationalAtom> con = c.getConsequence();
				Predicate pred = ((RelationalAtom) con).getPredicate();

				if (pred.getArity() > 1) {
					RelationalConditional generalizationOfClass = generateConditional(c, constraintOfClass,
							probability);

					generalization.add(generalizationOfClass);
				}
				if (constraintOfSecondClass != null) {
					RelationalConditional generalizationOfSecondClass = generateConditional(c, constraintOfSecondClass,
							probability2);
					generalization.add(generalizationOfSecondClass);
				}
			}

		}

		return generalization;

	}

	/**
	 * In this method the classes are sorted by size, biggest class first. In
	 * the biggest class there are inequality constraints for all of the
	 * elements of the other classes, when the number of the elements of the
	 * other classes is smaller than the size of the biggest class. For the
	 * elements of the smaller classes there will be equality constraints.
	 * 
	 * for example: <code>
	 * 
	 * c = flies(X)
	 * 
	 *(flies(X))[0.6636035435403175]<X!=Tweety>)(flies(X))[3.9682291970525344E-9]<X=Tweety>)
	 * 
	 * </code>
	 * 
	 * @param c
	 *            the conditional of the query
	 * @param classifiedClasses
	 *            ground instances of the query classified by equal probability
	 * @return generalization
	 */
	public Collection<RelationalConditional> generalizeNegative(RelationalConditional c,
			Collection<Collection<RelationalConditional>> classifiedClasses) {
		//TODO funktioniert nur f�r den Fall einstelliger Pr�dikate; f�r den Fall mehrstelliger Pr�dikate erg�nzen
		//TODO ordentlich umschreiben
		ArrayList<Collection<RelationalConditional>> classifiedClassesList = new ArrayList<>(classifiedClasses);
		Collection<Atom<RelationalAtom>> atomsOfQuery = getAtomsOfQuery(c);
		Collection<RelationalConditional> generalization = new ArrayList<RelationalConditional>();

		Collection<Collection<Atom<RelationalAtom>>> atomsOfClass = new ArrayList<Collection<Atom<RelationalAtom>>>();
		Collection<Collection<Atom<RelationalAtom>>> atomsOfBiggestClass = new ArrayList<Collection<Atom<RelationalAtom>>>();
		Collection<Collection<Atom<RelationalAtom>>> atomsOfTheNextBiggestClass = new ArrayList<Collection<Atom<RelationalAtom>>>();

		RelationalConditional generalizationOfClass = null;

		boolean oneConditional = false;

		Comparator<Collection> compareSize = new Comparator<Collection>() {

			@Override
			public int compare(Collection o1, Collection o2) {
				Integer size1 = o1.size();
				Integer size2 = o2.size();
				return size2.compareTo(size1);
			}

		};

		//sorts the classes by size, descendant
		Collections.sort(classifiedClassesList, compareSize);
		Iterator<Collection<RelationalConditional>> iterator = classifiedClassesList.iterator();
		Collection<RelationalConditional> biggestClass = iterator.next();
		Fraction probability = getProbabilitiesOfClass(biggestClass);

		//if the size of the biggest class smaller than the sum of the sizes of the other classes than: negative constraint for biggest class with all elements of the other classes
		//else positive constraint 
		//for the smaller classes positive constraints

		int numberOfElements = 0;

		while (iterator.hasNext()) {
			Collection<RelationalConditional> classification = iterator.next();

			if (!isReflexive(classification)) {
				numberOfElements = numberOfElements + classification.size();
			}
			if (classification.size() == 1) {
				oneConditional = true;
			}
		}

		Collections.sort(classifiedClassesList, compareSize);
		iterator = classifiedClassesList.iterator();
		biggestClass = iterator.next();

		//if the biggest and first class is not reflexive and the probability isn�t impossible to calculate f.e. because there is a division by zero
		//and the number of elements in the smaller classes is smaller than the size of the biggest class then get all the elements of the smaller
		//classes and create an conjunction of Inequality Constraints using the constants of the conditionals of the smaller classes 
		if (!isReflexive(biggestClass) && !isImpossible(biggestClass) && numberOfElements < biggestClass.size()) {

			Formula<AtomicConstraint> reflexiveconstraintOfBiggestClass = null;
			//get the atoms to generate the negative constraint for the biggest class, using the atoms of all the other classes 
			while (iterator.hasNext()) {
				Collection<RelationalConditional> classification = iterator.next();
				if (!isReflexive(classification))
					atomsOfClass.addAll(getAtomOfClass(classification));
				if (isReflexive(classification)) {
					reflexiveconstraintOfBiggestClass = generateReflexiveConstraint(atomsOfQuery);
				}
			}

			Formula<AtomicConstraint> constraintOfBiggestClass = null;

			if (!atomsOfClass.isEmpty()) {
				constraintOfBiggestClass = generateNegativeConstraint(atomsOfClass, atomsOfQuery);
				if (reflexiveconstraintOfBiggestClass != null) {
					constraintOfBiggestClass = constraintOfBiggestClass.and(reflexiveconstraintOfBiggestClass);
				}
			} else {
				constraintOfBiggestClass = generateReflexiveNegativeConstraint(atomsOfQuery);
			}

			if (!oneConditional) {
				generalizationOfClass = generateConditional(c, constraintOfBiggestClass, probability);
				generalization.add(generalizationOfClass);
			}

			//look ad the second class of the list
			iterator = classifiedClassesList.iterator();
			Collection<RelationalConditional> classOne = iterator.next();
			while (iterator.hasNext()) {
				Collection<RelationalConditional> classification = iterator.next();
				probability = getProbabilitiesOfClass(classification);

				Fraction probabilityOne = getProbabilitiesOfClass(biggestClass);
				//if there is olny one class: quantified conditional with probability = qualified conditional 
				// wenn eine Klasse dann R�ckgabe das quantifizierte Konditional mit Wahrscheinlichkeit
				if (classifiedClasses.size() == 1) {
					RelationalConditional generalizationOfClassOne = generateConditionalForOne(c, probability);
					generalization.add(generalizationOfClassOne);
				}

				//if the class only contains one conditional 
				if (classification.size() == 1) {
					oneConditional = true;
					Iterator<RelationalConditional> iterator1 = classification.iterator();
					iterator.hasNext();
					RelationalConditional con = (RelationalConditional) iterator1.next();
					if (con.getAntecedence() instanceof Tautology<?>) {
						con = new RelationalFact(con.getConsequence());
					}
					RelationalConditional generalizationOfClassOneCond = generateConditionalForOne(con, probability);
					generalization.add(generalizationOfClassOneCond);
				}

				Formula<AtomicConstraint> constraintOfClass = null;
				if (isReflexive(classification)) {
					constraintOfClass = generateReflexiveConstraint(atomsOfQuery);
				} else {
					constraintOfClass = generatePositiveConstraint(atomsOfClass, atomsOfQuery);
				}

				if (!oneConditional) {
					generalizationOfClass = generateConditional(c, constraintOfClass, probability);
					generalization.add(generalizationOfClass);
				}
			}
		}

		//if the biggest class is reflexive, there will be no more reflexive classes
		//generate a reflexive conditional for the biggest class 
		if (isReflexive(biggestClass) && classifiedClassesList.size() > 2) {

			atomsOfBiggestClass.addAll(getAtomOfClass(biggestClass));
			Formula<AtomicConstraint> constraintOfClass = null;
			constraintOfClass = generateReflexiveConstraint(atomsOfQuery);
			generalizationOfClass = generateConditional(c, constraintOfClass, probability);
			generalization.add(generalizationOfClass);

			//test if the next biggest class has more elements than the other classes and the generate a negative constraint with
			//the elements of the other classes, generate a positive constraint for each other class
			Collection<RelationalConditional> nextBiggestClass = iterator.next();
			atomsOfTheNextBiggestClass.addAll(getAtomOfClass(nextBiggestClass));

			numberOfElements = 0;
			while (iterator.hasNext()) {
				Collection<RelationalConditional> classification = iterator.next();
				numberOfElements = numberOfElements + classification.size();
			}
			atomsOfClass = new ArrayList<Collection<Atom<RelationalAtom>>>();
			if (numberOfElements < nextBiggestClass.size()) {

				while (iterator.hasNext()) {
					Collection<RelationalConditional> classification = iterator.next();
					if (!isReflexive(classification) && !isImpossible(classification)) {
						atomsOfClass.addAll(getAtomOfClass(classification));

					}

				}

				Formula<AtomicConstraint> constraintOfNextBiggestClass = null;
				constraintOfNextBiggestClass = generateNegativeConstraint(atomsOfClass, atomsOfQuery);

				generalizationOfClass = generateConditional(c, constraintOfNextBiggestClass, probability);
				generalization.add(generalizationOfClass);

				while (iterator.hasNext()) {
					Collection<RelationalConditional> classification = iterator.next();
					atomsOfClass = new ArrayList<Collection<Atom<RelationalAtom>>>();
					atomsOfClass.addAll(getAtomOfClass(classification));

					//Formula<AtomicConstraint> constraintOfClass = null;
					if (isReflexive(classification)) {
						constraintOfClass = generateReflexiveConstraint(atomsOfQuery);
					} else {
						constraintOfClass = generatePositiveConstraint(atomsOfClass, atomsOfQuery);
					}

					generalizationOfClass = generateConditional(c, constraintOfClass, probability);
					generalization.add(generalizationOfClass);
				}

			}
		}

		if (numberOfElements == biggestClass.size()) {
			generalizePositive(c, classifiedClasses);
		}
		return generalization;
	}

	public Formula<AtomicConstraint> generateReflexiveConstraint(Collection<Atom<RelationalAtom>> atomsOfQuery) {

		Collection<Formula<AtomicConstraint>> argsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		ArrayList<Formula<AtomicConstraint>> elementsOfConstraintsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		Formula<AtomicConstraint> constraint = null;

		for (Iterator<Atom<RelationalAtom>> iterator = atomsOfQuery.iterator(); iterator.hasNext();) {
			Atom<RelationalAtom> atom = (Atom<RelationalAtom>) iterator.next();

			Term[] argsOfQueryAtom = ((RelationalAtom) atom).getArguments();
			elementsOfConstraintsOfClass = generateElementsOfReflexiveConstraint(argsOfQueryAtom);
			argsOfClass.addAll(elementsOfConstraintsOfClass);
			constraint = generateDisjunctionConstraint(argsOfClass);

		}
		return constraint;

	}

	public Formula<AtomicConstraint> generateReflexiveNegativeConstraint(
			Collection<Atom<RelationalAtom>> atomsOfQuery) {

		Collection<Formula<AtomicConstraint>> argsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		ArrayList<Formula<AtomicConstraint>> elementsOfConstraintsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		Formula<AtomicConstraint> constraint = null;

		for (Iterator<Atom<RelationalAtom>> iterator = atomsOfQuery.iterator(); iterator.hasNext();) {
			Atom<RelationalAtom> atom = (Atom<RelationalAtom>) iterator.next();

			Term[] argsOfQueryAtom = ((RelationalAtom) atom).getArguments();
			elementsOfConstraintsOfClass = generateElementsOfReflexiveNegativeConstraint(argsOfQueryAtom);
			argsOfClass.addAll(elementsOfConstraintsOfClass);
			constraint = generateDisjunctionConstraint(argsOfClass);

		}
		return constraint;

	}

	/*
	 * 
	 * @param atomsOfClass
	 * @param atomsOfQuery
	 * @return constraint
	 */
	private Formula<AtomicConstraint> generatePositiveConstraint(
			Collection<Collection<Atom<RelationalAtom>>> atomsOfClass, Collection<Atom<RelationalAtom>> atomsOfQuery) {

		Collection<Formula<AtomicConstraint>> argsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		ArrayList<Formula<AtomicConstraint>> elementsOfConstraintsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		Formula<AtomicConstraint> constraint = null;
		Formula<AtomicConstraint> constraintTemp = null;
		boolean predicateMore = false;

		//get the arguments for each conditional
		//take each collection for the conditional
		for (Iterator<Collection<Atom<RelationalAtom>>> atoms = atomsOfClass.iterator(); atoms.hasNext();) {
			Collection<Atom<RelationalAtom>> atomOfCondtional = atoms.next();
			//take each atom of each conditional
			for (Iterator<Atom<RelationalAtom>> element = atomOfCondtional.iterator(); element.hasNext();) {
				Atom<RelationalAtom> atom = (Atom<RelationalAtom>) element.next();
				//get the arguments of each atom of the conditional
				for (Iterator<Atom<RelationalAtom>> iteratorOfQuery = atomsOfQuery.iterator(); iteratorOfQuery
						.hasNext();) {
					Atom<RelationalAtom> atomOfQuery = (Atom<RelationalAtom>) iteratorOfQuery.next();

					if (((RelationalAtom) atom).getPredicate().equals(((RelationalAtom) atomOfQuery).getPredicate())) {

						Term[] argsOfQueryAtom = ((RelationalAtom) atomOfQuery).getArguments();
						Term[] argsOfConditional = ((RelationalAtom) atom).getArguments();
						elementsOfConstraintsOfClass = generateElementsOfConstraint(argsOfConditional, argsOfQueryAtom);
						argsOfClass.addAll(elementsOfConstraintsOfClass);
						if (argsOfClass.size() > 1) {
							constraintTemp = generateDisjunctionConstraint(argsOfClass);
						}
					}

					if (((RelationalAtom) atom).getPredicate().getArity() > 1) {
						predicateMore = true;
					}

				}

			}

		}

		if (predicateMore) {
			Formula<AtomicConstraint> reflexiveNegativeConstraint = generateReflexiveNegativeConstraint(atomsOfQuery);
			constraint = new Conjunction<>(constraintTemp, reflexiveNegativeConstraint);
		} else {
			constraint = constraintTemp;
		}
		return constraint;
	}

	/*
	 * Creates a conjunction of InequalityConstraints
	 */
	private Formula<AtomicConstraint> generateNegativeConstraint(
			Collection<Collection<Atom<RelationalAtom>>> atomsOfClass, Collection<Atom<RelationalAtom>> atomsOfQuery) {

		Collection<Formula<AtomicConstraint>> negativeArgsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		ArrayList<Formula<AtomicConstraint>> elementsOfNegativeConstraintsOfClass = new ArrayList<Formula<AtomicConstraint>>();
		Formula<AtomicConstraint> constraint = null;

		//get the arguments for each conditional
		//take each collection for the conditional
		for (Iterator<Collection<Atom<RelationalAtom>>> atoms = atomsOfClass.iterator(); atoms.hasNext();) {
			Collection<Atom<RelationalAtom>> atomOfCondtional = atoms.next();
			//take each atom of each conditional
			for (Iterator<Atom<RelationalAtom>> element = atomOfCondtional.iterator(); element.hasNext();) {
				Atom<RelationalAtom> atom = (Atom<RelationalAtom>) element.next();
				//get the arguments of each atom of the conditional
				for (Iterator<Atom<RelationalAtom>> iteratorOfQuery = atomsOfQuery.iterator(); iteratorOfQuery
						.hasNext();) {
					Atom<RelationalAtom> atomOfQuery = (Atom<RelationalAtom>) iteratorOfQuery.next();

					//the predicate of the query and the predicate of the ground instance are equal create 
					//inequalityconstraints and a conjunction of the inequalityconstraints
					if (((RelationalAtom) atom).getPredicate().equals(((RelationalAtom) atomOfQuery).getPredicate())) {

						Term[] argsOfQueryAtom = ((RelationalAtom) atomOfQuery).getArguments();
						Term[] argsOfConditional = ((RelationalAtom) atom).getArguments();

						elementsOfNegativeConstraintsOfClass = generateElementsOfNegativeConstraint(argsOfConditional,
								argsOfQueryAtom);
						negativeArgsOfClass.addAll(elementsOfNegativeConstraintsOfClass);
						constraint = generateConjunctionConstraint(negativeArgsOfClass);

					}

				}

			}

		}

		return constraint;

	}

	/*
	 * This method generates a conjunction of Constraints
	 */
	private Formula<AtomicConstraint> generateConjunctionConstraint(Collection<Formula<AtomicConstraint>> argsOfClass) {

		Formula<AtomicConstraint> constraint = null;

		Iterator<Formula<AtomicConstraint>> equiCons = argsOfClass.iterator();
		constraint = equiCons.next();
		if (!(argsOfClass.size() > 1)) {

			return constraint;

		}

		while (equiCons.hasNext()) {
			Formula<AtomicConstraint> cons = equiCons.next();
			constraint = constraint.and(cons);

		}
		return constraint;

	}

	/*
	 * Creates a disjunction of Constraints.
	 */
	private Formula<AtomicConstraint> generateDisjunctionConstraint(Collection<Formula<AtomicConstraint>> argsOfClass) {

		Formula<AtomicConstraint> constraint = null;

		Iterator<Formula<AtomicConstraint>> cons = argsOfClass.iterator();
		constraint = cons.next();
		if (!(argsOfClass.size() > 1)) {

			return constraint;

		}

		while (cons.hasNext()) {
			Formula<AtomicConstraint> elementOfConstraint = cons.next();

			constraint = constraint.or(elementOfConstraint);

		}
		return constraint;
	}

	/*
	 * Generates the elements of a constraint of a reflexive class (conjunction of equality constraints). 
	 */
	private ArrayList<Formula<AtomicConstraint>> generateElementsOfReflexiveConstraint(Term[] args) {
		HashSet<EqualityConstraint> setOfEqualityConstraints = new HashSet<EqualityConstraint>();
		Collection<Formula<AtomicConstraint>> listOfEqualityContraints = new ArrayList<Formula<AtomicConstraint>>();
		ArrayList<Formula<AtomicConstraint>> listOfConstraints = new ArrayList<Formula<AtomicConstraint>>();

		/*
		 * Creates an EqualityConstraint using the variables of the query and constants of the generated conditionals
		 * if the artity of the predicate is 1 then create an InequalityConstraint, else 
		 */

		for (int i = 0; i < args.length; i++) {
			Variable var1 = new Variable(args[i].toString(), args[i].getType());
			for (int j = i + 1; j < args.length; j++) {
				Variable var2 = new Variable(args[j].toString(), args[j].getType());

				EqualityConstraint atomicConstraint = new EqualityConstraint(var1, var2);
				setOfEqualityConstraints.add(atomicConstraint);
			}
		}
		listOfEqualityContraints.addAll(setOfEqualityConstraints);

		if (listOfEqualityContraints.size() > 1) {
			Formula<AtomicConstraint> conjunction = generateConjunctionConstraint(listOfEqualityContraints);
			listOfConstraints.add(conjunction);
		} else
			listOfConstraints = (ArrayList<Formula<AtomicConstraint>>) listOfEqualityContraints;

		return listOfConstraints;

	}

	/*
	 * Generates a constraint of a not reflexive class (conjunction of inequality constraints) if there is a reflexive class.
	 */
	private ArrayList<Formula<AtomicConstraint>> generateElementsOfReflexiveNegativeConstraint(Term[] args) {
		HashSet<InequalityConstraint> setOfEqualityConstraints = new HashSet<InequalityConstraint>();
		Collection<Formula<AtomicConstraint>> listOfEqualityContraints = new ArrayList<Formula<AtomicConstraint>>();
		ArrayList<Formula<AtomicConstraint>> listOfConstraints = new ArrayList<Formula<AtomicConstraint>>();

		/*
		 * Creates an InequalityConstraint using the variables of the query and constants of the generated conditionals
		 * if the artity of the predicate is 1 then create an InequalityConstraint, else 
		 */

		for (int i = 0; i < args.length; i++) {
			Variable var1 = new Variable(args[i].toString(), args[i].getType());
			for (int j = i + 1; j < args.length; j++) {
				Variable var2 = new Variable(args[j].toString(), args[j].getType());

				InequalityConstraint atomicConstraint = new InequalityConstraint(var1, var2);
				setOfEqualityConstraints.add(atomicConstraint);
			}
		}
		listOfEqualityContraints.addAll(setOfEqualityConstraints);

		if (listOfEqualityContraints.size() > 1) {
			Formula<AtomicConstraint> conjunction = generateConjunctionConstraint(listOfEqualityContraints);
			listOfConstraints.add(conjunction);
		} else
			listOfConstraints = (ArrayList<Formula<AtomicConstraint>>) listOfEqualityContraints;

		return listOfConstraints;
	}

	//TODO nullstelliges Pr�dikat abfangen???

	/*
	 * Generates the elements of a negative constraint. (Conjunction of inequality constraints).
	 */
	private ArrayList<Formula<AtomicConstraint>> generateElementsOfNegativeConstraint(Term[] argsOfCond,
			Term[] argsOfQueryAtom) {
		ArrayList<Formula<AtomicConstraint>> listOfConstraints = new ArrayList<Formula<AtomicConstraint>>();
		Collection<Formula<AtomicConstraint>> listOfInequalityConstraints = new ArrayList<Formula<AtomicConstraint>>();

		/*
		 * Creates an InequalityConstraint using the variables of the query and constants of the generated conditionals
		 * if the artity of the predicate is 1 then create an InequalityConstraint, else 
		 */

		for (int i = 0; i < argsOfCond.length; i++) {
			for (int j = 0; j < argsOfQueryAtom.length; j++) {

				Variable var = new Variable(argsOfQueryAtom[j].toString(), argsOfQueryAtom[j].getType());
				Constant cons = new Constant(argsOfCond[i].toString(), argsOfCond[i].getType());
				if (argsOfCond.length == 1) {
					InequalityConstraint atomicConstraint = new InequalityConstraint(var, cons);
					listOfConstraints.add(atomicConstraint);

				}
				if (argsOfCond.length > 1) {
					InequalityConstraint atomicConstraint = new InequalityConstraint(var, cons);
					listOfInequalityConstraints.add(atomicConstraint);
				}
			}

		}
		for (int i = 0; i < listOfInequalityConstraints.size(); i++) {
			Formula<AtomicConstraint> conjunction = generateConjunctionConstraint(listOfInequalityConstraints);
			listOfConstraints.add(conjunction);
		}

		return listOfConstraints;
	}

	/*
	 * @param classification
	 * @return the average probability of the class
	 */
	private Fraction getProbabilitiesOfClass(Collection<RelationalConditional> classification) {

		//The following section contains the correct handling of the probabilities of class, but Log4KR is not able to compute it correctly so the heuristic here used is to return the probabilty of the first conditional of the class

		Fraction sum = new Fraction(0);

		for (Iterator<RelationalConditional> condOfClass = classification.iterator(); condOfClass.hasNext();) {
			RelationalConditional relationalConditional = (RelationalConditional) condOfClass.next();

			Fraction probabilityOfCond = relationalConditional.getProbability();
			sum = Fraction.addition(sum, probabilityOfCond);

		}

		return Fraction.division(sum, new Fraction(classification.size()));

		//return classification.iterator().next().getProbability();
	}

	/*
	 * Creates an EqualityConstraint using the variables of the query and constants of the generated conditionals.
	 */
	private ArrayList<Formula<AtomicConstraint>> generateElementsOfConstraint(Term[] argsOfCond,
			Term[] argsOfQueryAtom) {

		ArrayList<Formula<AtomicConstraint>> listOfConstraints = new ArrayList<Formula<AtomicConstraint>>();
		Collection<Formula<AtomicConstraint>> listOfEqualityConstraints = new ArrayList<Formula<AtomicConstraint>>();

		for (int i = 0; i < argsOfCond.length; i++) {

			Variable var = new Variable(argsOfQueryAtom[i].toString(), argsOfQueryAtom[i].getType());
			Constant cons = new Constant(argsOfCond[i].toString(), argsOfCond[i].getType());
			EqualityConstraint equalConstraint = new EqualityConstraint(var, cons);
			listOfEqualityConstraints.add(equalConstraint);

		}

		Formula<AtomicConstraint> conjunction = generateConjunctionConstraint(listOfEqualityConstraints);
		listOfConstraints.add(conjunction);

		//generate the negative reflexive Constraint

		return listOfConstraints;
	}

	/*
	 * Generates a conditional out of the relationalConditonal of the query, the generated constraint and 
	 * the probability.
	 * 
	 * @param constraintOfClass
	 * @param consequence
	 * @param antecedence
	 * @param probability
	 * @return
	 */
	private RelationalConditional generateConditional(RelationalConditional c,
			Formula<AtomicConstraint> constraintOfClass, Fraction probability) {

		Formula<RelationalAtom> consequence = c.getConsequence();

		if (c instanceof RelationalFact) {

			RelationalFact conditionalOfClass = null;

			if (constraintOfClass != null) {
				conditionalOfClass = new RelationalFact(consequence, probability, constraintOfClass);
			} else {
				conditionalOfClass = new RelationalFact(consequence, probability);
			}
			return conditionalOfClass;
		} else {
			RelationalConditional conditionalOfClass = null;
			Formula<RelationalAtom> antecedence = c.getAntecedence();
			conditionalOfClass = new RelationalConditional(consequence, antecedence, probability, constraintOfClass);
			return conditionalOfClass;
		}

	} //end

	private RelationalConditional generateConditionalForOne(RelationalConditional c, Fraction probability) {

		Formula<RelationalAtom> consequence = c.getConsequence();

		if (c instanceof RelationalFact) {
			RelationalFact conditionalOfClass = null;
			if (c.getConstraint() != null) {
				conditionalOfClass = new RelationalFact(consequence, probability, c.getConstraint());
			} else {
				conditionalOfClass = new RelationalFact(consequence, probability);
			}
			return conditionalOfClass;

		} else {
			Formula<RelationalAtom> antecedence = c.getAntecedence();
			RelationalConditional conditionalOfClass = null;
			if (antecedence instanceof Tautology<?>) {
				if (c.getConstraint() != null) {
					conditionalOfClass = new RelationalFact(consequence, probability, c.getConstraint());
				} else {
					conditionalOfClass = new RelationalFact(consequence, probability);
				}
			} else {
				conditionalOfClass = new RelationalConditional(consequence, antecedence, probability);
			}
			return conditionalOfClass;

		} //end else

	}

	/*
	 * Test if a class is reflexive (i.e. likes(a, a), likes (b, b), likes (c, c))
	 */
	public boolean isReflexive(Collection<RelationalConditional> classifiedClass) {

		boolean isreflexive = true;

		//if for each Relational Conditionals the constants are equal and the computed probability= 0.0 then the class is reflexive
		//for instance likes(a,a), likes(b,b), likes(c,c)

		//for each of the conditionals of the class get the atoms 
		//for instance likes(a,a), likes(b,b), likes(c,c)
		for (Iterator<RelationalConditional> iterator = classifiedClass.iterator(); iterator.hasNext();) {
			RelationalConditional relationalConditional = (RelationalConditional) iterator.next();
			Fraction prob = relationalConditional.getProbability();
			Double dprob = prob.toFloatingPoint();
			Collection<Atom<RelationalAtom>> atoms = new ArrayList<Atom<RelationalAtom>>();
			atoms = relationalConditional.getAtoms();
			//for each atom get the terms
			for (Iterator<Atom<RelationalAtom>> iterator2 = atoms.iterator(); iterator2.hasNext();) {
				Atom<RelationalAtom> atom = (Atom<RelationalAtom>) iterator2.next();
				Term[] arguments = ((RelationalAtom) atom).getArguments();
				if (arguments.length == 1) {
					isreflexive = false;
					break;
				}
				for (int i = 0; i < arguments.length - 1; i++) {

					if (!(arguments[0].equals(arguments[i + 1]))) {

						isreflexive = false;

					}

				}
				if (dprob != 0.0 && isreflexive) {
					isreflexive = false;
				}

			}
		}

		/*if ((getProbabilitiesOfClass(classifiedClass)).toFloatingPoint() == 0.0)
			return true;
		else
			return false;
		*/

		return isreflexive;
	}

	/*
	 * Tests if a class has the probability -1, which means the probability can not be computed becaus the marginalisation requires a division by zero.
	 */
	private boolean isImpossible(Collection<RelationalConditional> classifiedClass) {
		if ((getProbabilitiesOfClass(classifiedClass)).toFloatingPoint() == -1)

			return true;
		else
			return false;

	}

	/*
	 * Get the atoms for each conditional of a class.
	 * 
	 * @param classifiedClasses
	 * @return
	 */
	private Collection<Collection<Atom<RelationalAtom>>> getAtomOfClass(
			Collection<RelationalConditional> classifiedClasses) {

		//it has to be a collection of collections of atoms because to create the constraint each variable of the query has to compared 
		//with its grounded instance for each conditional of the class
		Collection<Collection<Atom<RelationalAtom>>> atomsOfClass = new ArrayList<Collection<Atom<RelationalAtom>>>();

		//take each conditional in the equivalence class, get the relational atoms of the class
		for (Iterator<RelationalConditional> element = classifiedClasses.iterator(); element.hasNext();) {
			RelationalConditional conditional = (RelationalConditional) element.next();
			Collection<Atom<RelationalAtom>> atomsOfElement = new ArrayList<Atom<RelationalAtom>>();

			//if the relational conditional is a fact, look at the consequence, else look at the antecedence
			if (conditional.getAntecedence() instanceof Tautology<?>) {

				atomsOfElement = conditional.getConsequence().getAtoms();
				atomsOfClass.add(atomsOfElement);

			} else {

				atomsOfElement = conditional.getAntecedence().getAtoms();
				atomsOfClass.add(atomsOfElement);
			}
		}

		return atomsOfClass;
	}

	/*
	 * Returns the atoms of the conditional of the query.
	 * 
	 * @param c
	 * @return atomsOfConditional
	 */
	private Collection<Atom<RelationalAtom>> getAtomsOfQuery(RelationalConditional c) {

		Collection<Atom<RelationalAtom>> atomsOfConditional = new ArrayList<Atom<RelationalAtom>>();

		if (c instanceof RelationalFact) {

			atomsOfConditional = c.getConsequence().getAtoms();

		} else {

			atomsOfConditional = c.getAntecedence().getAtoms();

		} //end else

		return atomsOfConditional;
	}

	@Override
	public void addGeneralization() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeGeneralization() {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Generalization> getChildGeneralizations() {
		// TODO Auto-generated method stub
		return null;
	}

}//end generalize
