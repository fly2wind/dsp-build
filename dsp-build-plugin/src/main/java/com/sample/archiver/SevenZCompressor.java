/*
 * Copyright 2022 [name of copyright owner]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.archiver;

import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.util.Compressor;

import java.io.IOException;

import static org.codehaus.plexus.archiver.util.Streams.bufferedOutputStream;
import static org.codehaus.plexus.archiver.util.Streams.fileOutputStream;

public class SevenZCompressor extends Compressor {
    private LZMACompressorOutputStream lzmaOut;

    @Override
    public void compress() throws ArchiverException {
        try {
            lzmaOut = new LZMACompressorOutputStream(bufferedOutputStream(fileOutputStream(getDestFile())));
            // BUffering of the source stream seems to have little/no impact
            compress(getSource(), lzmaOut);
        } catch (IOException ioe) {
            String msg = "Problem creating 7z " + ioe.getMessage();
            throw new ArchiverException(msg, ioe);
        }
    }

    @Override
    public void close() throws ArchiverException {
        try {
            if (this.lzmaOut != null) {
                this.lzmaOut.close();
                lzmaOut = null;
            }
        } catch (final IOException e) {
            throw new ArchiverException("Failure closing target.", e);
        }
    }
}
