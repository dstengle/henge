package com.kenzan.henge.domain.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.assertj.core.util.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableSet;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.exception.HengeParseException;

public class ScopeUtilsTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private final Scope env = Scope.builder("env", "dev").build();
	private final Scope stack = Scope.builder("stack", "stacknet").build();
	private final Scope region = Scope.builder("region", "us-west-2").build();
	
	@Test
	public void extractScopeKeysTest() {
		
		final Set<Scope> scopes = Sets.newLinkedHashSet(env, stack, region);
		
		ImmutableSet<String> extractScopeKeys = ScopeUtils.extractScopeKeys(scopes);
		assertTrue(extractScopeKeys.contains("env"));
		assertTrue(extractScopeKeys.contains("stack"));
		assertTrue(extractScopeKeys.contains("region"));
		
	}
	
	@Test
	public void subScopeSetTest() {
		//Case 1
		final Set<Scope> scopes = Sets.newLinkedHashSet(env, stack, region);
		final Set<String> keys1 = Sets.newLinkedHashSet("stack");
		
		ImmutableSet<Scope> subScopeSet1 = ScopeUtils.subScopeSet(scopes, keys1);
		assertTrue(subScopeSet1.contains(stack));
		assertFalse(subScopeSet1.contains(env));
		assertFalse(subScopeSet1.contains(region));
		
		//Case 2
		final Set<String> keys2 = Sets.newLinkedHashSet("env", "region");
		
		ImmutableSet<Scope> subScopeSet2 = ScopeUtils.subScopeSet(scopes, keys2);
		assertTrue(subScopeSet2.contains(env));
		assertTrue(subScopeSet2.contains(region));
		assertFalse(subScopeSet2.contains(stack));		
	}
	
	@Test
	public void parseScopeStringSuccessTest() {
		final String scopes = "env=dev,stack=stacknet,region=us-west-2";
		
		ImmutableSet<Scope> parseScopeString = ScopeUtils.parseScopeString(scopes);
		
		assertTrue(parseScopeString.contains(env));
		assertTrue(parseScopeString.contains(region));
		assertTrue(parseScopeString.contains(stack));	
		
	}
	
	@Test
	public void parseScopeStringDelimiterSuccessTest() {
		final String scopes = "env=dev&stack=stacknet&region=us-west-2";
		
		ImmutableSet<Scope> parseScopeString = ScopeUtils.parseScopeString(scopes, Optional.of("&"));
		
		assertTrue(parseScopeString.contains(env));
		assertTrue(parseScopeString.contains(region));
		assertTrue(parseScopeString.contains(stack));	
		
	}
	
	@Test
	public void parseScopeStringFailTest() {
		final String scopes = "env=dev/stack=stacknet/region=us-west-2";

		expectedException.expect(HengeParseException.class);
		expectedException.expectMessage("Incorrect format of scope string env=dev/stack=stacknet/region=us-west-2. Example: env=env-1,stack=stack-1,region=region-1");
		
		ScopeUtils.parseScopeString(scopes);
		
	}
	
	@Test
	public void validadeScopeStringTest() {
		final String scopeStringSuccess = "env=dev,region=us-west-2";
		assertTrue(ScopeUtils.validateScopeString(scopeStringSuccess));
		
		final String scopeStringFailed = "env=dev,";
		assertFalse(ScopeUtils.validateScopeString(scopeStringFailed));
	}

}
