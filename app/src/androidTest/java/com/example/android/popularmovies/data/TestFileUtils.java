package com.example.android.popularmovies.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.popularmovies.R;

/**
 * Created by Aaron Helton on 2/15/2016
 */
public class TestFileUtils extends AndroidTestCase
{
    private static final String LOG_TAG = TestFileUtils.class.getSimpleName();
    private Bitmap bm;
    private String filePath;
    @Override
    public void setUp()
    {
        bm = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
    }
    public void testStoreAndGetImage()
    {
        Log.d(LOG_TAG, "Test: TestFileUtils.testStoreAndGetImage()");
        String filePath = FileUtils.storeImage(mContext, bm, "ic_launcher");

        assertNotNull("Improperly Returned Filepath!", filePath);
        assertFalse("Filepath is Empty!", filePath.isEmpty());
        assertTrue("Default PNG Encoding Not Applied!", filePath.contains(".png"));

        Bitmap ret = FileUtils.getImage(filePath);
        assertNotNull("Returned Bitmap is Null!", ret);
        assertTrue("Bitmap Returned is not the Same!", bm.sameAs(ret));

        String filePath2 = FileUtils.storeImage(mContext, bm, "someImage");

        filePath = FileUtils.storeImage(null, bm, "ic_launcher");
        assertNull(filePath);
        filePath = FileUtils.storeImage(mContext, bm, null);
        assertNull(filePath);
        filePath = FileUtils.storeImage(mContext, bm, "");
        assertNull(filePath);

        filePath = FileUtils.storeImage(mContext, bm, "ic_launcher.jpg");
        String ext = FileUtils.getExtension(filePath);
        assertNotNull(ext);
        assertTrue(ext.equals("jpg"));

        ret = FileUtils.getImage(filePath);
        assertFalse("JPG Compression Should Make This False", bm.sameAs(ret));

        filePath = FileUtils.storeImage(mContext, bm, "someFileName");
        assertTrue(FileUtils.getImage(filePath).sameAs(bm));
    }

    public void testGetExtension()
    {
        Log.d(LOG_TAG, "TEST: TestFileUtils.testGetExtension()");
        final String TEST_EXTENSION = "someExtension";
        final String TEST_FILE_STEM = "test";

        String fileName = TEST_FILE_STEM + "." + TEST_EXTENSION;
        assertTrue(TEST_EXTENSION.equals(FileUtils.getExtension(fileName)));
        assertNull(FileUtils.getExtension(TEST_FILE_STEM));
    }

}
