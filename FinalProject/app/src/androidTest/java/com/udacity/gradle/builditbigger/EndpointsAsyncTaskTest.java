package com.udacity.gradle.builditbigger;

import android.os.AsyncTask;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EndpointsAsyncTaskTest {

    @Test
    public void endpointResultTest() {
        // Get joke from endpoint
        AsyncTask<Void, Void, String> endpointsAsyncTask = new EndpointsAsyncTask().execute();

        String res = null;
        try {
            // Testing that retrieves a valid string
            res = endpointsAsyncTask.get();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        }
        assertNotNull(res);
    }
}