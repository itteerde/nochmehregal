package de.bklaiber.printProbabilities;

import java.util.ArrayList;

import de.bklaiber.types.ProbabilityConditional;

/**
 * 
 * Used to generate the output if all conditionals have a different probability.
 * 
 * @author klaiber
 *
 */
public class PrintNoEqualProbabilities implements PrintConditionalProbabilities {

	private ArrayList<ProbabilityConditional> listOfConditionals;

	/**
	 * 
	 * Constructor of PrintNoEqualProbabilities.
	 * 
	 * @param listOfConditionals
	 */
	public PrintNoEqualProbabilities(
			ArrayList<ProbabilityConditional> listOfConditionals) {
		this.listOfConditionals = listOfConditionals;

	}// endofconstructor

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder probCond = new StringBuilder();

		for (ProbabilityConditional probabilityConditional : listOfConditionals) {

			probCond.append(probabilityConditional.toString());
			probCond.append("\n");

		}// endfor
		return probCond.toString();
	}// endoftoString
}// endofPrintNoEqualProbabilities

