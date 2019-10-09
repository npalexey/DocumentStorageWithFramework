package com.nikitiuk.documentstoragewithframework.services;

import com.nikitiuk.documentstoragewithframework.entities.DocBean;
import com.nikitiuk.documentstoragewithframework.entities.FolderBean;
import com.nikitiuk.documentstoragewithframework.exceptions.AlreadyExistsException;
import com.nikitiuk.javabeansinitializer.annotations.annotationtypes.beans.Bean;
import javassist.NotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Bean
public class LocalStorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);
    private static String PATH;

    public String getPath() {
        if (PATH == null) {
            PATH = System.getProperty("local.path.to.storage"); /*"/home/npalexey/workenv/DOWNLOADED/"*/
        }
        return PATH;
    }

    public List<DocBean> listDocumentsInPath() throws IOException {
        List<DocBean> docBeanList = new ArrayList<>();
        List<String> collectedDocuments = collectDocumentsInPath();
        if (!collectedDocuments.isEmpty()) {
            for (String docPath : collectedDocuments) {
                docBeanList.add(new DocBean(docPath.substring(docPath.lastIndexOf("/") + 1), docPath));
            }
        }
        return docBeanList;
    }

    public List<FolderBean> listFoldersInPath() throws IOException {
        List<FolderBean> folderBeanList = new ArrayList<>();
        List<String> collectedFolders = collectFoldersInPath();
        if (!collectedFolders.isEmpty()) {
            for (String folderPath : collectedFolders) {
                folderBeanList.add(new FolderBean(folderPath + "/"));
            }
        }
        return folderBeanList;
    }

    private List<String> collectFoldersInPath() throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(getPath()));
        return walk.filter(Files::isDirectory)
                .map(x -> x.toString())
                .collect(Collectors.toList());
    }

    private List<String> collectDocumentsInPath() throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(getPath()));
        Set<String> allowedFormats = Stream.of("doc", "docx", "pdf", "txt", "html", "xml")
                .collect(Collectors.toCollection(HashSet::new));
        return walk.filter(Files::isRegularFile)
                .map(x -> x.toString())
                .filter(f -> allowedFormats.contains(FilenameUtils.getExtension(f)))
                .collect(Collectors.toList());
    }

    public List<String> documentContentGetter(String filePath) throws Exception {
        /*int lastIndexOfDot = filePath.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            logger.info("No extension of file is set, using default content getter.");
            return otherTypesContentGetter(filePath);
        }
        switch (filePath.substring(lastIndexOfDot)) {
            case ".pdf":
                return pdfContentGetter(filePath);
            case ".docx":
                return docxContentGetter(filePath);
            default:
                return otherTypesContentGetter(filePath);
        }
*/
        if (filePath.endsWith(".pdf")) {
            return pdfContentGetter(filePath);
        } else if (filePath.endsWith(".docx")) {
            return docxContentGetter(filePath);
        } else {
            return otherTypesContentGetter(filePath);
        }
    }

    private List<String> pdfContentGetter(String filePath) throws IOException {
        List<String> docContent = new ArrayList<>();
        PDDocument document = PDDocument.load(new File(filePath));
        if (!document.isEncrypted()) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            docContent.add(text
                    .replace(" ", "&nbsp;")         //(non-breaking space)
                    .replace("\n", "<br />"));
        } else {
            docContent.add("Document content is encrypted. Download and decrypt using appropriate resources.");
        }
        document.close();
        return docContent;
    }

    private List<String> docxContentGetter(String filePath) throws Exception {
        List<String> docContent = new ArrayList<>();
        FileInputStream fis = new FileInputStream(filePath);
        XWPFDocument document = new XWPFDocument(fis);
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph para : paragraphs) {
            docContent.add(para.getText()
                    .replaceAll("(.{100})", "$1\n")   //replaces every 100 chars with themselves + \n
                    .replace("\n", "<br />"));
        }
        fis.close();
        return docContent;
    }

    private List<String> otherTypesContentGetter(String filePath) throws IOException {
        List<String> docContent = new ArrayList<>();
        Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8);
        stream.forEach(s -> docContent.add(s.replace(" ", "&nbsp;") + "<br />"));
        return docContent;
    }

    public BufferedInputStream fileDownloader(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        byte[] data = Files.readAllBytes(path);
        return new BufferedInputStream(new ByteArrayInputStream(data));
    }

    public void fileUploader(InputStream fileInputStream, String documentPath) throws IOException {
        int read;
        byte[] bytes = new byte[1024];
        OutputStream out = new FileOutputStream(new File(documentPath));
        while ((read = fileInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }

    public String fileUpdater(InputStream fileInputStream, String documentPath) throws Exception {
        File tempFile = new File(documentPath);
        if (tempFile.exists()) {
            fileUploader(fileInputStream, documentPath);
            return tempFile.getName();
        } else {
            throw new NotFoundException("No document found in storage with such properties, even so its entry exists in DB.");
        }
    }

    public String renameFile(String oldFilePath, String newFilePath) throws Exception {
        File tempFile = new File(oldFilePath);
        File tempFileToRenameTo = new File(newFilePath);
        if (tempFile.renameTo(tempFileToRenameTo)) {
            return tempFile.getName();
        } else {
            throw new AlreadyExistsException("File with such name already exists.");
        }
    }

    public void fileOrRecursiveFolderDeleter(String documentPath) throws IOException {
        FileUtils.touch(new File(documentPath));
        File fileToDelete = FileUtils.getFile(documentPath);
        FileUtils.deleteQuietly(fileToDelete);
    }
}
