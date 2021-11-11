package org.yarnandtail.andhow.test.bulktest;

import org.yarnandtail.andhow.AndHowTestConfig.AndHowTestConfigImpl;
import org.yarnandtail.andhow.BaseConfig;
import org.yarnandtail.andhow.api.AppFatalException;
import org.yarnandtail.andhow.internal.AndHowCore;

import java.util.Arrays;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestCoordinator<T> {
	protected AndHowTestConfigImpl config;
	protected PropValueLoader propValueLoader;
	protected List<PropExpectations> expectations;
	protected List<T> extraValues;

	public TestCoordinator(AndHowTestConfigImpl config,
			PropValueLoader propValueLoader, PropExpectations... expectations) {
		this.config = config;
		this.propValueLoader = propValueLoader;
		this.expectations = Arrays.asList(expectations);
	}

	public TestCoordinator(AndHowTestConfigImpl config,
			PropValueLoader<T> propValueLoader, List<T> extraValues, PropExpectations... expectations) {
		this.config = config;
		this.propValueLoader = propValueLoader;
		this.expectations = Arrays.asList(expectations);
		this.extraValues = extraValues;
	}

	public void runForValues(boolean useAliasIfAvailable, boolean verbose)
			throws Exception {

		runForValues(useAliasIfAvailable, true, 0, verbose);
	}

	public void runForValues(boolean useAliasIfAvailable, boolean useTrimmedValueforAssertions,
			boolean verbose) throws Exception {

		runForValues(useAliasIfAvailable, useTrimmedValueforAssertions, 0, verbose);
	}

	public void runForValues(boolean useAliasIfAvailable,
			boolean useTrimmedValueforAssertions, int expectIndex, boolean verbose)
			throws Exception {


		propValueLoader.buildAndAssignLoaderValues(config, expectations, expectIndex,
				useAliasIfAvailable, extraValues, verbose);

		// This would throw an exception if there were problems
		AndHowCore core = initCore(config);

		PropValueAssertions pas = new PropValueAssertions(
				core, core, expectIndex, useTrimmedValueforAssertions, expectations);

		pas.assertAll(verbose);
	}

	public void runForProblems(boolean useAliasIfAvailable, boolean verbose) throws Exception {
		runForProblems(useAliasIfAvailable, true, 0, verbose);
	}

	public void runForProblems(boolean useAliasIfAvailable, boolean useTrimmedValueforAssertions,
			boolean verbose) throws Exception {
		runForProblems(useAliasIfAvailable, useTrimmedValueforAssertions, 0, verbose);
	}

	public void runForProblems(boolean useAliasIfAvailable,
			boolean useTrimmedValueforAssertions, int expectIndex, boolean verbose) throws Exception {


		propValueLoader.buildAndAssignLoaderValues(config, expectations, expectIndex,
				useAliasIfAvailable, extraValues, verbose);

		//Trick to allow access w/in the lambda
		final AppFatalException[] afeArray = new AppFatalException[1];

		String outText = tapSystemErr(() -> {
			afeArray[0] = assertThrows(AppFatalException.class,
					() -> initCore(config));
		});


		PropProblemAssertions ppa = new PropProblemAssertions(config, expectIndex,
				useTrimmedValueforAssertions, expectations);

		ppa.assertErrors(afeArray[0], outText, verbose);
	}

	protected AndHowCore initCore(BaseConfig aConfig) throws AppFatalException {
		AndHowCore core = new AndHowCore(
				aConfig.getNamingStrategy(),
				aConfig.buildLoaders(),
				aConfig.getRegisteredGroups());

		return core;
	}
}
