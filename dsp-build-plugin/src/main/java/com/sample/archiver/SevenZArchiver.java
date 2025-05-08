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

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.Lists;
import org.codehaus.plexus.archiver.*;
import org.codehaus.plexus.archiver.exceptions.EmptyArchiveException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

//@Named("7zip")
@Component(role = Archiver.class, hint = "7z", instantiationStrategy = "per-lookup")
public class SevenZArchiver extends AbstractArchiver {
    private final SevenZCompressor compressor = new SevenZCompressor();

    @Override
    protected String getArchiveType() {
        return "7z";
    }

    @Override
    protected void close() throws IOException {
        compressor.close();
    }

    @Override
    protected void execute() throws ArchiverException, IOException {
        if (!checkForced()) {
            return;
        }

        ResourceIterator iter = getResources();
        if (!iter.hasNext()) {
            throw new EmptyArchiveException("archive cannot be empty");
        }

        File zipFile = getDestFile();
        if (zipFile == null) {
            throw new ArchiverException("You must set the destination tar file.");
        }
        if (zipFile.exists() && !zipFile.isFile()) {
            throw new ArchiverException(zipFile + " isn't a file.");
        }
        if (zipFile.exists() && !zipFile.canWrite()) {
            throw new ArchiverException(zipFile + " is read-only.");
        }

        Map<String, ArchiveEntry> filesMap = getFiles();
        filesMap.forEach((k,v) -> {
            System.out.println("    :" + k);
            System.out.println("    ====" + v.getName());
        });


        //this.asResourceCollection(this.getf)


        //this.asResourceCollection()


        List<ArchiveEntry> items = Lists.newArrayList(iter);
        items.forEach(a -> {
            System.out.println("===============" + a.getName());
        });


        IOutCreateArchiveZip outArchive = SevenZip.openOutArchiveZip();
        outArchive.setLevel(9);
        outArchive.createArchive(new RandomAccessFileOutStream(new RandomAccessFile(zipFile, "rw")), items.size(), new IOutCreateCallback<IOutItemZip>() {

            @Override
            public void setOperationResult(boolean b) throws SevenZipException {

            }

            @Override
            public IOutItemZip getItemInformation(int i, OutItemFactory<IOutItemZip> outItemFactory) throws SevenZipException {
                IOutItemZip item = outItemFactory.createOutItem();

                PlexusIoResource resource = items.get(i).getResource();

                System.out.println("item:" + resource.getName() + "   " + resource.isDirectory());
                if (resource.isDirectory()) {
                    item.setPropertyIsDir(true);
                } else {
                    item.setDataSize(resource.getSize());
                }
                item.setPropertyPath(resource.getName());

                return item;
            }

            @Override
            public ISequentialInStream getStream(int i) throws SevenZipException {
                PlexusIoResource resource = items.get(i).getResource();
                try {
                    return new ByteArrayStream(IOUtils.toByteArray(resource.getContents()), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setTotal(long l) throws SevenZipException {

            }

            @Override
            public void setCompleted(long l) throws SevenZipException {

            }
        });
        outArchive.close();


        //SevenZOutputFile sevenZOutput = new SevenZOutputFile(zipFile);
        //try {
        //    while (iter.hasNext()) {
        //        ArchiveEntry entry = iter.next();

        //        compress(sevenZOutput, new File(entry.getResource().getURL().toURI()), entry.getName());

        //sevenZOutput.putArchiveEntry(sevenZArchiveEntry);
        //sevenZOutput.write(entry.getInputStream());
        //        System.out.println(entry.getName() + ":" + entry.getResource().getURL());
        //    }

        //sevenZOutput.closeArchiveEntry();
        //} catch (URISyntaxException e) {
        //    throw new RuntimeException(e);
        //}

        //SevenZOutputFile sevenZOutput = new SevenZOutputFile(entry.getFile());
        //SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(fileToArchive, name);
        //sevenZOutput.putArchiveEntry(entry);
        //sevenZOutput.write(contentOfEntry);
        //sevenZOutput.closeArchiveEntry();

        //compressor.setSource(entry.getResource());
        //compressor.setDestFile(getDestFile());
        //compressor.compress();

    }

    private static void compress(SevenZOutputFile output, File input, String name) throws URISyntaxException, IOException {
        if (input.isDirectory()) {
            System.out.println("目录：" + input.getName());
            //取出文件夹中的文件（或子文件夹）
            File[] files = input.listFiles();
            if (files.length == 0) {
                //如果文件夹为空，则只需在目的地.7z文件中写入一个目录进入
                SevenZArchiveEntry sevenZArchiveEntry = output.createArchiveEntry(input, input.getName() + "/");
                output.putArchiveEntry(sevenZArchiveEntry);
                //sevenZOutput.closeArchiveEntry();
            } else {
                //如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
                for (File file : files) {
                    compress(output, file, name + "/" + file.getName());
                }
            }
        } else {
            System.out.println("文件：" + input.getName() + "      " + name);

            SevenZArchiveEntry sevenZArchiveEntry = output.createArchiveEntry(input, name);
            output.putArchiveEntry(sevenZArchiveEntry);

            // 这里是文件要写入的数据，可能是从其他文件中读取到的，也可以自己操作
            BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(input.getPath())));

            final byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = bis.read(buffer))) {
                output.write(buffer, 0, n);
            }
            bis.close();
            output.closeArchiveEntry();
        }
    }
}
