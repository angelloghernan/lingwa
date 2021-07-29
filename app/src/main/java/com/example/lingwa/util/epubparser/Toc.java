package com.example.lingwa.util.epubparser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.lingwa.util.epubparser.BaseFindings;
import com.example.lingwa.util.epubparser.Constants;
import com.example.lingwa.util.epubparser.ContextHelper;
import com.example.lingwa.util.epubparser.NavPoint;
import com.example.lingwa.util.epubparser.exception.ReadingException;

//toc.ncx
public class Toc extends com.example.lingwa.util.epubparser.BaseFindings implements Serializable {

	private static final long serialVersionUID = 8154412879349792795L;

	private Head head;
	private NavMap navMap;

	private int lastPageIndex;

	private transient boolean isHeadFound, isNavMapFound;

	public Toc() {
		head = new Head();
		navMap = new NavMap();
	}

	public class Head implements Serializable {

		private static final long serialVersionUID = -5861717309893477622L;

		private String uid;
		private String depth;
		private String totalPageCount;
		private String maxPageNumber;

		void fillAttributes(NodeList nodeList) throws ReadingException {

			Field[] fields = Head.class.getDeclaredFields();

			for (int i = 0; i < nodeList.getLength(); i++) {

				String metaNodeName = nodeList.item(i).getNodeName();

				if (metaNodeName.contains(Character.toString(com.example.lingwa.util.epubparser.Constants.COLON))) {
					metaNodeName = com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(metaNodeName, com.example.lingwa.util.epubparser.Constants.COLON);
				}

				if (metaNodeName.equals("meta")) {
					NamedNodeMap attributes = nodeList.item(i).getAttributes();

					for (int k = 0; k < attributes.getLength(); k++) {
						Node attribute = attributes.item(k);

						if (attribute.getNodeName().equals("name")) {

							String attributeNodeValue = attribute.getNodeValue();

							if (attributeNodeValue.contains(Character.toString(com.example.lingwa.util.epubparser.Constants.COLON))) {
								attributeNodeValue = com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(attributeNodeValue, com.example.lingwa.util.epubparser.Constants.COLON);
							}

							for (int j = 0; j < fields.length; j++) {

								if (attributeNodeValue.equals(fields[j].getName())) {

									// Find content in attributes
									for (int l = 0; l < attributes.getLength(); l++) {
										if (attributes.item(l).getNodeName().equals("content")) {
											fields[j].setAccessible(true);
											try {
												fields[j].set(this, attributes.item(l).getNodeValue());
											} catch (IllegalArgumentException | IllegalAccessException | DOMException e) {
												e.printStackTrace();
												throw new ReadingException("Exception while parsing " + com.example.lingwa.util.epubparser.Constants.EXTENSION_NCX + " content: " + e.getMessage());
											}
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		public String getUid() {
			return uid;
		}

		public String getDepth() {
			return depth;
		}

		public String getTotalPageCount() {
			return totalPageCount;
		}

		public String getMaxPageNumber() {
			return maxPageNumber;
		}

		void print() {
			System.out.println("\n\nPrinting Head...\n");
			System.out.println("uid: " + getUid());
			System.out.println("depth: " + getDepth());
			System.out.println("totalPageCount: " + getTotalPageCount());
			System.out.println("maxPageNumber: " + getMaxPageNumber());
		}
	}

	public class NavMap implements Serializable {

		private static final long serialVersionUID = -3629764613712749465L;

		private List<com.example.lingwa.util.epubparser.NavPoint> navPoints;

		public List<com.example.lingwa.util.epubparser.NavPoint> getNavPoints() {
			return navPoints;
		}

		// TODO: navMap (epub2) and pageList (epub3) should be merged as well. Just as we merged spine and toc.ncx. Or just sorting them by their playOrder is enough?
		void fillNavPoints(NodeList possiblyNavPoints) throws ReadingException {

			if (this.navPoints == null) {
				this.navPoints = new ArrayList<>();
			}

			for (int i = 0; i < possiblyNavPoints.getLength(); i++) {

				String navPointNodeName = possiblyNavPoints.item(i).getNodeName();

				if (navPointNodeName.contains(Character.toString(com.example.lingwa.util.epubparser.Constants.COLON))) {
					navPointNodeName = com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(navPointNodeName, com.example.lingwa.util.epubparser.Constants.COLON);
				}

				if (navPointNodeName.equals("navPoint") || navPointNodeName.equals("pageTarget")) {
					com.example.lingwa.util.epubparser.NavPoint navPoint = new com.example.lingwa.util.epubparser.NavPoint();

					NamedNodeMap nodeMap = possiblyNavPoints.item(i).getAttributes();

					for (int j = 0; j < nodeMap.getLength(); j++) {
						Node attribute = nodeMap.item(j);

						if (attribute.getNodeName().equals("id")) {
							navPoint.setId(attribute.getNodeValue());
						} else if (attribute.getNodeName().equals("playOrder")) {
							navPoint.setPlayOrder(Integer.parseInt(attribute.getNodeValue()));
						} else if (attribute.getNodeName().equals("type")) {
							navPoint.setType(attribute.getNodeValue());
						}

					}

					boolean hasNestedNavPoints = false;

					NodeList navPointChildNodes = possiblyNavPoints.item(i).getChildNodes();

					for (int k = 0; k < navPointChildNodes.getLength(); k++) {

						Node navPointChild = navPointChildNodes.item(k);

						String navPointChildNodeName = navPointChild.getNodeName();

						if (navPointChildNodeName.contains(Character.toString(com.example.lingwa.util.epubparser.Constants.COLON))) {
							navPointChildNodeName = com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(navPointChildNodeName, com.example.lingwa.util.epubparser.Constants.COLON);
						}

						if (navPointChildNodeName.equals("navLabel")) {
							NodeList navLabelChildNodes = navPointChild.getChildNodes();

							for (int l = 0; l < navLabelChildNodes.getLength(); l++) {

								String navLabelChildNodeName = navLabelChildNodes.item(l).getNodeName();

								if (navLabelChildNodeName.contains(Character.toString(com.example.lingwa.util.epubparser.Constants.COLON))) {
									navLabelChildNodeName = com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(navLabelChildNodeName, com.example.lingwa.util.epubparser.Constants.COLON);
								}

								if (navLabelChildNodeName.equals("text")) {
									navPoint.setNavLabel(navLabelChildNodes.item(l).getTextContent());
								}
							}

						} else if (navPointChildNodeName.equals("content")) {
							NamedNodeMap contentAttributes = navPointChild.getAttributes();

							for (int m = 0; m < contentAttributes.getLength(); m++) {
								Node contentAttribute = contentAttributes.item(m);

								if (contentAttribute.getNodeName().equals("src")) {
									String contentSrc = contentAttribute.getNodeValue();

									if (contentSrc != null && !contentSrc.equals("")) {
										String encodedContentSrc = com.example.lingwa.util.epubparser.ContextHelper.encodeToUtf8(com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(contentSrc, com.example.lingwa.util.epubparser.Constants.SLASH));
										navPoint.setContentSrc(encodedContentSrc);
									}
								}
							}
						} else if (!hasNestedNavPoints && navPointChildNodeName.equals("navPoint")) {
							hasNestedNavPoints = true;
						}
					}

					boolean duplicateOrNullContentSrc = false;

					for (com.example.lingwa.util.epubparser.NavPoint navPointItem : this.navPoints) {
						if (navPoint.getContentSrc() == null || navPoint.getContentSrc().equals(navPointItem.getContentSrc())) {
							duplicateOrNullContentSrc = true;
							break;
						}
					}

					if (!duplicateOrNullContentSrc) {
						this.navPoints.add(navPoint);
					}

					// Sometimes navPoint nodes may have another navPoint nodes inside them. Even though this means malformed toc.ncx file, it shouldn't hurt to try to read them as well.
					if (hasNestedNavPoints)
						fillNavPoints(navPointChildNodes);
				}
			}
		}

		void sortNavMaps() {

			// If playOrders are not given, then use the order in file.
			Collections.sort(this.navPoints, new Comparator<com.example.lingwa.util.epubparser.NavPoint>() {
				public int compare(com.example.lingwa.util.epubparser.NavPoint o1, com.example.lingwa.util.epubparser.NavPoint o2) {
					return o1.getPlayOrder() < o2.getPlayOrder() ? -1 : 1; // if equals, first occurence should be sorted as first.
				}
			});

		}

		void print() {
			System.out.println("\n\nPrinting NavPoints...\n");

			for (int i = 0; i < this.navPoints.size(); i++) {
				NavPoint navPoint = this.navPoints.get(i);

				System.out
						.println("navPoint (" + i + ") id: " + navPoint.getId() + ", playOrder: " + navPoint.getPlayOrder() + ", navLabel(Text): " + navPoint.getNavLabel() + ", content src: " + navPoint.getContentSrc());
			}
		}
	}

	@Override
	boolean fillContent(Node node) throws ReadingException {

		String nodeName = node.getNodeName();

		if (nodeName.contains(Character.toString(com.example.lingwa.util.epubparser.Constants.COLON))) {
			nodeName = com.example.lingwa.util.epubparser.ContextHelper.getTextAfterCharacter(nodeName, com.example.lingwa.util.epubparser.Constants.COLON);
		}

		if (nodeName.equals("head")) {
			getHead().fillAttributes(node.getChildNodes());
			isHeadFound = true;
		} else if (nodeName.equals("navMap") || nodeName.equals("pageList")) { // if pageList exists then it's epub3 if only navMap exists then it's epub2.
			getNavMap().fillNavPoints(node.getChildNodes());
			getNavMap().sortNavMaps();
			isNavMapFound = true;
		}

		return isHeadFound && isNavMapFound;
	}

	public Head getHead() {
		return head;
	}

	public NavMap getNavMap() {
		return navMap;
	}

	void print() {
		getHead().print();
		getNavMap().print();
	}

	int getLastPageIndex() {
		return lastPageIndex;
	}

	void setLastPageIndex(int lastPageIndex) {
		this.lastPageIndex = lastPageIndex;
	}

}
