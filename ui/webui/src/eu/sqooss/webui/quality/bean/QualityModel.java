/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by Stefanos Skalistis <sskalistis@gmail.com>
 * 											 <sskalist@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package eu.sqooss.webui.quality.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import eu.sqooss.webui.quality.bean.Criterion;
import eu.sqooss.webui.quality.bean.NumericCriterionElement;

public class QualityModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7208570691574743232L;

	private Criterion rootCriterion;

	private HashMap<String, Criterion> criteriaMap;

	private HashMap<String, Boolean> criteriaInitializationMap;
	
	private boolean initialized;

	private boolean objectOriented;

	public QualityModel() {
		initialize(false);
	}

	public void initialize(boolean objectOriented) {
		this.objectOriented = objectOriented;

		// Construct the Quality Model Tree (up side down) with the default
		// values as defined in D7
		// TODO: Must be removed When it's complete and all the
		
		// Root criterion
		Criterion SQOOSSQualityCharacteristics;

		// SQOOSSQualityCharacteristics subcriteria
		Criterion productQuality;
		Criterion communityQuality;

		// Product (Code) Quality subcriteria
		Criterion maintainability;
		Criterion reliability;
		Criterion security;

		// Maintainabilty subcriteria
		Criterion analyzability = null;
		Criterion changeability = null;
		Criterion stability = null;
		Criterion testability = null;

		// Security subcriteria
		Criterion undefinedValues;
		Criterion nullDeferences;

		// Reliability subcriteria
		Criterion maturity;
		Criterion effectiveness;

		// Maturity subcriteria
		Criterion numberOfOpenCriticalBugsInTheLastSixMonths;
		Criterion numberOfOpenBugsInTheLastSixMonths;

		// Effectiveness subcriteria
		Criterion numberOfCriticalBugsFixedInTheLastSixMonths;
		Criterion numberOfBugsFixedInTheLastSixMonths;
		Criterion averageTimeToFixABug;

		// Community Quality
		Criterion mailingListQuality;
		Criterion documentationQuality;

		// Mailing list subcriteria
		Criterion numberOfUniqueSubscribers;
		Criterion numberOfMessagesInUserOrSupportListPerMonth;
		Criterion numberOfMessagesInDeveloperListPerMonth;
		Criterion averageThreadDepth;
		Criterion involvement;

		// Documentation subcireteria
		Criterion availiableDocumentationDocuments;
		Criterion updateFrequency;

		criteriaMap = new HashMap<String, Criterion>();
		criteriaInitializationMap = new HashMap<String,Boolean>();
		// Constructing leaves for OO Product
		if (isObjectOriented()) {

		} else {
			// Constructing leaves for non-OO Product
			// Analyzability SubCriteria
			Criterion cyclomaticNumber = new NumericCriterionElement(
					"Cyclomatic number", 1.0 / 4.0, 
					CriterionScale.LessIsBetter, Arrays.asList(new Double[] {
							Double.POSITIVE_INFINITY, 8.0, 6.0, 4.0 }));
			Criterion numberOfStatements = new NumericCriterionElement(
					"Number of statements", 1.0 / 4.0, 
					CriterionScale.LessIsBetter, Arrays.asList(new Double[] {
							Double.POSITIVE_INFINITY, 50.0, 25.0, 10.0 }));
			Criterion commentsFrequency = new NumericCriterionElement(
					"Comments frequency", 1.0 / 4.0, 
					CriterionScale.MoreIsBetter, Arrays.asList(new Double[] {
							Double.NEGATIVE_INFINITY, 0.1, 0.3, 0.5 }));
			Criterion averageSizeOfStatements = new NumericCriterionElement(
					"Average size of statements", 1.0 / 4.0,
					 CriterionScale.LessIsBetter, Arrays
							.asList(new Double[] { Double.POSITIVE_INFINITY,
									10.0, 7.0, 4.0 }));

			criteriaMap.put(cyclomaticNumber.getName(), cyclomaticNumber);
			criteriaMap.put(numberOfStatements.getName(), numberOfStatements);
			criteriaMap.put(commentsFrequency.getName(), commentsFrequency);
			criteriaMap.put(averageSizeOfStatements.getName(),
					averageSizeOfStatements);

			// Analyzability ( Maintainability subcriterion)
			analyzability = new ComposedCriterion("Analyzability", 1.0 / 4.0,
					Arrays.asList(new Criterion[] { cyclomaticNumber,
							numberOfStatements, commentsFrequency,
							averageSizeOfStatements }));
			criteriaMap.put(analyzability.getName(), analyzability);

			// Changeability SubCriteria
			Criterion vocabularyFrequency = new NumericCriterionElement(
					"Vocabulary frequency", 1.0 / 4.0, 
					CriterionScale.LessIsBetter, Arrays.asList(new Double[] {
							Double.POSITIVE_INFINITY, 4.0, 3.0, 2.0 }));
			Criterion numberOfUnconditionalJumps = new NumericCriterionElement(
					"Number of unconditional jumps", 1.0 / 4.0,
					 CriterionScale.LessIsBetter, Arrays
							.asList(new Double[] { Double.POSITIVE_INFINITY,
									1.0, 0.0, 0.0 }));
			Criterion numberOfNestedLevels = new NumericCriterionElement(
					"Number of nested levels", 1.0 / 4.0, 
					CriterionScale.LessIsBetter, Arrays.asList(new Double[] {
							Double.POSITIVE_INFINITY, 5.0, 3.0, 1.0 }));
			// the averageSizeOfStatements is already defined

			criteriaMap.put(vocabularyFrequency.getName(), vocabularyFrequency);
			criteriaMap.put(numberOfNestedLevels.getName(),
					numberOfNestedLevels);
			criteriaMap.put(numberOfUnconditionalJumps.getName(),
					numberOfUnconditionalJumps);

			// Changeability ( Maintainability subcriterion)
			changeability = new ComposedCriterion("Changeability", 1.0 / 4.0,
					Arrays.asList(new Criterion[] { averageSizeOfStatements,
							vocabularyFrequency, numberOfUnconditionalJumps,
							numberOfNestedLevels }));
			criteriaMap.put(changeability.getName(), changeability);

			// Stability SubCreteria
			Criterion numberOfEntryNodes = new NumericCriterionElement(
					"Number of entry nodes", 1.0 / 4.0, 
					CriterionScale.LessIsBetter, Arrays.asList(new Double[] {
							Double.POSITIVE_INFINITY, 3.0, 2.0, 1.0 }));
			Criterion numberOfExitNodes = new NumericCriterionElement(
					"Number of exit nodes", 1.0 / 4.0, 
					CriterionScale.LessIsBetter, Arrays.asList(new Double[] {
							Double.NaN, 1.0, 1.0, 1.0 }));
			Criterion directlyCalledComponents = new NumericCriterionElement(
					"Directly called components", 1.0 / 4.0,
					 CriterionScale.LessIsBetter, Arrays
							.asList(new Double[] { Double.POSITIVE_INFINITY,
									7.0, 5.0, 2.0 }));
			// the numberOfUnconditionalJumps is already defined

			criteriaMap.put(numberOfEntryNodes.getName(), numberOfEntryNodes);
			criteriaMap.put(numberOfExitNodes.getName(), numberOfExitNodes);
			criteriaMap.put(directlyCalledComponents.getName(),
					directlyCalledComponents);

			// Stability ( Maintainability subcriterion)
			stability = new ComposedCriterion("Stability", 1.0 / 4.0, Arrays
					.asList(new Criterion[] { numberOfUnconditionalJumps,
							numberOfEntryNodes, numberOfExitNodes,
							directlyCalledComponents }));
			criteriaMap.put(stability.getName(), stability);

			// Testability SubCreteria
			Criterion numberOfExitsOfConditionalStructs = new NumericCriterionElement(
					"Number of exits of conditional structs", 1.0 / 4.0,
					 CriterionScale.LessIsBetter, Arrays
							.asList(new Double[] { Double.POSITIVE_INFINITY,
									4.0, 1.0, 0.0 }));
			// the cyclomaticNumber is already defined
			// the numberOfNestedLevels is already defined
			// the numberOfUnconditionalJumps is already defined
			criteriaMap.put(numberOfExitsOfConditionalStructs.getName(),
					numberOfExitsOfConditionalStructs);

			// Testability ( Maintainability subcriterion)
			testability = new ComposedCriterion("Testability", 1.0 / 4.0,
					Arrays.asList(new Criterion[] {
							numberOfExitsOfConditionalStructs,
							cyclomaticNumber, numberOfNestedLevels,
							numberOfUnconditionalJumps }));
			criteriaMap.put(testability.getName(), testability);

		}

		// Maintainability ( Product Quality subcriterion )
		maintainability = new ComposedCriterion("Maintainability", 1.0 / 3.0,
				Arrays.asList(new Criterion[] { analyzability, changeability,
						stability, testability }));
		criteriaMap.put(maintainability.getName(), maintainability);

		// Maturity SubCriteria
		numberOfOpenCriticalBugsInTheLastSixMonths = new NumericCriterionElement(
				"Number of open critical bugs in the last six months",
				1.0 / 2.0,  CriterionScale.LessIsBetter,
				Arrays.asList(new Double[] { Double.POSITIVE_INFINITY, 16.0,
						2.0, 1.0 }));
		numberOfOpenBugsInTheLastSixMonths = new NumericCriterionElement(
				"Number of open bugs in the last six months",
				1.0 / 2.0,  CriterionScale.LessIsBetter,
				Arrays.asList(new Double[] { Double.POSITIVE_INFINITY, 500.0,
						50.0, 20.0 }));

		criteriaMap.put(numberOfOpenCriticalBugsInTheLastSixMonths.getName(),
				numberOfOpenCriticalBugsInTheLastSixMonths);
		criteriaMap.put(numberOfOpenBugsInTheLastSixMonths.getName(),
				numberOfOpenBugsInTheLastSixMonths);

		// Maturity ( Reliability subcriterion )
		maturity = new ComposedCriterion("Maturity", 1.0 / 2.0, Arrays
				.asList(new Criterion[] {
						numberOfOpenCriticalBugsInTheLastSixMonths,
						numberOfOpenBugsInTheLastSixMonths }));
		criteriaMap.put(maturity.getName(), maturity);

		// Effectiveness SubCriteria
		numberOfCriticalBugsFixedInTheLastSixMonths = new NumericCriterionElement(
				"Number of critical bugs fixed in the last six months",
				1.0 / 3.0,  CriterionScale.MoreIsBetter,
				Arrays.asList(new Double[] { Double.NEGATIVE_INFINITY, 0.33,
						0.66, 0.9 }));
		numberOfBugsFixedInTheLastSixMonths = new NumericCriterionElement(
				"Number of bugs fixed in the last six months", 1.0 / 3.0,
				 CriterionScale.MoreIsBetter, Arrays
						.asList(new Double[] { Double.NEGATIVE_INFINITY, 0.25,
								0.5, 0.8 }));
		averageTimeToFixABug = new NumericCriterionElement(
				"Average time to fix a bug", 1.0 / 3.0, 
				CriterionScale.LessIsBetter, Arrays.asList(new Double[] {
						Double.POSITIVE_INFINITY, 62.0, 31.0, 7.0 }));

		criteriaMap.put(numberOfCriticalBugsFixedInTheLastSixMonths.getName(),
				numberOfCriticalBugsFixedInTheLastSixMonths);
		criteriaMap.put(numberOfBugsFixedInTheLastSixMonths.getName(),
				numberOfBugsFixedInTheLastSixMonths);
		criteriaMap.put(averageTimeToFixABug.getName(), averageTimeToFixABug);

		// Effectiveness ( Reliability subcriterion )
		effectiveness = new ComposedCriterion("Effectiveness", 1.0 / 2.0,
				Arrays.asList(new Criterion[] {
						numberOfCriticalBugsFixedInTheLastSixMonths,
						numberOfBugsFixedInTheLastSixMonths,
						averageTimeToFixABug }));
		criteriaMap.put(effectiveness.getName(), effectiveness);

		// Reliability ( Product Quality subcriterion )
		reliability = new ComposedCriterion("Reliability", 1.0 / 3.0, Arrays
				.asList(new Criterion[] { maturity, effectiveness }));
		criteriaMap.put(reliability.getName(), reliability);

		// Security SubCriteria
		undefinedValues = new NumericCriterionElement("Undefined values",
				1.0 / 2.0,  CriterionScale.ValueIsBetter,
				Arrays.asList(new Double[] { Double.NaN, 0.0, 0.0, 0.0 }));
		nullDeferences = new NumericCriterionElement("Null Deferences",
				1.0 / 2.0,  CriterionScale.ValueIsBetter,
				Arrays.asList(new Double[] { Double.NaN, 0.0, 0.0, 0.0 }));

		criteriaMap.put(undefinedValues.getName(), undefinedValues);
		criteriaMap.put(nullDeferences.getName(), nullDeferences);

		// Security ( Product Quality subcriterion )
		security = new ComposedCriterion("Security", 1.0 / 3.0, Arrays
				.asList(new Criterion[] { undefinedValues, nullDeferences }));
		criteriaMap.put(security.getName(), security);

		// Product (Code) Quality ( SQOOSS Quality Characteristics subcriterion)
		productQuality = new ComposedCriterion("Product (Code) Quality", 1.0 / 2.0,
				Arrays.asList(new Criterion[] { maintainability, reliability,
						security }));
		criteriaMap.put(productQuality.getName(), productQuality);

		// Mailing List subcriteria
		numberOfUniqueSubscribers = new NumericCriterionElement(
				"Number of unique subscribers", 1.0 / 5.0, 
				CriterionScale.MoreIsBetter, Arrays.asList(new Double[] {
						Double.NEGATIVE_INFINITY, 100.0, 350.0, 700.0 }));
		numberOfMessagesInUserOrSupportListPerMonth = new NumericCriterionElement(
				"Number of messages in user or support list per month",
				1.0 / 5.0,  CriterionScale.MoreIsBetter,
				Arrays.asList(new Double[] { Double.NEGATIVE_INFINITY, 750.0,
						1200.0, 1501.0 }));
		numberOfMessagesInDeveloperListPerMonth = new NumericCriterionElement(
				"Number of messages in developer list per month", 1.0 / 5.0,
				 CriterionScale.MoreIsBetter, Arrays
						.asList(new Double[] { Double.NEGATIVE_INFINITY, 509.0,
								800.0, 1091.0 }));
		averageThreadDepth = new NumericCriterionElement(
				"Average thread depth", 1.0 / 5.0, 
				CriterionScale.MoreIsBetter, Arrays.asList(new Double[] {
						Double.NEGATIVE_INFINITY, 0.0, 3.0, 5.0 }));
		involvement = new NumericCriterionElement("Involvement", 1.0 / 5.0,
				 CriterionScale.MoreIsBetter, Arrays
						.asList(new Double[] { Double.NEGATIVE_INFINITY, 0.0,
								2.0, 4.0 }));

		criteriaMap.put(numberOfUniqueSubscribers.getName(),
				numberOfUniqueSubscribers);
		criteriaMap.put(numberOfMessagesInUserOrSupportListPerMonth.getName(),
				numberOfMessagesInUserOrSupportListPerMonth);
		criteriaMap.put(numberOfMessagesInDeveloperListPerMonth.getName(),
				numberOfMessagesInDeveloperListPerMonth);
		criteriaMap.put(averageThreadDepth.getName(), averageThreadDepth);
		criteriaMap.put(involvement.getName(), involvement);

		// Mailing List ( Community Quality subcriterion )
		mailingListQuality = new ComposedCriterion("Mailing list Quality",
				1.0 / 2.0, Arrays.asList(new Criterion[] {
						numberOfUniqueSubscribers,
						numberOfMessagesInUserOrSupportListPerMonth,
						numberOfMessagesInDeveloperListPerMonth,
						averageThreadDepth, involvement }));

		criteriaMap.put(mailingListQuality.getName(), mailingListQuality);

		// Documentation subcriteria
		availiableDocumentationDocuments = new NumericCriterionElement(
				"Availiable documentation documents", 1.0 / 2.0,
				 CriterionScale.MoreIsBetter, Arrays
						.asList(new Double[] { Double.NEGATIVE_INFINITY, 0.0,
								3.0, 5.0 }));
		updateFrequency = new NumericCriterionElement("Update frequency",
				1.0 / 2.0,  CriterionScale.MoreIsBetter,
				Arrays.asList(new Double[] { Double.NEGATIVE_INFINITY, 0.0,
						1.0, 2.0 }));

		criteriaMap.put(availiableDocumentationDocuments.getName(),
				availiableDocumentationDocuments);
		criteriaMap.put(updateFrequency.getName(), updateFrequency);

		// Documentation ( Community Quality subcriterion )
		documentationQuality = new ComposedCriterion("Documentation Quality",
				1.0 / 2.0, Arrays.asList(new Criterion[] {
						availiableDocumentationDocuments, updateFrequency }));

		criteriaMap.put(documentationQuality.getName(), documentationQuality);

		// Community Quality ( SQOOSS Quality Characteristics subcriterion)
		communityQuality = new ComposedCriterion("Community Quality",
				1.0 / 2.0, Arrays.asList(new Criterion[] { mailingListQuality,
						documentationQuality }));

		criteriaMap.put(communityQuality.getName(), communityQuality);

		// SQOOSS Quality Characteristics ( Root Criterion)
		SQOOSSQualityCharacteristics = new ComposedCriterion(
				"SQO-OSS Quality Characteristics", 1.0, Arrays
						.asList(new Criterion[] { productQuality,
								communityQuality }));

		criteriaMap.put(SQOOSSQualityCharacteristics.getName(),
				SQOOSSQualityCharacteristics);
		rootCriterion = SQOOSSQualityCharacteristics;

		for (String criterionName : criteriaMap.keySet()) {
			criteriaInitializationMap.put(criterionName, Boolean.FALSE);
		}
	}

	public SQOOSSProfiles getPessimisticAssignement() {
		if (rootCriterion.isComposite())
			return QualityEvaluator
					.pessimisticAssignment((ComposedCriterion) rootCriterion);
		else
			return null;
	}
	
	public SQOOSSProfiles getOptimisticAssignement() {
		if (rootCriterion.isComposite())
			return QualityEvaluator
					.optimisticAssignment((ComposedCriterion) rootCriterion);
		else
			return null;
	}

	public Criterion getCriterion(String name) {
		return criteriaMap.get(name);
	}
	
	public boolean setCriterionElement(String name, double value) {
		Criterion criterion = criteriaMap.get(name);
		if(!criterion.isComposite())
		{
			((NumericCriterionElement)criterion).setMetricValue(value);
			criteriaInitializationMap.put(name, Boolean.TRUE);
			return true;
		}
		return false;
	}
	
	/**
	 * @return the objectOriented
	 */
	public boolean isObjectOriented() {
		return objectOriented;
	}

	/**
	 * @param objectOriented
	 *            the objectOriented to set
	 */
	public void setObjectOriented(boolean objectOriented) {
		this.objectOriented = objectOriented;
	}

	/**
	 * @return the rootCriterion
	 */
	public Criterion getRootCriterion() {
		return rootCriterion;
	}


	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		if(!initialized)
		{
			for (Boolean hasInitilized : criteriaInitializationMap.values()) {
				if (hasInitilized.equals(Boolean.FALSE))
					return false;
			}
			initialized = true;
		}
		return initialized;
	}
	
	public void setComposedCriterionValues(ComposedCriterion criterion, double relativeImportance, CriterionScale scale)
	{
		criterion.setRelativeImportance(relativeImportance);
		criterion.setCriterionScale(scale);
		criteriaInitializationMap.put(criterion.getName(),Boolean.TRUE);
	} 
}
