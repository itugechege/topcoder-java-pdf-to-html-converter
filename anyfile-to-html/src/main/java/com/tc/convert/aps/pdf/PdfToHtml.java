package com.tc.convert.aps.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.fit.pdfdom.PDFDomTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wipro.yahoo.aps.ApsConverter;

public class PdfToHtml implements ApsConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(PdfToHtml.class);

	public static void main(String args[]) {
		try {
			PdfToHtml converter = new PdfToHtml();
			Path pathIn = Paths.get("input/2006JD007601_published.pdf");
			List<Path> files = Files.isDirectory(pathIn)
					? Files.list(pathIn).filter(path -> path.getFileName().toString().toLowerCase().matches("^.*\\.(pdf)")).collect(Collectors.toList())
					: Arrays.asList(pathIn);
			for (Path nextFile : files) {
				long start = System.currentTimeMillis();
				int totalPages = converter.generateHtmlFromPdf(nextFile, "output");
				LOGGER.info("{} conversion of {} pages completed in {} milliseconds.", nextFile.toFile().getName(), totalPages, System.currentTimeMillis() - start);
			}
		} catch (IOException e) {
			LOGGER.error("Conversion failed", e);
		}
	}

	@Override
	public int convert(InputStream in, String dirOut, String fileName) throws FileNotFoundException, IOException, TransformerException {
		return generateHtmlFromPdf(in, dirOut, fileName);
	}

	public int generateHtmlFromPdf(Path pdf, String outputdir) throws IOException {
		try (FileInputStream in = new FileInputStream(pdf.toFile())) {
			return generateHtmlFromPdf(in, outputdir, FilenameUtils.getBaseName(pdf.toFile().getName()));
		}
	}

//	public int generateHtmlFromPdf(InputStream in, String outputdir, String fileName) throws IOException {
//		int totalPages = 0;
//		String inFileBaseName = FilenameUtils.getBaseName(fileName);
//		try (PDDocument pdfDoc = PDDocument.load(in)) {
//			int pageIndex = 0;
//			for (PDPage page : pdfDoc.getPages()) {
//				try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
//						Writer pdfWriter = new PrintWriter(byteArrayOut, true, StandardCharsets.UTF_8);
//						PDDocument tmpDoc = new PDDocument()) {
//					tmpDoc.addPage(page);
//					tmpDoc.save(byteArrayOut);
//					String titleText = inFileBaseName + "-pdf-" + ++pageIndex;
//					String outFilePath = outputdir + File.separator + titleText + ".html";
//					Files.write(Paths.get(outFilePath), generateHtmlFromPdf(tmpDoc, titleText));
//					totalPages++;
//				}
//			}
//		}
//		return totalPages;
//	}

	public int generateHtmlFromPdf(InputStream in, String outputDir, String fileName) throws IOException {
		int totalPages = 0;
		String inFileBaseName = FilenameUtils.getBaseName(fileName);

		// Load custom fonts from the directory
		File fontDirectory = new File("fonts/");
		Map<String, PDType0Font> loadedFonts = new HashMap<>();

		try (PDDocument pdfDoc = PDDocument.load(in)) {
			// Load each font in the directory into the map
			for (File fontFile : fontDirectory.listFiles()) {
				if (fontFile.isFile() && fontFile.getName().endsWith(".ttf")) {
					PDType0Font font = PDType0Font.load(pdfDoc, fontFile);
					loadedFonts.put(fontFile.getName(), font);
					System.out.println(font);
				}
			}

			int pageIndex = 0;
			for (PDPage page : pdfDoc.getPages()) {
				try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream()) {
					Writer pdfWriter = new PrintWriter(byteArrayOut, true, StandardCharsets.UTF_8);
					PDDocument tmpDoc = new PDDocument();
					tmpDoc.addPage(page);

					tmpDoc.save(byteArrayOut);
					tmpDoc.close();

					String titleText = inFileBaseName + "-pdf-" + ++pageIndex;
					String outFilePath = outputDir + File.separator + titleText + ".html";
					Files.write(Paths.get(outFilePath), generateHtmlFromPdf(tmpDoc, titleText));

					totalPages++;
				}
			}
		}
		return totalPages;
	}

	public byte[] generateHtmlFromPdf(PDDocument pdf, String titleText) throws IOException {
		try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream(); Writer pdfWriter = new PrintWriter(byteArrayOut, true, StandardCharsets.UTF_8)) {
			PDFDomTree pdfDomTree = new PDFDomTree() {
				@Override
				protected void createDocument() throws ParserConfigurationException {
					super.createDocument();
					title.setTextContent(titleText);
				}
			};
			pdfDomTree.writeText(pdf, pdfWriter);
			return byteArrayOut.toByteArray();
		}
	}
}
