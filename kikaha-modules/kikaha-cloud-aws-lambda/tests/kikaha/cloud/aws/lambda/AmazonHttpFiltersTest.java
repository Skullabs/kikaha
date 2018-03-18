package kikaha.cloud.aws.lambda;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AmazonHttpFiltersTest {

    final AmazonHttpFilters filters = spy( new AmazonHttpFilters() );

    @Mock AmazonHttpRequest request;
    @Mock AmazonHttpFilter next;

    @Test @SneakyThrows
    public void canCallAnEmptyChainOfFiltersCorrectly(){
        doReturn( new AmazonHttpFilter[0] ).when( filters ).loadFilters();

        val chain = filters.wrap( next );
        chain.doFilter( request );

        verify( next, never() ).stopExecution( any() );
        verify( next ).doFilter( eq(request), any() );
    }

    @Test @SneakyThrows
    public void canCallAChainOfFiltersCorrectly()
    {
        val first = spy( FilterThatCallTheNextFilter.class );
        val second = spy( FilterThatCallTheNextFilter.class );
        doReturn( new AmazonHttpFilter[]{ first, second } ).when( filters ).loadFilters();

        val chain = filters.wrap( next );
        chain.doFilter( request );

        for ( val filter : Arrays.asList( first, second, next ) ) {
            verify(filter, never()).stopExecution(any());
            verify(filter).doFilter(eq(request), any());
        }
    }

    @Test @SneakyThrows
    public void willCallElementsAppendedToTheChainDuringTheChainExecution()
    {
        val last = spy( FilterThatCallTheNextFilter.class );
        val first = spy( new FilterThatAppendANewFilterToBeExecutedAfterTheNextOne(last) );
        val second = spy( FilterThatCallTheNextFilter.class );
        doReturn( new AmazonHttpFilter[]{ first, second } ).when( filters ).loadFilters();

        val third = spy( FilterThatCallTheNextFilter.class );
        val chain = filters.wrap( third );
        chain.doFilter( request );

        for ( val filter : Arrays.asList( first, second, third, last ) ) {
            verify(filter, never()).stopExecution(any());
            verify(filter).doFilter(eq(request), any());
        }
    }

    @Test @SneakyThrows
    public void canStopPropagationAndMemorizeTheResponseAtTheChain()
    {
        val first = spy( FilterThatCallTheNextFilter.class );
        val second = spy( new FilterThatStopPropagationAndStoreResponse() );
        val third = spy( FilterThatCallTheNextFilter.class );
        doReturn( new AmazonHttpFilter[]{ first, second, third } ).when( filters ).loadFilters();

        val chain = filters.wrap( next );
        chain.doFilter( request );

        for ( val filter : Arrays.asList( first, second, third, next ) )
            verify(filter, never()).stopExecution(any());

        for ( val filter : Arrays.asList( third, next ) )
            verify( filter, never() ).doFilter( any(), any() );

        for ( val filter : Arrays.asList( third, next ) )
            verify( filter, never() ).doFilter( eq(request), eq(chain) );

        assertEquals( chain.response, second.response );
    }

    static class FilterThatCallTheNextFilter implements AmazonHttpFilter {

        @Override
        public void doFilter(AmazonHttpRequest request, AmazonHttpFilter next) throws Exception {
            next.doFilter( request );
        }
    }

    @RequiredArgsConstructor
    static class FilterThatAppendANewFilterToBeExecutedAfterTheNextOne implements AmazonHttpFilter {

        final AmazonHttpFilter newFilter;

        @Override
        public void doFilter(AmazonHttpRequest request, AmazonHttpFilter next) throws Exception {
            next.doFilter( request, newFilter );
        }
    }

    static class FilterThatStopPropagationAndStoreResponse implements AmazonHttpFilter {

        @Getter AmazonHttpResponse response = new AmazonHttpResponse();

        @Override
        public void doFilter(AmazonHttpRequest request, AmazonHttpFilter next) throws Exception {
            next.stopExecution( response );
        }
    }
}
