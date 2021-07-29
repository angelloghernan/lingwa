package com.example.lingwa.util.epubparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.lingwa.util.epubparser.BaseFindings;
import com.example.lingwa.util.epubparser.BaseFindings.XmlItem;
import com.example.lingwa.util.epubparser.BookSection;
import com.example.lingwa.util.epubparser.Constants;
import com.example.lingwa.util.epubparser.Content;
import com.example.lingwa.util.epubparser.ContextHelper;
import com.example.lingwa.util.epubparser.CssStatus;
import com.example.lingwa.util.epubparser.NavPoint;
import com.example.lingwa.util.epubparser.Optionals;
import com.example.lingwa.util.epubparser.Package;
import com.example.lingwa.util.epubparser.Toc;
import com.example.lingwa.util.epubparser.exception.OutOfPagesException;
import com.example.lingwa.util.epubparser.exception.ReadingException;

public class Reader {

	private boolean isFoundNeeded;

	private com.example.lingwa.util.epubparser.Content content;

	private boolean isProgressFileFound;

	/**
	 * Parses only needed files for book info.
	 * 
	 * @param filePath
	 * @throws ReadingException
	 */
	public void setInfoContent(String filePath) throws ReadingException {
		fillContent(filePath, false, false);
	}

	/**
	 * Parses all the files needed for reading book. This method must be called before calling readSection method.
	 * 
	 * @param filePath
	 * @throws ReadingException
	 */
	public void setFullContent(String filePath) throws ReadingException {
		fillContent(filePath, true, false);
	}

	/**
	 * Does the same job with setFullContent but also tries to load saved progress if found any. If no progress file is found then it'll work the same as setFullContent does.
	 * 
	 * @param filePath
	 * @return saved page index. 0 if no progress is found.
	 * @throws ReadingException
	 */
	public int setFullContentWithProgress(String filePath) throws ReadingException {
		fillContent(filePath, true, true);

		if (isProgressFileFound) {
			return loadProgress();
		} else {
			return 0;
		}
	}

	/**
	 * Main method that splits and gets the parts of the book.
	 * 
	 * @param index
	 * @return
	 * @throws ReadingException
	 * @throws OutOfPagesException
	 *             if index is greater than the page count.
	 */
	public BookSection readSection(int index) throws ReadingException, OutOfPagesException {
		return content.maintainBookSections(index);
	}

	// Optionals
	public void setMaxContentPerSection(int maxContentPerSection) {
		com.example.lingwa.util.epubparser.Optionals.maxContentPerSection = maxContentPerSection;
	}

	public void setCssStatus(CssStatus cssStatus) {
		com.example.lingwa.util.epubparser.Optionals.cssStatus = cssStatus;
	}

	public void setIsIncludingTextContent(boolean isIncludingTextContent) {
		com.example.lingwa.util.epubparser.Optionals.isIncludingTextContent = isIncludingTextContent;
	}

	public void setIsOmittingTitleTag(boolean isOmittingTitleTag) {
		com.example.lingwa.util.epubparser.Optionals.isOmittingTitleTag = isOmittingTitleTag;
	}

	// Additional operations
	public com.example.lingwa.util.epubparser.Package getInfoPackage() {
		return content.getPackage();
	}

	public Toc getToc() {
		return content.getToc();
	}

	public String getCoverImageFileName() throws ReadingException {

		if (content != null) {
			return content.getCoverImageFileName();
		}

		throw new ReadingException("Content info is not set.");
	}

	public byte[] getCoverImage() throws ReadingException {

		if (content != null) {
			return content.getCoverImage();
		}

		throw new ReadingException("Content info is not set.");
	}

	public void saveProgress(int lastPageIndex) throws ReadingException, OutOfPagesException {

		if (lastPageIndex < content.getToc().getNavMap().getNavPoints().size()) {
			content.getToc().setLastPageIndex(lastPageIndex);
		} else {
			throw new OutOfPagesException(lastPageIndex, content.getToc().getNavMap().getNavPoints().size());
		}

		saveProgress();
	}

