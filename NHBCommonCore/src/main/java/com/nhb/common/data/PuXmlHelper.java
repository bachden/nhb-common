package com.nhb.common.data;

import java.io.InputStream;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nhb.common.data.exception.XmlParseException;
import com.nhb.common.utils.PrimitiveTypeUtils;

public class PuXmlHelper {

	private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	private static final XPath xPath = XPathFactory.newInstance().newXPath();
	private static final String VARIABLE = "variable";
	private static final String ENTRY = "entry";
	private static final String NAME = "name";
	private static final String TYPE = "type";

	private static final void processXMLNode(Node node, PuArray holder) throws Exception {
		PuDataType type = PuDataType.fromName(node.getAttributes().getNamedItem(TYPE).getNodeValue());
		if (type == PuDataType.PUARRAY) {
			PuArrayList arr = new PuArrayList();
			NodeList entryList = (NodeList) xPath.compile(ENTRY).evaluate(node, XPathConstants.NODESET);
			for (int i = 0; i < entryList.getLength(); i++) {
				Node entry = entryList.item(i);
				processXMLNode(entry, arr);
			}
			holder.addFrom(arr);
		} else if (type == PuDataType.PUOBJECT) {
			PuObject puo = new PuObject();
			NodeList variableList = (NodeList) xPath.compile(VARIABLE).evaluate(node, XPathConstants.NODESET);
			for (int i = 0; i < variableList.getLength(); i++) {
				Node entry = variableList.item(i);
				processXMLNode(entry, puo);
			}
			holder.addFrom(puo);
		} else {
			String textContent = node.getTextContent().trim();
			PuValue value = PuValue.fromObject(textContent);
			value.setType(type);
			holder.add(value);
		}
	}

	private static final void processXMLNode(Node node, PuObject holder) throws Exception {
		PuDataType type = null;
		try {
			type = PuDataType.fromName(node.getAttributes().getNamedItem(TYPE).getNodeValue());
		} catch (NullPointerException ex) {
			throw new NullPointerException("PuObject's xml variable must specific type");
		}
		String name = node.getAttributes().getNamedItem(NAME).getNodeValue();
		if (type == PuDataType.PUARRAY) {
			PuArrayList arr = new PuArrayList();
			NodeList entryList = (NodeList) xPath.compile(ENTRY).evaluate(node, XPathConstants.NODESET);
			for (int i = 0; i < entryList.getLength(); i++) {
				Node entry = entryList.item(i);
				processXMLNode(entry, arr);
			}
			holder.setPuArray(name, arr);
		} else if (type == PuDataType.PUOBJECT) {
			PuObject puo = new PuObject();
			NodeList variableList = (NodeList) xPath.compile(VARIABLE).evaluate(node, XPathConstants.NODESET);
			for (int i = 0; i < variableList.getLength(); i++) {
				Node entry = variableList.item(i);
				processXMLNode(entry, puo);
			}
			holder.setPuObject(name, puo);
		} else {
			String textContent = node.getTextContent().trim();
			holder.set(name, textContent);
			holder.setType(name, type);
		}
	}

	static final PuElement parseXml(Node node) throws Exception {
		if (node != null) {
			NodeList entryList = (NodeList) xPath
					.compile((node instanceof Document) ? ("/" + VARIABLE + "s/" + ENTRY) : ENTRY)
					.evaluate(node, XPathConstants.NODESET);
			NodeList variableList = (NodeList) xPath
					.compile(node instanceof Document ? ("/" + VARIABLE + "s/" + VARIABLE) : VARIABLE)
					.evaluate(node, XPathConstants.NODESET);
			if (entryList.getLength() > 0 && variableList.getLength() > 0) {
				throw new XmlParseException("Invalid PuElement xml format");
			} else {
				if (entryList.getLength() > 0) {
					PuArrayList array = new PuArrayList();
					for (int i = 0; i < entryList.getLength(); i++) {
						processXMLNode(entryList.item(i), array);
					}
					return array;
				} else if (variableList.getLength() > 0) {
					PuObject puo = new PuObject();
					for (int i = 0; i < variableList.getLength(); i++) {
						processXMLNode(variableList.item(i), puo);
					}
					return puo;
				} else {
					Node inner = node instanceof Document
							? (Node) xPath.compile("/" + VARIABLE + "s").evaluate(node, XPathConstants.NODE)
							: node;
					Node typeNode = inner.getAttributes().getNamedItem("type");
					if (typeNode == null) {
						return PuNull.EMPTY;
					}
					PuDataType type = PuDataType.fromName(typeNode.getNodeValue());
					if (type != null) {
						return new PuValue(
								PrimitiveTypeUtils.getValueFrom(type.getDataClass(), inner.getTextContent().trim()),
								type);
					} else {
						throw new XmlParseException("Invalid PuElement xml format, variables type is not specificed");
					}
				}
			}
		}
		return null;
	}

	public static final PuElement parseXml(String xml) throws Exception {
		if (xml != null) {
			DocumentBuilder builder;
			builder = builderFactory.newDocumentBuilder();
			InputStream is = IOUtils.toInputStream(xml);
			Document document = builder.parse(is);
			return parseXml(document);
		}
		return null;
	}

	static final String generateXML(PuElement puElement) {
		if (puElement != null) {
			StringBuilder builder = new StringBuilder();
			if (puElement instanceof PuValue) {
				builder.append(
						"<" + VARIABLE + "s type=\"" + ((PuValue) puElement).getType().name().toLowerCase() + "\">");
				builder.append(puElement.toString());
			} else {
				builder.append("<" + VARIABLE + "s>");
				generateXMLContent(puElement, builder);
			}
			builder.append("</" + VARIABLE + "s>");
			return builder.toString();
		}
		return null;
	}

	private static final void generateXMLContent(PuElement element, StringBuilder builder) {
		if (element != null) {
			if (element instanceof PuObject) {
				PuObject puObject = (PuObject) element;
				for (Entry<String, PuValue> entry : puObject) {
					builder.append("<" + VARIABLE + " name=\"" + entry.getKey() + "\" type=\""
							+ entry.getValue().getType().name().toLowerCase() + "\">");
					if (entry.getValue().getType() == PuDataType.PUARRAY) {
						generateXMLContent(entry.getValue().getPuArray(), builder);
					} else if (entry.getValue().getType() == PuDataType.PUOBJECT) {
						generateXMLContent(entry.getValue().getPuObject(), builder);
					} else if (entry.getValue().getType() == PuDataType.RAW) {
						builder.append(entry.getValue().getRawAsBase64());
					} else {
						builder.append((Object) entry.getValue().getData());
					}
					builder.append("</" + VARIABLE + ">");
				}
			} else if (element instanceof PuArray) {
				PuArrayList puArrayList = (PuArrayList) element;
				for (PuValue value : puArrayList) {
					builder.append("<" + ENTRY + " type=\"" + value.getType().name().toLowerCase() + "\">");
					if (value.getType() == PuDataType.PUARRAY) {
						generateXMLContent(value.getPuArray(), builder);
					} else if (value.getType() == PuDataType.PUOBJECT) {
						generateXMLContent(value.getPuObject(), builder);
					} else if (value.getType() == PuDataType.RAW) {
						builder.append(value.getRawAsBase64());
					} else {
						builder.append((Object) value.getData());
					}
					builder.append("</" + ENTRY + ">");
				}
			}
		}
	}
}