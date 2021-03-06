/*
 * Thumbnailator - a thumbnail generation library
 *
 * Copyright (c) 2008-2020 Chris Kroells
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coobird.thumbnailator.tasks;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.TestUtils;
import net.coobird.thumbnailator.ThumbnailParameter;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.builders.BufferedImageBuilder;
import net.coobird.thumbnailator.builders.ThumbnailParameterBuilder;
import net.coobird.thumbnailator.tasks.io.BufferedImageSink;
import net.coobird.thumbnailator.tasks.io.FileImageSource;
import net.coobird.thumbnailator.tasks.io.ImageSink;
import net.coobird.thumbnailator.tasks.io.ImageSource;
import net.coobird.thumbnailator.tasks.io.InputStreamImageSource;
import net.coobird.thumbnailator.tasks.io.OutputStreamImageSink;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class SourceSinkThumbnailTaskTest {

    @SuppressWarnings("unchecked")
    @Test
    public void task_UsesPreferredFromDestination() throws Exception {
        // given
        ThumbnailParameter param =
                new ThumbnailParameterBuilder()
                        .size(50, 50)
                        .format(ThumbnailParameter.DETERMINE_FORMAT)
                        .build();

        ImageSource source = mock(ImageSource.class);
        when(source.read()).thenReturn(new BufferedImageBuilder(100, 100).build());
        when(source.getInputFormatName()).thenReturn("42a");

        ImageSink destination = mock(ImageSink.class);
        when(destination.preferredOutputFormatName()).thenReturn("42");

        // when
        Thumbnailator.createThumbnail(
                new SourceSinkThumbnailTask(param, source, destination)
        );

        // then
        verify(source).read();

        verify(destination).preferredOutputFormatName();
        verify(destination).setOutputFormatName("42");
        verify(destination).write(any(BufferedImage.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void task_UsesOriginalFormat() throws Exception {
        // given
        ThumbnailParameter param =
                new ThumbnailParameterBuilder()
                        .size(50, 50)
                        .format(ThumbnailParameter.ORIGINAL_FORMAT)
                        .build();

        ImageSource source = mock(ImageSource.class);
        when(source.read()).thenReturn(new BufferedImageBuilder(100, 100).build());
        when(source.getInputFormatName()).thenReturn("42");

        ImageSink destination = mock(ImageSink.class);
        when(destination.preferredOutputFormatName()).thenReturn("42a");

        // when
        Thumbnailator.createThumbnail(
                new SourceSinkThumbnailTask(param, source, destination)
        );

        // then
        verify(source).read();

        verify(destination, never()).preferredOutputFormatName();
        verify(destination).setOutputFormatName("42");
        verify(destination).write(any(BufferedImage.class));
    }

    @Test
    public void task_SizeOnly_InputStream_BufferedImage() throws IOException {
        // given
        ThumbnailParameter param =
                new ThumbnailParameterBuilder().size(50, 50).build();

        ByteArrayInputStream is =
                new ByteArrayInputStream(makeImageData("png", 200, 200));

        InputStreamImageSource source = new InputStreamImageSource(is);
        BufferedImageSink destination = new BufferedImageSink();

        // when
        Thumbnailator.createThumbnail(
                new SourceSinkThumbnailTask<InputStream, BufferedImage>(param, source, destination)
        );

        // then
        BufferedImage thumbnail = destination.getSink();
        assertEquals(50, thumbnail.getWidth());
        assertEquals(50, thumbnail.getHeight());
    }


    @Test
    public void task_SizeOnly_InputStream_OutputStream() throws IOException {
        // given
        ThumbnailParameter param =
                new ThumbnailParameterBuilder().size(50, 50).build();

        byte[] imageData = makeImageData("png", 200, 200);
        ByteArrayInputStream is = new ByteArrayInputStream(imageData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        InputStreamImageSource source = new InputStreamImageSource(is);
        OutputStreamImageSink destination = new OutputStreamImageSink(os);

        // when
        Thumbnailator.createThumbnail(
                new SourceSinkThumbnailTask<InputStream, OutputStream>(param, source, destination)
        );

        // then
        ByteArrayInputStream destIs = new ByteArrayInputStream(os.toByteArray());
        BufferedImage thumbnail = ImageIO.read(destIs);
        assertEquals(50, thumbnail.getWidth());
        assertEquals(50, thumbnail.getHeight());

        destIs = new ByteArrayInputStream(os.toByteArray());
        String formatName = TestUtils.getFormatName(destIs);

        assertEquals("png", formatName);
    }

    @Test
    public void task_ChangeOutputFormat_InputStream_OutputStream() throws IOException {
        // given
        ThumbnailParameter param =
                new ThumbnailParameterBuilder().size(50, 50).format("jpg").build();

        byte[] imageData = makeImageData("png", 200, 200);
        ByteArrayInputStream is = new ByteArrayInputStream(imageData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        InputStreamImageSource source = new InputStreamImageSource(is);
        OutputStreamImageSink destination = new OutputStreamImageSink(os);

        // when
        Thumbnailator.createThumbnail(
                new SourceSinkThumbnailTask<InputStream, OutputStream>(param, source, destination)
        );

        // then
        ByteArrayInputStream destIs = new ByteArrayInputStream(os.toByteArray());
        BufferedImage thumbnail = ImageIO.read(destIs);
        assertEquals(50, thumbnail.getWidth());
        assertEquals(50, thumbnail.getHeight());

        destIs = new ByteArrayInputStream(os.toByteArray());
        String formatName = TestUtils.getFormatName(destIs);
        assertEquals("JPEG", formatName);
    }

    @Test
    public void task_ChangeOutputFormat_File_OutputStream() throws IOException {
        // given
        ThumbnailParameter param =
                new ThumbnailParameterBuilder().size(50, 50).format("jpg").build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        FileImageSource source = new FileImageSource("src/test/resources/Thumbnailator/grid.bmp");
        OutputStreamImageSink destination = new OutputStreamImageSink(os);

        // when
        Thumbnailator.createThumbnail(
                new SourceSinkThumbnailTask<File, OutputStream>(param, source, destination)
        );

        // then
        ByteArrayInputStream destIs = new ByteArrayInputStream(os.toByteArray());
        BufferedImage thumbnail = ImageIO.read(destIs);
        assertEquals(50, thumbnail.getWidth());
        assertEquals(50, thumbnail.getHeight());

        destIs = new ByteArrayInputStream(os.toByteArray());
        String formatName = TestUtils.getFormatName(destIs);
        assertEquals("JPEG", formatName);
    }

    /**
     * Returns test image data as an array of {@code byte}s.
     *
     * @param format Image format.
     * @param width  Image width.
     * @param height Image height.
     * @throws IOException When a problem occurs while making image data.
     * @return A {@code byte[]} of image data.
     */
    private static byte[] makeImageData(String format, int width, int height)
            throws IOException {
        BufferedImage img = new BufferedImageBuilder(200, 200).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, format, baos);

        return baos.toByteArray();
    }
}
