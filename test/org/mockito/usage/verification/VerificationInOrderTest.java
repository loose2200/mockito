/*
 * Copyright (c) 2007 Mockito contributors 
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.usage.verification;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.Strictly;
import org.mockito.exceptions.*;
import org.mockito.usage.IMethods;

@SuppressWarnings("unchecked")  
public class VerificationInOrderTest {
    
    private IMethods mockOne;
    private IMethods mockTwo;
    private IMethods mockThree;
    private Strictly strictly;

    @Before
    public void setUp() {
        mockOne = mock(IMethods.class);
        mockTwo = mock(IMethods.class);
        mockThree = mock(IMethods.class);
        
        strictly = strictOrderVerifier(mockOne, mockTwo, mockThree);

        mockOne.simpleMethod(1);
        mockTwo.simpleMethod(2);
        mockTwo.simpleMethod(2);
        mockThree.simpleMethod(3);
        mockTwo.simpleMethod(2);
        mockOne.simpleMethod(4);
    }
    
    @Test
    public void shouldVerifyInOrder() {
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, 2).simpleMethod(2);
        strictly.verify(mockThree).simpleMethod(3);
        strictly.verify(mockTwo).simpleMethod(2);
        strictly.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    } 
    
    @Test
    public void shouldVerifyInOrderUsingAtLeastOnce() {
        strictly.verify(mockOne, atLeastOnce()).simpleMethod(1);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        strictly.verify(mockThree).simpleMethod(3);
        strictly.verify(mockTwo).simpleMethod(2);
        strictly.verify(mockOne, atLeastOnce()).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    } 
    
    @Test
    public void shouldVerifyInOrderWhenExpectingSomeInvocationsToBeCalledZeroTimes() {
        strictly.verify(mockOne, 0).oneArg(false);
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, 2).simpleMethod(2);
        strictly.verify(mockThree).simpleMethod(3);
        strictly.verify(mockTwo).simpleMethod(2);
        strictly.verify(mockOne).simpleMethod(4);
        strictly.verify(mockThree, 0).oneArg(false);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    } 
    
    @Test
    public void shouldFailWhenFirstMockCalledTwice() {
        strictly.verify(mockOne).simpleMethod(1);
        try {
            strictly.verify(mockOne).simpleMethod(1);
        } catch (StrictVerificationError e) {}
    }
    
    @Test
    public void shouldFailWhenLastMockCalledTwice() {
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, 2).simpleMethod(2);
        strictly.verify(mockThree).simpleMethod(3);
        strictly.verify(mockTwo).simpleMethod(2);
        strictly.verify(mockOne).simpleMethod(4);
        try {
            strictly.verify(mockOne).simpleMethod(4);
        } catch (StrictVerificationError e) {}
    }
    
    @Test(expected=NumberOfInvocationsAssertionError.class)
    public void shouldFailOnFirstMethodBecauseOneInvocationExpected() {
        strictly.verify(mockOne, 0).simpleMethod(1);
    }
    
    @Test(expected=NumberOfInvocationsAssertionError.class)
    public void shouldFailOnFirstMethodBecauseOneInvocationExpectedAgain() {
        strictly.verify(mockOne, 2).simpleMethod(1);
    }
    
    @Test
    public void shouldFailOnSecondMethodBecauseTwoInvocationsExpected() {
        strictly.verify(mockOne, 1).simpleMethod(1);
        try {
            strictly.verify(mockTwo, 3).simpleMethod(2);
            fail();
        } catch (NumberOfInvocationsAssertionError e) {}
    }
    
    @Test
    public void shouldFailOnSecondMethodBecauseTwoInvocationsExpectedAgain() {
        strictly.verify(mockOne, 1).simpleMethod(1);
        try {
            strictly.verify(mockTwo, 0).simpleMethod(2);
            fail();
        } catch (NumberOfInvocationsAssertionError e) {}
    }    
    
    @Test
    public void shouldFailOnLastMethodBecauseOneInvocationExpected() {
        strictly.verify(mockOne, atLeastOnce()).simpleMethod(1);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        strictly.verify(mockThree, atLeastOnce()).simpleMethod(3);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            strictly.verify(mockOne, 0).simpleMethod(4);
            fail();
        } catch (NumberOfInvocationsAssertionError e) {}
    }
    
    @Test
    public void shouldFailOnLastMethodBecauseOneInvocationExpectedAgain() {
        strictly.verify(mockOne, atLeastOnce()).simpleMethod(1);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        strictly.verify(mockThree, atLeastOnce()).simpleMethod(3);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            strictly.verify(mockOne, 2).simpleMethod(4);
            fail();
        } catch (NumberOfInvocationsAssertionError e) {}
    }    
    
    /* ------------- */
    
    @Test(expected=VerificationAssertionError.class)
    public void shouldFailOnFirstMethodBecauseDifferentArgsExpected() {
        strictly.verify(mockOne).simpleMethod(100);
    }
    
    @Test(expected=VerificationAssertionError.class)
    public void shouldFailOnFirstMethodBecauseDifferentMethodExpected() {
        strictly.verify(mockOne).oneArg(true);
    }
    
    @Test
    public void shouldFailOnSecondMethodBecauseDifferentArgsExpected() {
        strictly.verify(mockOne).simpleMethod(1);
        try {
            strictly.verify(mockTwo, 2).simpleMethod(-999);
            fail();
        } catch (StrictVerificationError e) {}
        //TODO those guys need tests around nice messages from exceptions and it's quite inconsistent
        //when StrictVerificationError and when VerificationAssertionError is fired
        //algorithm should be as follows:
        //  1. check if according to order: mock and method matches -> StrictVerificationError
        //  2. check method arguments -> VerificationAssertionError
        //  3. check invocation count -> NumberOfInvocationsAssertionError
    }
    
    @Test
    public void shouldFailOnSecondMethodBecauseDifferentMethodExpected() {
        strictly.verify(mockOne, 1).simpleMethod(1);
        try {
            strictly.verify(mockTwo, 2).oneArg(true);
            fail();
        } catch (StrictVerificationError e) {}
    }    
    
    @Test
    public void shouldFailOnLastMethodBecauseDifferentArgsExpected() {
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        strictly.verify(mockThree).simpleMethod(3);
        strictly.verify(mockTwo).simpleMethod(2);
        try {
            strictly.verify(mockOne).simpleMethod(-666);
            fail();
        } catch (VerificationAssertionError e) {}
    }
    
    @Test
    public void shouldFailOnLastMethodBecauseDifferentMethodExpected() {
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        strictly.verify(mockThree).simpleMethod(3);
        strictly.verify(mockTwo).simpleMethod(2);
        try {
            strictly.verify(mockOne).oneArg(false);
            fail();
        } catch (VerificationAssertionError e) {}
    }    
    
    /* -------------- */
    
    @Test(expected = StrictVerificationError.class)
    public void shouldFailWhenLastMethodCalledFirst() {
        strictly.verify(mockOne).simpleMethod(4);
    }
    
    @Test(expected = StrictVerificationError.class)
    public void shouldFailWhenSecondMethodCalledFirst() {
        strictly.verify(mockTwo, 2).simpleMethod(2);
    }
    
    @Test
    public void shouldFailWhenLastMethodCalledToEarly() {
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, 2).simpleMethod(2);
        try {
            strictly.verify(mockOne).simpleMethod(4);
            fail();
        } catch (StrictVerificationError e) {}
    }
    
    @Test
    public void shouldFailWhenMockTwoIsToEarly() {
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, 2).simpleMethod(2);
        try {
            strictly.verify(mockTwo, 1).simpleMethod(2);
            fail();
        } catch (StrictVerificationError e) {}
    }
    
    @Test
    public void shouldFailWhenLastMockIsToEarly() {
        strictly.verify(mockOne).simpleMethod(1);
        try {
            strictly.verify(mockOne).simpleMethod(4);
            fail();
        } catch (StrictVerificationError e) {}
    }
    
    @Test
    public void shouldFailOnVerifyNoMoreInteractions() {
        strictly.verify(mockOne).simpleMethod(1);
        strictly.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        strictly.verify(mockThree).simpleMethod(3);
        strictly.verify(mockTwo).simpleMethod(2);
        
        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (VerificationAssertionError e) {}
    } 
    
    @Test(expected=VerificationAssertionError.class)
    public void shouldFailOnVerifyZeroInteractions() {
        verifyZeroInteractions(mockOne);
    }
}