	public void saveProgress() throws ReadingException {

		ZipFile epubFile = null;
		ZipOutputStream zipOutputStream = null;
		ObjectOutputStream objectOutputStream = null;

		String newFilePath = null;

		try {
			epubFile = new ZipFile(content.getZipFilePath());

			String fileName = new File(content.getZipFilePath()).getName();
			newFilePath = content.getZipFilePath().replace(fileName, "tmp_" + fileName);

			zipOutputStream = new ZipOutputStream(new FileOutputStream(newFilePath));

			Enumeration<? extends ZipEntry> entries = epubFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				if (entry.getName().equals(com.example.lingwa.util.epubparser.Constants.SAVE_FILE_NAME)) // Don't copy the progress file. We'll put the new one already.
					continue;

				ZipEntry destEntry = new ZipEntry(entry.getName());
				zipOutputStream.putNextEntry(destEntry);
				if (!entry.isDirectory()) {
					com.example.lingwa.util.epubparser.ContextHelper.copy(epubFile.getInputStream(entry), zipOutputStream);
				}
				zipOutputStream.closeEntry();
			}

			ZipEntry progressFileEntry = new ZipEntry(com.example.lingwa.util.epubparser.Constants.SAVE_FILE_NAME);
			zipOutputStream.putNextEntry(progressFileEntry);
			objectOutputStream = new ObjectOutputStream(zipOutputStream);
			objectOutputStream.writeObject(content.getToc());
			zipOutputStream.closeEntry();

		} catch (IOException e) {
			e.printStackTrace();

			File newFile = new File(newFilePath);

			if (newFile.exists()) {
				newFile.delete();
			}

			throw new ReadingException("Error writing progressed ZipFile: " + e.getMessage());
		} finally {

			if (epubFile != null) {
				try {
					epubFile.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new ReadingException("Error closing ZipFile: " + e.getMessage());
				}
			}

			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new ReadingException("Error closing object output stream: " + e.getMessage());
				}
			}

		}

		File oldFile = new File(content.getZipFilePath());

		if (oldFile.exists()) {
			oldFile.delete();
		}

		File newFile = new File(newFilePath);

		if (newFile.exists() && !oldFile.exists()) {
			newFile.renameTo(new File(content.getZipFilePath()));
		}

	}

	public boolean isSavedProgressFound() {
		return isProgressFileFound;
	}

	public int loadProgress() throws ReadingException {

		if (!isProgressFileFound)
			throw new ReadingException("No save files are found. Loading progress is unavailable.");

		ZipFile epubFile = null;
		InputStream saveFileInputStream = null;
		ObjectInputStream oiStream = null;

		try {

			try {
				epubFile = new ZipFile(content.getZipFilePath());
				ZipEntry zipEntry = epubFile.getEntry(com.example.lingwa.util.epubparser.Constants.SAVE_FILE_NAME);
				saveFileInputStream = epubFile.getInputStream(zipEntry);

				oiStream = new ObjectInputStream(saveFileInputStream);
				Toc toc = (Toc) oiStream.readObject();

				content.setToc(toc);
				return content.getToc().getLastPageIndex();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				throw new ReadingException("Error initializing ZipFile: " + e.getMessage());
			}

		} finally {

			if (epubFile != null) {
				try {
					epubFile.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new ReadingException("Error closing ZipFile: " + e.getMessage());
				}
			}

			if (oiStream != null) {
				try {
					oiStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new ReadingException("Error closing object input stream: " + e.getMessage());
				}
			}

		}

	}

	// Private methods
	private com.example.lingwa.util.epubparser.Content fillContent(String zipFilePath, boolean isFullContent, boolean isLoadingProgress) throws ReadingException {

		if (zipFilePath == null) {
			throw new ReadingException("Epub file path is null.");
		}

		this.content = new com.example.lingwa.util.epubparser.Content();
		this.content.setZipFilePath(zipFilePath);

		ZipFile epubFile = null;
		try {
			try {
				epubFile = new ZipFile(zipFilePath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new ReadingException("Error initializing ZipFile: " + e.getMessage());
			}

			Enumeration<? extends ZipEntry> files = epubFile.entries();

			while (files.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) files.nextElement();
				if (!entry.isDirectory()) {
					String entryName = entry.getName();

					if (entryName != null) {
						content.addEntryName(entryName);

						if (entryName.equals(com.example.lingwa.util.epubparser.Constants.SAVE_FILE_NAME)) {
							isProgressFileFound = true;
						}
					}
				}
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			try {
				factory.setFeature("http://xml.org/sax/features/namespaces", false);
				factory.setFeature("http://xml.org/sax/features/validation", false);
				// factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
				// factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				// throw new ReadingException("Error initializing DocumentBuilderFactory: " + e.getMessage());
			}

			DocumentBuilder docBuilder;

			try {
				docBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				throw new ReadingException("DocumentBuilder cannot be created: " + e.getMessage());
			}

			boolean isContainerXmlFound = false;
			boolean isTocXmlFound = false;

			for (int i = 0; i < content.getEntryNames().size(); i++) {

				if (isContainerXmlFound && (isTocXmlFound || !isFullContent)) {
					break;
				}

				String currentEntryName = content.getEntryNames().get(i);

				if (currentEntryName.contains(com.example.lingwa.util.epubparser.Constants.FILE_NAME_CONTAINER_XML)) {
					isContainerXmlFound = true;

					ZipEntry container = epubFile.getEntry(currentEntryName);

					InputStream inputStream;
					try {
						inputStream = epubFile.getInputStream(container);
					} catch (IOException e) {
						e.printStackTrace();
						throw new ReadingException("IOException while reading " + com.example.lingwa.util.epubparser.Constants.FILE_NAME_CONTAINER_XML + " file: " + e.getMessage());
					}

					Document document = getDocument(docBuilder, inputStream, com.example.lingwa.util.epubparser.Constants.FILE_NAME_CONTAINER_XML);
					parseContainerXml(docBuilder, document, epubFile);
				} else if ((!isLoadingProgress || !isProgressFileFound) && isFullContent && currentEntryName.endsWith(com.example.lingwa.util.epubparser.Constants.EXTENSION_NCX)) {
					isTocXmlFound = true;

					ZipEntry toc = epubFile.getEntry(currentEntryName);

					InputStream inputStream;
					try {
						inputStream = epubFile.getInputStream(toc);
					} catch (IOException e) {
						e.printStackTrace();
						throw new ReadingException("IOException while reading " + com.example.lingwa.util.epubparser.Constants.EXTENSION_NCX + " file: " + e.getMessage());
					}

					Document document = getDocument(docBuilder, inputStream, com.example.lingwa.util.epubparser.Constants.EXTENSION_NCX);
					parseTocFile(document, content.getToc());
				}
			}

			if (!isContainerXmlFound) {
				throw new ReadingException("container.xml not found.");
			}

			if (!isTocXmlFound && isFullContent && (!isLoadingProgress || !isProgressFileFound)) {
				throw new ReadingException("toc.ncx not found.");
			}

			if (isFullContent && (!isLoadingProgress || !isProgressFileFound)) {
				mergeTocElements();
			}

			// Debug
			// content.print();

			return content;

		} finally {
			if (epubFile != null) {
				try {

					epubFile.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new ReadingException("Error closing ZipFile: " + e.getMessage());
				}
			}
		}
	}

	private void parseContainerXml(DocumentBuilder docBuilder, Document document, ZipFile epubFile) throws ReadingException {
		if (document.hasChildNodes()) {
			isFoundNeeded = false;
			traverseDocumentNodesAndFillContent(document.getChildNodes(), content.getContainer());
		}

		String opfFilePath = content.getContainer().getFullPathValue();
		ZipEntry opfFileEntry = epubFile.getEntry(opfFilePath);

		if (opfFileEntry == null) {
			for (String entryName : content.getEntryNames()) {
				if (entryName.contains(com.example.lingwa.util.epubparser.Constants.EXTENSION_OPF)) {
					opfFileEntry = epubFile.getEntry(entryName);
					break;
				}
			}
		}

		if (opfFileEntry == null) {
			throw new ReadingException(".opf file not found");
		}

		InputStream opfFileInputStream;
		try {
			opfFileInputStream = epubFile.getInputStream(opfFileEntry);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReadingException("IO error while reading " + com.example.lingwa.util.epubparser.Constants.EXTENSION_OPF + " inputstream: " + e.getMessage());
		}

		Document packageDocument = getDocument(docBuilder, opfFileInputStream, com.example.lingwa.util.epubparser.Constants.EXTENSION_OPF);
		parseOpfFile(packageDocument, content.getPackage());
	}

	private void parseOpfFile(Document document, Package pckage) throws ReadingException {
		if (document.hasChildNodes()) {
			isFoundNeeded = false;
			traverseDocumentNodesAndFillContent(document.getChildNodes(), pckage);
		}
	}

	private void parseTocFile(Document document, Toc toc) throws ReadingException {
		if (document.hasChildNodes()) {
			isFoundNeeded = false;
			traverseDocumentNodesAndFillContent(document.getChildNodes(), toc);
		}
	}

	private Document getDocument(DocumentBuilder docBuilder, InputStream inputStream, String fileName) throws ReadingException {
		Document document;
		try {
			document = docBuilder.parse(inputStream);
			inputStream.close();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ReadingException("Parse error while parsing " + fileName + " file: " + e.getMessage());
		}
	}

	private void traverseDocumentNodesAndFillContent(NodeList nodeList, com.example.lingwa.util.epubparser.BaseFindings findings) throws ReadingException {

		if (isFoundNeeded)
			return;

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node tempNode = nodeList.item(i);

			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				isFoundNeeded = findings.fillContent(tempNode);

				if (isFoundNeeded)
					break;

				if (tempNode.hasChildNodes()) {
					traverseDocumentNodesAndFillContent(tempNode.getChildNodes(), findings);
				}
			}
		}
	}

	private void mergeTocElements() throws ReadingException {

		List<com.example.lingwa.util.epubparser.NavPoint> currentNavPoints = new ArrayList<>(content.getToc().getNavMap().getNavPoints());

		int navPointIndex = 0; // Holds the last duplicate content position, when the new content found insertion is done from that position.
		int insertedNavPointCount = 0;

		for (XmlItem spine : content.getPackage().getSpine().getXmlItemList()) {

			Map<String, String> spineAttributes = spine.getAttributes();

			String idRef = spineAttributes.get("idref");

			for (XmlItem manifest : content.getPackage().getManifest().getXmlItemList()) {

				Map<String, String> manifestAttributes = manifest.getAttributes();

				String manifestElementId = manifestAttributes.get("id");

				if (idRef.equals(manifestElementId)) {

					com.example.lingwa.util.epubparser.NavPoint navPoint = new com.example.lingwa.util.epubparser.NavPoint();
					// navPoint.setPlayOrder(currentNavPoints.size() + spineNavPoints.size() + 1); // Is playOrder needed? I think not because we've already sorted the navPoints with playOrder before
					// merging.
					navPoint.setContentSrc(com.example.lingwa.util.epubparser.ContextHelper.encodeToUtf8(com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(manifestAttributes.get("href"), com.example.lingwa.util.epubparser.Constants.SLASH)));

					boolean duplicateContentSrc = false;
					boolean isAnchoredFound = false;

					for (int j = 0; j < currentNavPoints.size(); j++) {

						NavPoint navPointItem = currentNavPoints.get(j);

						if (navPoint.getContentSrc().equals(navPointItem.getContentSrc())) {
							duplicateContentSrc = true;
							navPointIndex = j;
							break;
						} else if (!isAnchoredFound && navPoint.getContentSrc().startsWith(navPointItem.getContentSrc()) && navPoint.getContentSrc().replace(navPointItem.getContentSrc(), "").startsWith("%23")) {
							isAnchoredFound = true;
							navPointIndex = j;
						} else if (!isAnchoredFound && navPointItem.getContentSrc().startsWith(navPoint.getContentSrc()) && navPointItem.getContentSrc().replace(navPoint.getContentSrc(), "").startsWith("%23")) {
							isAnchoredFound = true;
							navPointIndex = j;
						}

					}

					if (!duplicateContentSrc) {
						content.getToc().getNavMap().getNavPoints().add(navPointIndex + insertedNavPointCount++, navPoint);
					}

				}

			}
		}

	}

}
