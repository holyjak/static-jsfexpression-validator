package net.jakubholy.jeeutils.jsfelcheck;

import java.util.Collection;

import net.jakubholy.jeeutils.jsfelcheck.expressionfinder.impl.jasper.variables.DeclareTypeOfVariableException;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ExpressionRejectedByFilterResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.FailedValidationResult;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.ResultsIterable;
import net.jakubholy.jeeutils.jsfelcheck.validator.results.SuccessfulValidationResult;

public interface CollectedValidationResults {

    public Collection<DeclareTypeOfVariableException> getVariablesNeedingTypeDeclaration();

    public ResultsIterable<SuccessfulValidationResult> goodResults();

    public ResultsIterable<FailedValidationResult> failures();

    public ResultsIterable<ExpressionRejectedByFilterResult> excluded();

    public boolean hasErrors();

}