package org.wisdom.api.bodies;

import org.apache.commons.io.FileUtils;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.http.Result;

import java.io.File;
import java.io.InputStream;

/**
 * Render a file.
 */
public class RenderableFile implements Renderable<File> {


    private final File file;
    private boolean mustBeChunked;

    public RenderableFile(File file) {
        this(file, true);
    }

    public RenderableFile(File file, boolean mustBechunked) {
        this.file = file;
        this.mustBeChunked = mustBechunked;
    }

    @Override
    public InputStream render(Context context, Result result) throws Exception {
        return FileUtils.openInputStream(file);
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public String mimetype() {
        return MimeTypes.getMimeTypeForFile(file);
    }

    @Override
    public File content() {
        return file;
    }

    @Override
    public boolean requireSerializer() {
        return false;
    }

    @Override
    public void setSerializedForm(String serialized) {
        // Nothing because serialization is not supported for this renderable class.
    }

    @Override
    public boolean mustBeChunked() {
        return mustBeChunked;
    }

}