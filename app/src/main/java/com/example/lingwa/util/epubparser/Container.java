package com.example.lingwa.util.epubparser;

import org.w3c.dom.Node;

import com.example.lingwa.util.epubparser.BaseFindings;
import com.example.lingwa.util.epubparser.Constants;
import com.example.lingwa.util.epubparser.exception.ReadingException;

class Container extends com.example.lingwa.util.epubparser.BaseFindings {

	private XmlItem rootFile;

	public XmlItem getRootFile() {
		return rootFile;
	}

	public void setRootFile(XmlItem rootFile) {
		this.rootFile = rootFile;
	}

	public String getFullPathValue() throws ReadingException {
		if (getRootFile() != null && getRootFile().getAttributes() != null && getRootFile().getAttributes().containsKey("full-path") && getRootFile().getAttributes().get("full-path") != null
				&& !getRootFile().getAttributes().get("full-path").equals("")) {
			return getRootFile().getAttributes().get("full-path");
		} else {
			throw new ReadingException(com.example.lingwa.util.epubparser.Constants.EXTENSION_OPF + " file not found.");
		}
	}

	@Override
	public boolean fillContent(Node node) {
		if (node.getNodeName() != null && node.getNodeName().equals("rootfile")) {
			this.rootFile = nodeToXmlItem(node);
			return true;
		}

		return false;
	}

	// debug
	public void print() {
		System.out.println("\n\nPrinting Container...\n");
		System.out.println("title: " + (getRootFile() != null ? getRootFile().getValue() : null));
	}

}
